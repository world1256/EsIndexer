package com.bcht.es;

import com.alibaba.fastjson.JSON;
import com.bcht.common.FieldType;
import com.bcht.common.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.es
 * @ClassName: ElasticsearchUtil
 * @Description: es工具类  这里主要是建索相关功能
 * @Author: zhengchuan
 * @CreateDate: 2019/5/15 16:01
 * @UpdateUser:
 * @UpdateDate: 2019/5/15 16:01
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ElasticsearchUtil {

    private static Logger logger = LoggerFactory.getLogger(ElasticsearchUtil.class);

    private static String host;
    private static int port;
    private static String schema = "http";
    private static int connectTimeOut = 1000;
    private static int socketTimeOut = 30000;
    private static int connectionRequestTimeOut = 500;
    private static int maxConnectNum = 100;
    private static int maxConnectPerRoute = 100;
    private static boolean uniqueConnectTimeConfig = true;
    private static boolean uniqueConnectNumConfig = true;
    private static RestClientBuilder builder;
    private static RestHighLevelClient client;

    private static BulkProcessor bulkProcessor;
    /**
     * 批量提交条数
     */
    private static int batchSize;

    /**
     * 批量提交容量  MB
     */
    private static int byteSize;

    /**
     * 批量提交间隔
     */
    private static int interval;


    /**
     * 默认监听
     */
    private static BulkProcessor.Listener defaultBulkListener = new BulkProcessor.Listener() {
        @Override
        public void beforeBulk(long executionId, BulkRequest request) {

        }

        @Override
        public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
            BulkItemResponse[] brs = bulkResponse.getItems();
            if (bulkResponse.hasFailures() && null != brs && brs.length > 0) {
                for (int i = 0; i < brs.length; i++) {
                    if (brs[i].isFailed()) {
                        logger.error("ES调用发生错误，index:{},id:{}, 错误代码为:{}",brs[i].getIndex(),brs[i].getId(), brs[i].getFailureMessage());
                    }
                }
            }
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

        }
    };


    static{
        host = PropertiesUtil.getValue("es.host");
        port = PropertiesUtil.getIntValue("es.port");
        batchSize = PropertiesUtil.getIntValue("es.batch.batchSize",10000);
        byteSize = PropertiesUtil.getIntValue("es.batch.byteSize",10);
        interval = PropertiesUtil.getIntValue("es.batch.interval",5);

        initClient();
        initBulkProcessor();
    }

    /**
     * Bean name default  函数名字
     *
     * @return
     */
    public static void initClient() {
        String[] hosts = host.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for(int i=0;i<hosts.length;i++){
            httpHosts[i] = new HttpHost(hosts[i], port, schema);
        }
        builder = RestClient.builder(httpHosts);
        if (uniqueConnectTimeConfig) {
            setConnectTimeOutConfig();
        }
        if (uniqueConnectNumConfig) {
            setMutiConnectConfig();
        }
        client = new RestHighLevelClient(builder);
    }

    /**
     * 异步httpclient的连接延时配置
     */
    public static void setConnectTimeOutConfig() {
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(connectTimeOut);
                requestConfigBuilder.setSocketTimeout(socketTimeOut);
                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
                return requestConfigBuilder;
            }
        });
    }


    /**
     * 异步httpclient的连接数配置
     */
    public static void setMutiConnectConfig() {
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(maxConnectNum);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                return httpClientBuilder;
            }
        });
    }

    /**
     * 关闭连接
     */
    public static void close() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取批量提交工具类
     *
     * @return
     */
    public static void initBulkProcessor() {

        if (bulkProcessor == null) {
            BulkProcessor.Builder builder = BulkProcessor.builder(client::bulkAsync, defaultBulkListener);
            builder.setBulkActions(batchSize);
            builder.setBulkSize(new ByteSizeValue(byteSize, ByteSizeUnit.MB));
            builder.setFlushInterval(TimeValue.timeValueSeconds(interval));
            builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3));
            bulkProcessor = builder.build();
        }
    }

    /**
     * MethodName: upsert
     * Description:   创建或者更新索引
     * @param index
     * @param type
     * @param id
     * @param source
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/15 17:08
     */
    public static void upsert(String index, String type, String id, String source) {
        index = index.toLowerCase();
        type = type.toLowerCase();
        IndexRequest indexRequest = new IndexRequest(index, type, id).source(source, XContentType.JSON);
        UpdateRequest updateRequest = new UpdateRequest(index, type, id).doc(source, XContentType.JSON).upsert(indexRequest);
        bulkProcessor.add(updateRequest);//批量保存
    }


    /**
     * MethodName: updateByQuery
     * Description:  根据规则更新索引字段
     * @param index
     * @param type
     * @param id
     * @param fieldType
     * @param value
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/17 17:33
     */
    public static void updateByQuery(String index,String type,String id,FieldType fieldType,Object value){
        index = index.toLowerCase();
        type = type.toLowerCase();
        UpdateRequest request = new UpdateRequest(index, type, id);
        String op = "";
        if(FieldType.RULE_LESS.equals(fieldType.getRule())){
            op = ">";
        }else if(FieldType.RULE_GRATER.equals(fieldType.getRule())){
            op = "<";
        }
        if(StringUtils.isEmpty(op)){
            logger.warn("进行updateByQuery操作时无法获取字段操作类型  表名:{},字段名:{}",index,fieldType.getFieldName());
            return;
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(fieldType.getFieldName(),value);
        Script inline = new Script(
                ScriptType.INLINE,
                 "painless",
            "if (ctx._source."+fieldType.getFieldName()+" == null || ctx._source."+fieldType.getFieldName()+" "+op+" params."+fieldType.getFieldName()+") {"
                        + " ctx._source."+fieldType.getFieldName()+" = params."+fieldType.getFieldName()
                        + "}",
                parameters);
        request.script(inline);
        IndexRequest indexRequest = new IndexRequest(index, type, id).source(parameters,XContentType.JSON);
        request.upsert(indexRequest);
        bulkProcessor.add(request);
    }

    /**
     * MethodName: delete
     * Description:  删除doc
     * @param index
     * @param type
     * @param id
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/24 9:50
     */
    public static void delete(String index,String type,String id){
        index = index.toLowerCase();
        type = type.toLowerCase();
        DeleteRequest request = new DeleteRequest(index,type,id);
        bulkProcessor.add(request);
    }

    /**
     * MethodName: createMapping
     * Description: 创建index mapping
     * @param index
     * @param type
     * @param fieldTypes
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/24 9:49
     */
    public static void createMapping(String index, String type, Map<String, FieldType> fieldTypes){
        try{
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject().startObject("properties");
            for(FieldType fieldType : fieldTypes.values()){
                builder.startObject(fieldType.getFieldName());
                builder.field("type",fieldType.getType());
                builder.endObject();
            }
            builder.endObject().endObject();
            if(!existIndex(index)){
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
                createIndexRequest.mapping(type, builder);
                CreateIndexResponse indexResponse = client.indices().create(createIndexRequest, new Header[0]);
                if (indexResponse.isAcknowledged()) {
                    logger.info("创建{}的索引及映射成功", index);
                } else {
                    logger.error("{}索引的创建失败", index);
                }
            }else{
                PutMappingRequest request = new PutMappingRequest(index).type(type).source(builder);
                PutMappingResponse putMappingResponse = client.indices().putMapping(request);
                if(putMappingResponse.isAcknowledged()){
                    logger.info("更新{}的索引及映射成功", index);
                }else{
                    logger.error("{}索引的映射更新失败", index);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            logger.error("创建{}索引时发生IO异常",index);
        }
    }

    /**
     * MethodName: existIndex
     * Description:   判断索引是否已经存在
     * @param index
     * @return boolean
     * Author: zhengchuan
     * Date: 2019/5/27 10:52
     */
    private static boolean existIndex (String index) {
        try {
            return client.indices().exists(new GetIndexRequest().indices(index), new Header[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * MethodName: upsert
     * Description:   插入或者更新 doc  使用painless 脚本进行doc操作
     * @param index
     * @param type
     * @param id
     * @param fieldsMap
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/27 10:52
     */
    public static void upsert(String index,String type,String id,Map<String,FieldType> fieldsMap){
        index = index.toLowerCase();
        type = type.toLowerCase();
        UpdateRequest request = new UpdateRequest(index, type, id);
        StringBuilder script = new StringBuilder();
        Map<String,Object> params = new HashMap<>();

        fieldsMap.forEach((fieldName,fieldType)->{
            Object value = fieldType.getValue();
            switch (fieldType.getType()){
                case "integer":
                    value = Integer.parseInt((String)value);
                    break;
                case "long":
                    value = Long.parseLong((String)value);
                    break;
                case "double":
                    value = Double.parseDouble((String)value);
                    break;
                case "float":
                    value = Float.parseFloat((String)value);
                    break;
            }
            params.put(fieldName,value);

            String op = "";
            if(FieldType.RULE_LESS.equals(fieldType.getRule())){
                op = ">";
            }else if(FieldType.RULE_GRATER.equals(fieldType.getRule())){
                op = "<";
            }
            String setValue = "ctx._source."+fieldName+" = params."+fieldName+";";
            if(StringUtils.isEmpty(op)){
               script.append(setValue);
            }else{
               script.append("if(ctx._source."+fieldName+" == null || ctx._source."+fieldName+" "+op+" params."+fieldName+"){"+setValue+"}");
            }
        });
        Script inline = new Script(ScriptType.INLINE, "painless",script.toString(), params);
        request.script(inline);
        IndexRequest indexRequest = new IndexRequest(index, type, id).source(params,XContentType.JSON);
        request.upsert(indexRequest);
        bulkProcessor.add(request);
    }



    public static void main(String[] args) {
//        Map<String,Object> map = new HashMap<>();
//        map.put("name","zc2");
//        map.put("age",18);
//        map.put("birth","1992-04-19 00:00:00");
//        String source = JSON.toJSONString(map);
//        upsert("zc_test","zc_test","zc2",source);

//        FieldType fieldType = new FieldType();
//        fieldType.setFieldName("age");
//        fieldType.setType("integer");
//        fieldType.setRule(FieldType.RULE_GRATER);
//        fieldType.setValue("16");
//        Map<String,FieldType> map = new HashMap<>();
//        map.put(fieldType.getFieldName(),fieldType);
//        upsert("zc_test","zc_test","zc3",map);


        FieldType fieldType = new FieldType();
        fieldType.setFieldName("age");
        fieldType.setRule(FieldType.RULE_GRATER);
        updateByQuery("zc_test","zc_test","zc3",fieldType,17);

        System.out.println("创建索引完成");


//        delete("zc_test","zc_test","zc");
//        System.out.println("删除索引完成");
    }
}

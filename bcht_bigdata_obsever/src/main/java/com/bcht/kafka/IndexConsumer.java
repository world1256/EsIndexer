package com.bcht.kafka;

import com.alibaba.fastjson.JSON;
import com.bcht.common.FieldType;
import com.bcht.common.IndexConfigUtil;
import com.bcht.common.PropertiesUtil;
import com.bcht.common.TableIndex;
import com.bcht.es.ElasticsearchUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.obsever.kafka
 * @ClassName: IndexConsumer
 * @Description:
 * @Author: zhengchuan
 * @CreateDate: 2019/5/10 10:45
 * @UpdateUser:
 * @UpdateDate: 2019/5/10 10:45
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class IndexConsumer implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(IndexConsumer.class);

    private final KafkaConsumer<String, Map<String,Object>> consumer;

    private final  int id;

    private final String topic;

    public IndexConsumer(int id){
        this.id = id;
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", PropertiesUtil.getValue("kafka.bootstrap.servers"));
        props.setProperty("group.id", PropertiesUtil.getValue("kafka.consumer.group.id"));
        props.setProperty("enable.auto.commit", "false");
//        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("max.poll.records", PropertiesUtil.getValue("kafka.consumer.max.poll.records","3000"));
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "com.bcht.kryo.KryoDeserializer");
        this.topic = PropertiesUtil.getValue("kafka.esIndex.topic");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
    }


    @Override
    public void run() {
        try{
            while (true){
                ConsumerRecords<String, Map<String,Object>> records = consumer.poll(Duration.ofMillis(100));
                int count = 0;
                for (ConsumerRecord<String, Map<String, Object>> record : records) {
                    TableIndex tableIndex = IndexConfigUtil.INDEX_CONFIG_MAP.get(record.key());
                    Map<String, Object> indexMap = new HashMap<>();
                    String indexId;
                    //index名称 只能是小写的
                    String indexName = record.key().toLowerCase();
                    if (tableIndex != null) {
                        Map<String, Object> dataMap = record.value();
                        indexId = (String) dataMap.get("indexId");
                        String operate = (String)dataMap.get("operate");
                        if("delete".equals(operate)){
                            ElasticsearchUtil.delete(indexName,indexName,indexId);
                        }else if("update".equals(operate)){
                            dataMap.forEach((field, value) -> {
                                if (tableIndex.getFieldTypes().containsKey(field)) {
                                    FieldType fieldType = tableIndex.getFieldTypes().get(field);
                                    if (FieldType.RULE_NORMAL.equals(fieldType.getRule())) {
                                        indexMap.put(field, value);
                                    } else {
                                        ElasticsearchUtil.updateByQuery(indexName, indexName, indexId, fieldType, value);
                                    }
                                }
                            });
                            ElasticsearchUtil.upsert(indexName, indexName, indexId, JSON.toJSONString(indexMap));
                        }
                        count++;
                    } else {
                        logger.error("indexSchema中不包含的表数据   表名:{}", record.key());
                    }
                }
                consumer.commitAsync();
                logger.info("IndexConsumer id:{}  消费数据完成  提交{}条数据到es...",id, count);
            }
        }catch (WakeupException e){
            logger.info("IndexConsumer id:{}   开始停止消费...",id);
        }finally {
            // 在退出线程之前调用consumer.close()是很有必要的，它会提交任何还没有提交的东西，并向组协调器发送消息，告知自己要离开群组。
            // 接下来就会触发再均衡，而不需要等待会话超时。
            consumer.commitSync();
            consumer.close();
            logger.info("IndexConsumer id:{}   停止消费完成,consumer关闭连接...",id);
        }
    }

    public void shutdown() {
        consumer.wakeup();
    }

    public static void main(String[] args){
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "bd2.bcht:9092,bd3.bcht:9092");
        props.setProperty("group.id", "test2");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "com.bcht.kryo.KryoDeserializer");
        KafkaConsumer<String, Map<String,Object>> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("es_index"));
        while (true) {
            ConsumerRecords<String, Map<String,Object>> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Map<String,Object>> record : records)
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }
}

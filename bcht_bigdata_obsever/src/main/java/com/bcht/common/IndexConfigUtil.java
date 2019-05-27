package com.bcht.common;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.common
 * @ClassName: IndexConfigUtil
 * @Description:
 * @Author: zhengchuan
 * @CreateDate: 2019/5/13 16:37
 * @UpdateUser:
 * @UpdateDate: 2019/5/13 16:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class IndexConfigUtil {

    private static Logger logger = LoggerFactory.getLogger(IndexConfigUtil.class);

    private static String CONFIG_PATH = "/conf/indexSchema.xml";

    public static Map<String, TableIndex> INDEX_CONFIG_MAP;

    /**
     * MethodName: generateIndexConfigMap
     * Description:   生成表 的建索字段  配置map
     * @param
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/27 10:47
     */
    public static void generateIndexConfigMap(){
        INDEX_CONFIG_MAP = new HashMap<>();
        SAXReader reader = new SAXReader();
        try {
            String path = System.getProperty("user.dir")+CONFIG_PATH;
            Document document = reader.read(path);
            Element tables = document.getRootElement();
            Iterator tableIt = tables.elementIterator();
            while(tableIt.hasNext()){
                TableIndex tableIndex = new TableIndex();
                Element tableElement = (Element) tableIt.next();
                String tableName =  tableElement.attribute("name").getValue().trim();
                if(StringUtils.isEmpty(tableName)){
                    logger.error("indexSchema.xml 配置错误  tableName不能为空!!!");
                    INDEX_CONFIG_MAP = null;
                    return;
                }
                tableIndex.setTableName(tableName);

                Map<String,FieldType> fieldTypes = new HashMap<>();
                Element types = tableElement.element("types");
                if(types != null){
                    Iterator typeIt = types.elementIterator();
                    while (typeIt.hasNext()){
                        Element typeElement = (Element) typeIt.next();
                        String typeName = typeElement.attribute("name").getValue().trim();
                        if(StringUtils.isEmpty(typeName)){
                            logger.error("indexSchema.xml 配置错误  表名:{}  typeName不能为空!!!",tableName);
                            INDEX_CONFIG_MAP = null;
                            return;
                        }
                        Element fields = typeElement.element("fields");
                        if(fields != null){
                            generateTypeMap(fields,FieldType.RULE_NORMAL,typeName,fieldTypes,tableName);
                            generateTypeMap(fields,FieldType.RULE_LESS,typeName,fieldTypes,tableName);
                            generateTypeMap(fields,FieldType.RULE_GRATER,typeName,fieldTypes,tableName);
                        }
                    }
                }
                tableIndex.setFieldTypes(fieldTypes);
                INDEX_CONFIG_MAP.put(tableName,tableIndex);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        logger.info("初始化indexSchema 配置完成...");
    }

    /**
     * MethodName: generateTypeMap
     * Description:   读取不同规则的字段信息
     * @param fields
     * @param rule
     * @param typeName
     * @param fieldTypes
     * @param tableName
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/27 10:48
     */
    private static void generateTypeMap(Element fields,String rule,String typeName,Map<String,FieldType> fieldTypes,String tableName){
        Element rulerFields = fields.element(rule);
        if(rulerFields != null){
            String filedString = rulerFields.getStringValue().trim();
            if(StringUtils.isNotEmpty(filedString)){
                String[] rulers = filedString.split(",");
                for(String ruler : rulers){
                    FieldType fieldType = new FieldType();
                    fieldType.setFieldName(ruler);
                    fieldType.setType(typeName);
                    fieldType.setRule(rule);
                    if(fieldTypes.put(ruler,fieldType)!=null){
                        logger.warn("一张表中同一字段配置多次...  表名:{},字段名:{}",tableName,ruler);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        generateIndexConfigMap();
    }

}

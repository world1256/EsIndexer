package com.bcht.kafka;

import com.bcht.common.KafkaPropertiesUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.obsever.kafka
 * @ClassName: IndexProducer
 * @Description:
 * @Author: zhengchuan
 * @CreateDate: 2019/5/10 9:15
 * @UpdateUser:
 * @UpdateDate: 2019/5/10 9:15
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class IndexProducer {

    private static Logger logger = LoggerFactory.getLogger(IndexProducer.class);

    private static KafkaProducer<String, Map<String,Object>> producer;

    private static String topic;

    public static void initProducer(){
        Properties properties = new Properties();
        properties.put("bootstrap.servers", KafkaPropertiesUtil.getValue("kafka.bootstrap.servers"));
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("batch.size", KafkaPropertiesUtil.getIntValue("kafka.producer.batch.size",200000));
        properties.put("linger.ms", 0);
        properties.put("buffer.memory", KafkaPropertiesUtil.getIntValue("kafka.producer.buffer.memory",33554432));
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "com.bcht.kryo.KryoSerializer");
        try {
            producer = new KafkaProducer<String, Map<String,Object>>(properties);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("创建kafka indexProducer失败...",e);
        }
        topic = KafkaPropertiesUtil.getValue("kafka.esIndex.topic");
        logger.info("IndexProducer  初始化完成...");
    }

    public static void sendData(String tableName, Map<String,Object> dataMap){
        producer.send(new ProducerRecord<String, Map<String,Object>>(topic,tableName,dataMap));
    }

    public static void close(){
        producer.close();
    }

    public static void main(String[] args) {
        initProducer();
        for(int i=0;i<1000;i++){
            Map<String,Object> map = new HashMap<>();
            map.put("indexId","zc");
            map.put("age",i);
            map.put("operate","update");
            sendData("zc_test",map);
        }

//        Properties properties = new Properties();
//        properties.put("bootstrap.servers", "bd2.bcht:9092,bd3.bcht:9092");
//        properties.put("acks", "all");
//        properties.put("retries", 0);
//        properties.put("batch.size", 10000);
//        properties.put("linger.ms", 1);
//        properties.put("buffer.memory", 33554432);
//        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        KafkaProducer<String, String> producer = null;
//        try {
//            producer = new KafkaProducer<String, String>(properties);
//            for (int i = 1; i < 200; i++) {
//                String msg = "Message " + i;
//                producer.send(new ProducerRecord<String, String>("test", msg));
//                System.out.println("Sent:" + msg);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        } finally {
//            producer.close();
//        }

        logger.info("发送完成...");
    }

}

package com.bcht.es;

import com.bcht.common.IndexConfigUtil;
import com.bcht.common.PropertiesUtil;
import com.bcht.kafka.IndexConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.es
 * @ClassName: EsIndexer
 * @Description: es建索主入口   注意：停止程序的时候请使用kill -15  命令   会触发程序钩子   保证建索信息不丢失
 * @Author: zhengchuan
 * @CreateDate: 2019/5/13 16:30
 * @UpdateUser:
 * @UpdateDate: 2019/5/13 16:30
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class EsIndexer {

    private static Logger logger = LoggerFactory.getLogger(EsIndexer.class);

    private static void init(){
        IndexConfigUtil.generateIndexConfigMap();
        logger.info("初始化   开始创建各个表的   index  mapping信息...");
        IndexConfigUtil.INDEX_CONFIG_MAP.forEach((key,value)->{
            ElasticsearchUtil.createMapping(key,key,value.getFieldTypes());
        });
        logger.info("初始化  各个表的   index  mapping信息创建完成...");
        logger.info("等待10s  让index的mapping信息更新完成...");
        try{
            Thread.sleep(10*1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        logger.info("等待时间结束   mapping信息更新完成...");
    }


    /**
     * MethodName: main
     * Description:   indexer  建索程序主入口
     * @param args
     * @return void
     * Author: zhengchuan
     * Date: 2019/5/27 10:36
     */
    public static void main(String[] args) {
        init();
        int consumerNum = PropertiesUtil.getIntValue("indexer.consumer.num",3);
        ExecutorService executor = Executors.newFixedThreadPool(consumerNum);
        final List<IndexConsumer> consumers = new ArrayList<>();
        for (int i = 0; i < consumerNum; i++) {
            IndexConsumer consumer = new IndexConsumer(i);
            consumers.add(consumer);
            executor.submit(consumer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("EsIndexer  开始停止...");
                for (IndexConsumer consumer : consumers) {
                    consumer.shutdown();
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ElasticsearchUtil.close();
                logger.info("EsIndexer  停止完成...");
            }
        });
    }

}

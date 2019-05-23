package com.bcht.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.common
 * @ClassName: KafkaPropertiesUtil
 * @Description: kafka连接相关信息  协处理器需要上传到hdfs上  配置信息需要在jar包中
 * @Author: zhengchuan
 * @CreateDate: 2019/5/21 17:29
 * @UpdateUser:
 * @UpdateDate: 2019/5/21 17:29
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class KafkaPropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(KafkaPropertiesUtil.class);

    private static final String CONFIG_PATH = "config.properties";

    private static Properties properties;

    static{
        try{
            InputStream in = KafkaPropertiesUtil.class.getClassLoader().getResourceAsStream(CONFIG_PATH);
            properties = new Properties();
            properties.load(in);
        }catch (IOException e){
            logger.error("读取配置文件出错",e);
        }
    }

    public static String getValue(String key,String defaultValue){
        return properties.getProperty(key,defaultValue);
    }

    public static String getValue(String key){
        return properties.getProperty(key);
    }

    public static Integer getIntValue(String key){
        String value = getValue(key);
        if(StringUtils.isNotEmpty(value)){
            return Integer.parseInt(value);
        }
        return null;
    }

    public static Integer getIntValue(String key,int defaultValue){
        String value = getValue(key);
        if(StringUtils.isNotEmpty(value)){
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

}

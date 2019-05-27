package com.bcht.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.common
 * @ClassName: PropertiesUtil
 * @Description: 配置信息读取   配置文件放在和项目平级的conf文件夹下
 * 免除后期更新jar包频繁替换配置文件
 * @Author: zhengchuan
 * @CreateDate: 2019/5/20 13:42
 * @UpdateUser:
 * @UpdateDate: 2019/5/20 13:42
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static final String CONFIG_PATH = "/conf/config.properties";

    private static Properties properties;

    static{
        try{
            String path = System.getProperty("user.dir")+CONFIG_PATH;
            InputStream in = new FileInputStream(path);
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

    public static Long getLongValue(String key){
        String value = getValue(key);
        if(StringUtils.isNotEmpty(value)){
            return Long.parseLong(value);
        }
        return null;
    }

    public static Long getLongValue(String key,long defaultValue){
        String value = getValue(key);
        if(StringUtils.isNotEmpty(value)){
            return Long.parseLong(value);
        }
        return defaultValue;
    }
}

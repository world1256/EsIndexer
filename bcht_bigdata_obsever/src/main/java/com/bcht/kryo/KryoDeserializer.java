package com.bcht.kryo;

import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.obsever.kryo
 * @ClassName: KryoDeserializer
 * @Description:
 * @Author: zhengchuan
 * @CreateDate: 2019/5/13 10:31
 * @UpdateUser:
 * @UpdateDate: 2019/5/13 10:31
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class KryoDeserializer implements Deserializer<Map<String,Object>> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public Map<String,Object> deserialize(String topic, byte[] data) {
        KryoUtils kryoUtils = new KryoUtils();
        return kryoUtils.deserialize(data);
    }

    @Override
    public void close() {

    }
}

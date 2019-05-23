package com.bcht.kryo;

import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.obsever.kryo
 * @ClassName: KryoSerializer
 * @Description:
 * @Author: zhengchuan
 * @CreateDate: 2019/5/13 10:05
 * @UpdateUser:
 * @UpdateDate: 2019/5/13 10:05
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class KryoSerializer implements Serializer<Map<String,Object>> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Map<String,Object> map) {
        KryoUtils kryoUtils = new KryoUtils();
        return  kryoUtils.serialize(map);
    }

    @Override
    public void close() {

    }
}

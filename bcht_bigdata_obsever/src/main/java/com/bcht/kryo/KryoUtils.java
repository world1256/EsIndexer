package com.bcht.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.obsever.kryo
 * @ClassName: KryoUtils
 * @Description:
 * @Author: zhengchuan
 * @CreateDate: 2019/5/13 10:14
 * @UpdateUser:
 * @UpdateDate: 2019/5/13 10:14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class KryoUtils {

    public static final int BUFFER_SIZE = 2048;
    public static final int MAX_BUFFER_SIZE = 10485760;

    // 由于kryo不是线程安全的，所以每个线程都使用独立的kryo
    final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class, new MapSerializer());
        return kryo;
        }
    };
    final ThreadLocal<Output> outputLocal = new ThreadLocal<Output>();
    final ThreadLocal<Input> inputLocal = new ThreadLocal<Input>();

    public byte[] serialize(Object obj) {
        Kryo kryo = getKryo();
        Output output = getOutput();
        kryo.writeClassAndObject(output,obj);
        return output.toBytes();
    }


    /**
     * 获取kryo
     *
     * @return
     */
    private Kryo getKryo() {
        return kryoLocal.get();
    }

    /**
     * 获取Output并设置初始数组
     *
     * @return
     */
    private Output getOutput() {
        Output output = null;
        if ((output = outputLocal.get()) == null) {
            output = new Output(BUFFER_SIZE, MAX_BUFFER_SIZE);
            outputLocal.set(output);
        }
        return output;
    }

    /**
     * 获取Input
     *
     * @param bytes
     * @param offset
     * @param count
     * @return
     */
    private Input getInput(byte[] bytes, int offset, int count) {
        Input input = null;
        if ((input = inputLocal.get()) == null) {
            input = new Input();
            inputLocal.set(input);
        }
        if (bytes != null) {
            input.setBuffer(bytes, offset, count);
        }
        return input;
    }

    public <T> T deserialize(byte[] bytes, int offset, int count) {
        Kryo kryo = getKryo();
        Input input = getInput(bytes, offset, count);
        return (T) kryo.readClassAndObject(input);
    }

    public <T> T deserialize(byte[] bytes) {
        return deserialize(bytes, 0, bytes.length);
    }


    public static void main(String[] args) {
        KryoUtils kryoUtils = new KryoUtils();
        Map<String,String> map = new HashMap<>();
        map.put("1","1");
        map.put("2","2");
        byte[] bytes = kryoUtils.serialize(map);
        Map<String,String> map2 = kryoUtils.deserialize(bytes);
        System.out.println(map2);
    }
}

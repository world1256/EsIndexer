package com.bcht.obsever;

import com.bcht.kafka.IndexProducer;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessor;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.coprocessor.RegionObserver;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.wal.WALEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.obsever
 * @ClassName: DataObserver
 * @Description: hbase协处理器  监听数据变化  将数据发送到kafka  hbase2.0版本以后使用
 * @Author: zhengchuan
 * @CreateDate: 2019/5/21 16:47
 * @UpdateUser:
 * @UpdateDate: 2019/5/21 16:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class DataObserver implements RegionObserver, RegionCoprocessor {

    private static Logger logger = LoggerFactory.getLogger(DataObserver.class);

    /**
     * 该方法必须实现  声明该RegionObserver  否则默认是空值  协处理器无法生效
     * @return
     */
    @Override
    public Optional<RegionObserver> getRegionObserver() {
        // Extremely important to be sure that the coprocessor is invoked as a RegionObserver
        return Optional.of(this);
    }


    @Override
    public void start(CoprocessorEnvironment env) throws IOException {
        IndexProducer.initProducer();
    }

    @Override
    public void stop(CoprocessorEnvironment env) throws IOException {
        IndexProducer.close();
    }

    @Override
    public void prePut(ObserverContext<RegionCoprocessorEnvironment> c, Put put, WALEdit edit, Durability durability) throws IOException {
        String tableName = c.getEnvironment().getRegionInfo().getTable().getNameAsString();
        String indexId = new String(put.getRow());
        NavigableMap<byte[], List<Cell>> familyMap = put.getFamilyCellMap();
        Map<String, Object> dataMap = new HashMap();
        for (Map.Entry<byte[], List<Cell>> entry : familyMap.entrySet()) {
            for (Cell cell : entry.getValue()) {
                String key = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                dataMap.put(key, value);
            }
        }
        dataMap.put("indexId",indexId);
        dataMap.put("operate","update");
        logger.info("开始发送数据到kafka 表名{}  rowKey{}  update",tableName,indexId);
        IndexProducer.sendData(tableName,dataMap);
        logger.info("update 发送完成...");
    }

    @Override
    public void preDelete(ObserverContext<RegionCoprocessorEnvironment> c, Delete delete, WALEdit edit, Durability durability) throws IOException {
        String tableName = c.getEnvironment().getRegionInfo().getTable().getNameAsString();
        String indexId = new String(delete.getRow());
        Map<String, Object> dataMap = new HashMap();
        dataMap.put("indexId",indexId);
        dataMap.put("operate","delete");
        logger.info("开始发送数据到kafka 表名{}  rowKey{} delete",tableName,indexId);
        IndexProducer.sendData(tableName,dataMap);
        logger.info("delete 发送完成...");
    }
}

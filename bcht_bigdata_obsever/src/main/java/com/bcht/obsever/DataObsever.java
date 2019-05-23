//package com.bcht.obsever;
//
//import com.bcht.kafka.IndexProducer;
//import org.apache.hadoop.hbase.Cell;
//import org.apache.hadoop.hbase.CellUtil;
//import org.apache.hadoop.hbase.CoprocessorEnvironment;
//import org.apache.hadoop.hbase.client.Delete;
//import org.apache.hadoop.hbase.client.Durability;
//import org.apache.hadoop.hbase.client.Put;
//import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
//import org.apache.hadoop.hbase.coprocessor.ObserverContext;
//import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
//import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
//import org.apache.hadoop.hbase.util.Bytes;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.NavigableMap;
//
///**
// * @ProjectName: bcht_bigdata
// * @Package: com.bcht.obsever
// * @ClassName: DataObsever   2.0以下版本使用该类
// * @Description:
// * @Author: zhengchuan
// * @CreateDate: 2019/4/22 9:30
// * @UpdateUser:
// * @UpdateDate: 2019/4/22 9:30
// * @UpdateRemark: 更新说明
// * @Version: 1.0
// */
//public class DataObsever extends BaseRegionObserver {
//
//    @Override
//    public void start(CoprocessorEnvironment e) throws IOException {
//        super.start(e);
//        IndexProducer.initProducer();
//    }
//
//    @Override
//    public void stop(CoprocessorEnvironment e) throws IOException {
//        super.stop(e);
//    }
//
//    @Override
//    public void prePut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {
//        super.prePut(e, put, edit, durability);
//        String tableName = e.getEnvironment().getRegionInfo().getTable().getNameAsString();
//        String indexId = new String(put.getRow());
//        NavigableMap<byte[], List<Cell>> familyMap = put.getFamilyCellMap();
//        Map<String, Object> dataMap = new HashMap<String, Object>();
//        for (Map.Entry<byte[], List<Cell>> entry : familyMap.entrySet()) {
//            for (Cell cell : entry.getValue()) {
//                String key = Bytes.toString(CellUtil.cloneQualifier(cell));
//                String value = Bytes.toString(CellUtil.cloneValue(cell));
//                dataMap.put(key, value);
//            }
//        }
//        dataMap.put("indexId",indexId);
//        dataMap.put("operate","update");
//        IndexProducer.sendData(tableName,dataMap);
//    }
//
//    @Override
//    public void preDelete(ObserverContext<RegionCoprocessorEnvironment> e, Delete delete, WALEdit edit, Durability durability) throws IOException {
//        super.preDelete(e, delete, edit, durability);
//        String tableName = e.getEnvironment().getRegionInfo().getTable().getNameAsString();
//        String indexId = new String(delete.getRow());
//        Map<String, Object> dataMap = new HashMap<String, Object>();
//        dataMap.put("indexId",indexId);
//        dataMap.put("operate","delete");
//        IndexProducer.sendData(tableName,dataMap);
//    }
//}

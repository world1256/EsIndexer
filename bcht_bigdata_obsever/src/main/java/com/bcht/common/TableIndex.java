package com.bcht.common;

import java.util.Map;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.common
 * @ClassName: TableIndex
 * @Description: 表的 索引字段信息
 * @Author: zhengchuan
 * @CreateDate: 2019/5/13 16:33
 * @UpdateUser:
 * @UpdateDate: 2019/5/13 16:33
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TableIndex {

    private String tableName;

    private Map<String,FieldType> fieldTypes;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, FieldType> getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(Map<String, FieldType> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }
}

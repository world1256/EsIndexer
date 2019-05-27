package com.bcht.common;

/**
 * @ProjectName: bcht_bigdata
 * @Package: com.bcht.common
 * @ClassName: FieldType
 * @Description: 单个字段的索引信息
 * @Author: zhengchuan
 * @CreateDate: 2019/5/13 16:35
 * @UpdateUser:
 * @UpdateDate: 2019/5/13 16:35
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class FieldType {

    public static String RULE_NORMAL = "normal";

    public static String RULE_LESS = "less";

    public static String RULE_GRATER = "grater";

    private String type;

    private String fieldName;

    private String rule;

    private Object value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

package com.lancelot.config;

//属性类
public class Field {
    //字段名
    private String fieldName;
    //字段类型
    private String fieldType;
    //特殊类型
    private String specialFieldType;
    //字段注释
    private String fieldRemarks;
    //字段名首字母大写
    private String fieldNameUpperFirstLetter;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getSpecialFieldType() {
        return specialFieldType;
    }

    public void setSpecialFieldType(String specialFieldType) {
        this.specialFieldType = specialFieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldRemarks() {
        return fieldRemarks;
    }

    public void setFieldRemarks(String fieldRemarks) {
        this.fieldRemarks = fieldRemarks;
    }

    public String getFieldNameUpperFirstLetter() {
        return fieldNameUpperFirstLetter;
    }

    public void setFieldNameUpperFirstLetter(String fieldNameUpperFirstLetter) {
        this.fieldNameUpperFirstLetter = fieldNameUpperFirstLetter;
    }
}
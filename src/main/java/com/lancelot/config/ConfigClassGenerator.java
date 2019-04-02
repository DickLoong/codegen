package com.lancelot.config;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ConfigClassGenerator {


    //根据表名生产出类
    public ConfigClass generateClass(File foundFile) {
        ConfigClass configClass = new ConfigClass();
        String name = foundFile.getName();
        String className = org.apache.commons.lang3.StringUtils.remove(name,".csv");
        configClass.setClassName(className);
        List<Field> fieldList = new ArrayList<Field>();
        try {
            FileInputStream fis = new FileInputStream(foundFile);
            BufferedReader br = null;
            CSVParser csvFileParser = null;
            List list = null;
            // 创建CSVFormat（header mapping）
            CSVFormat csvFileFormat = CSVFormat.DEFAULT;
            // 初始化FileReader object
            br = new BufferedReader(new InputStreamReader(new FileInputStream(foundFile), "UTF-8"));//解决乱码问题
            // 初始化 CSVParser object
            csvFileParser = new CSVParser(br, csvFileFormat);
            // CSV文件records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            Map<Integer, String> typeLineMap = new TreeMap<>();
            Map<Integer, String> typeMap = new HashMap<>();
            CSVRecord lineRow = csvRecords.get(0);
            CSVRecord typeRow = csvRecords.get(3);
            CSVRecord commentRow = csvRecords.get(2);
            for (int i = 1; i < lineRow.size(); i++) {
                String stringCellValue = lineRow.get(i);
                String typeString = typeRow.get(i);
                String javaType = typeString;
                if(StringUtils.equals(typeString , "string")){
                    javaType = "String";
                }
                if(StringUtils.equals(typeString , "text")){
                    javaType = "String";
                }
                String comment = commentRow.get(i);
                if (StringUtils.isNotEmpty(stringCellValue) && !StringUtils.contains(javaType, "[")) {
                    Field field = new Field();
                    field.setFieldName(stringCellValue);
                    field.setFieldType(javaType);
                    if(StringUtils.equals(typeString , "text")){
                        field.setSpecialFieldType("TEXT");
                    }
                    field.setFieldRemarks(comment);
                    field.setFieldNameUpperFirstLetter(upperFirstLetter(stringCellValue));
                    fieldList.add(field);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        configClass.setFieldList(fieldList);

        return configClass;
    }


    //首字母大写
    public String upperFirstLetter(String src) {
        String firstLetter = src.substring(0, 1).toUpperCase();
        String otherLetters = src.substring(1);
        return firstLetter + otherLetters;
    }
}
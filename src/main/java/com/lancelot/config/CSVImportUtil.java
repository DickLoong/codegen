package com.lancelot.config;

import com.alibaba.fastjson.JSONObject;
import com.lizongbo.codegentool.csv2db.Result;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;



import java.io.*;
import java.util.*;

public class CSVImportUtil {
    private static String EXCEL_DIR = Generator.CONFIG_CSV_DIR;
    private static String JSON_OUTPUT_DIR = "D:\\Users\\Burst\\IdeaProjects\\marvelbi-backend\\src\\main\\resources\\dbconfig";

    public static void main(String[] args) {
        try {
            if (args != null && args.length > 0) {
                EXCEL_DIR = args[0];
                JSON_OUTPUT_DIR = args[1];
            }
            File baseFile = new File(EXCEL_DIR);
            List<File> fileList = new LinkedList<>();
            List<File> searchingFileList = new ArrayList<>();
            searchingFileList.add(baseFile);
            Iterator<File> iterator = searchingFileList.iterator();
            while(CollectionUtils.isNotEmpty(searchingFileList)) {
                List<File> swappingFileList = new ArrayList<>();
                for (File next : searchingFileList) {
                    if (!next.isDirectory()) {
                        String name = next.getName();
                        if (StringUtils.contains(name, ".csv")  ) {
                            fileList.add(next);
                        }
                    } else {
                        File[] files = next.listFiles();
                        for (File listingFile : files) {
                            String name = listingFile.getName();
                            if (StringUtils.equals(name, "不用导")) {
                                continue;
                            }
                            if (StringUtils.contains(name, ".csv") || listingFile.isDirectory()) {
                                swappingFileList.add(listingFile);
                            }
                        }
                    }
                }
                searchingFileList = swappingFileList;
            }
            boolean successFlag = true;
            List<String> configNameList = new LinkedList<>();
            for(File foundFile : fileList) {
                try {
                    FileInputStream fis = new FileInputStream(foundFile);
                    BufferedReader br = null;
                    CSVParser csvFileParser = null;
                    List list = null;
                    // 创建CSVFormat（header mapping）
                    CSVFormat csvFileFormat = CSVFormat.DEFAULT;
                    // 初始化FileReader object
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(foundFile), "utf-8"));//解决乱码问题
                    // 初始化 CSVParser object
                    csvFileParser = new CSVParser(br, csvFileFormat);
                    // CSV文件records
                    List<CSVRecord> csvRecords = csvFileParser.getRecords();
                    Map<Integer,String> typeLineMap = new TreeMap<>();
                    Map<Integer,String> typeMap = new HashMap<>();
                    CSVRecord lineRow = csvRecords.get(0);
                    CSVRecord typeRow = csvRecords.get(3);
                    for (int i = 0; i < lineRow.size(); i++) {
                        String stringCellValue = lineRow.get(i);
                        String typeString = typeRow.get(i);
                        if (StringUtils.isNotEmpty(stringCellValue) && !StringUtils.contains(typeString,"[")) {
                            typeLineMap.put(i,stringCellValue );
                            typeMap.put(i,typeString);
                        }
                    }
                    Map<Integer,String> typeLineMap1 = new TreeMap<>();
                    List<TreeMap> jsonObjectList = new LinkedList<>();
                    String name = foundFile.getName();
                    System.out.println("generating config|" + name);
                    for (int rowNum = 1; rowNum < csvRecords.size(); rowNum++) {
                        CSVRecord workingRow = csvRecords.get(rowNum);
                        TreeMap workingObject = new TreeMap();
                        for (Map.Entry<Integer, String> entry : typeLineMap.entrySet()) {
                             Integer value= entry.getKey();
                            String key = entry.getValue();
                            if(value >= workingRow.size()){
                                System.out.println("error:ArrayIndexOutOfBoundsException|" + name + "|" + rowNum);
                            }
                            String stringCellValue = workingRow.get(value);
                            String s = typeMap.get(value);
                            if(rowNum > 3) {
                                Result<String> result = typeValidate(name,key,s,rowNum+1, stringCellValue);
                                if(result.isSuccess()){
                                    String object = result.getObject();
                                    stringCellValue = object;
                                }else{
                                    successFlag = false;
                                }
                            }
                            workingObject.put(key, stringCellValue);
                        }
                        jsonObjectList.add(workingObject);
                    }
                    System.out.println("generating config|checked|" + name + "|" + csvRecords.size());
                    if(successFlag) {
                        String outputString = JSONObject.toJSONString(jsonObjectList);
                        name = StringUtils.remove(name, ".csv");
                        name = name + ".json.txt";
                        File targetJsonFileLocation = new File(JSON_OUTPUT_DIR, name);
                        if (targetJsonFileLocation.exists()) {
                            targetJsonFileLocation.delete();
                            targetJsonFileLocation = new File(JSON_OUTPUT_DIR, name);
                        }
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(targetJsonFileLocation), "UTF-8"));
                        bw.write(outputString);
                        bw.close();
                        System.out.println("generating config|achieved|" + name + "|" + csvRecords.size());
                        configNameList.add(name);
                    }
                }catch(Throwable th){
                    th.printStackTrace();
                }
            }
            File menuList = new File(JSON_OUTPUT_DIR,"config.menu");
            try(FileWriter fw = new FileWriter(menuList)){
                String jsonArrayList = JSONObject.toJSONString(configNameList);
                fw.write(jsonArrayList);
                fw.flush();
                System.out.println("generating config|menued|" + configNameList.size() + "|" + jsonArrayList);
            }
            if(!successFlag){
                System.exit(-1);
            }else{
                System.exit(0);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.exit(-1);
    }

    private static Result<String> typeValidate(String tableName ,String columnName,String type ,int rowNum, String value){
        try {
            if (null == type) {
                return new Result<>(value);
            }
            value = StringUtils.remove(value, "\"\"");
            value = StringUtils.trim(value);
            if (StringUtils.isEmpty(value)) {
                return new Result<>(value);
            }
            switch (type) {
                case "int": {
                    if (StringUtils.contains(value, "0x")) {
                        value = StringUtils.remove(value, "0x");
                        int i = Integer.valueOf(value, 16);
                        value = Integer.toString(i);
                    } else {
                        int i = Integer.parseInt(value);
                        value = Integer.toString(i);
                    }
                    return new Result<>(value);
                }
                case "short": {
                    if (StringUtils.contains(value, "0x")) {
                        value = StringUtils.remove(value, "0x");
                        int i = Integer.valueOf(value, 16);
                        value = Integer.toString(i);
                    } else {
                        int i = Integer.parseInt(value);
                        value = Integer.toString(i);
                    }
                    return new Result<>(value);
                }
                case "long": {
                    if (StringUtils.contains(value, "0x")) {
                        value = StringUtils.remove(value, "0x");
                        long i = Long.valueOf(value, 16);
                        value = Long.toString(i);
                    } else {
                        long i = Long.parseLong(value);
                        value = Long.toString(i);
                    }
                    return new Result<>(value);
                }
                case "double": {
                    Double l = Double.parseDouble(value);
                    return new Result<>(value);
                }
                case "float": {
                    Double l = Double.parseDouble(value);
                    value = Float.toString(l.floatValue());
                    return new Result<>(value);
                }
                case "string": {
                    return new Result<>(value);
                }
                default: {
                    return new Result<>(value);
                }
            }
        }catch(Throwable th){
            String actualTableName = StringUtils.remove(tableName,"_Sheet1.csv");
            System.out.println("error during parsing table|" + actualTableName + "|" + columnName + "|" + type + "|" + rowNum + "|"  + value);
            return Result.CreateErrorResult(-3);
        }
    }
}

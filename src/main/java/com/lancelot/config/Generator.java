package com.lancelot.config;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

public class Generator {

    public static  String CONFIG_CSV_DIR = "/Users/project/svndbconfig";
    private static String JAVA_OUTPUT_DIR = "/Users/project/dmp-api/src/main/java/com/lexing360/dmp/dbconfig/";
    private static String JAVA_DAO_OUTPUT_DIR = "/Users/project/dmp-api/src/main/java/com/lexing360/dmp/dbconfig/dao/";
    private static String CONFIG_PACKAGE_NAME = "com.lexing360.dmp.dbconfig";
    private static String CONFIG_DAO_PACKAGE_NAME = "com.lexing360.dmp.dbconfig.dao";

    public static void main(String[] args) throws Exception{
        if(null != args && args.length > 4){
            CONFIG_CSV_DIR = args[0];
            JAVA_OUTPUT_DIR = args[1];
            JAVA_DAO_OUTPUT_DIR = args[2];
            CONFIG_PACKAGE_NAME = args[3];
            CONFIG_DAO_PACKAGE_NAME = args[4];
        }
        File baseFile = new File(CONFIG_CSV_DIR);
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
        //创建Configuration对象
        Configuration configuration = new Configuration();
        //设置模板所在目录
        String path = Generator.class.getClassLoader().getResource("ftl").getPath();
        configuration.setDirectoryForTemplateLoading(new File(path));
        //获取模板
        Template template = configuration.getTemplate("entity.ftl");


        configuration = new Configuration();
        //设置模板所在目录
        path = Generator.class.getClassLoader().getResource("ftl").getPath();
        configuration.setDirectoryForTemplateLoading(new File(path));
        Template daoTemplate = configuration.getTemplate("dao.ftl");
        List<String> configNameList = new LinkedList<>();
        for(File foundFile : fileList) {
            try {
                //设置数据并执行
                Map map = new HashMap();

                ConfigClassGenerator classGenerator = new ConfigClassGenerator();
                ConfigClass myClass = classGenerator.generateClass(foundFile);
                System.out.println("generating class|" + myClass.getClassName());
                map.put("myClass", myClass);
                map.put("config_package_name",CONFIG_PACKAGE_NAME);
                map.put("config_dao_package_name",CONFIG_DAO_PACKAGE_NAME);
                Writer writer = new OutputStreamWriter(new FileOutputStream(JAVA_OUTPUT_DIR + myClass.getClassName() + ".java"));

                template.process(map, writer);
                Writer daoWriter = new OutputStreamWriter(new FileOutputStream(JAVA_DAO_OUTPUT_DIR + myClass.getClassName() + "ConfigDao.java"));
                daoTemplate.process(map,daoWriter);
                System.out.println("finished generating class|" + myClass.getClassName());
                configNameList.add(myClass.getClassName());
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }
}

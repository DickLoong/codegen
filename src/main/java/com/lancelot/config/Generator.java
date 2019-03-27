package com.lancelot.config;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class Generator {

    public static final String CONFIG_CSV_DIR = "/Users/project/dbconfig";
    private static final String JAVA_OUTPUT_DIR = "/Users/project/dmp-api/src/main/java/com/lexing360/dmp/dbconfig/";
    private static final String JAVA_DAO_OUTPUT_DIR = "/Users/project/dmp-api/src/main/java/com/lexing360/dmp/dbconfig/dao/";
    private static final String CONFIG_PACKAGE_NAME = "com.lexing360.dmp.dbconfig";
    private static final String CONFIG_DAO_PACKAGE_NAME = "com.lexing360.dmp.dbconfig.dao";

    public static void main(String[] args) throws Exception{
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
        for(File foundFile : fileList) {
            try {
                //设置数据并执行
                Map map = new HashMap();

                ConfigClassGenerator classGenerator = new ConfigClassGenerator();
                ConfigClass myClass = classGenerator.generateClass(foundFile);

                map.put("myClass", myClass);
                map.put("config_package_name",CONFIG_PACKAGE_NAME);
                map.put("config_dao_package_name",CONFIG_DAO_PACKAGE_NAME);
                Writer writer = new OutputStreamWriter(new FileOutputStream(JAVA_OUTPUT_DIR + myClass.getClassName() + ".java"));

                template.process(map, writer);
                Writer daoWriter = new OutputStreamWriter(new FileOutputStream(JAVA_DAO_OUTPUT_DIR + myClass.getClassName() + "ConfigDao.java"));
                daoTemplate.process(map,daoWriter);
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }
}

package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.lizongbo.codegentool.db2java.dbsql2xml.DBUtil;

/**
 * 将行说明的表配置文件转成可导入数据库的列的形式
 * 
 * @author lizongbo
 *
 */
public class CSVHang2Lie {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String csvDir = "/Users/lizongbo/Documents/workspace/sango_configs/csvfiles/Public/Common";
		for (File csvFile : new File(csvDir).listFiles()) {
			if (csvFile.getName().startsWith("TSkill")
					&& csvFile.getName().endsWith(".csv")) {

				String className = CSVUtil.getTableNameFromCSVFile(csvFile
						.getAbsolutePath());
				System.out.println(className);
				StringBuilder sb = new StringBuilder();
				sb.append("using System.Collections;\n");

				sb.append("public class "+className+"  {\n");
				// String
				// csvFile="/Users/lizongbo/Documents/workspace/sango_configs/csvfiles/Public/Common/TSkill(技能配置)_LevelUpInfo(技能升级表).csv";
				List<String[]> colList = getDataFromMacCSV(csvFile
						.getAbsolutePath());
				for (String[] cols : colList) {
					// System.out.println(Arrays.toString(cols));

				}
				String[] titles = colList.get(1);
				Map<String,Integer> countMap=new HashMap<String,Integer>();
				for (int i=0;i<titles.length;i++) {
					String title = titles[i];
					String pinyinName=title;
					if(pinyinName==null || pinyinName.length()<1){
						System.err.println(i+"|pinyinName="+pinyinName+"|for|"+title +"|"+csvFile.getName() +"|"+colList.get(0)[i]);
					continue;
					}
					char c=pinyinName.charAt(0);
					if(c >='0' && c<='9'){
						pinyinName="di"+pinyinName;
					}
					pinyinName= pinyinName.replace('（', '(');
					pinyinName= pinyinName.replace(':', ' ');
					if(pinyinName.contains("(")){
						pinyinName=pinyinName.substring(0,pinyinName.indexOf("("));
					}
					if(!countMap.containsKey(pinyinName)){
						countMap.put(pinyinName, 0);
					}
					int count=countMap.get(pinyinName);
					if(count>0){
						countMap.put(pinyinName, count+1);
						pinyinName =pinyinName+(count+1);
					}else{
						countMap.put(pinyinName, count+1);
					}
					
					String type =colList.get(0)[i];
					sb.append("public ").append(type).append(" ")
					.append(pinyinName).append(";///").append(title).append("\n");
					//public int id;//sss
					//System.out.println(title + " === " + getPinyin(title));
				}

				
				sb.append("\n");
				sb.append("}\n");
System.out.println(sb);
DBUtil.writeFile("/Users/lizongbo/Documents/workspace/gecao_client/gecaodemo/gecaodemo0/Assets/gecaodemou3d/Modules/Skill/"+className+".cs", 
		sb.toString());
			}
		}
		for (File csvFile : new File(csvDir).listFiles()) {
			if (csvFile.getName().startsWith("TSkill")
					&& csvFile.getName().endsWith(".csv")) {
				System.out.println(csvFile);
				}
			}

	}


	public static List<String[]> getDataFromMacCSV(String csvFilePath) {
		try {
			java.io.FileInputStream fis = new FileInputStream(csvFilePath);
			java.io.InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			// CSVReader cr = new CSVReader(isr);
			// List<String[]> colList = cr.readAll();//
			// 前三行是潜规则的，第一行是表的字段名，第二行是字段类型，第三行是表的字段说明，第一列是主键。

			org.apache.commons.csv.CSVParser csvp = new CSVParser(isr,
					org.apache.commons.csv.CSVFormat.EXCEL.withDelimiter(';'));

			List<CSVRecord> csvrList = csvp.getRecords();

			List<String[]> colList = new ArrayList<String[]>();
			for (CSVRecord csvr : csvrList) {
				List<String> valueList = new ArrayList<String>();
				java.util.Iterator<String> vi = csvr.iterator();
				while (vi.hasNext()) {
					valueList.add(vi.next());
				}
				// System.out.println(valueList);
				colList.add(valueList.toArray(new String[0]));
			}
			return colList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<String[]>();
	}
}

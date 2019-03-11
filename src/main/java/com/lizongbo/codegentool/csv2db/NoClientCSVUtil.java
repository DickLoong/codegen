package com.lizongbo.codegentool.csv2db;

import java.io.*;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.util.*;

import com.lizongbo.codegentool.db2java.dbsql2xml.DBUtil;

/**
 * 生成I18N字段的映射信息
 * 
 * @author quickli
 *
 */
public class NoClientCSVUtil {

	public static void main(String[] args) {
		String csvpath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/Ttransformer(机甲养成配置)_Propertyinfo(机甲属性表).csv";
		String tableName = CSVUtil.getTableNameFromCSVFile(csvpath);
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvpath);
		genDbColNoClientCSVIndex(tableName, colList);
		System.out.println("getDbColNoClientCSVIndex==" + getDbColNoClientCSVIndex(tableName));

		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		for (int i = 0; i < colNames.length; i++) {
			if (colNames[i].trim().length() > 0
					&& (colTypes[i].toLowerCase().contains("str") || colTypes[i].toLowerCase().contains("text"))) {
				String propName = colNames[i].toLowerCase().trim();

				System.out.println(
						"getDbColNoClientCSVIndex|" + propName + "==" + getDbColNoClientCSVIndex(tableName, propName));
			}
		}
	}

	public static String getDbColNoClientCSVIndex(String tableName) {
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		return getDbColNoClientCSVIndexByClassName(className);

	}

	public static String getDbColNoClientCSVIndexByClassName(String className) {

		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/NoClientCSVindexs";
		String protoIndexFileName = "alldb.noclientcsv.txt";

		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String propName = className;
			String pbIndex = pp.getProperty(propName, "").trim();
			return pbIndex;
		}
	}

	public static String getDbColNoClientCSVIndex(String tableName, String propName) {
		String flag = getDbColNoClientCSVIndex(tableName);
		if ("no".equals(flag)) {// 如果是整个表在客户端禁用，就直接返回
			return flag;
		}
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		return getDbColNoClientCSVIndexByClassName(className, propName);
	}

	public static String getDbColNoClientCSVIndexByClassName(String className, String propName) {
		String flag = getDbColNoClientCSVIndexByClassName(className);
		if ("no".equals(flag)) {// 如果是整个表在客户端禁用，就直接返回
			return flag;
		}
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/NoClientCSVindexs";
		String protoIndexFileName = className + ".noclientcsv.txt";

		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String pbIndex = pp.getProperty(propName, "").trim();
			return pbIndex;
		}
	}

	public static Set<String> getDbColNoClientCSVColumnsByClassName(String className) {
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/NoClientCSVindexs";
		String protoIndexFileName = className + ".noclientcsv.txt";
		Set<String> set = new TreeSet<String>();
		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
					for (String key : pp.stringPropertyNames()) {
						if ("no".equals(pp.getProperty(key))) {
							set.add(key);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return set;
	}

	public static void genNoClientCSVIndex(String tableName) {
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/NoClientCSVindexs";
		String protoIndexFileName = "alldb.noclientcsv.txt";

		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		pbIndexFile.getParentFile().mkdirs();
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String propName = className;
			String pbIndex = pp.getProperty(propName);
			if (pbIndex == null) { // 是新字段，则需要分配新序号，粗暴一点自动分配
				System.out.println("genNoClientCSVIndex|update|" + tableName + "." + propName + "==");
				pp.setProperty(propName, "yes");
				if (propName.toLowerCase().startsWith("tuser") || propName.toLowerCase().startsWith("twebadmin")) {
					pp.setProperty(propName, "no");
				}
				try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
					storeProperties(pp, fos);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("genNoClientCSVIndex|exits|" + tableName + "." + propName + "==" + pbIndex);
			}
		}

	}

	private static void storeProperties(Properties pp, OutputStream fos) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));
		Set<String> keyList = new TreeSet<String>(pp.stringPropertyNames());
		System.err.println("storeProperties ==" + keyList);
		for (String key : keyList) {
			String val = pp.getProperty(key);
			bw.write(key + "=" + val);
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}

	public static void genDbColNoClientCSVIndex(String tableName, List<String[]> colList) {
		genNoClientCSVIndex(tableName);
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/NoClientCSVindexs";
		String protoIndexFileName = className + ".noclientcsv.txt";
		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (pp.size() < 1) {// 尚无数据，则生成一次
				for (int i = 0; i < colNames.length; i++) {
					if (colNames[i].trim().length() > 0 && (colTypes[i].toLowerCase().contains("str")
							|| colTypes[i].toLowerCase().contains("text"))) {
						String propName = colNames[i].toLowerCase().trim();
						pp.setProperty(propName, "yes");
					}
				}
				pbIndexFile.getParentFile().mkdirs();
				try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
					storeProperties(pp, fos);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < colNames.length; i++) {
				if (colNames[i].trim().length() > 0
						&& (colTypes[i].toLowerCase().contains("str") || colTypes[i].toLowerCase().contains("text"))) {
					String propName = colNames[i].toLowerCase().trim();
					String pbIndex = pp.getProperty(propName);
					if (pbIndex == null) { // 是新字段，则需要分配新序号
						System.out.println("genNoClientCSVIndex|update|" + tableName + "." + propName);
						pp.setProperty(propName, "yes");
						try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
							storeProperties(pp, fos);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
				storeProperties(pp, fos);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将 简体中文的多语言和其它国家或地区的多语言进行纯数字比较
	 * 
	 * @param zhCNCsvPath
	 * @param otherLocaeCsvPath
	 */
	public static void compareI18NDbColValue(String zhCNCsvPath, String otherLocaeCsvPath) {

	}
}

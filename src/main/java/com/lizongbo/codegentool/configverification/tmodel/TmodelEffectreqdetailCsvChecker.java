package com.lizongbo.codegentool.configverification.tmodel;

import java.io.*;
import java.util.*;

import com.lizongbo.codegentool.MailTest;
import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.csv2db.*;
import com.lizongbo.codegentool.tools.StringUtil;

public class TmodelEffectreqdetailCsvChecker implements CsvChecker {

	@Override
	public List<String> checkCsv(String csvPath) {
		List<String> errorList = new ArrayList<String>();
		File csvFile = new File(csvPath);
		if (!csvFile.exists() || !csvFile.isFile()) {
			errorList.add("File Error:" + csvFile);
			return errorList;
		}
		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		// 先获取所有的已有特效prefab
		Set<String> es = getEffectPrafabs();
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String effect_prefab_names = CSVUtil.getColValue("effect_prefab_name", s, colList);
			String efs[] = StringUtil.split(effect_prefab_names, ",");
			for (String e : efs) {
				if (e.length() > 0 && !es.contains(e)) {// 如果特效不存在，就告警
					MailTest.sendErrorMail(es.size() + "个特效中不存在配置的特效" + e,
							"配置的特效" + e + "不存在,当前设置为：" + Arrays.toString(s) + ",来自" + csvFile.getName(),
							new String[] { "zprui@billionkj.com", "ymw@billionkj.com",
									"quickli@billionkj.com" });
				}
			}
		}
		return errorList;
	}

	public static Set<String> getEffectPrafabs() {
		Set<String> set = new HashSet<String>();
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Prefabs/Effect";
		getEffectPrafabs(dir, set);
		return set;
	}

	public static Set<String> getEffectPrafabs(String dir, Set<String> set) {
		File f = new File(dir);
		if (f.isDirectory()) {
			File[] subfs = f.listFiles();
			for (File subf : subfs) {
				if (subf.isFile() && subf.getName().endsWith(".prefab")) {
					String name = StringUtil.replaceAll(subf.getName(), ".prefab", "");
					set.add(name);
				}
				if (subf.isDirectory()) {
					set.addAll(getEffectPrafabs(subf.getAbsolutePath(), set));
				}
			}
		}
		return set;
	}

	public static void main(String[] args) {
		Set<String> es = getEffectPrafabs();
		System.out.println(es.size() + "|" + es);
	}
}

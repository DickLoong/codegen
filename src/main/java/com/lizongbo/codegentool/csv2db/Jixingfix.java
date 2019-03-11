package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.util.TreeMap;

public class Jixingfix {

	/**
	 * 整理机型
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 先逐行读取csv,针对华为，小米，三星，中兴，oppo，vivo,coolpad,HTC等机型进行修正处理
		StringBuilder sb = new StringBuilder();
		File readFile;
		try {
			TreeMap<String, Double> zhanbiMap = new TreeMap<String, Double>();
			String path = "/mgamedev/workspace/jixing20160226.csv";
			readFile = new File(path);
			// 如果文本文件不存在则返回空串
			if (!readFile.exists()) {
				return;
			}
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(path), "UTF-8"));

			String data = null;
			while ((data = br.readLine()) != null) {
				// System.out.println("data==" + data);
				data = data.trim();
				if (data.length() > 1) {
					String name = "";
					double zhanbi = 0.0f;
					name = data.substring(0, data.lastIndexOf(";"));
					zhanbi = Double.parseDouble(data.substring(data.lastIndexOf(";") + 1, data.length() - 1));
					// System.out.println("name=" + name + "|zhanbi=" + zhanbi);

					if (name.length() > 0) {// 是有机型的，则做转换处理
						String newName = fixName(name);
						if (zhanbiMap.containsKey(newName)) {// 则累计
							double dd = zhanbiMap.get(newName);
							dd = dd + zhanbi;
							zhanbiMap.put(newName, dd);
						} else {
							zhanbiMap.put(newName, zhanbi);
						}
					}
				}
			}
			br.close();

			for (String k : zhanbiMap.keySet()) {
				System.out.println(k + "\t" + (zhanbiMap.get(k) * 1000000));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static String fixName(String orgName) {
		orgName = orgName.replace("%0D", "");
		orgName = orgName.replace("%2B", "+");
		orgName = orgName.replace("%28", "(");
		orgName = orgName.replace("%29", ")");
		String newName = orgName;
		if (newName.startsWith("Coolpad+")) {// Coolpad+
			// 把最后一位字母掐掉
			char c = newName.charAt(newName.length() - 1);
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
				newName = newName.substring(0, newName.length() - 2);
			}
		} else if (newName.startsWith("vivo+")) {// vivo
			// 把最后一位字母掐掉
			char c = newName.charAt(newName.length() - 1);
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
				newName = newName.substring(0, newName.length() - 2);
			}
		} else if (newName.startsWith("GT-")) {// 三星
			// 把最后一位字母掐掉
			char c = newName.charAt(newName.length() - 1);
			if (c >= 'A' && c <= 'Z') {
				newName = newName.substring(0, newName.length() - 2);
			}
		} else if (orgName.startsWith("HUAWEI")) {
			if (orgName.contains("+") && orgName.contains("-")) {
				newName = orgName.substring(0, orgName.indexOf("-"));
			}
		} else if (orgName.startsWith("Redmi")) {
			newName = orgName.replace("Redmi", "HM");
		}
		if (newName.startsWith("HM+")) {// 红米系列
			newName = newName.replace("LTE", "").replace("TD", "");
		}
		if (newName.startsWith("MI+")) {// 小米系列
			newName = newName.replace("LTE", "").replace("TD", "");
		}
		if (newName.startsWith("SAMSUNG+GT")) {// 三星系列
			newName = newName.replace("SAMSUNG+GT", "GT");
		}
		return newName;
	}

}

package com.lizongbo.codegentool;

import java.io.File;

import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.db2java.dbsql2xml.DBUtil;
import com.lizongbo.codegentool.db2java.dbsql2xml.XmlCodeGen;

public class UnityInspectorTool {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// /加载cs文件，找到有注释的变量，然后判断数据类型必须是基础数据类型，才生成对应的辅助代码。

		String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/forClient/net/bilinkeji/gecaoshoulie/maplevels/PkMapLevelInfo.cs";
		csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/forClient/net/bilinkeji/gecaoshoulie/maplevels/GoblinBatchInfo.cs";
		paseFile(csFilePath, "UTF-8");
	}

	public static String paseFile(String path, String encoding) {
		String allText = XmlCodeGen.readFile(path, encoding);// 先读取所有内容,再逐行解析找到变量
		StringBuilder sb = new StringBuilder();
		File readFile;
		try {
			readFile = new File(path);
			// 如果文本文件不存在则返回空串
			if (!readFile.exists()) {
				return "";
			}
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(path), encoding));

			boolean inClass = false;
			String data = null;
			while ((data = br.readLine()) != null) {
				if (data.contains("public class")) {
					inClass = true;
				} else {
					if (inClass && data.trim().startsWith("public ") && data.trim().endsWith(";")
							&& data.contains("=")) {
						// 在这里要提取变量名称和类型;
						String paramStr = data;
						paramStr = paramStr.replaceAll("  ", " ").trim();
						String paramArr[] = paramStr.split(" ");
						String paramName = paramArr[2];
						String paramType = paramArr[1];
						String labelText = "";
						// boolean haveZhushi = false;
						/// System.out.println("aaaaaaaa|" + data);//
						// 找到变量定义，开始解析注释
						String prevText = allText.substring(0, allText.indexOf(data));
						if (prevText.contains("/// <summary>") && prevText.contains("/// </summary>")
								&& prevText.lastIndexOf("/// </summary>") > prevText.lastIndexOf("/// <summary>")) {
							String zhushiText = prevText.substring(prevText.lastIndexOf("/// <summary>"),
									prevText.lastIndexOf("/// </summary>") + "/// </summary>".length());
							String subStr = prevText.substring(prevText.lastIndexOf("/// </summary>"));
							if (!subStr.contains("public ")) {
								// /System.out.println("bbbbbbbbbbb|" +
								// zhushiText);
								labelText = zhushiText.substring(zhushiText.indexOf("<summary>") + "<summary>".length(),
										zhushiText.indexOf("/// </summary>"));
								labelText = labelText.replaceAll("///", "").trim();
								// System.err.println("ccccccccccc|" + subStr);
								// System.out.println("labelText======"
								// + labelText);
							}

						}

						if (labelText.trim().length() < 1) {
							labelText = paramName;
						}
						StringBuilder sb2 = new StringBuilder();
						sb2.append("\n");
						sb2.append(paramType + " " + paramName + " = Target." + paramName + ";\n");
						sb2.append("" + paramName + " = EditorGUILayout." + getFieldMethod(paramType) + " (\""
								+ labelText + "\", " + paramName + ");\n");
						sb2.append("if (" + getPropCheckCond(paramType, paramName) + ") {\n");
						sb2.append("	Target." + paramName + " = " + paramName + ";\n");
						sb2.append("}\n");
						if (isSupportPropType(paramType)) {
							System.out.println(sb2);
						}
					}
				}
				// sb.append(data).append("\n");
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}

	private static String getPropCheckCond(String paramType, String paramName) {
		if ("string".equals(paramType)) {
			return paramName + " != \"\" ";
		}
		if ("int".equals(paramType)) {
			return paramName + " > 0";
		}
		if ("float".equals(paramType)) {
			return paramName + " > 0";
		}
		if ("long".equals(paramType)) {
			return paramName + " > 0";
		}
		if ("string".equals(paramType)) {
			return "TextField";
		}
		return "";
	}

	private static String getFieldMethod(String paramType) {
		if ("string".equals(paramType)) {
			return "TextField";
		}
		if ("int".equals(paramType)) {
			return "IntField";
		}
		if ("float".equals(paramType)) {
			return "FloatField";
		}
		if ("long".equals(paramType)) {
			return "LongField";
		}
		if ("string".equals(paramType)) {
			return "TextField";
		}
		return "";
	}

	private static boolean isSupportPropType(String paramType) {
		if ("string".equals(paramType)) {
			return true;
		}
		if ("int".equals(paramType)) {
			return true;
		}
		if ("float".equals(paramType)) {
			return true;
		}
		if ("long".equals(paramType)) {
			return true;
		}
		if ("string".equals(paramType)) {
			return true;
		}
		return false;
	}
}

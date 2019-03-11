package com.lizongbo.codegentool.json;

import java.io.File;

import org.json.JSONObject;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

public class GenJsonCall4iOS {

	public static void main(String[] args) {
		String iOSSrcDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Plugins/iOS";
		geniOSJsonCallClass(iOSSrcDir);
	}

	public static void geniOSJsonCallClass(String dir) {
		File d = new File(dir);
		if (d.isDirectory()) {
			File[] fs = d.listFiles();
			for (int i = 0; fs != null && i < fs.length; i++) {
				File file = fs[i];
				if (file.isFile() && file.getName().endsWith("4iOSImpl.mm")) {
					genJsonCallClass4File(file);

				}
				if (file.isDirectory()) {
					geniOSJsonCallClass(file.getAbsolutePath());
				}

			}
		}

	}

	private static void genJsonCallClass4File(File f) {
		if (!f.exists()) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		StringBuilder sbh = new StringBuilder();
		String calssName = f.getName().substring(0, f.getName().indexOf("Impl.mm"));
		File hf = new File(f.getParentFile(), calssName + ".h");
		File nf = new File(f.getParentFile(), calssName + ".mm");
		String packageName = "";
		try {
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(f), "UTF-8"));

			String data = null;
			while ((data = br.readLine()) != null) {
				if (data.trim().startsWith("@implementation")) {
					packageName = data.replace("Impl", "");
					// sb.append(packageName + "\n");
					sb.append("#import \"" + calssName + ".h\"\n");
					sb.append("\n");
					sb.append("#import \"" + calssName + "Impl.h\"\n");
					sb.append("\n");
					sb.append("@implementation " + calssName + "\n");
					sbh.append("#import <UIKit/UIKit.h>\n");

					sbh.append("@interface " + calssName + " : NSObject\n");
					sbh.append("{\n");

					sbh.append("}\n");
				}
				// 在这里判断是否有
				if (data.contains("+(NSDictionary*)")) {// 是方法定义
					String methodName = data;

					if (data.contains(":")) {
						methodName = data.substring(0, data.indexOf(":"));
						methodName = methodName.substring(methodName.lastIndexOf(")") + 1).trim();
					} else {
						methodName = methodName.substring(methodName.lastIndexOf(")") + 1).trim();
						if (methodName.contains("{")) {
							methodName = methodName.substring(0, methodName.indexOf("{"));
						}
					}
					System.err.println("methodName======" + methodName);
					String paramsStr = data.substring(data.indexOf(":") + 1);
					while (!paramsStr.contains("{")) {
						paramsStr = paramsStr + br.readLine();
					}
					System.out.println("paramsStr====" + paramsStr);
					paramsStr = paramsStr.substring(0, paramsStr.indexOf("{"));
					System.out.println("paramsStrAAAAAAAAAAA====" + paramsStr);
					paramsStr = paramsStr.replaceAll("final", "");
					String paramArr[] = paramsStr.split(" ");
					sb.append("+(NSDictionary*)" + methodName.replaceAll("_Internal", "") + ":(NSDictionary*)args{\n");
					for (int i = 0; i < paramArr.length; i++) {
						String param = paramArr[i].trim();
						System.out.println(param);
						String javaType = param.substring(param.indexOf("(") + 1).trim();
						javaType = javaType.substring(0, javaType.indexOf(")")).trim();
						String javaName = param.substring(param.indexOf(")") + 1).trim();
						if (javaType.contains("NSDictionary") || javaType.contains("NSString")) {
							sb.append("			" + javaType + " " + javaName + "TmpAA = [args valueForKey:@\""
									+ javaName + "\"];\n");
						} else {
							sb.append("			" + javaType + " " + javaName + "TmpAA = [[args valueForKey:@\""
									+ javaName + "\"] " + getjsongetMethodName(javaType) + "];\n");
						}
					}
					sb.append("			return [" + calssName + "Impl" + " " + methodName + "");
					for (int i = 0; i < paramArr.length; i++) {
						String param = paramArr[i].trim();
						System.out.println(param);
						String javaType = param.substring(param.indexOf("(") + 1).trim();
						javaType = javaType.substring(0, javaType.indexOf(")")).trim();
						String javaName = param.substring(param.indexOf(")") + 1).trim();
						if (i > 0) {
							sb.append(javaName);
						}
						sb.append(":").append(javaName).append("TmpAA ");
					}

					sbh.append("+(NSDictionary*)" + methodName.replaceAll("_Internal", "") + ":(NSDictionary*)args;\n");

					sb.append("];\n");
					sb.append("	}\n");

				}
			}
			sb.append("@end\n");
			sbh.append("@end\n");
			System.out.println(sb);
			System.out.println(sbh);
			br.close();
			GameCSV2DB.writeFile(nf.getAbsolutePath(), sb.toString());
			GameCSV2DB.writeFile(hf.getAbsolutePath(), sbh.toString());

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static String getjsongetMethodName(String javaType) {
		if ("int".equalsIgnoreCase(javaType.trim())) {
			return "integerValue";
		}
		if ("bool".equalsIgnoreCase(javaType.trim())) {
			return "boolValue";
		}
		if ("boolean".equalsIgnoreCase(javaType.trim())) {
			return "boolValue";
		}
		if ("float".equalsIgnoreCase(javaType.trim())) {
			return "floatValue";
		}
		if ("double".equalsIgnoreCase(javaType.trim())) {
			return "doubleValue";
		}
		if ("String".equalsIgnoreCase(javaType.trim())) {
			return "stringValue";
		}
		if (javaType.trim().contains("NSString")) {
			return "stringValue";
		}
		if ("JSONObject".equalsIgnoreCase(javaType.trim())) {
			return "getJSONObject";
		}
		if ("long".equalsIgnoreCase(javaType.trim())) {
			return "longValue";
		}
		if ("JSONArray".equalsIgnoreCase(javaType.trim())) {
			return "getJSONArray";
		}

		System.err.println("getjsongetMethodName|for|" + javaType);
		return "";
	}

}

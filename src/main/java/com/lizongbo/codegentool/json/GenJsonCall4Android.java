package com.lizongbo.codegentool.json;

import java.io.File;

import org.json.JSONObject;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

/**
 * 其实生成android代码的时候，iOS接口和默认实现也可以都生成了
 * 
 * @author lizongbo
 *
 */
public class GenJsonCall4Android {

	public static void main(String[] args) {
		String androidSrcDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/build/AndroidProj/gecaoshouliedemo/src/net/bilinkeji/u3dnative";
		//androidSrcDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/build/templateproj/AndroidProj/gecaoshouliedemo/src/net/bilinkeji/u3dnative";
		genAndroidJsonCallClass(androidSrcDir);
		long a = 20391111020304l;
		System.err.println(new java.sql.Timestamp(System.currentTimeMillis()));
	}

	public static void genAndroidJsonCallClass(String dir) {
		File d = new File(dir);
		if (d.isDirectory()) {
			File[] fs = d.listFiles();
			for (int i = 0; fs != null && i < fs.length; i++) {
				File file = fs[i];
				if (file.isFile()
						&& file.getName().endsWith("4AndroidImpl.java")) {
					genJsonCallClass4File(file);

				}
				if (file.isDirectory()) {
					genAndroidJsonCallClass(file.getAbsolutePath());
				}

			}
		}

	}

	private static void genJsonCallClass4File(File f) {
		if (!f.exists()) {
			return;
		}

		StringBuilder sb4Unity = new StringBuilder();
		StringBuilder sb4Unity2 = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder sb4iOSImpl4h = new StringBuilder();
		StringBuilder sb4iOSImpl = new StringBuilder();
		String calssName = f.getName().substring(0,
				f.getName().indexOf("Impl.java"));
		File nf = new File(f.getParentFile(), calssName + ".java");
		String packageName = "";
		try {
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(
							new java.io.FileInputStream(f), "UTF-8"));

			String data = null;
			while ((data = br.readLine()) != null) {
				if (data.trim().startsWith("package")) {
					packageName = data;
					sb.append(packageName + "\n");
					sb.append("import java.util.*;\n");
					sb.append("\n");
					sb.append("import org.json.*;\n");
					sb.append("\n");
					sb.append("import android.util.Log;\n");
					sb.append("\n");
					sb.append("public class " + calssName + " {\n");
					sb.append("	private static String TAG = \"" + calssName
							+ "\";\n");
				}
				// 在这里判断是否有
				if (data.contains("public static JSONObject")) {// 是方法定义
					String methodName = data.substring(0, data.indexOf("("));
					methodName = methodName.substring(
							methodName.lastIndexOf(" ")).trim();
					String paramsStr = data.substring(data.indexOf("(") + 1);
					while (!paramsStr.contains(")")) {
						paramsStr = paramsStr + br.readLine();
					}
					System.out.println("paramsStr====" + paramsStr);
					paramsStr = paramsStr.substring(0, paramsStr.indexOf(")"));
					System.out.println("paramsStrAAAAAAAAAAA====" + paramsStr);
					paramsStr = paramsStr.replaceAll("final", "").trim();
					String paramArr[] = paramsStr.split(",");

					sb4iOSImpl.append("+(NSDictionary*)" + methodName
							+ "_Internal");
					sb4iOSImpl4h.append("+(NSDictionary*)" + methodName
							+ "_Internal");
					// +(NSDictionary*)CreateU3DWebView_Internal:(int)x y:(int)y
					// width:(int)width height:(int)height url:(NSString*)url{

					sb.append("	public static String " + methodName
							+ "(String paramJson) {\n");
					sb.append("		try {\n");
					sb.append("			JSONObject json = new JSONObject(paramJson);\n");
					for (int i = 0; paramsStr.length() > 0
							&& i < paramArr.length; i++) {
						String param = paramArr[i].trim();
						System.out.println(param);
						String javaType = param
								.substring(0, param.indexOf(" ")).trim();
						String javaName = param.substring(
								param.indexOf(" ") + 1).trim();

						sb.append("			" + javaType + " " + javaName
								+ " = json." + getjsongetMethodName(javaType)
								+ "(\"" + javaName + "\");\n");
						if (i > 0) {
							sb4iOSImpl4h.append(javaName);
							sb4iOSImpl.append(javaName);
						}
						String iOSType = javaType;
						iOSType = iOSType.replaceAll("String", "NSString*");
						iOSType = iOSType.replaceAll("boolean", "bool");
						iOSType = iOSType.replaceAll("JSONObject",
								"NSDictionary*");
						sb4iOSImpl.append(":").append("(" + iOSType + ")")
								.append(javaName).append(" ");
						sb4iOSImpl4h.append(":").append("(" + iOSType + ")")
								.append(javaName).append(" ");
					}
					sb4iOSImpl4h.append(";\n");
					sb4iOSImpl.append("{\n");
					sb.append("			JSONObject jsonRs = "
							+ f.getName().substring(0,
									f.getName().lastIndexOf(".java")) + "."
							+ methodName + "(");
					for (int i = 0; paramsStr.length() > 0
							&& i < paramArr.length; i++) {
						String param = paramArr[i].trim();
						String javaType = param
								.substring(0, param.indexOf(" ")).trim();
						String javaName = param.substring(
								param.indexOf(" ") + 1).trim();
						if (i > 0) {
							sb.append(",");
						}
						sb.append(javaName);
					}
					sb.append(");\n");
					sb.append("			return jsonRs.toString();\n");
					sb.append("		} catch (Exception e) {\n");
					sb.append("			e.printStackTrace();\n");
					sb.append("		}\n");
					sb.append("		return \"NULL\";\n");
					sb.append("	}\n");
					if (methodName.contains("GetDevice")) {
						String paramName = methodName.substring(methodName
								.indexOf("GetDevice") + "GetDevice".length());

						sb4Unity.append("public static string GetDevice"
								+ paramName + " ()\n");
						sb4Unity.append("{\n");
						sb4Unity.append("	try {\n");
						sb4Unity.append("		SimpleJson.JsonObject json = new SimpleJson.JsonObject ();\n");
						sb4Unity.append("		string jsonStr = json.ToString ();\n");
						sb4Unity.append("		string jsonRsStr = UnityNativeProxy.u3dNativeCallStaticMethod (\"BilinLocalUtil\", \"GetDevice"
								+ paramName + "\", jsonStr);\n");
						sb4Unity.append("		object jsonRs;\n");
						sb4Unity.append("		SimpleJson.SimpleJson.TryDeserializeObject (jsonRsStr, out jsonRs);\n");
						sb4Unity.append("	object " + paramName + ";\n");
						sb4Unity.append("	if(((SimpleJson.JsonObject)jsonRs).TryGetValue (\""
								+ paramName + "\", out " + paramName + ")){\n");
						sb4Unity.append("	return \"\" + " + paramName + ";}\n");
						sb4Unity.append("	return \"NONE-\" + " + paramName
								+ ";\n");
						sb4Unity.append("} catch (Exception e) {\n");
						sb4Unity.append("	BLDebug.LogException (e);\n");
						sb4Unity.append("}\n");
						sb4Unity.append("return \"NULL\";\n");
						sb4Unity.append("}\n");

						sb4Unity2.append("+(NSDictionary*)GetDevice"
								+ paramName + "_Internal{\n");
						sb4Unity2.append("    NSString* " + paramName
								+ "=@\"no" + paramName + "\";\n");
						sb4Unity2
								.append("    NSDictionary *dic = [[NSDictionary alloc] initWithObjectsAndKeys:"
										+ paramName
										+ ",@\""
										+ paramName
										+ "\", nil];\n");
						sb4Unity2.append("   return dic; \n");
						sb4Unity2.append("}\n");
					}

					if (paramsStr.contains("webviewId")) {

						sb4iOSImpl.append("UIWebView* webView = nil;\n");
						sb4iOSImpl.append(" if(globalViews!= nil){\n");
						sb4iOSImpl
								.append("  webView =  [globalViews objectForKey:webviewId];\n");
						sb4iOSImpl.append("}\n");
						sb4iOSImpl.append("if (webView != nil) {\n");
						sb4iOSImpl
								.append("//[webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:url]]];\n");
						sb4iOSImpl
								.append("return [[NSDictionary alloc] initWithObjectsAndKeys:[NSNumber numberWithBool:true],@\"result\", nil];\n");
						sb4iOSImpl.append("}\n");

					}

					sb4iOSImpl
							.append("NSDictionary *dic = [[NSDictionary alloc] initWithObjectsAndKeys:[NSNumber numberWithBool:false],@\"result\", nil];\n");
					sb4iOSImpl.append("return dic;\n");
					sb4iOSImpl.append("}\n");
				}
			}
			sb.append("}\n");
			System.out.println(sb);
			System.out.println(sb4Unity);
			System.out.println(sb4Unity2);
			System.out.println();
			System.out.println();
			System.out.println("//--------------------");
			System.out.println(sb4iOSImpl4h.toString());
			System.out.println("//--------------------");
			System.out.println(sb4iOSImpl.toString());
			br.close();
			GameCSV2DB.writeFile(nf.getAbsolutePath(), sb.toString());

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static String getjsongetMethodName(String javaType) {
		if ("int".equalsIgnoreCase(javaType.trim())) {
			return "getInt";
		}
		if ("boolean".equalsIgnoreCase(javaType.trim())) {
			return "getBoolean";
		}
		if ("float".equalsIgnoreCase(javaType.trim())) {
			return "getFloat";
		}
		if ("double".equalsIgnoreCase(javaType.trim())) {
			return "getDouble";
		}
		if ("String".equalsIgnoreCase(javaType.trim())) {
			return "getString";
		}
		if ("JSONObject".equalsIgnoreCase(javaType.trim())) {
			return "getJSONObject";
		}
		if ("long".equalsIgnoreCase(javaType.trim())) {
			return "getLong";
		}
		if ("JSONArray".equalsIgnoreCase(javaType.trim())) {
			return "getJSONArray";
		}

		System.err.println("getjsongetMethodName|for|" + javaType);
		return "";
	}

}

package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

public class AddLogCheckCode {

	public static void main(String[] args) throws IOException {
		// String fbxRootDir =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		String animPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/Net/ProtoBufRPCUtil.cs";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/";
		File dir4Win = new File("D:" + dir);
		if (dir4Win.isDirectory()) {
			dir = dir4Win.getAbsolutePath();
		}
		long startTime = System.currentTimeMillis();
		AddLogCheckDir(new File(dir));
		long endTime = System.currentTimeMillis();
		System.out.println("AddLogCheckCode|" + dir + "|use|" + (endTime - startTime) + "ms");
		 System.exit(0);
		try {
			AddLogCheck(animPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void AddLogCheckDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					AddLogCheckDir(f);
				} else {
					try {
						AddLogCheck(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	private static boolean noLogCheck(String str, String preLineData, String preLineData2) {
		if (preLineData.contains(str)) {
			return false;
		}
		if (preLineData.trim().equals("{") && preLineData2.contains(str)) {
			System.out.println("前两行有日志级别开关:preLineData2=" + preLineData2);
			return false;
		}
		return true;
	}

	public static void AddLogCheck(String animFilePath) throws IOException {
		if (animFilePath != null && animFilePath.endsWith(".cs") && !animFilePath.endsWith("BLDebug.cs")
				&& new File(animFilePath).isFile()) {
			StringBuilder sb = new StringBuilder((int) (new File(animFilePath).length() + 1024));
			// System.out.println("AddLogCheck=" + new
			// File(animFilePath).getAbsolutePath());
			// OutputStreamWriter osw = new java.io.OutputStreamWriter(new
			// java.io.FileOutputStream(newAnim), "UTF-8");
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(animFilePath), "UTF-8"));
			String data = null;
			int lineCount = 0;
			String preLineData2 = "";// 前两行
			String preLineData = "";
			while ((data = br.readLine()) != null) {
				lineCount++;
				if (!data.trim().startsWith("//") && data.contains("BLDebug.Log")) {// 是日志行，就看前一行是否有开关
					if (data.contains("BLDebug.LogWarning")) {
						if (noLogCheck("BLDebug.isLogWarningEnabled", preLineData, preLineData2)) {
							sb.append("if(BLDebug.isLogWarningEnabled){").append("\n");
							sb.append(data).append("\n");
							sb.append("}").append("\n");
						} else {
							System.err.println("不需要追加日志开关 BLDebug.isLogWarningEnabled:" + preLineData2 + "||"
									+ preLineData + "|" + data);
							sb.append(data).append("\n");
						}
					} else if (data.contains("BLDebug.LogException")) {
						if (noLogCheck("BLDebug.isLogExceptionEnabled", preLineData, preLineData2)) {
							sb.append("if(BLDebug.isLogExceptionEnabled){").append("\n");
							sb.append(data).append("\n");
							sb.append("}").append("\n");
						} else {
							System.err.println("不需要追加日志开关 BLDebug.isLogExceptionEnabled:" + preLineData2 + preLineData
									+ "|" + data);
							sb.append(data).append("\n");
						}
					} else if (data.contains("BLDebug.LogError")) {
						if (noLogCheck("BLDebug.isLogErrorEnabled", preLineData, preLineData2)) {
							sb.append("if(BLDebug.isLogErrorEnabled){").append("\n");
							sb.append(data).append("\n");
							sb.append("}").append("\n");
						} else {
							System.err.println("不需要追加日志开关 BLDebug.isLogErrorEnabled:" + preLineData2 + "||"
									+ preLineData + "|" + data);
							sb.append(data).append("\n");
						}
					} else if (data.contains("BLDebug.Log")) {
						if (noLogCheck("BLDebug.isLogEnabled", preLineData, preLineData2)) {
							sb.append("if(BLDebug.isLogEnabled){").append("\n");
							sb.append(data).append("\n");
							sb.append("}").append("\n");
						} else {
							System.err.println(
									"不需要追加日志开关 BLDebug.isLogEnabled:" + preLineData2 + "||" + preLineData + "|" + data);
							sb.append(data).append("\n");
						}
					} else {
						System.err.println("不需要追加日志开关:" + preLineData2 + "||" + preLineData + "|" + data);
						sb.append(data).append("\n");
					}
				} else {
					sb.append(data).append("\n");
				}
				preLineData2 = preLineData;
				preLineData = data;
			}
			// osw.flush();
			// osw.close();
			br.close();

			GameCSV2DB.writeFile(animFilePath, sb.toString());
		}

	}

}

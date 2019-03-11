package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

public class FindDebugLog {

	public static void main(String[] args) throws IOException {
		// String fbxRootDir =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		String animPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/Net/ProtoBufRPCTcpHelper.cs";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji";
		File dir4Win = new File("D:" + dir);
		if (dir4Win.isDirectory()) {
			dir = dir4Win.getAbsolutePath();
		}
		long startTime = System.currentTimeMillis();
		FindDebugLogDir(new File(dir));
		long endTime = System.currentTimeMillis();
		System.out.println("FindDebugLogCode|" + dir + "|use|" + (endTime - startTime) + "ms");
		System.exit(0);
		try {
			FindDebugLog(animPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void FindDebugLogDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					FindDebugLogDir(f);
				} else {
					try {
						FindDebugLog(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void FindDebugLog(String animFilePath) throws IOException {
		if (animFilePath != null && animFilePath.endsWith(".cs") && !animFilePath.endsWith("BLDebug.cs")
				&& !animFilePath.contains("/Editor/") && new File(animFilePath).isFile()) {
			StringBuilder sb = new StringBuilder((int) (new File(animFilePath).length() + 1024));
			// System.out.println("FindDebugLog=" + new
			// File(animFilePath).getAbsolutePath());
			// OutputStreamWriter osw = new java.io.OutputStreamWriter(new
			// java.io.FileOutputStream(newAnim), "UTF-8");
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(animFilePath), "UTF-8"));
			String data = null;
			int lineCount = 0;
			String preLineData = "";
			while ((data = br.readLine()) != null) {
				lineCount++;
				if (data.contains("Debug.Log") && !data.contains("Debug.LogException") && !data.contains("BLDebug.Log")
						&& !data.trim().startsWith("//")) {// 是日志行，就看前一行是否有开关
					System.out.println(data.trim() + "|" + animFilePath);
				}
				preLineData = data;
			}
			// osw.flush();
			// osw.close();
			br.close();

		}

	}

}

package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

public class FindHalfIf {

	public static void main(String[] args) throws IOException {
		// String fbxRootDir =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		String animPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/Net/ProtoBufRPCTcpHelper.cs";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts";
		File dir4Win = new File("D:" + dir);
		if (dir4Win.isDirectory()) {
			dir = dir4Win.getAbsolutePath();
		}
		long startTime = System.currentTimeMillis();
		FindHalfIfDir(new File(dir));
		long endTime = System.currentTimeMillis();
		System.out.println("FindHalfIfCode|" + dir + "|use|" + (endTime - startTime) + "ms");
		System.exit(0);
		try {
			FindHalfIf(animPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void FindHalfIfDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					FindHalfIfDir(f);
				} else {
					try {
						FindHalfIf(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void FindHalfIf(String animFilePath) throws IOException {
		if (animFilePath != null && animFilePath.endsWith(".cs") && !animFilePath.endsWith("kcp.cs")
				&& !animFilePath.endsWith("GameGuideExtension.cs") && !animFilePath.endsWith("MKGlow.cs")
				&& !animFilePath.contains("/Editor/") && new File(animFilePath).isFile()) {
			StringBuilder sb = new StringBuilder((int) (new File(animFilePath).length() + 1024));
			// System.out.println("FindHalfIf=" + new
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
				String tmpData = preLineData.trim().replaceAll(" ", "");
				boolean youkuohao = false;
				if (tmpData.startsWith("if")) {// if后面的条件没大括号
					if (tmpData.endsWith("{") || tmpData.contains("{//")) {
						youkuohao = true;
					}
					if (data.trim().startsWith("{") || data.trim().endsWith("{")) {
						youkuohao = true;
					}

					if (!youkuohao) {
						// if (!youkuohao && data.contains("Debug.Log")) {
						System.out.println((lineCount - 1) + "|" + tmpData + "|" + animFilePath);
						System.out.println((lineCount) + "|" + data + "|" + animFilePath);

					}
				}
				preLineData2 = preLineData;
				preLineData = data;
			}
			// osw.flush();
			// osw.close();
			br.close();

		}

	}

}

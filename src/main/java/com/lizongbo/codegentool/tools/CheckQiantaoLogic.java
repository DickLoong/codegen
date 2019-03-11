package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CheckQiantaoLogic {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String prefabPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/UI/Core/Window.cs";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/";
		// checkQiantaoLogic(prefabPath);
		checkQiantaoLogicDir(new File(dir));

	}

	public static void checkQiantaoLogicDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					checkQiantaoLogicDir(f);
				} else {
					try {
						checkQiantaoLogic(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void checkQiantaoLogic(String prefabFilePath) throws IOException {
		if (!new File(prefabFilePath).isFile() || !prefabFilePath.endsWith(".cs")) {
			return;
		}

		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		int lineCount = 0;
		boolean isWindow = false;
		boolean needAppend = false;
		StringBuilder sb = new StringBuilder(256);
		while ((data = br.readLine()) != null) {
			lineCount++;
			//if (data.contains(": Window")) {
				isWindow = true;
			//}

			if (isWindow) {
				if (data.contains("override void OnClose") || data.contains("override void OnBack")) {
					needAppend = true;
				}
				if (needAppend) {
					sb.append(lineCount).append("    ").append(data).append("\n");
				}
				if (data.contains("}")) {
					needAppend = false;
				}
			}
		}
		br.close();
		// System.out.println(sb);
		String s = sb.toString();
		s = s.replaceAll("base.OnClose", "");
		s = s.replaceAll("base.OnBack", "");
		if (s.contains("Close") || s.contains("Back")) {
			System.err.println("checkQiantaoLogic=" + prefabFilePath);
			//System.err.println(sb);
		}
	}
}

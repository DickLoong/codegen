package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CheckEditorLogic {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String prefabPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/UI/Core/Window.cs";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/";
		// checkEditorLogic(prefabPath);
		checkEditorLogicDir(new File(dir));

	}

	public static void checkEditorLogicDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					checkEditorLogicDir(f);
				} else {
					try {
						checkEditorLogic(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void checkEditorLogic(String prefabFilePath) throws IOException {
		if (!new File(prefabFilePath).isFile() || !prefabFilePath.endsWith(".cs")) {
			return;
		}

		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		int lineCount = 0;
		boolean needAppend = false;
		StringBuilder sb = new StringBuilder(256);
		while ((data = br.readLine()) != null) {
			lineCount++;
			if (data.contains("#if UNITY_EDITOR")) {
				needAppend = true;
			}
			if (needAppend) {
				sb.append(lineCount).append("    ").append(data).append("\n");
			}
			if (data.contains("#endif")) {
				needAppend = false;
			}
		}
		br.close();
		// System.out.println(sb);
		if (!prefabFilePath.contains("dbbeans4proto")
				&& (sb.indexOf("return") >= 0 || sb.indexOf("break") >= 0 || sb.indexOf("continue") >= 0)) {
			System.err.println("checkEditorLogic=" + prefabFilePath);
			//System.err.println(sb);
		}
	}
}

package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;

public class FindCsStaticVar {

	static int countAA = 0;

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String prefabPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/UI/Core/Window.cs";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/";
		// checkEditorLogic(prefabPath);
		checkStaticBianliang(new File(dir));

	}

	public static void checkStaticBianliang(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					checkStaticBianliang(f);
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
		if (!new File(prefabFilePath).isFile() || !prefabFilePath.endsWith(".cs")
				|| prefabFilePath.endsWith("ArrayCsvHelper.cs")) {
			return;
		}

		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		int lineCount = 0;
		while ((data = br.readLine()) != null) {
			lineCount++;
			if (data.contains("static ") && !data.contains("static void") && !data.contains("static int ")
					&& !data.contains("static bool ") && !data.contains("static long ")
					&& !data.contains("private static string ") && !data.contains("public static string ")
					&& !data.contains("private static float ") && !data.contains("private static Color ")

					&& !data.contains("private static string[] ") && !data.contains("public static string[] ")
					&& !data.contains("public static 	string[,] ") && !data.contains("(this ")
					&& !data.trim().startsWith("//")

					&& !data.contains("static T ") && !data.contains("private static extern ")
					&& !data.contains("public static extern ") && !data.contains("static class ")
					&& !data.contains("(TProto_Cmd cmd ") && !data.contains("(int ") && !data.contains(" tcpa,")
					&& !data.contains("public static UInt16 ") && !data.endsWith(")")) {
				countAA++;
				System.out.println(countAA + "|" + data + "||" + new File(prefabFilePath).getAbsolutePath());
			}
		}
		br.close();
	}

}

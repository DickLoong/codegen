package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class UnityFontChange {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String prefabPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Resources/UI/Window/Vip/VipRightWindow.prefab";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Resources/UI/Window/";

		cutFontDir(new File(dir));

	}

	public static void cutFontDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					cutFontDir(f);
				} else {
					try {
						cutFont(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void cutFont(String prefabFilePath) throws IOException {
		if (!new File(prefabFilePath).isFile() || prefabFilePath.endsWith(".meta")) {
			return;
		}
		System.out.println("cutFont=" + prefabFilePath);
		StringBuilder sb = new StringBuilder((int) (new File(prefabFilePath).length() + 128));

		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		int lineCount = 0;
		while ((data = br.readLine()) != null) {
			lineCount++;
			String str = StringUtil.replaceAll(data, "m_ReferenceResolution: {x: 1136, y: 640}",
					"m_ReferenceResolution: {x: 1334, y: 750}");
			// 然后替换字体
			if (str.contains("m_fontSize:")) {// 字体加2
				for (int fs = 4; fs <= 30; fs = fs + 2) {
					String fsStr = "m_fontSize: " + fs;
					String fsStrNew = "m_fontSize: " + (fs + 2);
					if (str.contains(fsStr)) {
						str = StringUtil.replaceAll(str, fsStr, fsStrNew);
						break;
					}
				}
			}
			sb.append(str);
			sb.append("\n");
		}
		br.close();

		OutputStreamWriter osw = new java.io.OutputStreamWriter(new java.io.FileOutputStream(prefabFilePath), "UTF-8");
		osw.write(sb.toString());
		osw.close();
	}
}

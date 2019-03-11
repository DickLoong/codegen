package com.lizongbo.codegentool.tools;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

/**
 * 精简unity动画文件里的坐标精度
 * 
 * @author quickli
 *
 */
public class Pichash2Name {
	static Map<String, String> map = new HashMap<String, String>();

	public static Map<String, String> getHashidMap() {
		map = new HashMap<String, String>();
		try {

			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream("/tmp/unitylog/hashcode.txt"), "UTF-8"));
			String data = null;
			while ((data = br.readLine()) != null) {
				if (data.contains("=")) {
					String s[] = data.split("=");
					map.put(s[0], s[1]);
				}
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(map);
		return map;
	}

	public static void main(String[] args) {
		// String fbxRootDir =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		// String animPath =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/bawang/animFs/bawang@attack01.anim";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/";
		File dir4Win = new File(
				"D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/");
		if (dir4Win.isDirectory()) {
			dir = dir4Win.getAbsolutePath();
		}
		long startTime = System.currentTimeMillis();
		getHashidMap();
		picHash2NameDir(new File(dir));
		long endTime = System.currentTimeMillis();
		System.out.println("UnityHash2Name|" + dir + "|use|" + (endTime - startTime) + "ms");
		System.exit(0);
		try {
			// picHash2Name(animPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void picHash2NameDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					picHash2NameDir(f);
				} else {
					try {
						picHash2Name(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void picHash2Name(String animFilePath) throws IOException {
		if (animFilePath != null && animFilePath.endsWith(".cs") && new File(animFilePath).isFile()) {
			// System.out.println("picHash2Name=" + new
			// File(animFilePath).getName());
			StringBuilder sbAll = new StringBuilder();

			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(animFilePath), "UTF-8"));
			String data = null;
			boolean hasHash = false;
			int lineCount = 0;
			while ((data = br.readLine()) != null) {
				lineCount++;
				String line = data;
				for (Map.Entry<String, String> e : map.entrySet()) {
					if (data.contains(e.getKey()) && !data.contains(e.getKey() + "=")) {
						hasHash = true;
						line = line.replace(e.getKey(), "\"" + e.getValue() + "\"") + " // " + e.getKey() + "="
								+ e.getValue();
						// sbAll.append(" //").append(e.getKey()).append(" =
						// ").append("\"").append(e.getValue())
						// .append("\" ");
						line = line.replaceAll("const int", "const string");
						System.out.println(new File(animFilePath).getName() + "|" + lineCount + "|" + line);
					}
				}
				sbAll.append(line);

				sbAll.append("\n");
			}

			br.close();
			if (hasHash) {
				GameCSV2DB.writeFile(animFilePath, sbAll.toString());
			}
		}

	}
}

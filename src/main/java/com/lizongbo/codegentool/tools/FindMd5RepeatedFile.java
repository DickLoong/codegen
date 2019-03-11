package com.lizongbo.codegentool.tools;

import java.io.*;
import java.io.IOException;
import java.util.*;

import com.lizongbo.codegentool.GuidCompare;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.HashCalc;
import com.lizongbo.codegentool.db2java.GenAll;

public class FindMd5RepeatedFile {

	/**
	 * key是md5，值是文件路径列表
	 */
	static Map<String, Set<String>> fileMd5Map = new TreeMap<String, Set<String>>();
	/**
	 * key是去掉前缀的文件名，值是文件路径列表
	 */
	static Map<String, Set<String>> fileNameMap = new TreeMap<String, Set<String>>();
	/// guid和对应文件名
	static Map<String, String> guidFileMap = new TreeMap<String, String>();
	// 文件名和对应guid
	static Map<String, String> fileGuidMap = new TreeMap<String, String>();
	static Map<String, Set<String>> guidRefFileMap = new TreeMap<String, Set<String>>();

	public static void main(String[] args) {

		// TODO Auto-generated method stub
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Art";
		File d = new File(dir);
		dir = d.getAbsolutePath();
		getMd5Dir(d);
		int md5Count = 0;
		int bascimd5count = 0;
		for (Map.Entry<String, Set<String>> e : fileMd5Map.entrySet()) {
			if (e.getValue().size() > 1) {
				md5Count++;
				System.out.println("md5重复的文件有：" + (e.getValue().toString().replaceAll(dir, "/Art")));
				if ((e.getValue().toString().replaceAll(dir, "/Art")).contains("BasicUIElement")) {
					bascimd5count++;
					System.err.println("md5重复的基础文件有：" + (e.getValue().toString().replaceAll(dir, "/Art")));
				}
				for (String picFilePath : e.getValue()) {
					String metafilePath = picFilePath + ".meta";
					String guid = GuidCompare.getGuid(new File(metafilePath));
					guidFileMap.put(guid, picFilePath);
					fileGuidMap.put(picFilePath, guid);
					// System.out.println(metafilePath.replaceAll(dir, "/Art") +
					// " === " + guid);
				}
			}
		}
		System.out.println("md5重复的个数是：" + md5Count);
		System.out.println("md5重复的基础元素个数是：" + bascimd5count);
		System.out.println("guidFileMap.size == " + guidFileMap.size());
		// 生成替换合并复用基础元素的guid map
		Map<String, String> guidReplaceMap = new TreeMap<String, String>();

		for (Map.Entry<String, Set<String>> e : fileMd5Map.entrySet()) {
			if (e.getValue().size() > 1) {
				System.out.println("md5重复的文件有：" + (e.getValue().toString().replaceAll(dir, "/Art")));
				if ((e.getValue().toString().replaceAll(dir, "/Art")).contains("BasicUIElement")) {
					String buiGuid = "";
					for (String aa : e.getValue()) {
						if (aa.contains("BasicUIElement")) {
							buiGuid = fileGuidMap.get(aa);
						}
					}
					for (String bb : e.getValue()) {
						if (!bb.contains("BasicUIElement")) {
							String replaceGuid = fileGuidMap.get(bb);
							guidReplaceMap.put(replaceGuid, buiGuid);
						}
					}
				}
			}
		}
		// 在这里列出需要替换的文件的guid和路径

		for (Map.Entry<String, String> e : guidReplaceMap.entrySet()) {
			// System.out.println("111替换" + e.getKey() + "|为|" + e.getValue());
			// System.out.println("222替换" +
			// guidFileMap.get(e.getKey()).replaceAll(dir, "/Art") + "|为|"
			// + guidFileMap.get(e.getValue()).replaceAll(dir, "/Art"));
			System.out.println("rm " + guidFileMap.get(e.getKey()));

		}
		String prefabDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Resources";
		replacePrefabFileByMap(new File(prefabDir), guidReplaceMap);
		// System.exit(0);
		getGuidRefDir(new File(prefabDir));
		for (Map.Entry<String, Set<String>> e : guidRefFileMap.entrySet()) {
			if (e.getValue().size() > 0 && !guidFileMap.get(e.getKey()).contains("BasicUIElement")) {
				System.out.println(guidFileMap.get(e.getKey()).toString().replaceAll(dir, "/Art") + "图片确实被引用的文件有："
						+ (e.getValue().toString().replaceAll(
								"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets",
								"/Assets")));

			}
		}
	}

	/**
	 * 将Prefbab引用的文件指向新的需要引用的文件
	 * 
	 * @param fbxDir
	 * @param map
	 *            key是需要替换的guid，value是替换后的guid
	 */
	public static void replacePrefabFileByMap(File fbxDir, Map<String, String> map) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				replacePrefabFileByMap(f, map);
			}

		}
		if (fbxDir.isFile() && fbxDir.getName().endsWith(".prefab")) {
			String fileText = GenAll.readFile(fbxDir.getAbsolutePath(), "UTF-8");
			boolean hashGuid = false;
			for (Map.Entry<String, String> e : map.entrySet()) {
				if (fileText.contains(e.getKey())) {
					hashGuid = true;
				}
			}
			if (hashGuid) {
				System.out.println(fbxDir + "|需要替换md5重复文件进行复用");
				for (Map.Entry<String, String> e : map.entrySet()) {
					fileText = fileText.replaceAll(e.getKey(), e.getValue());

				}
			}
			GameCSV2DB.writeFile(fbxDir.getAbsolutePath(), fileText);

		} else {
			// System.err.println("文件夹不存在" + fbxDir);
		}
	}

	public static void getMd5Dir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					getMd5Dir(f);
				} else {
					try {
						getMd5(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} else {
			System.err.println("文件夹不存在" + fbxDir);
		}
	}

	public static void getMd5(String animFilePath) throws IOException {
		File f = new File(animFilePath);
		if (!f.getName().endsWith(".jpg") && !f.getName().endsWith(".png") && !f.getName().endsWith(".tga")) {
			return;
		}
		String fileMd5 = HashCalc.md5(f);
		{
			Set<String> s = fileMd5Map.get(fileMd5);
			if (s == null) {
				s = new TreeSet<String>();
				fileMd5Map.put(fileMd5, s);
			}
			s.add(f.getAbsolutePath());
		}
		String suffixName = getSuffixName(f.getName());
		if (!suffixName.equals(f.getName())) {
			Set<String> s = fileNameMap.get(suffixName);
			if (s == null) {
				s = new TreeSet<String>();
				fileNameMap.put(suffixName, s);
			}
			s.add(f.getAbsolutePath());
		}
	}

	public static void getGuidRefDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					getGuidRefDir(f);
				} else {
					try {
						getGuidRef(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} else {
			System.err.println("文件夹不存在" + fbxDir);
		}
	}

	public static void getGuidRef(String animFilePath) throws IOException {
		File f = new File(animFilePath);
		if (!f.getName().endsWith(".prefab")) {
			return;
		}
		String metaFile = f.getAbsolutePath();
		String fileText = GenAll.readFile(metaFile, "UTF-8");
		// System.out.println("getGuidRef|" + fileText.length() + metaFile);

		for (String guid : guidFileMap.keySet()) {
			if (fileText.contains(guid)) {
				Set<String> s = guidRefFileMap.get(guid);
				if (s == null) {
					s = new TreeSet<String>();
					guidRefFileMap.put(guid, s);
				}
				s.add(f.getAbsolutePath());
			}
		}

	}

	public static String getSuffixName(String fileName) {
		if (!fileName.startsWith("ui_") && !fileName.contains("_ui_")) {
			return fileName;
		}
		if (!(fileName.indexOf("_", 4) > 4)) {
			return fileName;
		}
		String s = fileName.substring(fileName.indexOf("_", 4) + 1);
		// System.out.println(fileName + " == " + s);
		return s;
	}
}

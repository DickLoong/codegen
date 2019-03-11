package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.esotericsoftware.yamlbeans.YamlReader;

/**
 * 检查Assets目录，看有哪些资源丢失了
 * 
 * @author quickli
 *
 */
public class CheckShaderLost {

	public static void main(String[] args) {
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scenes";
		dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Resources/";
		Set<String> set = new TreeSet<String>();
		getShadersDir(new File(dir), set);
		System.out.println(set.toString().replace(',', '\n'));

		Map<String, List<String>> usedSet = new TreeMap<String, List<String>>();
		dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/";
		getShaderFindsDir(new File(dir), usedSet);
		System.out.println(usedSet);
		for (String s : usedSet.keySet()) {
			if (!set.contains(s)) {
				System.err.println(s + "==lost==" + usedSet.get(s));
			} else {
				System.out.println(s + "==have==" + usedSet.get(s));
			}
		}

		// 得到支持的shader之后，再遍历所有代码中Shader.Find用到的shader
	}

	public static Set<String> getShadersDir(File fbxDir, Set<String> set) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					getShadersDir(f, set);
				} else {
					try {
						String sn = getShadersFile(f.getAbsolutePath());
						if (sn.length() > 0) {
							set.add(sn);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
		return set;
	}

	public static String getShadersFile(String prefabFilePath) throws IOException {
		File f = new File(prefabFilePath);
		if (!f.isFile() || !prefabFilePath.endsWith(".shader")) {
			// System.err.println("no|getShadersFile|" + f);
			return "";
		}
		// Shader "Character/BlendReflectCharacter"
		String shaderName = "";
		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		StringBuilder sb = new StringBuilder(256);
		while ((data = br.readLine()) != null) {
			data = data.trim();
			if (shaderName.length() < 1 && data.contains("Shader")) {
				if (data.indexOf("\"") != data.lastIndexOf("\"")) {
					shaderName = data.substring(data.indexOf("\"") + 1, data.lastIndexOf("\""));
					//System.out.println("招到shader  " + shaderName + "|" + f);
				} else {
					// System.out.println("招不到到shader " + shaderName + "|" +
					// data);
				}

			}
		}
		br.close();
		if (shaderName.length() < 1) {
			System.err.println("招不到shader  " + shaderName + "|" + f);
		}
		return shaderName;
	}

	public static Map<String, List<String>> getShaderFindsDir(File fbxDir, Map<String, List<String>> set) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					getShaderFindsDir(f, set);
				} else {
					try {
						List<String> snList = getShaderFindsFile(f.getAbsolutePath());
						for (String sn : snList) {
							List<String> list = set.getOrDefault(sn, new ArrayList<String>());
							list.add(f.getAbsolutePath());
							set.put(sn, list);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
		return set;
	}

	public static List<String> getShaderFindsFile(String prefabFilePath) throws IOException {
		List<String> list = new ArrayList<String>();
		File f = new File(prefabFilePath);
		if (!f.isFile() || !prefabFilePath.endsWith(".cs") || !prefabFilePath.contains("/Scripts/")) {
			// System.err.println("no|getShaderFindsFile|" + f);
			return list;
		}
		// Shader "Character/BlendReflectCharacter"
		// String shaderName = "";
		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		StringBuilder sb = new StringBuilder(256);
		while ((data = br.readLine()) != null) {
			data = data.trim();
			if (data.contains("Shader.Find") && data.indexOf("\"") != data.lastIndexOf("\"")) {
				String shaderName = data.substring(data.indexOf("\"") + 1, data.lastIndexOf("\""));
				list.add(shaderName);
			}
		}
		br.close();
		return list;
	}
}

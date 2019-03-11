package com.lizongbo.codegentool.tools;

import java.io.*;
import java.util.*;
import java.util.Set;

import com.lizongbo.codegentool.GuidCompare;

/**
 * 检查美术资源图片的依赖关系 先获取Art目录下每个资源的guid和路径 ，然后获取整个Asset目录下的资源依赖Map，然后反查这些图片被哪些资源依赖了
 * 
 * @author quickli
 *
 */
public class GuidRefCheckTool {
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String artDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Art";
		String assetDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets";
		Map<String, String> picGuidMap = getPicFileGuidMap(new File(artDir), null);
		System.out.println("资源个数" + picGuidMap.size());
		Map<String, Set<String>> picGuidRefMap = getPicFileGuidRefMap(new File(assetDir), null);
		System.out.println("引用资源的资源个数" + picGuidRefMap.size());
		Map<String, Set<String>> picGuidRefFileMap = new TreeMap<String, Set<String>>();
		for (Map.Entry<String, String> e : picGuidMap.entrySet()) {
			picGuidRefFileMap.put(e.getKey(), new TreeSet<String>());
		}
		for (Map.Entry<String, Set<String>> e : picGuidRefMap.entrySet()) {
			for (String refGuid : e.getValue()) {
				if (picGuidRefFileMap.containsKey(refGuid)) {
					picGuidRefFileMap.get(refGuid).add(e.getKey());
				}
			}
		}
		int cc = 0;
		Set<String> noRefSet = new TreeSet<String>();
		for (Map.Entry<String, String> e : picGuidMap.entrySet()) {
			if (picGuidRefFileMap.get(e.getKey()).size() < 1) {
				// System.err.println("图片没被引用:" + e.getValue() + "|" +
				// e.getKey());
				noRefSet.add(e.getValue());
				cc++;
			}
		}
		System.err.println("没有被引用的资源个数为：" + cc);
		for (String s : noRefSet) {
			System.err.println("图片没被引用:" + s);
		}
		for (String s : noRefSet) {
			if (s.contains("FirstRecharge") || s.contains("Chat") || s.contains("Expedition")) {
				String picFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/"
						+ s.substring(0, s.lastIndexOf("."));
				System.out.println("可以删除：" + picFilePath);
				new File(picFilePath).delete();
			}
		}
	}

	public static Map<String, String> getPicFileGuidMap(File f, Map<String, String> guidMap) {
		if (guidMap == null) {
			guidMap = new TreeMap<String, String>();
		}
		if (!f.exists()) {
			System.err.println(f + "不存在");
		}
		if (f.isFile() && !f.getAbsolutePath().contains("GameLogos")
				&& (f.getName().endsWith(".png.meta") || f.getName().endsWith(".jpg.meta"))) {
			String assetPath = f.getAbsolutePath();
			assetPath = assetPath.substring(assetPath.indexOf("Assets"));
			String guid = GuidCompare.getGuid(f);
			if (guidMap.containsKey(guid)) {
				System.err.println("已经存在：" + guidMap.get(guid) + "|但是现在指向" + assetPath);
			}
			guidMap.put(guid, assetPath);
		}
		if (f.isDirectory()) {
			File subfs[] = f.listFiles();
			for (File subf : subfs) {
				getPicFileGuidMap(subf, guidMap);
			}
		}
		return guidMap;
	}

	public static Map<String, Set<String>> getPicFileGuidRefMap(File f, Map<String, Set<String>> guidRefMap) {
		if (guidRefMap == null) {
			guidRefMap = new TreeMap<String, Set<String>>();
		}
		if (!f.exists()) {
			System.err.println(f + "不存在");
		}
		if (f.isFile()) {
			if (!f.getAbsolutePath().contains("uiAtlas")) {
				String assetPath = f.getAbsolutePath();
				assetPath = assetPath.substring(assetPath.indexOf("Assets"));
				Set<String> guidSet = GuidCompare.getRefGuids(f);
				if (guidSet.size() > 0) {
					guidRefMap.put(assetPath, guidSet);
				} else {
					// System.out.println("没有引用别的资源：" + f);
				}
			} else {
				// System.out.println("跳过检查引用的文件：" + f);
			}
		}
		if (f.isDirectory()) {
			File subfs[] = f.listFiles();
			for (File subf : subfs) {
				getPicFileGuidRefMap(subf, guidRefMap);
			}
		}
		return guidRefMap;
	}

}

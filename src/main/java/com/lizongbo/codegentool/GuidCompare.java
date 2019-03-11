package com.lizongbo.codegentool;

import java.util.*;

import com.lizongbo.codegentool.db2java.GenAll;

import java.io.*;

/**
 * 比较美术工程和client里的脚本guid是否不一致
 * 
 * @author quickli
 *
 */
public class GuidCompare {

	public static void main(String[] args) {
		System.out.println(getRefGuids(new File(
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Test.unity")));
	}

	public static void main2(String[] args) {
		File cDir = new File(
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets");
		File aDir = new File(
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets");

		Map<String, Set<String>> cmap = new TreeMap<String, Set<String>>();
		Map<String, Set<String>> cPathMap = new TreeMap<String, Set<String>>();

		getFileGuidmap(cDir, cmap, cPathMap);

		Map<String, Set<String>> amap = new TreeMap<String, Set<String>>();
		Map<String, Set<String>> aPathMap = new TreeMap<String, Set<String>>();

		getFileGuidmap(aDir, amap, aPathMap);

		System.err.println(cmap.size());
		for (Map.Entry<String, Set<String>> ae : amap.entrySet()) {
			Set<String> guidSet4C = cmap.get(ae.getKey());
			if (!ae.getValue().equals(guidSet4C) && !ae.getKey().contains("/Editor/")
					&& cPathMap.get(ae.getKey()) != null) {
				System.err.println("GUID不匹配的美术文件为：" + aPathMap.get(ae.getKey()));
				System.err.println("美术guid是" + ae.getValue());
				System.err.println("GUID不匹配的客户端文件为：" + cPathMap.get(ae.getKey()));
				System.err.println("客户端guid是" + cmap.get(ae.getKey()));
			}

		}
	}

	public static Map<String, Set<String>> getFileGuidmap(File f, Map<String, Set<String>> map,
			Map<String, Set<String>> pathMap) {
		if (map == null) {
			map = new TreeMap<String, Set<String>>();
		}
		if (pathMap == null) {
			pathMap = new TreeMap<String, Set<String>>();
		}
		if (f.exists()) {
			if (f.isDirectory()) {
				File subfs[] = f.listFiles();
				for (File subf : subfs) {
					getFileGuidmap(subf, map, pathMap);
				}
			}
			if (f.isFile() && (f.getName().endsWith(".cs.meta") || f.getName().endsWith(".shader.meta"))) {
				{
					Set<String> guidSet = pathMap.get(f.getName());
					if (guidSet == null) {
						guidSet = new TreeSet<>();
						guidSet.add(f.getAbsolutePath());
						pathMap.put(f.getName(), guidSet);
					} else {
						guidSet.add(f.getAbsolutePath());
						pathMap.put(f.getName(), guidSet);
					}
				}

				String guid = getGuid(f);
				Set<String> guidSet = map.get(f.getName());
				if (guidSet == null) {
					guidSet = new TreeSet<>();
					guidSet.add(guid);
					map.put(f.getName(), guidSet);
				} else {
					guidSet.add(guid);
					map.put(f.getName(), guidSet);
					if (!f.getAbsolutePath().contains("Behavior Designer")) {
						System.err.println("有重复文件名的文件，需要关注:" + f + "\n");
						for (String p : pathMap.get(f.getName())) {
							System.err.println(f.getName() + "|" + p);
						}

					}
					if (!f.getAbsolutePath().contains("Behavior Designer")) {
						if (guidSet.size() > 1) {
							System.err.println("有多个guid:" + f + "|" + guidSet + "\n\n");
						}
					}
				}
			}
		}
		return map;
	}

	/**
	 * 获取meta 文件里标记资源的guid
	 * 
	 * @param f
	 * @return
	 */
	public static String getGuid(File f) {
		if (f == null || !f.isFile() || !f.getName().endsWith(".meta")) {
			System.err.println("只有meta文件才能获取单一的guid:" + f);
			return "";
		}
		String metaTxt = GenAll.readFile(f.getAbsolutePath(), "UTF-8");
		String[] arr = metaTxt.split("\n");
		for (String s : arr) {
			s = s.trim();
			if (s.startsWith("guid:")) {
				String guid = s.substring("guid:".length()).trim();
				// System.err.println(guid + "|" + f);
				return guid;

			}
		}
		return "";
	}

	public static Set<String> getRefGuids(File f) {
		Set<String> set = new HashSet<String>();
		if (f == null || !f.isFile()) {
			System.err.println("只有文件才能获取guid 引用:" + f);
			return set;
		}
		if (!f.getName().endsWith(".prefab") && !f.getName().endsWith(".mat") && !f.getName().endsWith(".unity")
				&& !f.getName().endsWith(".guiskin")) {
			// System.err.println("只有prefab,mat,unity,guiskin,文件才能获取guid 引用:" +
			// f);
			return set;
		}
		String metaTxt = GenAll.readFile(f.getAbsolutePath(), "UTF-8");
		String[] arr = metaTxt.split("\n");
		for (String s : arr) {
			s = s.trim();
			if (s.contains("guid:")) {
				// System.out.println("guid 行是" + s);
				String guid = s.substring(s.indexOf("guid:") + 5).trim();
				if (guid.contains(",")) {
					guid = guid.substring(0, guid.indexOf(",")).trim();
				}
				set.add(guid);
			}
		}

		return set;
	}
}

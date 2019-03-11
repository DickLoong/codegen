package com.lizongbo.codegentool.tools;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

/**
 * 
 * 检查AssetBundle资源是否有重名，这样导致打包的资源会有问题
 * 
 * @author quickli
 *
 */
public class FindRepeatNameFile4AB {
	static String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets";

	public static void main(String[] args) {
		Map<String, List<String>> map = new TreeMap<String, List<String>>();
		initKeys(map);
		map = getFileNameMap(new File(dir), map);
		System.out.println("abCount==" + map.size());
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			Set<String> set = new HashSet<String>();
			set.addAll(e.getValue());
			if (set.size() > 1) {
				GameCSV2DB
						.addErrMailMsgList("FindRepeatNameFile4AB|发现文件重名：" + e.getKey() + "|" + set.size() + "|" + set);
			}
		}
		GameCSV2DB.sendMailAndExit();
	}

	private static void initKeys(Map<String, List<String>> map) {
		map.put("effectmats", new ArrayList<String>());
		map.put("effectmeshs", new ArrayList<String>());
		map.get("effectmats").add("effectmats");
		map.get("effectmeshs").add("effectmeshs");
		String modelDir = dir + "/Art/Players/ModelFBXs";
		File md = new File(modelDir);
		for (File mf : md.listFiles()) {
			if (mf.isDirectory()) {
				String modelAnim = mf.getName() + "_anim";
				map.put(modelAnim, new ArrayList<String>());
				map.get(modelAnim).add(modelAnim);
			}
		}
		System.out.println("需占位的资源名有：" + map.keySet());
	}

	private static Map<String, List<String>> getFileNameMap(File f, Map<String, List<String>> effectPrefabMap) {
		if (effectPrefabMap == null) {
			effectPrefabMap = new TreeMap<String, List<String>>();
		}
		if (!f.exists()) {
			System.err.println(f + "不存在");
		}
		// if (f.isFile() && !f.getName().endsWith(".meta") &&
		// !f.getAbsolutePath().contains("unused/")
		// && !f.getAbsolutePath().contains("ThirdParty/")
		// && !f.getAbsolutePath().contains("AnimatorControllers4preview/")
		// && (f.getName().endsWith(".controller") ||
		// f.getName().endsWith(".prefab"))) {// ||
		if (isNeedCheck(f)) { // f.getName().endsWith(".unity")
			String assetPath = f.getAbsolutePath();
			assetPath = assetPath.substring(assetPath.indexOf("Assets"));
			String guid = f.getName().substring(0, f.getName().indexOf(".")).toLowerCase();
			if (!effectPrefabMap.containsKey(guid)) {
				effectPrefabMap.put(guid, new ArrayList<String>());
				// System.err.println("已经存在：" + effectPrefabMap.get(guid) +
				// "|但是现在指向" + assetPath);
			}
			effectPrefabMap.get(guid).add(assetPath);
		}
		if (f.isDirectory()) {
			File subfs[] = f.listFiles();
			for (File subf : subfs) {
				getFileNameMap(subf, effectPrefabMap);
			}
		}
		return effectPrefabMap;
	}

	private static boolean isNeedCheck(File f) {
		if (f == null || !f.isFile()) {
			return false;
		}
		String fileName = f.getName();
		if (fileName.endsWith(".meta")) {
			return false;
		}

		String assetPath = f.getAbsolutePath();
		if (assetPath.indexOf("Assets") < 1) {
			return false;
		}
		assetPath = assetPath.substring(assetPath.indexOf("Assets"));
		assetPath = assetPath.replace('\\', '/');
		//System.out.println(assetPath);
		if (assetPath.contains("/unused/")) {
			return false;
		}
		if (assetPath.contains("Assets/Art/Effects/EffectRes/effectMaterials/")) {
			return false;
		}
		if (assetPath.contains("Assets/Art/Effects/EffectRes/effectModel/")) {
			return false;
		}
		if (assetPath.contains("Assets/Art/Effects/EffectRes/effectAnimation/Anim/")) {
			return false;
		}
		if (assetPath.contains("Assets/Prefabs/") && (fileName.endsWith(".png") || fileName.endsWith(".jpg")
				|| fileName.endsWith(".prefab") || fileName.endsWith(".unity"))) {
			if (fileName.endsWith(".prefab")) {
				//System.out.println(f);
			}
			return true;
		}
		if (assetPath.contains("Assets/Art/Effects/EffectRes/effectMaterials/") && (fileName.endsWith(".mat"))) {
			return true;
		}
		if (assetPath.contains("Assets/Art/Effects/EffectRes/effectModel/") && (fileName.endsWith(".FBX"))) {
			return true;
		}
		if (assetPath.contains("Assets/Prefabs/Sound/") && (fileName.endsWith(".ogg") || fileName.endsWith(".mp3"))) {
			return true;
		}
		if (assetPath.contains("Assets/Art/Players/ModelFBXs/")
				&& (fileName.endsWith("_zhanshi.controller") || fileName.endsWith("_fight.controller"))) {
			return true;
		}
		if (assetPath.contains("Assets/Art/Players/ModelFBXs/") && (fileName.endsWith("_zhanshi.controller")
				|| fileName.endsWith("_fight.controller") || fileName.endsWith("_png.asset"))) {
			return true;
		}
		if (assetPath.contains("Assets/Art/Effects/EffectRes/effectAnimation/") && (fileName.endsWith(".controller"))) {
			return true;
		}
		/*
		 * Assets/Prefab/ *.png *.jpg Assets/Shaders *./shader
		 * Assets/Art/Effects/EffectRes/effectMaterials *.mat >> effectmats
		 * Assets/Art/Effects/EffectRes/effectModel *.FBX >> effectmeshs
		 * Assets/Prefabs/Sound *.ogg *.mp3 Assets/Prefabs *.prefab
		 * Assets/Prefabs *.unity Assets/Art/Players/ModelFBXs 模型名字+_anim
		 * Assets/Art/Players/ModelFBXs
		 * 模型名字+_zhanshi.controller,模型名字+_fight.controller
		 * Assets/Art/Effects/EffectRes/effectAnimation *.controller
		 * Assets/Art/Players/ModelFBXs gpuskin目录的*.asset
		 * 
		 */

		return false;
	}
}

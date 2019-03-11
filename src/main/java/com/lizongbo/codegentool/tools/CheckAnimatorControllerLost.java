package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.esotericsoftware.yamlbeans.YamlReader;

/**
 * 检查AnimatorController目录，看有哪些资源丢失了
 * 
 * @author quickli
 *
 */
public class CheckAnimatorControllerLost {

	static Set<String> guidSet = new TreeSet<String>();
	static Set<String> csGuidSet = new TreeSet<String>();
	static Set<String> animGuidSet = new TreeSet<String>();

	static Set<String> clientGuidSet = new TreeSet<String>();

	public static void main(String[] args) {
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/AnimatorController/Scenes";
		dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		getAnimatorControllersDir(new File(dir));
		System.out.println(guidSet.toString().replace(',', '\n'));
		System.out.println(guidSet.size());
		dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets";
		checkEditorLogicDir(new File(dir));
		System.out.println("cs文件个数");
		System.out.println(csGuidSet.toString().replace(',', '\n'));
		System.out.println(csGuidSet.size());

		dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets";
		checkClientCsLogicDir(new File(dir));
		System.out.println("Clientcs文件个数");
		System.out.println(clientGuidSet.toString().replace(',', '\n'));
		System.out.println(clientGuidSet.size());
		for (String g : csGuidSet) {
			if (!csGuidSet.contains(g)) {
				System.out.println("guid对应补上有缺失：" + g);
			}
		}

	}

	public static void getAnimatorControllersDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					getAnimatorControllersDir(f);
				} else {
					try {
						getAnimatorControllerFile(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void getAnimatorControllerFile(String prefabFilePath) throws IOException {
		File f = new File(prefabFilePath);
		if (!f.isFile() || (!prefabFilePath.endsWith("_fight.controller"))) {
			// System.err.println("no|getAnimatorControllerFile|" + f);

			return;
		}
		System.out.println("try|getAnimatorControllerFile|" + f);

		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		StringBuilder sb = new StringBuilder(256);
		while ((data = br.readLine()) != null) {
			data = data.trim();
			if (data.contains("guid:")) {
				String guid = data.substring(data.indexOf("guid:") + 5);
				guid = guid.substring(0, guid.indexOf(","));
				System.out.println(data + "|" + guid + "|" + f);
				guidSet.add(guid);
			}
		}
		br.close();
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
		if (!new File(prefabFilePath).isFile() || !prefabFilePath.endsWith(".meta")) {
			return;
		}

		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		int lineCount = 0;
		boolean needAppend = false;
		StringBuilder sb = new StringBuilder(256);
		while ((data = br.readLine()) != null) {
			for (String guid : guidSet) {
				if (data.contains(guid)) {
					System.out.println("checkEditorLogic=" + guid + "=" + new File(prefabFilePath));
					if (prefabFilePath.endsWith(".cs.meta")) {
						csGuidSet.add(guid);
					}
					if (prefabFilePath.endsWith(".anim.meta")) {
						animGuidSet.add(guid);
					}
				}
			}
		}
		br.close();
		// System.out.println(sb);

	}

	public static void checkClientCsLogicDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					checkClientCsLogicDir(f);
				} else {
					try {
						checkClientCsLogic(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void checkClientCsLogic(String prefabFilePath) throws IOException {
		if (!new File(prefabFilePath).isFile() || !prefabFilePath.endsWith(".meta")) {
			return;
		}

		java.io.BufferedReader br = new java.io.BufferedReader(
				new java.io.InputStreamReader(new java.io.FileInputStream(prefabFilePath), "UTF-8"));
		String data = null;
		int lineCount = 0;
		boolean needAppend = false;
		StringBuilder sb = new StringBuilder(256);
		while ((data = br.readLine()) != null) {
			for (String guid : csGuidSet) {
				if (data.contains(guid)) {
					System.out.println("checkClientCsLogic=" + guid + "=" + new File(prefabFilePath));
					if (prefabFilePath.endsWith(".cs.meta")) {
						clientGuidSet.add(guid);
					}
				}
			}
		}
		br.close();
		// System.out.println(sb);

	}

}

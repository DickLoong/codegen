package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlReader;

/**
 * 检查Assets目录，看有哪些资源丢失了
 * 
 * @author quickli
 *
 */
public class CheckAssetsLost {

	public static void main(String[] args) {
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scenes";
		dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Resources/UI/Window/Vip";
		getAssetssDir(new File(dir));
	}

	public static void getAssetssDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					getAssetssDir(f);
				} else {
					try {
						getAssetsFile(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void getAssetsFile(String prefabFilePath) throws IOException {
		File f = new File(prefabFilePath);
		if (!f.isFile() || (!prefabFilePath.endsWith(".meta") && !prefabFilePath.endsWith(".prefab")
				&& !prefabFilePath.endsWith(".unity"))) {
			System.err.println("no|getAssetsFile|" + f);

			return;
		}
		if (f.getName().endsWith(".meta") && f.getName().indexOf(".") == f.getName().lastIndexOf(".")) {// 只有一个点，说明是文件夹的，需要跳过
			System.err.println("isdir|getAssetsFile|" + f);
			return;
		}
		System.out.println("try|getAssetsFile|" + f);
		YamlReader reader = new YamlReader(new FileReader(new File(prefabFilePath)));
		while (true) {
			Map contact = (Map) reader.read();
			if (contact == null)
				break;
			System.out.println(f.getName() + "|guid==" + contact.get("guid"));
		}
	}
}

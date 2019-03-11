package com.lizongbo.codegentool.tools;

import java.io.File;

public class CopyRun2Jipao {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fbxRootDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		File dir = new File(fbxRootDir);
		File[] fs = dir.listFiles();
		for (File f : fs) {
			if (!f.isDirectory()) {
				continue;
			}
			boolean hasJipao = false;
			String runFBXName = "";
			File[] fbxs = f.listFiles();
			for (File fbx : fbxs) {
				if (fbx.getName().endsWith(".FBX")) {
					if (fbx.getName().endsWith("@jipao.FBX")) {
						hasJipao = true;
					}
					if (fbx.getName().endsWith("@run.FBX")) {
						runFBXName = fbx.getName();
					}
				}
			}
			// 有跑但是没有疾跑的,则复制动作fbx占位
			if (!hasJipao && runFBXName.length() > 0) {
				System.out.println("cp " + f.getName() + "/" + runFBXName + " " + f.getName() + "/"
						+ runFBXName.replace("run", "jipao"));
			}
		}
	}

}

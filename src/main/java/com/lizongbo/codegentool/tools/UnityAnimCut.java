package com.lizongbo.codegentool.tools;

import java.io.*;

/**
 * 精简unity动画文件里的坐标精度
 * 
 * @author quickli
 *
 */
public class UnityAnimCut {

	public static void main(String[] args) throws IOException {
		// String fbxRootDir =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		// String animPath =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/bawang/animFs/bawang@attack01.anim";

		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/";
		File dir4Win = new File(
				"D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/");
		if (dir4Win.isDirectory()) {
			dir = dir4Win.getAbsolutePath();
		}
		long startTime = System.currentTimeMillis();
		cutAnimDir(new File(dir));
		// String ff =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/yase/animFs/yase@run.anim";
		// cutAnimDir(new File(
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/yase/"));
		long endTime = System.currentTimeMillis();
		System.out.println("UnityAnimCut|" + dir + "|use|" + (endTime - startTime) + "ms");
		System.exit(0);
		try {
			// cutAnim(animPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void cutAnimDir(File fbxDir) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					cutAnimDir(f);
				} else {
					try {
						cutAnim(f.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void cutAnim(String animFilePath) throws IOException {
		if (animFilePath != null && !animFilePath.contains("_cut") && animFilePath.endsWith(".anim")
				&& new File(animFilePath).isFile()) {
			System.out.println("cutAnim=" + new File(animFilePath).getAbsolutePath());
			File newAnim = new File(new File(animFilePath).getParent() + "_cut", new File(animFilePath).getName());
			newAnim.getParentFile().mkdirs();
			System.out.println("newAnim==" + newAnim.getAbsolutePath());
			OutputStreamWriter osw = new java.io.OutputStreamWriter(new java.io.FileOutputStream(newAnim), "UTF-8");
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(animFilePath), "UTF-8"));
			String data = null;
			int lineCount = 0;
			while ((data = br.readLine()) != null) {
				lineCount++;
				if (data.contains("x:") && data.contains("y:") && data.contains("z:")) {
					String sb = trimFloat(data, lineCount);
					osw.write(sb);
				} else {
					osw.write(data);
				}

				osw.write("\n");
			}
			osw.flush();
			osw.close();
			br.close();

		}

	}

	private static String trimFloat(String data, int lineCount) {
		StringBuilder sb = new StringBuilder(256);
		String[] arr = data.split(",");
		boolean needDouhao = false;
		for (String s : arr) {
			if (!needDouhao) {
				needDouhao = true;
			} else {
				sb.append(",");
			}
			if (s.contains(":") && s.contains(".") && s.lastIndexOf(".") < s.length() - 4) {
				String cutStr = s;
				if (s.contains("e-")) {
					cutStr = s.substring(0, s.lastIndexOf(":") + 2) + 0;
				} else {
					cutStr = s.substring(0, s.lastIndexOf(".") + 4);
				}
				if (cutStr.contains(".000")) {
					cutStr = StringUtil.replaceAll(cutStr, ".000", "");
				}
				// 切完之后结尾是0的，再去掉0
				if (cutStr.endsWith("0")) {
					// cutStr = cutStr.substring(0, cutStr.length()
					// - 2);
				}
				if (cutStr.endsWith("-0")) {
					cutStr = cutStr.replace("-0", "0");
				}
				if (cutStr.endsWith("-0}")) {
					cutStr = cutStr.replace("-0}", "0}");
				}
				// System.out.println(lineCount + "hang" + "||" + cutStr);
				sb.append(cutStr);
				if (s.endsWith("}")) {
					sb.append("}");
				}
			} else {
				// System.out.println(lineCount + "hang" + "||s=====" + s);
				if (s.endsWith("-0")) {
					s = s.replace("-0", "0");
				}
				if (s.endsWith("-0}")) {
					s = s.replace("-0}", "0}");
				}
				sb.append(s);
			}
		}
		if (data.endsWith(",")) {
			sb.append(",");
		}

		// System.out.println(lineCount + "hang cut==" + data);
		// System.out.println(lineCount + "hang to==" + sb);

		return sb.toString();
	}
}

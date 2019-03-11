package com.lizongbo.codegentool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.db2java.GenAll;

public class AppDeployTool {

	public static void main(String[] args) {
		AppDeployTool.quickDeployWebApp4Resin("112.74.108.38", 8090,
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_web");

	}

	/**
	 * 快速部署 webapp到resin,除了
	 * 
	 * @param remoteIp
	 * @param serverPort
	 * @param localWebAppRootDir
	 */
	public static void quickDeployWebApp4Resin(String remoteIp, int serverPort, String localWebAppRootDir) {
		deployWebApp4ResinInternal(remoteIp, serverPort, localWebAppRootDir, false);
	}

	/**
	 * 部署 webapp到resin
	 * 
	 * @param remoteIp
	 * @param remotePort
	 * @param localWebAppRootDir
	 */
	public static void deployWebApp4Resin(String remoteIp, int serverPort, String localWebAppRootDir) {
		deployWebApp4ResinInternal(remoteIp, serverPort, localWebAppRootDir, true);
	}

	public static void deployWebApp4ResinInternal(String remoteIp, int serverPort, String localWebAppRootDir,
			boolean deployLibDir) {
		if (remoteIp == null || remoteIp.length() < 4 || serverPort < 1024) {
			return;
		}
		if (!new File(localWebAppRootDir).isDirectory()) {
			return;
		}
		if (!new File(new File(localWebAppRootDir), "WEB-INF").isDirectory()) {
			return;
		}
		// 前期做成传本地目录的，后面做成直接拉svn的代码来发布
		String appDirName = new File(localWebAppRootDir).getName();
		String tmpZipFile = "/tmp/" + new File(localWebAppRootDir).getName() + ".zip";

		System.out.println("try Copy!!!!!!!");
		// Process process;
		try {
			System.out.println(
					"try /mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + localWebAppRootDir + "/WEB-INF/build.xml");
			/*
			 * process = new
			 * ProcessBuilder("/mgamedev/tools/apache-ant-1.9.6/bin/ant", "-f",
			 * localWebAppRootDir + "/WEB-INF/build.xml").start();
			 */
			ServerContainerGenTool.runShellCmd(
					"/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + localWebAppRootDir + "/WEB-INF/build.xml");
			/*
			 * if (process.waitFor() != 0) { InputStream is =
			 * process.getErrorStream(); InputStreamReader isr = new
			 * InputStreamReader(is); BufferedReader br = new
			 * BufferedReader(isr); String line; while ((line = br.readLine())
			 * != null) { System.err.println(line); }
			 * 
			 * } else { InputStream is = process.getInputStream();
			 * InputStreamReader isr = new InputStreamReader(is); BufferedReader
			 * br = new BufferedReader(isr); String line; while ((line =
			 * br.readLine()) != null) { System.out.println(line); }
			 * 
			 * }
			 */

			String webAppNewDir = "/tmp/tools/" + localWebAppRootDir;
			ServerContainerGenTool.delAllFile(webAppNewDir);
			ServerContainerGenTool.copyDir(new File(localWebAppRootDir), new File(webAppNewDir));
			ServerContainerGenTool.delAllFile4Need(webAppNewDir);
			if (!deployLibDir) {// 删除lib目录再打包
				ServerContainerGenTool.delAllFile(webAppNewDir + "/WEB-INF/lib");
			}
			ServerContainerGenTool.zipDir(webAppNewDir, tmpZipFile, new File(localWebAppRootDir).getName());

		} catch (Throwable e) {
			e.printStackTrace();
		}
		//
		String linuxUserName = "root";
		String linuxUserPwd = "quick10343QQ";
		if ("10.0.0.16".equals(remoteIp)) {
			linuxUserName = "bilin";
			linuxUserPwd = "bilinkeji.net";
		}
		SCPUtil.doSCPTo(tmpZipFile, linuxUserName, linuxUserPwd, remoteIp,
				"/usr/local/apps/resin_" + serverPort + "/webapps/" + new File(tmpZipFile).getName());
		// .main(new String[] { tmpZipFile,
		// linuxUserName+"@" + serverInerIp + ":/usr/local/apps/" + new
		// File(tmpZipFile).getName() });
		String remoteCmd = "cd /usr/local/apps/resin_" + serverPort + "/webapps/;unzip -u -o ./"
				+ new File(tmpZipFile).getName() + ";rm ./" + new File(tmpZipFile).getName() + "";
		if (deployLibDir) {// 把整个webapp目录删除重新部署
			remoteCmd = "cd /usr/local/apps/resin_" + serverPort + "/webapps/; rm -rf /usr/local/apps/resin_"
					+ serverPort + "/webapps/" + new File(localWebAppRootDir).getName() + "/; unzip -u -o ./"
					+ new File(tmpZipFile).getName() + ";rm ./" + new File(tmpZipFile).getName() + "";
		}
		LinuxRemoteCommandUtil.runCmd(remoteIp, 22, linuxUserName, linuxUserPwd, remoteCmd);
		System.out.println("============" + new File(localWebAppRootDir).getName());
		if ("gecaoshoulie_webaaa".equals(new File(localWebAppRootDir).getName())) {// 特殊生成2015和2016的外链
			LinuxRemoteCommandUtil.runCmd(remoteIp, 22, linuxUserName, linuxUserPwd,
					"cd /usr/local/apps/resin_" + serverPort + "/webapps/"
							+ new File(localWebAppRootDir)
									.getName()
					+ "/ci/ ;pwd;ln -s /data/release_pkg/2015 ./2015; ln -s /data/release_pkg/2016 ./2016; ls -alh ");
		}

	}
}

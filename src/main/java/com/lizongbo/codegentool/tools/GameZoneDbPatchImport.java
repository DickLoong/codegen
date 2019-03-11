package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.SCPUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;

public class GameZoneDbPatchImport {
	public static void main(String[] args) {
		String remoteIp = // "120.76.54.48";//
				System.getenv("remoteIp");
		String linuxUserName = "root";
		String linuxUserPwd = "quick10343QQ";
		String sqlFilepath ;
		System.out.println("System.getenv===" + System.getenv());

		// TODO 读取一个配置文件
		Properties prop = new Properties();
		InputStream in = Object.class.getResourceAsStream("/DeployConfig." + remoteIp + ".properties");
		System.out.println("try load properties.");
		try {
			prop.load(in);
			if (prop.containsKey("linuxUserName")) {
				linuxUserName = prop.getProperty("linuxUserName").trim();
			}
			if (prop.containsKey("linuxUserPwd")) {
				linuxUserPwd = prop.getProperty("linuxUserPwd").trim();
			}
			if (prop.containsKey("outputPath")) {
				sqlFilepath = prop.getProperty("outputPath").trim();
			}

		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("use default properties.");
		}
		sqlFilepath = args[0];
		System.out.println("try load properties finished.");
		System.out.println(sqlFilepath);
		remoteIp = (remoteIp == null) ? "10.0.0.16" : remoteIp;
		if (remoteIp.startsWith("10.0.0.")) {// 内网的数据库只在10.0.0.16
			remoteIp = "10.0.0.16";
		}
		System.out.println("remoteIp===" + remoteIp);
		System.out.println("System.getenv===" + System.getenv());
		int zoneId = // 40002;
				StringUtil.toInt(System.getenv("zoneId"), 40004);

		System.out.println("zoneId===" + zoneId);
		String tmpZipFile = "/tmp/server_" + remoteIp + "_" + zoneId + "/dropCreateInsertdb4dbconfig.sql.zip";
		try {
			ServerContainerGenTool.zipFile(sqlFilepath, tmpZipFile);
		} catch (Exception e) {
			System.err.println("zipFile|err|" + sqlFilepath + "|" + tmpZipFile);
			System.exit(-1);
			e.printStackTrace();
		}
		// 先确保文件夹存在
		LinuxRemoteCommandUtil.runCmd(remoteIp, 22, linuxUserName, linuxUserPwd,
				"mkdir -p " + new File(tmpZipFile).getParent().replace('\\', '/'));
		// 上传sql文件
		SCPUtil.doSCPTo(tmpZipFile, linuxUserName, linuxUserPwd, remoteIp, tmpZipFile);
		String databaseName = "mgamedb_gecaoshoulie_maindb_zone" + zoneId;
		if (zoneId <= 0) {
			databaseName = "mgamedb_gecaoshoulie";
		}
		LinuxRemoteCommandUtil.runCmd(remoteIp, 22, linuxUserName, linuxUserPwd,
				"cd " + new File(tmpZipFile).getParent().replace('\\', '/') + "; unzip -u -o ./"
						+ new File(tmpZipFile).getName() + "; "
						+ "mysql -h127.0.0.1 -uroot -pmysqlpwdbilinkejinet -P3306 " + databaseName
						+ " -e \"select database();\" ; " + "mysql -h127.0.0.1 -uroot -pmysqlpwdbilinkejinet -P3306 "
						+ databaseName + " < " + "/tmp/server_" + remoteIp + "_" + zoneId
						+ "/Insertdb4dbuser.sql 2>&1" + " ; ");
	}
}

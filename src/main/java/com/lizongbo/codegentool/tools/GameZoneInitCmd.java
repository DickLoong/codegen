package com.lizongbo.codegentool.tools;

import java.io.InputStream;
import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;

public class GameZoneInitCmd {

	public static void main(String[] args) {
		String remoteIp = System.getenv("remoteIp");
		remoteIp = (remoteIp == null) ? "noip" : remoteIp;
		System.out.println("remoteIp===" + remoteIp);
		System.out.println("System.getenv===" + System.getenv());
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 40001);
		if (zoneId < 40001) {
			zoneId = 40001;
		}
		int arenaRobotCount = StringUtil.toInt(System.getenv("arenaRobotCount"), 180);
		System.out.println("zoneId===" + zoneId);
		String linuxUserName = "bilin";
		String linuxUserPwd = "bilinkeji.net";
		// TODO 读取一个配置文件
		Properties prop = new Properties();
		InputStream in = Object.class.getResourceAsStream("/DeployConfig." + remoteIp + ".properties");
		System.out.println("try load properties.");
		try {
			prop.load(in);
			if(prop.containsKey("linuxUserName")){
				linuxUserName = prop.getProperty("linuxUserName").trim();
			}
			if(prop.containsKey("linuxUserPwd")){
				linuxUserPwd = prop.getProperty("linuxUserPwd").trim();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("use default properties.");
		}
		System.out.println("try load properties finished.");
		LinuxRemoteCommandUtil.runCmd(remoteIp, 22, linuxUserName, linuxUserPwd, "export arenaRobotCount="
				+ arenaRobotCount + ";/usr/local/apps/javaserver_gecaoshoulie_server_" + zoneId + "/bin/bl_reinit.sh");

	}

}

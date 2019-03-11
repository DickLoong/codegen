package com.lizongbo.codegentool;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.zip.*;


import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.tools.GameZoneDbImport;
import com.lizongbo.codegentool.tools.StringUtil;

public class ServerIncreasementBuildTool {

	public static void main(String[] args) {
		// genResinServer("10.0.0.16", 8090, "10.0.0.16");
		// genResinServer("112.74.108.38", 8090, "10.116.33.153");
		// 部署到内网服务器
		String buildIp = System.getenv("buildIp");
		buildIp = (buildIp == null) ? "10.0.0.137" : buildIp;
		String remoteIp = System.getenv("remoteIp");
		remoteIp = (remoteIp == null) ? "10.0.0.16" : remoteIp;
		System.out.println("remoteIp===" + remoteIp);
		System.out.println("buildIp===" + buildIp);
		System.out.println("System.getenv===" + System.getenv());
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 40001);
		if (zoneId < 40001) {
			zoneId = 40001;
		}
		increasementBuildJavaServer(buildIp, zoneId, remoteIp);
	}

	private static void increasementBuildJavaServer(String buildIp, int zoneId, String remoteIp) {
		System.out.println("this increase build started at " + LocalDateTime.now());
		System.out.println("------------------------------------------------------------------------");
		String buildLinuxUserName = "bilinbuild";
		String buildLinuxPassWord = "bilinkeji.net";
		String buildScriptName = "BilinServerIncreasementBuildScript_BuidingPart.sh";
		String buildScriptPath = "/home/" + buildLinuxUserName + "/" + buildScriptName;
//		String buildScriptPath = "cat /etc/profile";
		
		
		System.out.println("try running the build script on " + buildIp + "|" + buildScriptPath);
		LinuxRemoteCommandUtil.runCmd(buildIp, 22, buildLinuxUserName, buildLinuxPassWord,
				buildScriptPath);
		
		String deployLinuxUserName = "bilindeploy";
		String deployLinuxPassWord = "bilinkeji.net";
		String deployScriptName = "BilinServerIncreasementBuildScript_DeployPart.sh";
		String deployScriptPath = "/home/" + deployLinuxUserName + "/" + deployScriptName;
		String deployFullCmd = deployScriptPath + " " + remoteIp;
		System.out.println("try running the deploy script on " + buildIp + "|" + buildScriptPath);
		LinuxRemoteCommandUtil.runCmd(buildIp, 22, deployLinuxUserName, deployLinuxPassWord,
				deployFullCmd);
		
		String workingLinuxUserName = "bilin";
		String workingLinuxPassWord = "bilinkeji.net";
		String workingScriptName = "BilinServerIncreasementBuildScript_WorkingPart.sh";
		String workingScriptPath = "/home/" + workingLinuxUserName + "/" + workingScriptName;
		String workingFullCmd = workingScriptPath + " " + zoneId;
		
		System.out.println("try running the working script on " + buildIp + "|" + buildScriptPath);
		LinuxRemoteCommandUtil.runCmd(remoteIp, 22, workingLinuxUserName, workingLinuxPassWord,
				workingFullCmd);
		System.out.println("------------------------------------------------------------------------");
		System.out.println("this increase build finished at " + LocalDateTime.now());
	}

}

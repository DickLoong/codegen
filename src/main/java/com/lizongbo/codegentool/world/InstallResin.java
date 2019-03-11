package com.lizongbo.codegentool.world;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.SCPUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.tools.StringUtil;

public class InstallResin {
	private static String resinSetupZipedFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/serversofts/resin-4.0.46.zip";
	private static String resinconftempdir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/resinconftemp/resin-4.0.46";
	
	public static void main(String[] args){
		//genResinServer("bl_Test");
	}
	
	/**
	 * 安装支付服务器的Resin
	 * @param worldName
	 */
	public static void genPayServerResinServer(String worldName){
		List<String> allHttpHost = BilinGameWorldConfig.getPayServerHost(worldName);
		int httpPort = BilinGameWorldConfig.getPayServerPort(worldName);
		
		for (String httpHost : allHttpHost)
		{
			genResinServer(worldName, httpHost, httpPort);
		}
	}
	
	/**
	 * 安装运营平台的Resin
	 */
	public static void genOperateResinServer(String worldName){
		//读取本地发布用的配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
				
		//下载世界用的配置文件
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
				
		int httpPort = StringUtil.toInt(worldProp.getProperty("operateServerPort"));
		String httpHost = worldProp.getProperty("operateServerHost");
		
		genResinServer(worldName, httpHost, httpPort);
	}
	
	/**
	 * 生成安装包
	 * 
	 * @param httpPort
	 *            端口
	 * @param rmiHost
	 *            外网ip
	 */
	private static void genResinServer(String worldName, String httpHost, int httpPort) {
		//读取本地发布用的配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String rmiHost = httpHost;
		
		int jmxPort = 4003 + (httpPort - 8090);
		int appServerPort = 6800 + (httpPort - 8090);
		int watchDogPort = 6600 + (httpPort - 8090);
		
		String resinNewDir = I18NUtil.getWorldServerBuildRoot(worldName) + "/resin_" + httpPort;
		ServerContainerGenTool.delAllFile(resinNewDir);
		
		String resinVersion = new File(resinSetupZipedFilePath).getName();
		resinVersion = resinVersion.substring(0, resinVersion.indexOf(".zip"));
		
		String resinDestTempDir = "/tmp/" + worldName + "/resin_temp";
		ServerContainerGenTool.unZipFile(resinSetupZipedFilePath, resinDestTempDir);
		new File(resinDestTempDir + "/" + resinVersion).renameTo(new File(resinNewDir));
		new File(resinDestTempDir).delete();
		
		String resinRemoteDir = BilinGameWorldConfig.getRemoteResinRoot(httpPort);
		
		String bl_startShText = GenAll.readFile(resinconftempdir + "/bin/bl_start.sh", "UTF-8");
		String killShText = GenAll.readFile(resinconftempdir + "/bin/kill.sh", "UTF-8");
		String resinPropertiesText = GenAll.readFile(resinconftempdir + "/conf/resin.properties", "UTF-8");
		String resinXmlText = GenAll.readFile(resinconftempdir + "/conf/resin.xml", "UTF-8");
		
		killShText = ServerContainerGenTool.replaceAll(killShText, "/mgamedev/tools/resin-4.0.45", resinRemoteDir);

		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText, "resin_doc      : true",
				"resin_doc      : false");// 关闭resindoc
		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText,
				"app_servers      : 127.0.0.1:6800", "app_servers      : 127.0.0.1:" + appServerPort);// 设置appServer端口

		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText, "app.http          : 8080",
				"app.http          : " + httpPort);//
		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText, "web.http          : 8080",
				"web.http          : " + httpPort);//
		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText, "port_thread_max   : 256",
				"port_thread_max   : 1500");//
		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText, "web_admin_enable : true",
				"web_admin_enable : false");//
		
		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText,
				"# http_ping_urls : http://127.0.0.1/test.jsp", "http_ping_urls : http://127.0.0.1/blinfo.jsp");//

		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText, "# jvm_mode    : -server",
				"jvm_mode    : -server");//
		String jvmArgs = " -server "
				// jmx
				+ " -Djava.rmi.server.hostname=" + rmiHost + " -Dcom.sun.management.jmxremote.port=" + jmxPort
				// 不绑定ip，通过密码限制
				///// + " -Dcom.sun.management.jmxremote.host="
				///// + serverInerIp + " "
				+ " -Dcom.sun.management.jmxremote.ssl=false " + " -Dcom.sun.management.jmxremote.authenticate=false  "
				// 需要验证的时候才加下面的
				///// + " -Dcom.sun.management.jmxremote.password.file=" +
				// resinRemoteDir + "/conf/jmxremote.password "
				///// + " -Dcom.sun.management.jmxremote.access.file=" +
				// resinRemoteDir + "/conf/jmxremote.access "
				// common
				+ "-Dfile.encoding=UTF-8 " + " -Dlog4j.debug=true "
				// gc
				+ "-verbose:gc " + "-XX:+PrintGCDetails " + " -XX:+PrintGCTimeStamps " + "-XX:+PrintGCDateStamps "
				+ " -Xloggc:" + resinRemoteDir + "/log/gc.log" + " -Xms2000m -Xmx2000m -Xmn700m -Xss1000k "
				+ " -XX:PermSize=64m -XX:+UseConcMarkSweepGC " + " -XX:CMSInitiatingOccupancyFraction=80 "
				// other
				+ " -Dsun.net.http.allowRestrictedHeaders=true " + " -Djava.awt.headless=true " + " -Dresin.home="
				+ resinRemoteDir + " -Dresin.root=" + resinRemoteDir + " -Dserver.root=" + resinRemoteDir
				+ " -Djavaserver.home=" + resinRemoteDir + "/";

		resinPropertiesText = ServerContainerGenTool.replaceAll(resinPropertiesText,
				"# jvm_args  : -Xmx2048m -XX:MaxPermSize=256m", "jvm_args  :" + jvmArgs);//

		// port="6800" watchdog-port="6600"
		resinXmlText = ServerContainerGenTool.replaceAll(resinXmlText, "port=\"6800\" watchdog-port=\"6600\"",
				"port=\"" + appServerPort + "\" watchdog-port=\"" + watchDogPort + "\"");
		try {
			ServerContainerGenTool.copyDir(new File(resinconftempdir), new File(resinNewDir));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		GameCSV2DB.writeFile(resinNewDir + "/bin/bl_start.sh", bl_startShText);
		GameCSV2DB.writeFile(resinNewDir + "/bin/kill.sh", killShText);
		GameCSV2DB.writeFile(resinNewDir + "/conf/resin.properties", resinPropertiesText);
		GameCSV2DB.writeFile(resinNewDir + "/conf/resin.xml", resinXmlText);
		ServerContainerGenTool.delAllFile4Need(resinNewDir);
		String tmpZipFile = "/tmp/" + new File(resinNewDir).getName() + ".zip";
		ServerContainerGenTool.zipDir(resinNewDir, tmpZipFile, new File(resinNewDir).getName());
		
		SCPUtil.doSCPTo(tmpZipFile, 
				deployProp.getProperty("linuxUserName").trim(), deployProp.getProperty("linuxUserPwd").trim(), 
				httpHost,
				BilinGameWorldConfig.appsRoot + "/" + new File(tmpZipFile).getName());
		
		LinuxRemoteCommandUtil.runCmd(httpHost, 22, 
				deployProp.getProperty("linuxUserName").trim(), deployProp.getProperty("linuxUserPwd").trim(),
					"cd " + BilinGameWorldConfig.appsRoot + ";"
					+ "rm -rf " + BilinGameWorldConfig.appsRoot + "/" + new File(resinNewDir).getName() + "/;"
					+ "unzip ./" + new File(tmpZipFile).getName() + ";"
					+ "chmod +x " + BilinGameWorldConfig.appsRoot + "/" + new File(resinNewDir).getName() + "/bin/*.sh;"
					+ "chmod 400 " + BilinGameWorldConfig.appsRoot + "/" + new File(resinNewDir).getName() + "/conf/jmxremote.password;");
		
		LinuxRemoteCommandUtil.runCmd(httpHost, 22, 
				deployProp.getProperty("linuxUserName").trim(), deployProp.getProperty("linuxUserPwd").trim(),
				BilinGameWorldConfig.appsRoot + "/" + new File(resinNewDir).getName() + "/bin/bl_start.sh;");
	}
}

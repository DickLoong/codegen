package com.lizongbo.codegentool.world;

import java.io.File;
import java.util.Properties;

import com.jcraft.jsch.Session;
import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.HostsGen4WorldUtil;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 发布用到的各个命令在这里,如建SQL库,复制文件,等等.
 * 实质是对各个LinuxRemoteCommandUtil的显式封装
 */
public class DeployCommandUtil {
	
	/**
	 * 上传并更新指定服务器上的连接池配置文件,keyword文件
	 */
	public static void updateOtherCfgFiles(String worldName, String cmdHost,
			String poolcfgZipFile, String keywordZipFile){
		LogUtil.printLog("updateOtherCfgFiles|Host|start|host|" + cmdHost);
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		ScpCommandUtil.ScpTo(cmdHost, loginUser, loginPwd, poolcfgZipFile, 
				BilinGameWorldConfig.appsRoot + "/" + new File(poolcfgZipFile).getName());
		Unzip(cmdHost, loginUser, loginPwd, BilinGameWorldConfig.appsRoot + "/" + new File(poolcfgZipFile).getName(), 
				BilinGameWorldConfig.appsRoot);
		
		ScpCommandUtil.ScpTo(cmdHost, loginUser, loginPwd, keywordZipFile, 
				BilinGameWorldConfig.appsRoot + "/" + new File(keywordZipFile).getName());
		Unzip(cmdHost, loginUser, loginPwd, BilinGameWorldConfig.appsRoot + "/" + new File(keywordZipFile).getName(), 
				BilinGameWorldConfig.appsRoot);
		
		LogUtil.printLog("updateOtherCfgFiles|Host|end|host|" + cmdHost);
	}
	
	/**
	 * 上传并更新指定服务器上的hosts文件
	 */
	public static void updateServerHosts(String worldName, String cmdHost){
		LogUtil.printLog("updateServerHosts|Host|start|host|" + cmdHost);
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		String hostFilePath = HostsGen4WorldUtil.getHostsPath(worldName);
		String remoteFile = "/var/tmp/hosts.my";
		
		ScpCommandUtil.ScpTo(cmdHost, loginUser, loginPwd, hostFilePath, remoteFile);
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd, 
				"sudo sh -c 'cat /var/tmp/hosts.system " + remoteFile + " > /etc/hosts';");
		
		LogUtil.printLog("updateServerHosts|Host|end|host|" + cmdHost);
	}
	
	public static void chmodAddX(String worldName, String cmdHost, String path){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd,
				"chmod +x " + path + ";");
	}
	
	/**
	 * 只方法暂时只用在GameServer上面
	 * 为*.sh添加可执行权限
	 */
	public static void zoneChmodAddX(String worldName, int zoneId, String path){
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		chmodAddX(worldName, zoneConfig.server_public_ip, path);
	}
	
	/**
	 * 在Game Server执行远程的Shell文件
	 */
	private static void execZoneShellScript(String worldName, int zoneId, String shellScriptPath){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		zoneChmodAddX(worldName, zoneId, shellScriptPath);
		
		LinuxRemoteCommandUtil.runCmd(zoneConfig.server_public_ip, 22, 
				loginUser, loginPwd,
				shellScriptPath + ";");
	}
	
	/**
	 * 启动Resin
	 */
	public static void startResinServer(String worldName, String cmdHost, int httpPort){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String loginUser = deployProp.getProperty("linuxUserName").trim();
		String loginPasswd = deployProp.getProperty("linuxUserPwd").trim();
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPasswd,
				BilinGameWorldConfig.getRemoteResinRoot(httpPort) + "/bin/bl_start.sh;");
	}
	
	/**
	 * 启动Game Server
	 */
	public static void startGameGameServer(String worldName, int zoneId){
		String item = BilinGameWorldConfig.getRemoteZoneGameServerRoot(zoneId);
		String startShName = item + "/bin/bl_start.sh";
		
		execZoneShellScript(worldName, zoneId, startShName);
	}
	
	/**
	 * 停止Game Server
	 */
	public static void StopGameGameServer(String worldName, int zoneId){
		String item = BilinGameWorldConfig.getRemoteZoneGameServerRoot(zoneId);
		String killShName = item + "/bin/kill.sh";
		
		execZoneShellScript(worldName, zoneId, killShName);
	}
	
	/**
	 * 启动Common Server
	 */
	public static void startGameCommonServer(String worldName, ServerDeployConfig deployConfig){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		String item = BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id);
		String startShName = item + "/bin/bl_start.sh";
		
		LinuxRemoteCommandUtil.runCmd(deployConfig.server_public_ip, 22, loginUser, loginPwd, startShName);	
	}
	
	/**
	 * 停止Common Server
	 */
	public static void StopGameCommonServer(String worldName, ServerDeployConfig deployConfig){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		String item = BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id);
		String killShName = item + "/bin/kill.sh";
		
		LinuxRemoteCommandUtil.runCmd(deployConfig.server_public_ip, 22, loginUser, loginPwd, killShName);
	}
	
	/**
	 * 启动场景服务器
	 */
	public static void startGameMapServer(String worldName, int zoneId){
		String item = BilinGameWorldConfig.getRemoteZoneMapServerServerRoot(zoneId);
		String startShName = item + "/bin/bl_start.sh";
		
		execZoneShellScript(worldName, zoneId, startShName);
	}
	
	/**
	 * 停止场景服务器
	 */
	public static void StopGameMapServer(String worldName, int zoneId){
		String item = BilinGameWorldConfig.getRemoteZoneMapServerServerRoot(zoneId);
		String killShName = item + "/bin/kill.sh";
		
		execZoneShellScript(worldName, zoneId, killShName);
	}
	
	/**
	 * 启动帧同步服务器
	 */
	public static void startGameFrameSyncServer(String worldName, int zoneId){
		String[] binPaths = new String[]{
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 1),
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 2),
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 3),
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 4),
		};
		for (String item : binPaths){
			String startShName = item + "/bin/bl_start.sh";
			execZoneShellScript(worldName, zoneId, startShName);
		}
	}
	
	/**
	 * 停止帧同步服务器
	 */
	public static void StopGameFrameSyncServer(String worldName, int zoneId){
		String[] binPaths = new String[]{
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 1),
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 2),
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 3),
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, 4),
		};
		for (String item : binPaths){
			String killShName = item + "/bin/kill.sh";
			execZoneShellScript(worldName, zoneId, killShName);
		}
	}
	
	/**
	 * 启动世界服务器Redis
	 */
	public static void StartWorldServerRedis(String worldName, ServerDeployConfig serverConfig){
		LogUtil.printLog("start redis starting");
		
		String[] binPaths = new String[]{
				BilinGameWorldConfig.getRemoteWorldServerRedisRoot(serverConfig.zone_id, serverConfig.redis1_port),
				BilinGameWorldConfig.getRemoteWorldServerRedisRoot(serverConfig.zone_id, serverConfig.redis2_port),
				BilinGameWorldConfig.getRemoteWorldServerRedisRoot(serverConfig.zone_id, serverConfig.redis3_port),
				BilinGameWorldConfig.getRemoteWorldServerRedisRoot(serverConfig.zone_id, serverConfig.redis4_port),
		};
		for (String item : binPaths){
			String startShName = item + "/bin/bl_start.sh";
			execZoneShellScript(worldName, serverConfig.zone_id, startShName);
		}
		
		LogUtil.printLog("start redis completely");
	}
	
	/**
	 * 启动游戏服Redis
	 */
	public static void StartGameRedisServer(String worldName, int zoneId){
		LogUtil.printLog("start redis starting");
		
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String[] binPaths = new String[]{
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis1_port),
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis2_port),
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis3_port),
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis4_port),
		};
		for (String item : binPaths){
			String startShName = item + "/bin/bl_start.sh";
			execZoneShellScript(worldName, zoneId, startShName);
		}
		
		LogUtil.printLog("start redis completely");
	}
	
	/**
	 * 停止游戏服Redis
	 */
	public static void StopGameRedisServer(String worldName, int zoneId){
		LogUtil.printLog("stop redis starting");
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String[] binPaths = new String[]{
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis1_port),
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis2_port),
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis3_port),
				BilinGameWorldConfig.getRemoteZoneRedisRoot(zoneConfig.zone_id, zoneConfig.redis4_port),
		};
		for (String item : binPaths){
			String killShName = item + "/bin/kill.sh";
			LinuxRemoteCommandUtil.runCmd(zoneConfig.server_public_ip, 22, 
					deployProp.getProperty("linuxUserName"), deployProp.getProperty("linuxUserPwd"),
					"chmod +x " + killShName + ";");
			
			LinuxRemoteCommandUtil.runCmd(zoneConfig.server_public_ip, 22, 
					deployProp.getProperty("linuxUserName"), deployProp.getProperty("linuxUserPwd"),
					killShName + ";");
		}
		
		LogUtil.printLog("stop redis completely");
	}
	
	/**
	 * 安装世界服务器的Redis
	 */
	public static void InstallWorldServerRedis(String worldName, ServerDeployConfig serverConfig){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		LinuxRemoteCommandUtil.runCmd(serverConfig.server_public_ip, 22, 
				deployProp.getProperty("linuxUserName").trim(), deployProp.getProperty("linuxUserPwd").trim(),
				"/usr/bin/perl /data/bilin/software/gecaoshoulie_server_init/install_redis.pl"
				+ " " + serverConfig.redis1_port
				+ " " + serverConfig.redis2_port
				+ " " + serverConfig.redis3_port
				+ " " + serverConfig.redis4_port
				+ " " + BilinGameWorldConfig.softwareRoot
				+ " " + BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id));
	}
	
	/**
	 * 为游戏服安装Redis
	 */
	public static void InstallGameServerRedis(String worldName, int zoneId){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		LinuxRemoteCommandUtil.runCmd(zoneConfig.server_public_ip, 22, 
				deployProp.getProperty("linuxUserName").trim(), deployProp.getProperty("linuxUserPwd").trim(),
				"/usr/bin/perl /data/bilin/software/gecaoshoulie_server_init/install_redis.pl"
				+ " " + zoneConfig.redis1_port
				+ " " + zoneConfig.redis2_port
				+ " " + zoneConfig.redis3_port
				+ " " + zoneConfig.redis4_port
				+ " " + BilinGameWorldConfig.softwareRoot
				+ " " + BilinGameWorldConfig.getZoneAppRoot(zoneId));
	}
	
	/**
	 * Jenkins在版本服务器上解压文件
	 */
	public static void UnzipVersionServerFile(String worldName, String zipFile, String dest){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String cmdHost = deployProp.getProperty("releaseVersionHost");
		String loginUser = deployProp.getProperty("releaseVersionUser");
		String loginPwd = deployProp.getProperty("releaseVersionPwd");
		
		Unzip(cmdHost, loginUser, loginPwd, zipFile, dest);
	}
	
	/**
	 * 复制GameServer的jar包到指定lib目录
	 */
	public static void cpGameServerLibTo(String worldName, String cmdHost, String libPath){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd,
				"mkdir -p " + libPath);
	
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd,
				"rm -f " + libPath + "/*.jar;");
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd,
				"cp " + BilinGameWorldConfig.softwareRoot + "/javalib4server/* " 
				+ libPath + "/");
	}
	
	/**
	 * 复制GameServer的jar包到lib目录
	 */
	public static void cpGameServerLib(String worldName, int zoneId){
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		cpGameServerLibTo(worldName, zoneConfig.server_public_ip, BilinGameWorldConfig.getRemoteZoneGameServerRoot(zoneId) + "/WEB-INF/lib");
	}
	
	/**
	 * 复制FrameServer的jar包到lib目录
	 * @param worldName
	 * @param zoneId
	 */
	public static void cpFrameServerLib(String worldName, int zoneId, int seq){
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		cpGameServerLibTo(worldName, zoneConfig.server_public_ip, 
				BilinGameWorldConfig.getRemoteZoneFrameServerRoot(zoneId, seq) + "/WEB-INF/lib");
	}
	
	/**
	 * 复制MapServer的jar包到lib目录
	 * @param worldName
	 * @param zoneId
	 */
	public static void cpMapServerLib(String worldName, int zoneId){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		String libPath = BilinGameWorldConfig.getRemoteZoneMapServerServerRoot(zoneId) + "/WEB-INF/lib";
		
		cpGameServerLibTo(worldName, zoneConfig.server_public_ip, libPath);
		
		LinuxRemoteCommandUtil.runCmd(zoneConfig.server_public_ip, 22, loginUser, loginPwd,
				"cp " + BilinGameWorldConfig.getJavaLib4MapServerPath() + "/* " + libPath + "/");
	}
	
	/**
	 * 运营平台的文件复制
	 * 复制.war文件
	 * 解压.war文件
	 * 复制lib文件
	 */
	public static void cpOperateLib(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		int httpPort = StringUtil.toInt(worldProp.getProperty("operateServerPort"));
		
		String releaseFile = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/gecaoshoulie_operateconsole.war";
		String tmpZipFile = BilinGameWorldConfig.getWorldTmpRoot(worldName) + "/gecaoshoulie_operateconsole.war";
		String webappsPath = BilinGameWorldConfig.getRemoteResinRoot(httpPort) + "/webapps/gecaoshoulie_operateconsole";
		
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"scp -P " + LinuxRemoteCommandUtil.GetSSHPort() + " "
				+ worldProp.getProperty("releaseVersionUser") + "@" + worldProp.getProperty("releaseVersionHost") 
				+ ":" + releaseFile + " " + tmpZipFile + ";");
		
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"unzip -q -o " + tmpZipFile + " -d " + webappsPath + ";");
		
		
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"mkdir -p " + webappsPath + "/WEB-INF/lib/;");
		
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"rm -f " + webappsPath + "/WEB-INF/lib/*.jar;");
		
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"cp " + BilinGameWorldConfig.softwareRoot + "/javalib4operate/* " + webappsPath + "/WEB-INF/lib/;");
		
		//删除ServerLib.jar
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"rm -f " + webappsPath + "/WEB-INF/lib/ServerLib.jar;");
		
		//复制
		String localPath = webappsPath + "/WEB-INF/lib/" + I18NUtil.getServercodegenJarName();
		ScpServerCodeGenJar(worldName, worldProp.getProperty("operateServerHost"), "latest", localPath);
		
		
	}
	
	/**
	 * 支付服务器的文件复制
	 * 复制.war文件
	 * 解压.war文件
	 * 复制lib文件
	 */
	public static void cpPayServerLib(String worldName, String httpHost){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		int httpPort = BilinGameWorldConfig.getPayServerPort(worldName);
		
		String releaseFile = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/gecaoshoulie_pay_server.war";
		String tmpZipFile = BilinGameWorldConfig.getWorldTmpRoot(worldName) + "/gecaoshoulie_pay_server.war";
		String webappsPath = BilinGameWorldConfig.getRemoteResinRoot(httpPort) + "/webapps/gecaoshoulie_pay_server";
		
		LinuxRemoteCommandUtil.runCmd(httpHost, 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"scp -P " + LinuxRemoteCommandUtil.GetSSHPort() + " " 
				+ worldProp.getProperty("releaseVersionUser") + "@" + worldProp.getProperty("releaseVersionHost") 
				+ ":" + releaseFile + " " + tmpZipFile + ";");
		
		LinuxRemoteCommandUtil.runCmd(httpHost, 22, 
				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
				"unzip -q -o " + tmpZipFile + " -d " + webappsPath + ";");
		
		//重要: 先不复制,因为现在一个完整的WAR包只有60M
//		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
//				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
//				"mkdir -p " + webappsPath + "/WEB-INF/lib/;");
		
//		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
//				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
//				"rm -f " + webappsPath + "/WEB-INF/lib/*.jar;");
		
//		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
//				deployProp.getProperty("linuxUserName").trim(),  deployProp.getProperty("linuxUserPwd").trim(), 
//				"cp " + BilinGameWorldConfig.softwareRoot + "/javalib4payserver/* " + webappsPath + "/WEB-INF/lib/;");
		
		//复制
		String localPath = webappsPath + "/WEB-INF/lib/" + I18NUtil.getServercodegenJarName();
		ScpServerCodeGenJar(worldName, httpHost, "latest", localPath);
	}
	
	/**
	 * Jenkins在Game服务器上解压文件
	 * @param zipFile 压缩文件路径
	 * @param dest 解压到目标路径
	 */
	public static void UnzipGameServerFile(String worldName, int zoneId, String zipFile, String dest){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String cmdHost = zoneConfig.server_public_ip;
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		Unzip(cmdHost, loginUser, loginPwd, zipFile, dest);
	}
	
	/**
	 * 远程解压文件
	 */
	public static void Unzip(String cmdHost, String loginUser, String loginPwd, String zipFile, String dest){
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd,  "unzip -q -o " + zipFile + " -d " + dest);
	}
	
	/**
	 * 公开的方法请使用copyFileFromVersionServer
	 */
	private static void ScpFileFromVersionServer(String worldName, String cmdHost, String loginUser, String loginPwd, 
			String remotePath, String localPath){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		//总是先创建本地目录
		File parentFile = new File(localPath).getParentFile();
		LogUtil.printLog("mkdir -p " + parentFile.toString().replace("\\", "/") + "/");
		Mkdir(worldName, cmdHost, parentFile.toString().replace("\\", "/") + "/");
		
		//连上目标服务器,再从版本服务器抄文件
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser,  loginPwd, 
				"scp -r -P " + LinuxRemoteCommandUtil.GetSSHPort() + " " 
				+ worldProp.getProperty("releaseVersionUser") + "@" + worldProp.getProperty("releaseVersionHost") 
				+ ":" + remotePath + " " + localPath);
	}
	
	/**
	 * 从版本服务器复制文件
	 */
	public static void copyFileFromVersionServer(String worldName, String cmdHost, String remotePath, String localPath)
	{
		//读本地配置文件,找出版本服务器配置
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		ScpFileFromVersionServer(worldName, cmdHost, 
				deployProp.getProperty("linuxUserName"), deployProp.getProperty("linuxUserPwd"), remotePath, localPath);
	}
	
	/**
	 * 从版本服务器复制世界下的版本文件
	 * @param remotePath 不需要传入完整的路径.其根是版本服务器下的世界
	 */
	private static void copyWorldFileFromVersionServer(String worldName, String cmdHost, String remotePath, String localPath)
	{
		//读本地配置文件,找出版本服务器配置
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//连上目标服务器,再从版本服务器抄文件
		ScpFileFromVersionServer(worldName, cmdHost, 
				deployProp.getProperty("linuxUserName"), deployProp.getProperty("linuxUserPwd"), 
				BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + remotePath, localPath);
	}
	
	/**
	 * 创建目录,一般的创建目录可以调用这个,如果是特殊会被公用的请另外写一个方法来处理
	 */
	public static void Mkdir(String worldName, String cmdHost, String path){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String loginUser = deployProp.getProperty("linuxUserName").trim();
		String loginPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd,
				"mkdir -p " + path + "/");
	}
	
	/**
	 * 创建root db
	 * 导root db tables
	 */
	public static void CreateAndImportRootDB(String worldName){
		LogUtil.printLog("CreateDatabase root starting");
		DeployCommandUtil.CreateRootDatabase(worldName);
		LogUtil.printLog("CreateDatabase root completely");
		
		LogUtil.printLog("gen rootDB starting");
		DeployCommandUtil.ImportRootDatabase(worldName);
		LogUtil.printLog("gen rootDB completely");
	}
	
	/**
	 * 创建Root DB
	 */
	private static void CreateRootDatabase(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String mysqlHost = worldProp.getProperty("rootDBHost");
		String mysqlPort = worldProp.getProperty("rootDBPort");
		String dbName = worldProp.getProperty("rootDBName");
		
		CreateDatabase(worldName, mysqlHost, mysqlPort, dbName);
	}
	
	public static void CreateAndImportOperateDB(String worldName){
		LogUtil.printLog("CreateDatabase operate starting");
		DeployCommandUtil.CreateOperateDatabase(worldName);
		LogUtil.printLog("CreateDatabase operate completely");
		
		LogUtil.printLog("gen operateDB starting");
		DeployCommandUtil.ImportOperateDatabase(worldName);
		LogUtil.printLog("gen operateDB completely");
	}
	
	/**
	 * 创建报表DB
	 */
	public static void CreateReportDatabase(String worldName){
		LogUtil.printLog("CreateReportDatabase|start");
		
		String mysqlHost = BilinGameWorldConfig.getStatDBHost(worldName);
		String mysqlPort = BilinGameWorldConfig.getStatDBPort(worldName);
		String dbName = BilinGameWorldConfig.getStatReportDBName(worldName);
		
		CreateDatabase(worldName, mysqlHost, mysqlPort, dbName);
		
		LogUtil.printLog("CreateReportDatabase|end");
	}

	/**
	 * 创建运营DB
	 */
	private static void CreateOperateDatabase(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String mysqlHost = worldProp.getProperty("operateDBHost");
		String mysqlPort = worldProp.getProperty("operateDBPort");
		String dbName = worldProp.getProperty("operateDBName");
		
		CreateDatabase(worldName, mysqlHost, mysqlPort, dbName);
	}
	
	/**
	 * 创建Game DB
	 */
	public static void CreateGameDatabase(String worldName, int zoneId){
		String mysqlHost = BilinGameWorldConfig.getDBHostByZoneId(worldName, zoneId);
		String mysqlPort = BilinGameWorldConfig.getDBPortByZoneId(worldName, zoneId);
		String dbName = BilinGameWorldConfig.getDBNameByZoneId(zoneId);
		
		CreateDatabase(worldName, mysqlHost, mysqlPort, dbName);
	}
	
	/**
	 * 执行MySQL语句
	 */
	public static void ExecSQLStatement(String worldName, String mysqlHost, String mysqlPort, String dbName, String sqlStr){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		LinuxRemoteCommandUtil.runCmd(deployProp.getProperty("releaseVersionHost"), 22, 
				deployProp.getProperty("releaseVersionUser").trim(), deployProp.getProperty("releaseVersionPwd").trim(),
				"mysql -h" + mysqlHost + " --default-character-set=utf8 -u" + deployProp.getProperty("mysqlUser") + " -p" + deployProp.getProperty("mysqlPwd") 
				+ " -P" + mysqlPort + " " + dbName
				+ " -e '" + sqlStr + "'");
	}
	
	/**
	 * 创建数据库
	 * 创建数据库的操作都是通过Version Server终端来操作
	 */
	public static void CreateDatabase(String worldName, String mysqlHost, String mysqlPort, String dbName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		LinuxRemoteCommandUtil.runCmd(deployProp.getProperty("releaseVersionHost"), 22, 
				deployProp.getProperty("releaseVersionUser").trim(), deployProp.getProperty("releaseVersionPwd").trim(),
				"mysql -h " + mysqlHost + " --default-character-set=utf8 -u" + deployProp.getProperty("mysqlUser") + " -p" + deployProp.getProperty("mysqlPwd") 
				+ " -P" + mysqlPort + " "
				+ " -e 'CREATE DATABASE IF NOT EXISTS " + dbName + "'");
	}
	
	/**
	 * 导GAME DB
	 */
	public static void ImportGameDatabase(String worldName, int zoneId, String sqlFile){
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String mysqlHost = BilinGameWorldConfig.getDBHostByZoneId(worldName, zoneId);
		String mysqlPort = BilinGameWorldConfig.getDBPortByZoneId(worldName, zoneId);
		String dbName = zoneConfig.getDbName();
		
		ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, sqlFile);
	}
	
	/**
	 * 导ROOT DB
	 */
	private static void ImportRootDatabase(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String mysqlHost = worldProp.getProperty("rootDBHost");
		String mysqlPort = worldProp.getProperty("rootDBPort");
		String dbName = worldProp.getProperty("rootDBName");
		
		String sqlFileCreateDB = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/createworlddb.sql";
		ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, sqlFileCreateDB);
		
		String sqlFileInsertDB = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/insertworlddb.sql";
		ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, sqlFileInsertDB);
	}
	
	/**
	 * 导运营DB
	 */
	private static void ImportOperateDatabase(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String mysqlHost = worldProp.getProperty("operateDBHost");
		String mysqlPort = worldProp.getProperty("operateDBPort");
		String dbName = worldProp.getProperty("operateDBName");

		String sqlFileCreateDB = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/createoperate.sql";
		ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, sqlFileCreateDB);
		
		String sqlFileCreateFromConfigDB = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/createoperatefromconfig.sql";
		ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, sqlFileCreateFromConfigDB);
		
		String sqlFileInsertFromConfigDB = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/insertoperatefromconfig.sql";
		ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, sqlFileInsertFromConfigDB);
	}
	
	/**
	 * 从sql文件导数据库
	 */
	public static void ImportDatabase(String worldName, String mysqlHost, String mysqlPort, String dbName, String sqlFile){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		LinuxRemoteCommandUtil.runCmd(deployProp.getProperty("releaseVersionHost"), 22, 
				deployProp.getProperty("releaseVersionUser").trim(), deployProp.getProperty("releaseVersionPwd").trim(),
				"mysql -h" + mysqlHost + " --default-character-set=utf8 -u" + deployProp.getProperty("mysqlUser") + " -p" + deployProp.getProperty("mysqlPwd") 
				+ " -P" + mysqlPort + " " + dbName
				+ " < " + sqlFile + " 2>&1;");
	}

	/**
	 * 从版本服务器scp World.properties到本机一致的目录(/data/bilin/opconf)
	 */
	public static void ScpWorldPropertiest(String worldName, String cmdHost){
		CreateOpConfRoot(worldName, cmdHost);
		
		String remotePath = "/World.properties";
		String localPath = BilinGameWorldConfig.opConfRoot + "/";
		
		copyWorldFileFromVersionServer(worldName, cmdHost, remotePath, localPath);
	}
	
	/**
	 * 从版本服务器scp gecaoshoulie_servercodegen.jar到目标机器(可以是Game Server,也可以是其它机,所以这里传入host)
	 * @param worldName
	 */
	public static void ScpServerCodeGenJar4MapServer(String worldName, String cmdHost, String releaseTag){
		String localPath = BilinGameWorldConfig.getJavaLib4MapServerPath() + "/" + I18NUtil.getServercodegenJarName();
		
		ScpServerCodeGenJar(worldName, cmdHost, releaseTag, localPath);
	}
	
	private static void ScpServerCodeGenJar(String worldName, String cmdHost, String releaseTag, String localPath){
		String remotePath = "/" + releaseTag + "/" + I18NUtil.getServercodegenJarName();
		
		copyWorldFileFromVersionServer(worldName, cmdHost, remotePath, localPath);
	}
	
	/**
	 * 创建客户端release目录,仅限客户端构建调用
	 */
	public static void CreateClientReleaseTagRoot(String worldName, Properties clientDeployProp,
			String tag){
		String path = BilinGameWorldConfig.releaseVersionRoot + "/" + worldName + "/forClient/" + tag;
		LinuxRemoteCommandUtil.runCmd(clientDeployProp.getProperty("releaseVersionHost"), 22, 
				clientDeployProp.getProperty("releaseVersionUser").trim(), clientDeployProp.getProperty("releaseVersionPwd").trim(),
				"mkdir -p " + path + "/");
	}
	
	/**
	 * 创建 /data/bilin/opconf 目录
	 */
	public static void CreateOpConfRoot(String worldName, String cmdHost){
		String path = BilinGameWorldConfig.opConfRoot;
		Mkdir(worldName, cmdHost, path);
	}
	
	/**
	 * 创建跳过监控的占位文件
	 * /data/bilin/opconf/skip_zoneId.conf
	 */
	public static void CreateSkipFile(String worldName, int zoneId){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		CreateOpConfRoot(worldName, deployConfig.server_public_ip);
		
		String loginUser = deployProp.getProperty("linuxUserName").trim();
		String loginPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		String skipFile = BilinGameWorldConfig.opConfRoot + "/skip_" + zoneId + ".conf";
		LinuxRemoteCommandUtil.runCmd(deployConfig.server_public_ip, 22, loginUser, loginPwd,
				"touch " + skipFile);
	}

	/**
	 * 删除跳过监控的占位文件
	 */
	public static void RemoveSkipFile(String worldName, int zoneId){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String loginUser = deployProp.getProperty("linuxUserName").trim();
		String loginPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		String skipFile = BilinGameWorldConfig.opConfRoot + "/skip_" + zoneId + ".conf";
		LinuxRemoteCommandUtil.runCmd(deployConfig.server_public_ip, 22, loginUser, loginPwd,
				"rm -f " + skipFile);
	}
	
	/**
	 * 在版本服务器添加skip_zoneId.conf文件,给监控中心跳过
	 */
	public static void CreateSkipFileOnVersionServer(String [] zoneArray, Properties deployProp)
	{
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		Session versionServerSSHSession = LinuxRemoteCommandUtil.getSSHSession(deployProp.getProperty("releaseVersionHost"), 22, linuxUserName, linuxUserPwd);
		for (String item : zoneArray)
		{
			String skipFile = BilinGameWorldConfig.opConfRoot + "/skip_" + Integer.valueOf(item) + ".conf";
			LinuxRemoteCommandUtil.runCmd(versionServerSSHSession, "touch " + skipFile);
		}
		versionServerSSHSession.disconnect();
	}
	
	/**
	 * 添加退出钩子,用于在程序退出时,在版本服务器中 
	 * 1.添加reload文件
	 * 2.删除skip_zoneId.conf
	 */
	public static void AddHookRemoveSkipFileOnVersionServerOnAppExit(String [] zoneArray, Properties deployProp)
	{
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				LogUtil.printLog("RemoveSkipFileOnVersionServerOnAppExit|");
				Session versionServerSSHSession = LinuxRemoteCommandUtil.getSSHSession(deployProp.getProperty("releaseVersionHost"), 22, linuxUserName, linuxUserPwd);
				for (String item : zoneArray)
				{
					// 先添加reload文件,用于监控中心
					String reloadFile = BilinGameWorldConfig.opConfRoot + "/reload_" + Integer.valueOf(item) + ".conf";
					LinuxRemoteCommandUtil.runCmd(versionServerSSHSession, "touch " + reloadFile);
					
					// 再删除skip文件
					String skipFile = BilinGameWorldConfig.opConfRoot + "/skip_" + Integer.valueOf(item) + ".conf";
					LinuxRemoteCommandUtil.runCmd(versionServerSSHSession, "rm -f " + skipFile);
				}
				versionServerSSHSession.disconnect();
			}
		});
	}

	/**
	 * 在Version Server 创建 /data/bilin/release_version/worldName 目录
	 */
	public static void CreateReleaseVersionRoot(String worldName, String cmdHost){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String path = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName);
		
		LinuxRemoteCommandUtil.runCmd(deployProp.getProperty("releaseVersionHost"), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"mkdir -p " + path + "/");
	}
	
	/**
	 * 在Version Server 创建/data/biln/release_version/worldName/tag 目录
	 */
	public static void CreateReleaseVersionTagRoot(String worldName, String tag){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String path = BilinGameWorldConfig.getWorldReleaseTagRoot(worldName, tag);
		
		LinuxRemoteCommandUtil.runCmd(deployProp.getProperty("releaseVersionHost"), 
				22, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				"mkdir -p " + path + "/");
	}
	
	/**
	 * 在目标机器 创建 /data/bilin/worldName/tmp 目录
	 */
	public static void CreateWorldTmpRoot(String worldName, String cmdHost){
		String path = BilinGameWorldConfig.getWorldTmpRoot(worldName);
		Mkdir(worldName, cmdHost, path);
	}
	
	/**
	 * 在Version Server创建世界的tmp目录
	 * @param worldName
	 */
	public static void CreateVersionServerWorldTmpRoot(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String path = BilinGameWorldConfig.getWorldTmpRoot(worldName);
		
		LinuxRemoteCommandUtil.runCmd(deployProp.getProperty("releaseVersionHost"), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"mkdir -p " + path + "/");
	}
	
	/**
	 * 在Version Server 创建服务器版本latest的tag
	 */
	public static void CreateReleaseVersionLatestTag(String worldName, String tag){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String versionHost = deployProp.getProperty("releaseVersionHost");
		String versionUser = deployProp.getProperty("releaseVersionUser");
		String versionPwd = deployProp.getProperty("releaseVersionPwd");
		
		String latestTagPath = BilinGameWorldConfig.getWorldReleaseTagRoot(worldName, "latest");
		LinuxRemoteCommandUtil.runCmd(versionHost, 22, versionUser, versionPwd, 
				"rm -rf " + latestTagPath + ";");
		
		LinuxRemoteCommandUtil.runCmd(versionHost, 22, versionUser, versionPwd, 
				"mkdir -p " + latestTagPath + ";");
		
		LinuxRemoteCommandUtil.runCmd(versionHost, 22, versionUser, versionPwd,
				"cp " + BilinGameWorldConfig.getWorldReleaseTagRoot(worldName, tag) + "/* " + latestTagPath + "/;");
	}
	
}

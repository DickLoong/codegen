package com.lizongbo.codegentool.world;

import java.io.File;
import java.util.Properties;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.SCPUtil;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;

/**
 * 在Jenkins中用到的各个scp命令在这里
 * 服务器发布构建的scp基本上是和Version Server交互的
 * 客户端的构建则除了和Version Server也和Game Server(.pb.bytes)有交互
 */
public class ScpCommandUtil {
	
	public static void ScpTo(String host, String login, String pwd, String localFile, String remoteFile){
		SCPUtil.doSCPTo(localFile, login, pwd,  host, remoteFile);
	}
	
	public static void ScpFrom(String host, String login, String pwd, String remoteFile, String localFile){
		SCPUtil.doSCPFrom(login, pwd, host, remoteFile, localFile);
	}
	
	/**
	 * 复制游戏服务器的pb.bytes到本地,仅限于客户端构建使用
	 */
	public static void ScpPbBytesFromGameServer(String worldName, Properties clientDeployProp, String localPath){
		String remotePath = clientDeployProp.getProperty("pbBytesPath");
		
		LogUtil.printLog("ScpPbBytesFromGameServer|" 
				+ clientDeployProp.getProperty("linuxUserName") + "|" 
				+ clientDeployProp.getProperty("linuxHost") + "|" + remotePath);
		SCPUtil.doSCPFrom(clientDeployProp.getProperty("linuxUserName"), clientDeployProp.getProperty("linuxUserPwd"), 
				clientDeployProp.getProperty("linuxHost"), 
				remotePath, localPath);
	}
	
	/**
	 * 复制版本服务器的客户端相关文件(pb.bytes/ab.zip/apk/ipa)到本地,仅限于客户端构建使用
	 */
	public static void ScpClientFileFromReleaseVersionServer(String worldName, Properties clientDeployProp, String localPath, String tag){
		String remotePath = BilinGameWorldConfig.releaseVersionRoot + "/" + worldName + "/forClient/" + tag;
		
		SCPUtil.doSCPFrom(clientDeployProp.getProperty("releaseVersionUser"), clientDeployProp.getProperty("releaseVersionPwd"), 
				clientDeployProp.getProperty("releaseVersionHost"), 
				remotePath + "/" + new File(localPath).getName(), localPath);
	}
	
	/**
	 * 复制本地客户端相关文件(pb.bytes/ab.zip/apk/ipa)到版本服务器,仅限于客户端构建使用
	 */
	public static void ScpClientFileToReleaseVersionServer(String worldName, Properties clientDeployProp, String localPath, String tag){
		String remotePath = BilinGameWorldConfig.releaseVersionRoot + "/" + worldName + "/forClient/" + tag;
		
		SCPUtil.doSCPTo(localPath, 
				clientDeployProp.getProperty("releaseVersionUser"), 
				clientDeployProp.getProperty("releaseVersionPwd"), 
				clientDeployProp.getProperty("releaseVersionHost"),
				remotePath + "/" + new File(localPath).getName());
	}
	
	/**
	 * 复制TServer到版本服务器
	 */
	public static void ScpTServerToVersionServe(String worldName){
		String localGameZoneFile = I18NUtil.worldRootDir + "/" + worldName + "/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
		String remoteGameZoneFile = "/TServer_Gamezone.csv";
		scpWorldFileToVersionServer(worldName, localGameZoneFile, remoteGameZoneFile);
		
		String localWarZoneFile = I18NUtil.worldRootDir + "/" + worldName + "/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Warzone(战区服务区).csv";
		String remoteWarZoneFile = "/TServer_Warzone.csv";
		scpWorldFileToVersionServer(worldName, localWarZoneFile, remoteWarZoneFile);
	}
	
	/**
	 * 复制世界World.properties到版本服务器
	 */
	public static void ScpWorldPropToVersionServer(String worldName){
		String savePath = "/tmp/" + worldName;
		String gameZoneSavePath = savePath + "/World.properties";
		String remoteFile = "/World.properties";
		scpWorldFileToVersionServer(worldName, gameZoneSavePath, remoteFile);
		
		ScpTServerToVersionServe(worldName);
		
		String softwarefFilesZip = "/tmp/gecaoshoulie_version_server_softwares.zip";
		String remoteSoftwareFile = "/tmp/VersionServerSoftwareFiles.zip";
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ScpTo(deployProp.getProperty("releaseVersionHost"), deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), softwarefFilesZip, remoteSoftwareFile);
		
		DeployCommandUtil.UnzipVersionServerFile(worldName, remoteSoftwareFile, BilinGameWorldConfig.softwareRoot);
	}
	
	/**
	 * 复制创建世界数据库表的SQL文件,到版本服务器
	 * 复制INSERT世界数据库表的SQL文件到版本服务器
	 */
	public static void ScpCreateInsertWorldDbSql(String worldName){
		String localFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/worldSqls/createworlddb.sql";
		String remoteFile = "/createworlddb.sql";
		
		scpWorldFileToVersionServer(worldName, localFile, remoteFile);
		
		String insertDBLocalFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/worldSqls/insertworlddb.sql";
		String insertDBRemoteFile = "/insertworlddb.sql";
		
		scpWorldFileToVersionServer(worldName, insertDBLocalFile, insertDBRemoteFile);
		
		String worldFilesZipFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/worldSqlFile.zip";
		if (new File(worldFilesZipFile).exists()){
			scpWorldFileToVersionServer(worldName, worldFilesZipFile, "/" + new File(worldFilesZipFile).getName());
		}
	}
	
	/**
	 * 复制GameDB的数据表文件到版本服务器的/tmp目录
	 */
	public static void ScpGameDbSqlToTmp(String worldName, String sqlFilesZipFile, String remoteFile){
		//读本地配置文件,找出版本服务器配置
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		SCPUtil.doSCPTo(sqlFilesZipFile, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				deployProp.getProperty("releaseVersionHost"),
				remoteFile);
	}
	
	/**
	 * 复制创建运营数据库表的SQL文件,到版本服务器
	 * 复制INSERT运营数据库表的SQL文件到版本服务器
	 * 同时复制运营的war包到版本服务器
	 */
	public static void ScpCreateInsertOperateDbSql(String worldName){
		String localFile = I18NUtil.getWorldOperateSQLRoot(worldName) + "/createoperatefromconfig.sql";
		String remoteFile = "/createoperatefromconfig.sql";
		scpWorldFileToVersionServer(worldName, localFile, remoteFile);
		
		String insertDBLocalFile = I18NUtil.getWorldOperateSQLRoot(worldName) + "/insertoperatefromconfig.sql";
		String insertDBRemoteFile = "/insertoperatefromconfig.sql";
		scpWorldFileToVersionServer(worldName, insertDBLocalFile, insertDBRemoteFile);
		
		//复制平台要求的表
		String operateDBLocalFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_operateconsole/Sqls/createoperate.sql";
		String operateDBRemoteFile = "/createoperate.sql";
		scpWorldFileToVersionServer(worldName, operateDBLocalFile, operateDBRemoteFile);
		
		//复制平台的war包
		LogUtil.printLog("scp gecaoshoulie_operateconsole.war|start");
		String operateWarLocalFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_operateconsole/dist/gecaoshoulie_operateconsole.war";
		String operateWarRemoteFile = "/gecaoshoulie_operateconsole.war";
		scpWorldFileToVersionServer(worldName, operateWarLocalFile, operateWarRemoteFile);
		LogUtil.printLog("scp gecaoshoulie_operateconsole.war|end");
	}
	
	/**
	 * 复制支付服务器的war包到版本服务器
	 */
	public static void ScpPayServerWarFile(String worldName){
		//复制支付服务器的war包
		LogUtil.printLog("scp gecaoshoulie_pay_server.war|start");
		String payServerWarLocalFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_pay_server/dist/gecaoshoulie_pay_server.war";
		String payServerWarRemoteFile = "/gecaoshoulie_pay_server.war";
		scpWorldFileToVersionServer(worldName, payServerWarLocalFile, payServerWarRemoteFile);
		LogUtil.printLog("scp gecaoshoulie_pay_server.war|end");
	}
	
	/**
	 * Jenkins将本地文件传到版本服务器
	 */
	public static void scpWorldFileToVersionServer(String worldName, String localFile, String remoteFile){
		//读本地配置文件,找出版本服务器配置
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		SCPUtil.doSCPTo(localFile, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				deployProp.getProperty("releaseVersionHost"),
				BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + remoteFile);
	}
	
	/**
	 * Jenkins将本地文件传到版本服务器,世界的临时目录下
	 * @param remoteFile 要添加/前缀
	 */
	public static void scpWorldFileToVersionServerTmp(String worldName, String localFile, String remoteFile){
		//读本地配置文件,找出版本服务器配置
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
				
		SCPUtil.doSCPTo(localFile, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				deployProp.getProperty("releaseVersionHost"),
				BilinGameWorldConfig.getWorldTmpRoot(worldName) + remoteFile);
	}
	
	/**
	 * Jenkins将本地构建好的服务器文件传到版本服务器
	 */
	public static void scpServerReleaseFileToVersionServer(String worldName, String releaseTag, String localFile, String remoteFile){
		//读本地配置文件,找出版本服务器配置
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		SCPUtil.doSCPTo(localFile, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				deployProp.getProperty("releaseVersionHost"),
				BilinGameWorldConfig.getWorldReleaseTagRoot(worldName, releaseTag) + "/" + remoteFile);
	}
	
	/**
	 * Jenkins将本地构建好的服务器文件传到GameServer
	 */
	public static void scpToGameServer(String worldName, int zoneId, String localFile, String remoteFile){
		//读本地配置文件,找出版本服务器配置
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		SCPUtil.doSCPTo(localFile, loginUser, loginPwd, zoneConfig.server_public_ip,
				remoteFile);
	}
}

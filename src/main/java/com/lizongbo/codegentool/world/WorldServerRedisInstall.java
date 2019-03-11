package com.lizongbo.codegentool.world;

import java.util.Properties;

import com.lizongbo.codegentool.DeployUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.csv2db.UploadAllPoolAndHosts;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 世界服务器Redis安装操作
 * 包括以下操作 

1.	读取版本服务器下相应世界的TServer到本地
3.	装Redis
5.	生成hosts(hostfiles目录)
7.	Redis连接配置文件(poolcfgfiles/redispoolcfg)
8.	更新所有GS机器配置文件(hosts/dbpoolcfg/redispoolcfg)及Hosts

 * @author linyaoheng
 *
 */
public class WorldServerRedisInstall {

	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateZoneConfig(worldName, zoneId);
		
		worldServerRedisInstall(worldName, zoneId);
	}
	
	/**
	 * 世界服务器的Redis安装,支持多台世界服务器
	 */
	private static void worldServerRedisInstall(String worldName, int zoneId){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("WorldServer|Redis|installation|worldName|" + worldName);
		
		DeployUtil.ShowEnv();
		
		//读取配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//1.	读取版本服务器下相应世界的TServer到本地
		BilinGameWorldConfig.downloadTserverCSV(worldName, deployProp);
		
		ServerDeployConfig worldServerConfig = BilinGameWorldConfig.getTheWorldServerConfig(worldName, zoneId);
		if (null == worldServerConfig){
			LogUtil.printLogErr("No|WorldServer|For|" + zoneId);
			System.exit(1);
		}
		
		//2.	装Redis
		LogUtil.printLog("Install Redis starting");
		DeployCommandUtil.InstallWorldServerRedis(worldName, worldServerConfig);
		LogUtil.printLog("Install Redis completely");
		
		//3.	生成各Server目录结构,启动脚本
		//生成所有的模板文件,复制所有的模板文件到server
		LogUtil.printLog("GenAndCopyServerConfig starting");
		GenAndCopyWorldServerConfig(worldName, worldServerConfig);
		LogUtil.printLog("GenAndCopyServerConfig completely");
		
		//4.	生成所有配置文件,并更新所有GS机器,Common Server,World Server配置文件(hosts/dbpoolcfg/redispoolcfg)及Hosts
		LogUtil.printLog("GenAndUploadServerConfigWithHosts starting");
		UploadAllPoolAndHosts.GenAndUploadServerConfigWithHosts(worldName);
		LogUtil.printLog("GenAndUploadServerConfigWithHosts completely");
		
		//启动世界服务器Redis
		DeployCommandUtil.StartWorldServerRedis(worldName, worldServerConfig);
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("WorldServer|Redis|installation|completely|worldName|" + worldName + "|usedTime|" + (endTime - startTime));
	}
	
	public static void GenAndCopyWorldServerConfig(String worldName, ServerDeployConfig serverConfig){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("生成模板文件|worldName|" + worldName + "|开始执行");
		
		String mainClassName = "net.bilinkeji.gecaoshoulie.mgameprotorpc.worldserver";
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName"); 
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		String worldServerFilePath = I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/serverconfs/javaserver_world_server/" + serverConfig.zone_id;
	
		String bl_startShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/single_server_bl_start.sh", "UTF-8");
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");
	
		I18NUtil.SERVER_TYP serverType = SERVER_TYP.WORLD_SERVER;
	
		bl_startShText = ServerContainerGenTool.replaceSingleServerConfigVar(worldName, serverConfig, bl_startShText, mainClassName, serverType);
		killShText = ServerContainerGenTool.replaceSingleServerConfigVar(worldName, serverConfig, killShText, mainClassName, serverType);

		String saveStartFile = worldServerFilePath + "/bin/bl_start.sh";
		GameCSV2DB.writeFile(saveStartFile, bl_startShText);
	
		String saveKillFile= worldServerFilePath + "/bin/kill.sh";
		GameCSV2DB.writeFile(saveKillFile, killShText);
	
		DeployCommandUtil.Mkdir(worldName, serverConfig.server_public_ip, BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/bin");
		DeployCommandUtil.Mkdir(worldName, serverConfig.server_public_ip, BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/endorsed");
		DeployCommandUtil.Mkdir(worldName, serverConfig.server_public_ip, BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/businesslog");
		DeployCommandUtil.Mkdir(worldName, serverConfig.server_public_ip, BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/log");
	
		//上传文件
		ScpCommandUtil.ScpTo(serverConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				saveStartFile, 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/bin/bl_start.sh");
		DeployCommandUtil.chmodAddX(worldName, serverConfig.server_public_ip, 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/bin/bl_start.sh");
	
		ScpCommandUtil.ScpTo(serverConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				saveKillFile, 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/bin/kill.sh");
		DeployCommandUtil.chmodAddX(worldName, serverConfig.server_public_ip, 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/bin/kill.sh");
	
		ScpCommandUtil.ScpTo(serverConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				ServerContainerGenTool.javaserverconftempdir + "/endorsed/jmxext_lizongbo.jar", 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/endorsed/jmxext_lizongbo.jar");
		
		GenRedisDir(worldName, serverConfig, serverConfig.redis1_port);
		GenRedisDir(worldName, serverConfig, serverConfig.redis2_port);
		GenRedisDir(worldName, serverConfig, serverConfig.redis3_port);
		GenRedisDir(worldName, serverConfig, serverConfig.redis4_port);
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("GenAndCopyWorldServerConfig|worldName|" + worldName + "|usedtime|" + (endTime - startTime));
	}
	
	/**
	 * 按端口来生成目录
	 */
	private static void GenRedisDir(String worldName, ServerDeployConfig serverConfig, int port){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName"); 
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		String redisPath = I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/serverconfs/javaserver_world_server/" + serverConfig.zone_id  + "/redis_" + port;
		
		//读取配置文件
		Properties prop = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		GameCSV2DB.writeFile(redisPath + "/bin/bl_start.sh", 
				replaceStartScriptTpl(worldName, serverConfig.zone_id, port));
		ScpCommandUtil.ScpTo(serverConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				redisPath + "/bin/bl_start.sh", 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/redis_" + port + "/bin/bl_start.sh");
		
		GameCSV2DB.writeFile(redisPath + "/bin/kill.sh", 
				replaceKillScriptTpl(worldName, serverConfig.zone_id, prop, port));
		ScpCommandUtil.ScpTo(serverConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				redisPath + "/bin/kill.sh", 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/redis_" + port + "/bin/kill.sh");
		
		GameCSV2DB.writeFile(redisPath + "/conf/redis_" + port + ".conf", 
				replaceConfTpl(worldName, serverConfig.zone_id, prop, port));
		ScpCommandUtil.ScpTo(serverConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				redisPath + "/conf/redis_" + port + ".conf", 
				BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/redis_" + port + "/conf/redis_" + port + ".conf");
		
		DeployCommandUtil.Mkdir(worldName, serverConfig.server_public_ip, BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/redis_" + port + "/data_" + port);
		DeployCommandUtil.Mkdir(worldName, serverConfig.server_public_ip, BilinGameWorldConfig.getWorldServerRoot(serverConfig.zone_id) + "/redis_" + port + "/log");
	}
	
	private static String replaceStartScriptTpl(String worldName, int zoneId, int port){
		String scriptContent = "" 
		+ "#!/bin/sh\n"
		+ "#Configurations injected by install_server below....\n"
		+ "\n\n"
		+ "#kill first\n"
		+ "sh " + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/bin/kill.sh\n"
		+ "\n\n"
		+ "EXEC=" + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/bin/redis-server\n"
		+ "CLIEXEC=" + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/bin/redis-cli\n"
		+ "PIDFILE=/var/tmp/redis_" + port + ".pid\n"
		+ "CONF=\"" + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/conf/redis_" + port + ".conf\"\n"
		+ "REDISPORT=\"" + port + "\"\n"
		+ "echo \"Starting Redis server...\"\n"
		+ "$EXEC $CONF\n"
		+ "\n";
		
		return scriptContent;
	}
	
	private static String replaceKillScriptTpl(String worldName, int zoneId, Properties prop, int port){
		String scriptContent = "" 
				+ "#!/bin/sh\n"
				+ "#Configurations injected by install_server below....\n"
				+ "\n"
				+ "EXEC=" + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/bin/redis-server\n"
				+ "CLIEXEC=" + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/bin/redis-cli\n"
				+ "PIDFILE=/var/tmp/redis_" + port + ".pid\n"
				+ "CONF=\"" + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/conf/redis_" + port + ".conf\"\n"
				+ "REDISPORT=\"" + port + "\"\n"
				+ "if [ ! -f $PIDFILE ]\n"
				+ "then\n"
				+ "    exit 0\n"
				+ "fi\n"
				
				+ "PID=$(cat $PIDFILE)\n"
				+ "echo \"Stopping ...\"\n"
				+ "$CLIEXEC -p $REDISPORT -a " + prop.getProperty("redisPwd") + " shutdown\n"
				
	            + "while [ -x /proc/${PID} ]\n"
	            + "do\n"
	            + "    echo \"Waiting for Redis to shutdown ...\"\n"
        		+ "    sleep 1\n"
        		+ "done\n"
	            + "echo \"Redis stopped\"\n"
            	+ "\n";
	            
				return scriptContent;
	}
	
	private static String replaceConfTpl(String worldName, int zoneId, Properties prop, int port){
		String confTpl = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/redis_config/redis_port.conf", "UTF-8");
		
		confTpl = confTpl.replaceAll("port 44001", "port " + port);
		confTpl = confTpl.replaceAll("pidfile /var/tmp/redis_44001.pid", "pidfile /var/tmp/redis_" + port + ".pid");
		confTpl = confTpl.replaceAll("logfile /usr/local/apps/redis_44001/log/redis_44001.log", "logfile " + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/log/redis_" + port + ".log");
		confTpl = confTpl.replaceAll("dir /usr/local/apps/redis_44001/data_44001", "dir " + BilinGameWorldConfig.getWorldServerRoot(zoneId) + "/redis_" + port + "/data_" + port);
		confTpl = confTpl.replaceAll("requirepass 8246ee36b541cb07f33a62538103fc6b", "requirepass " + prop.getProperty("redisPwd"));
		
		return confTpl;
	}
	
}

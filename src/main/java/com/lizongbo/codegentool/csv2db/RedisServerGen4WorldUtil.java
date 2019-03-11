package com.lizongbo.codegentool.csv2db;

import java.util.Properties;

import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 生成Redis(4个)服务器目录结构和启动/停止文件
 * @author linyaoheng
 *
 */
public class RedisServerGen4WorldUtil {
	public static void main(String[] args) {
		genWorldRedisServer("bilin_Ksyun", 40002);
	}

	/**
	 * 按世界读取世界目录下的csv生成对应的Redis(4个)服务器目录结构和文件
	 */
	public static void genWorldRedisServer(String worldName, int zoneId) {
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		
		// 开始解析世界内的csv，生成对应的Redis(4个)服务器目录结构和文件
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		GenRedisDir(worldName, deployConfig.zone_id, deployConfig.redis1_port);
		GenRedisDir(worldName, deployConfig.zone_id, deployConfig.redis2_port);
		GenRedisDir(worldName, deployConfig.zone_id, deployConfig.redis3_port);
		GenRedisDir(worldName, deployConfig.zone_id, deployConfig.redis4_port);
	}
	
	/**
	 * 按端口来生成目录
	 */
	private static void GenRedisDir(String worldName, int zoneId, int port){
		String redisPath = getRedisPath(worldName, zoneId, port);
		
		//读取配置文件
		Properties prop = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		GameCSV2DB.writeFile(redisPath + "/bin/bl_start.sh", 
				replaceStartScriptTpl(worldName, zoneId, port));
		
		GameCSV2DB.writeFile(redisPath + "/bin/kill.sh", 
				replaceKillScriptTpl(worldName, zoneId, prop, port));
		
		GameCSV2DB.writeFile(redisPath + "/conf/redis_" + port + ".conf", 
				replaceConfTpl(worldName, zoneId, prop, port));
		
		GameCSV2DB.createDir(redisPath + "/data_" + port);
		GameCSV2DB.createDir(redisPath + "/log");
	}
	
	/**
	 * 取得Redis文件名
	 */
	public static String getRedisPath(String worldName, int zoneId, int port){
		return I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/serverconfs/" + zoneId + "/redis_" + port;
	}
	
	private static String replaceStartScriptTpl(String worldName, int zoneId, int port){
		String scriptContent = "" 
		+ "#!/bin/sh\n"
		+ "#Configurations injected by install_server below....\n"
		+ "\n\n"
		+ "#kill first\n"
		+ "sh " + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/bin/kill.sh\n"
		+ "\n\n"
		+ "EXEC=" + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/bin/redis-server\n"
		+ "CLIEXEC=" + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/bin/redis-cli\n"
		+ "PIDFILE=/var/tmp/redis_" + port + ".pid\n"
		+ "CONF=\"" + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/conf/redis_" + port + ".conf\"\n"
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
				+ "EXEC=" + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/bin/redis-server\n"
				+ "CLIEXEC=" + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/bin/redis-cli\n"
				+ "PIDFILE=/var/tmp/redis_" + port + ".pid\n"
				+ "CONF=\"" + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/conf/redis_" + port + ".conf\"\n"
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
		confTpl = confTpl.replaceAll("logfile /usr/local/apps/redis_44001/log/redis_44001.log", "logfile " + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/log/redis_" + port + ".log");
		confTpl = confTpl.replaceAll("dir /usr/local/apps/redis_44001/data_44001", "dir " + BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/redis_" + port + "/data_" + port);
		confTpl = confTpl.replaceAll("requirepass 8246ee36b541cb07f33a62538103fc6b", "requirepass " + prop.getProperty("redisPwd"));
		
		return confTpl;
	}

}

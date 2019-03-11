package com.lizongbo.codegentool.csv2db;

import java.util.List;
import java.util.Properties;

import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 生成连接池文件
 * @author linyaoheng
 *
 */
public class PoolCfgGen4WorldUtil {
	public static void main(String[] args) {
		//genWorldPoolCfg("bilin_102");
	}

	/**
	 * 按世界读取世界目录下的csv生成对应的Pool文件
	 * 
	 * @param worldName
	 */
	public static void genWorldPoolCfg(String worldName) {
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		// 开始解析世界内的csv，生成对应的连接池文件
		List<ServerDeployConfig> allDeployConfigs = BilinGameWorldConfig.getAllDeployConfigs(worldName);
		
		//读取配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String dbPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/poolcfgfiles/dbpoolcfg";
		ServerContainerGenTool.delAllFile(dbPath);
		
		String indexFile = dbPath + "/dbpoolcfg.txt";
		GameCSV2DB.writeFile(indexFile, "only index");
		
		String redisPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/poolcfgfiles/redispoolcfg";
		ServerContainerGenTool.delAllFile(redisPath);
		
		String redisIndexFile = redisPath + "/redispoolcfg.txt";
		GameCSV2DB.writeFile(redisIndexFile, "only index");
		
		for (ServerDeployConfig item : allDeployConfigs){
			//生成Redis Pool文件
			if (BilinGameWorldConfig.isGameServer(item.zone_id)){
				writeDBPoolPropFile(worldName, item.zone_id, 
						BilinGameWorldConfig.getDBPortByZoneId(worldName, item.zone_id), item.getDbName());
				
				GameCSV2DB.writeFile(getRedisFileName(worldName, "dbcache", item.zone_id),
						replaceRedisCfg(deployProp, "dbcache", item.zone_id, item.redis1_port));
				GameCSV2DB.writeFile(getRedisFileName(worldName, "counter", item.zone_id),
						replaceRedisCfg(deployProp, "counter", item.zone_id, item.redis2_port));
				GameCSV2DB.writeFile(getRedisFileName(worldName, "ranklist", item.zone_id),
						replaceRedisCfg(deployProp, "ranklist", item.zone_id, item.redis3_port));
				GameCSV2DB.writeFile(getRedisFileName(worldName, "common", item.zone_id),
						replaceRedisCfg(deployProp, "common", item.zone_id, item.redis4_port));
			}
			else if (BilinGameWorldConfig.isWorldServer(item.zone_id)){
				//2017-08-18 linyaoheng 世界服务器也需要DB
				writeDBPoolPropFile(worldName, item.zone_id, 
						BilinGameWorldConfig.getWorldServerDBPort(worldName, item.zone_id), item.getDbName());
				
				GameCSV2DB.writeFile(getRedisFileName(worldName, "dbcache", item.zone_id),
						replaceRedisCfg(deployProp, "dbcache", item.zone_id, item.redis1_port));
				GameCSV2DB.writeFile(getRedisFileName(worldName, "counter", item.zone_id),
						replaceRedisCfg(deployProp, "counter", item.zone_id, item.redis2_port));
				GameCSV2DB.writeFile(getRedisFileName(worldName, "ranklist", item.zone_id),
						replaceRedisCfg(deployProp, "ranklist", item.zone_id, item.redis3_port));
				GameCSV2DB.writeFile(getRedisFileName(worldName, "common", item.zone_id),
						replaceRedisCfg(deployProp, "common", item.zone_id, item.redis4_port));
			}
		}
		
		writeDBPoolPropFile(worldName, 0, worldProp.getProperty("rootDBPort"), worldProp.getProperty("rootDBName"));
		writeDBPoolPropFile(worldName, 1, worldProp.getProperty("operateDBPort"), worldProp.getProperty("operateDBName"));
		
		writeDBPoolPropFile(worldName, "/mgamedb_gecaoshoulie_stat_report.properties", BilinGameWorldConfig.getStatDBHost(worldName),
				BilinGameWorldConfig.getStatDBPort(worldName), BilinGameWorldConfig.getStatReportDBName(worldName), true);
		
		writeDBPoolPropFile(worldName, "/mgamedb_gecaoshoulie_slave.properties", BilinGameWorldConfig.getRootDBSlaveHost(worldName),
				BilinGameWorldConfig.getRootDBPort(worldName), worldProp.getProperty("rootDBName"), false);
	}
	
	/**
	 * 
	 * @param dbFileName 根目录为poolcfgfiles/dbpoolcfg,所以指定相对文件位置即可
	 */
	private static void writeDBPoolPropFile(String worldName, String dbFileName, String dbHost, String dbPort, String dbName,
			boolean allowMultiQueries) {
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//生成DB Pool文件
		//db pool的根目录
		String dbPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/poolcfgfiles/dbpoolcfg";
		dbFileName = dbPath + dbFileName;
		
		StringBuilder dbPropertiesContent = new StringBuilder();
		dbPropertiesContent.append("driverClassName=com.mysql.jdbc.Driver\n");
		
		String url = "url=jdbc:mysql\\://" + dbHost + "\\:" + dbPort 
				+ "/" + dbName + "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
		if (allowMultiQueries){
			url += "&allowMultiQueries=true";
		}
		dbPropertiesContent.append(url + "\n");
		
		dbPropertiesContent.append("username=" + deployProp.getProperty("mysqlUser") + "\n");
		dbPropertiesContent.append("password=" + deployProp.getProperty("mysqlPwd") + "\n");
		dbPropertiesContent.append("validationQuery=select version()\n");
		dbPropertiesContent.append("testWhileIdle=true\n");
		
		GameCSV2DB.writeFile(dbFileName, dbPropertiesContent.toString());
	}
	
	private static void writeDBPoolPropFile(String worldName, int zoneId, String dbPort, String dbName) {
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//生成DB Pool文件
		String dbPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/poolcfgfiles/dbpoolcfg";
		String dbFileName = dbPath + "/dbpool_4maindb/dbpool_4maindb_zone" + zoneId + ".properties";
		
		StringBuilder dbPropertiesContent = new StringBuilder();
		dbPropertiesContent.append("driverClassName=com.mysql.jdbc.Driver\n");
		
		dbPropertiesContent.append("url=jdbc:mysql\\://mysqlservermaindb4zone" + zoneId + "\\:" + dbPort 
				+ "/" + dbName + "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8\n");
		
		dbPropertiesContent.append("username=" + deployProp.getProperty("mysqlUser") + "\n");
		dbPropertiesContent.append("password=" + deployProp.getProperty("mysqlPwd") + "\n");
		dbPropertiesContent.append("validationQuery=select version()\n");
		dbPropertiesContent.append("testWhileIdle=true\n");
		
		GameCSV2DB.writeFile(dbFileName, dbPropertiesContent.toString());
	}

	/**
	 * 取得Redis文件名
	 */
	private static String getRedisFileName(String worldName, String dbName, int zoneId){
		return I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/poolcfgfiles/redispoolcfg/redispool_4" + dbName + "/redispool_4" + dbName + "_zone" + zoneId + ".properties";
	}
	
	/**
	 * 替换Redis配置模板的内容
	 */
	private static String replaceRedisCfg(Properties prop, String dbName, int zoneId, int port){
		String redisPoolCfgTpl = 
				  "#dbppol cfg for zone 0\n"
				+ "#Tue Nov 17 11:05:33 CST 2015\n"
				+ "jmxEnabled=true\n"
				+ "testWhileIdle=true\n"
				+ "#lifo=\n"
				+ "numTestsPerEvictionRun=-1\n"
				+ "minEvictableIdleTimeMillis=60000\n"
				+ "#testOnReturn=\n"
				+ "#softMinEvictableIdleTimeMillis=\n"
				+ "#testOnCreate=\n"
				+ "jmxNameBase=redispool4" + dbName + "zone" + zoneId + "\n"
				+ "#blockWhenExhausted=\n"
				+ "timeBetweenEvictionRunsMillis=30000\n"
				+ "#maxWaitMillis=\n"
				+ "#testOnBorrow=\n"
				+ "#fairness=\n"
				+ "jmxNamePrefix=redispool4redis_server_" + dbName + "zone" + zoneId + "\n"
				+ "#evictionPolicyClassName=\n"
				+ "\n"
				+ "redisServer.host = redisserver" + dbName + "4zone" + zoneId + "\n"
				+ "redisServer.port = " + port + "\n"
				+ "redisServer.connectionTimeout = 2000\n"
				+ "redisServer.soTimeout = 2000\n"
				+ "redisServer.password = " + prop.getProperty("redisPwd") + "\n"
				+ "#redisServer.database = not use\n"
				+ "redisServer.clientName = jedis\n"
				
				+ "maxTotal = 1024\n"
				+ "maxIdle = 200\n"
				+ "minIdle = 15\n"
				
				+ "\n";
		
		return redisPoolCfgTpl;
	}
	
}

package com.lizongbo.codegentool.csv2db;

import java.util.List;

import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 生成hosts文件
 * @author linyaoheng
 *
 */
public class HostsGen4WorldUtil {
	public static void main(String[] args) {
		//genWorldHosts("bilin_Ksyun");
	}

	/**
	 * 按世界读取世界目录下的csv生成对应的Hosts文件
	 * 
	 * @param worldName
	 */
	public static void genWorldHosts(String worldName) {
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		// 开始解析世界内的csv，生成对应的hosts文件
		List<ServerDeployConfig> allDeployConfigs = BilinGameWorldConfig.getAllDeployConfigs(worldName);
		
		StringBuilder myHostsContent = new StringBuilder();
		myHostsContent.append("\n\n"); //多两个换行,避免旧文件没有换行引起的错误
		
		//RootDB指定对应的IP
		myHostsContent.append(BilinGameWorldConfig.getRootDBHost(worldName) + " mysqlservermaindb4zone0\n");
		
		//运营DB指定对应的IP
		myHostsContent.append(BilinGameWorldConfig.getOperateDBHost(worldName) + " mysqlservermaindb4zone1\n");
		
		//统计DB,报表DB
		myHostsContent.append(BilinGameWorldConfig.getStatDBHost(worldName) + " mysqlservermaindb4stats\n");
		
		for (ServerDeployConfig item : allDeployConfigs){
			if (BilinGameWorldConfig.isGameServer(item.zone_id)){
				myHostsContent.append(item.server_inner_ip + " gameserver4zone" + item.zone_id + "\n");
				
				//数据库可以指定对应的IP
				myHostsContent.append(BilinGameWorldConfig.getDBHostByZoneId(worldName, item.zone_id) + " mysqlservermaindb4zone" + item.zone_id + "\n");
				
				myHostsContent.append(item.server_inner_ip + " redisserverdbcache4zone" + item.zone_id + "\n");
				myHostsContent.append(item.server_inner_ip + " redisservercounter4zone" + item.zone_id + "\n");
				myHostsContent.append(item.server_inner_ip + " redisserverranklist4zone" + item.zone_id + "\n");
				myHostsContent.append(item.server_inner_ip + " redisservercommon4zone" + item.zone_id + "\n");	
			}
			else if (BilinGameWorldConfig.isWorldServer(item.zone_id)){
				myHostsContent.append(item.server_inner_ip + " gameserver4zone" + item.zone_id + "\n");
				
				//世界服务器DB指定对应的IP
				myHostsContent.append(BilinGameWorldConfig.getWorldServerDBHost(worldName, item.zone_id) + " mysqlservermaindb4zone" + item.zone_id + "\n");
				
				myHostsContent.append(item.server_inner_ip + " redisserverdbcache4zone" + item.zone_id + "\n");
				myHostsContent.append(item.server_inner_ip + " redisservercounter4zone" + item.zone_id + "\n");
				myHostsContent.append(item.server_inner_ip + " redisserverranklist4zone" + item.zone_id + "\n");
				myHostsContent.append(item.server_inner_ip + " redisservercommon4zone" + item.zone_id + "\n");
			}
		}
		
		String hostFilePath = getHostsPath(worldName);
		
		GameCSV2DB.writeFile(hostFilePath, myHostsContent.toString());
	}
	
	public static String getHostsPath(String worldName){
		return I18NUtil.worldRootDir + "/" + worldName + "/forServer/hostfiles/bilinhosts.txt";
	}
	
}

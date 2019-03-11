package com.lizongbo.codegentool.world;

import java.util.List;

import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;
import com.lizongbo.codegentool.world.porter.CommonServerPorter;

/**
 * 发布单个公共服务器
 * @author linyaoheng
 */
public class CommonServerSingleDeploy {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateZoneConfig(worldName, zoneId);
		
		List<ServerDeployConfig> commonServerHosts = BilinGameWorldConfig.getCommonServerHosts(worldName);
		for (ServerDeployConfig cs : commonServerHosts){
			if (cs.zone_id == zoneId)
			{
				new CommonServerPorter(worldName, cs).DeployVersion();
			}
		}
	}
	
}

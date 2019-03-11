package com.lizongbo.codegentool.world;

import java.util.List;
import java.util.Properties;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;

/**
 * 根据配置表来生成客户端的配置文件,如公共服务器等
 * @author linyaoheng
 *
 */
public class GenWorldClientConf {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		LogUtil.printLog("GenWorldClientConf|start");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		BilinGameWorldConfig.downloadTserverCSV(worldName, deployProp);
		
		//生成文件
		String saveConfigPath = I18NUtil.worldRootDir + "/" + worldName 
				+ "/forClient/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/Common/WorldServerConfig.cs";
		
		//内容
		StringBuilder sbContent = new StringBuilder();
		sbContent.append("using net.bilinkeji.gecaoshoulie.mgameproto.common;\n");
		sbContent.append("using System.Collections.Generic;\n");

		sbContent.append("namespace Bilinkeji.Common\n");
		sbContent.append("{\n");
		sbContent.append("/// <summary>\n");
		sbContent.append("/// 登录服务器列表.除了主干外,其它世界皆由客户端构建工具生成\n");
		sbContent.append("/// </summary>\n");
		sbContent.append("public class WorldServerConfig\n");
		sbContent.append("{\n");

		sbContent.append("public static List<StringIntMapEntry> allLoginServers = new List<StringIntMapEntry>()\n");
		sbContent.append("{\n");
		
		List<ServerDeployConfig> commonServerHosts = BilinGameWorldConfig.getCommonServerHosts(worldName);
		for (ServerDeployConfig item : commonServerHosts){
			sbContent.append("new StringIntMapEntry() { key = \"" + item.server_public_ip + "\", value = " + item.game_server_port + " },\n");
		}
		
		sbContent.append("};\n");

		sbContent.append("}\n");
		sbContent.append("}\n");
		
		GameCSV2DB.writeFile(saveConfigPath, sbContent.toString());
		
		LogUtil.printLog("GenWorldClientConf|end");
	}
	
}

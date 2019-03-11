package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;

/**
 * 功能是生成世界表的SQL
 * 如果没有设置世界配置表的,则不上传到版本服务器
 * @author linyaoheng
 *
 */
public class GenTheWorldSQL {

	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		
		GenWorldSQL(worldName);
	}
	
	/**
	 * 生成游戏服的SQL
	 */
	private static void GenWorldSQL(String worldName){
		//根据CSV生成相应的SQL
		SQLGen4WorldUtil.createOnlyWorldSql(worldName);
		
		// 尝试上传sql文件
		if (BilinGameWorldConfig.ExistsGameEnvProp(worldName)){
			ScpCommandUtil.ScpCreateInsertWorldDbSql(worldName);
		}
	}
	
}

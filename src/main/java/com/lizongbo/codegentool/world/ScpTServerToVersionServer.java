package com.lizongbo.codegentool.world;

/**
 * 复制TServer到版本服务器
 * @author linyaoheng
 *
 */
public class ScpTServerToVersionServer {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
//		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
//		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		ScpCommandUtil.ScpTServerToVersionServe(worldName);
	}
	
}

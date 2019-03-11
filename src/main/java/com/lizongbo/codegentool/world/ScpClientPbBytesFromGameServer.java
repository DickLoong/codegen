package com.lizongbo.codegentool.world;

import java.util.Properties;

/**
 * 从版本服务机器拉取指定TAG的AB, .pb.bytes到打包时指定的目录
 *
 */
public class ScpClientPbBytesFromGameServer {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		String pbBytesPath = System.getenv("PB_TYPES_PATH");
		
		if (worldName == null || worldName.isEmpty()){
			System.out.println("env|" + System.getenv());
			System.out.println("please set worldName");
			System.out.println("please set PB_TYPES_PATH");
			System.exit(1);
		}
		
		Properties clientDeployProp = BilinGameWorldConfig.getClientBuildProp(worldName);
		
		//将.pb.bytes拉回版本服务器(scpClientTagFromReleaseVersion.pl)
		ScpCommandUtil.ScpPbBytesFromGameServer(worldName, clientDeployProp, pbBytesPath);
	}
	
}

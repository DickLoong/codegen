package com.lizongbo.codegentool.world.worldserver;

import java.util.List;

import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;
import com.lizongbo.codegentool.world.porter.WorldServerPorter;

public class WorldServerDeploy {
	
	public static void main(String[] args){
		String worldName = System.getenv("worldName");
		String releaseTag = System.getenv("releaseTag");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);

		//sqlfiles.zip只解压一次
		String sqlFilesZipFile = BilinGameWorldConfig.getWorldReleaseTagRoot(worldName, releaseTag) + "/sqlfiles.zip";
		DeployCommandUtil.UnzipVersionServerFile(worldName, sqlFilesZipFile, BilinGameWorldConfig.appsRoot);
		
		List<ServerDeployConfig> allWorldServers = BilinGameWorldConfig.getWorldServerHost(worldName);
		for (ServerDeployConfig ws : allWorldServers){
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() 
				{
					new WorldServerPorter(worldName, ws).DeployVersion();
				}
			});
			
			try {
				Thread.sleep(500);
				t.start();
			} catch (InterruptedException e) {
				e.printStackTrace();
				
				System.exit(1);
			}
		}
	}

}

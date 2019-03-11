package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.world.porter.MonitorCenterPorter;

/**
 * 构建+发布监控中心
 * @author linyaoheng
 */
public class MonitorCenterReleaseVersion {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		MonitorCenterPorter monitorCenterPorter = new MonitorCenterPorter(worldName);
		monitorCenterPorter.BuildVersion();
		monitorCenterPorter.DeployVersion();
	}
	
}

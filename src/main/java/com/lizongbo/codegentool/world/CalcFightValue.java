package com.lizongbo.codegentool.world;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 战力重算
 * @author linyaoheng
 *
 */
public class CalcFightValue {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		String zoneIds = System.getenv("zoneId");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		calcFightValue(worldName, zoneIds);
	}
	
	/**
	 * 战力重算
	 */
	private static void calcFightValue(String worldName, String zoneIds){
		//校验所有的zoneId先
		String[] zoneArray = StringUtil.split(zoneIds, " ");
		if (zoneArray == null){
			LogUtil.printLogErr("please type the zoneIds");
			System.exit(1);
		}
		
		//读取配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = BilinGameWorldConfig.getHostsMap(worldName, zoneArray); //同个物理机的用同一个线程来跑
		
		for (Entry<String, List<ServerDeployConfig>> hostItem : serverHostsMap.entrySet()){
			List<ServerDeployConfig> tmpList = hostItem.getValue();
			String hostIP = hostItem.getKey();

			Thread t = new Thread(new Runnable() {
				
				public void run() {
					boolean hasGS = false;
					
					for (ServerDeployConfig dc : tmpList){
						if (BilinGameWorldConfig.isGameServer(dc.zone_id))
						{
							hasGS = true;
							
							String cmd = "/data/bilin/apps/jdk/bin/java -Djavaserver.home=/data/bilin/apps/"
									+ dc.zone_id + "/javaserver_gecaoshoulie_server"
									+ " -cp \"/data/bilin/apps/" + dc.zone_id + "/javaserver_gecaoshoulie_server/WEB-INF/classes"
									+ ":/data/bilin/apps/" + dc.zone_id + "/javaserver_gecaoshoulie_server/WEB-INF/lib/*\""
									+ " net.bilinkeji.gecaoshoulie.mgameprotorpc.hessian.impl.CalcAttrHessianServiceImpl"
									+ " " + dc.server_inner_ip + " " + dc.game_server_hessian_port;
							LinuxRemoteCommandUtil.runCmd(dc.server_public_ip, 22, linuxUserName, linuxUserPwd, cmd);
						}
					}
					
					if (hasGS)
					{
						LogUtil.printLog("Login|to|" + hostIP + "|and|run|cmd|tail -f /data/bilin/apps/*/javaserver_gecaoshoulie_server/log/GameZoneCalcAttr*");
					}
				}
			});
			
			try {
				Thread.sleep(800);
				t.start();
			} catch (InterruptedException e) {
				e.printStackTrace();
				
				//如果有出错则直接退出了
				System.exit(1);
			}
		}
	}
}

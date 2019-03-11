package com.lizongbo.codegentool.world;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 发布游戏服务器,仅仅发布代码
 * @author linyaoheng
 *
 *
1.	填写归档的版本号
2.	scp复制所选版本的gecaoshoulie_game_server_pub.zip到机器上/data/bilin/世界/tmp
4.	scp复制所选版本的gecaoshoulie_map_server_pub.zip到机器上/data/bilin/世界/tmp
5.	scp复制所选版本的gecaoshoulie_servercodegen.jar到机器上/data/bilin/software/javalib4mapserver
5.	scp复制所选版本的sqlfiles.zip到机器上/data/bilin/世界/tmp
6.	停止所有Server
a)	停止GS
b)	停止FS(1-4)
c)	停止MS
7.	发布GS
a)	解压gecaoshoulie_game_server_pub.zip文件
b)	将javalib4server的jar复制到GS的WEB-INF/lib目录
8.	发布FS(同时操作4个FS)
a)	解压gecaoshoulie_game_server_pub.zip文件
b)	将javalib4server的jar复制到FS的WEB-INF/lib目录
9.	发布MS
a)	解压gecaoshoulie_map_server_pub.zip文件
b)	将javalib4server的jar复制到MS的WEB-INF/lib目录
c)	将javalib4mapserver的jar复制到MS的WEB-INF/lib目录
11.	启动所有Server
a)	启动GS
c)	启动FS(1-4)
e)	启动MS

 */
public class ServerDeployCodeOnly {

	public static void main(String[] args) {
		// 部署到相应的服务器
		String worldName = System.getenv("worldName");
		String zoneIds = System.getenv("zoneId");
		String releaseTag = System.getenv("releaseTag");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		//校验所有的zoneId先
		String[] zoneArray = StringUtil.split(zoneIds, " ");
		if (zoneArray == null){
			LogUtil.printLogErr("please type the zoneIds");
			System.exit(1);
		}
		
		//读取配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//1.	读取版本服务器下相应世界的TServer到本地
		LogUtil.printLog("downloadTserverCSV starting");
		BilinGameWorldConfig.downloadTserverCSV(worldName, deployProp);
		LogUtil.printLog("downloadTserverCSV completely");
		
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = BilinGameWorldConfig.getHostsMap(worldName, zoneArray); //同个物理机的用同一个线程来跑
		
		// 添加退出钩子,用于在程序退出时,在版本服务器中 1.添加reload文件; 2.删除skip_zoneId.conf
		DeployCommandUtil.AddHookRemoveSkipFileOnVersionServerOnAppExit(zoneArray, deployProp);
		
		// 在版本服务器添加skip_zoneId.conf文件,给监控中心跳过
		DeployCommandUtil.CreateSkipFileOnVersionServer(zoneArray, deployProp);
		
		for (Entry<String, List<ServerDeployConfig>> hostItem : serverHostsMap.entrySet())
		{
			List<ServerDeployConfig> tmpList = hostItem.getValue();
			
			Thread t = new Thread(new Runnable() {
				public void run() {
					for (ServerDeployConfig tmpConfig : tmpList){
						int zoneId = tmpConfig.zone_id;
						
						long startTime = System.currentTimeMillis();
						
						//改为调用远程的Perl命令
						RemotePerlCommandUtil.execPerlScript(worldName, tmpConfig.server_public_ip, "remoteGameServerDeploy.pl",
								zoneId, releaseTag);
						
						LogUtil.printLog("OOOOOOOOOOOK|zoneId|" + zoneId + "|usedtime|" + (System.currentTimeMillis() - startTime));
					}
				}
			});
			
			try {
				Thread.sleep(500);
				t.start();
			} catch (InterruptedException e) {
				e.printStackTrace();
				
				//如果有出错则直接退出了
				System.exit(1);
			}
		}
	}
	
}

package com.lizongbo.codegentool.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 发布游戏服务器,(配置表+代码)
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
10.	导数据库表
a)	用mysql命令导入createdb4user_$worldName.sql到GS的数据库
b)	用mysql命令导入dropandcreatedb_$worldName.sql到GS的数据库
c)	用mysql命令导入insertdb_$worldName.sql到GS的数据库
d)	用mysql命令导入$zoneId/alterdb _$worldName_$zoneId.sql到GS的数据库
e)	用mysql命令导入createdb4user_$worldName.sql到0区的数据库
f)	用mysql命令导入dropandcreatedb_$worldName.sql到0区的数据库
g)	用mysql命令导入insertdb_$worldName.sql到0区的数据库
h)	用mysql命令导入$zoneId/alterdb _$worldName_$zoneId.sql到0区的数据库
11.	启动所有Server
a)	启动GS
c)	启动FS(1-4)
e)	启动MS
12.	自动测试

 */
public class ServerDeploy {

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
		
		
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = new TreeMap<>(); //同个物理机的用同一个线程来跑
		
		for (String item : zoneArray){
			ServerDeployConfig tmpDeployConfig = BilinGameWorldConfig.getDeployConfig(worldName, Integer.valueOf(item));
			
			//数据校验
			BilinGameWorldConfig.validateZoneConfig(worldName, tmpDeployConfig);
			
			String tmpIP = tmpDeployConfig.server_public_ip;
			
			if (!serverHostsMap.containsKey(tmpIP)){
				serverHostsMap.put(tmpIP, new ArrayList<ServerDeployConfig>());
			}
			List<ServerDeployConfig> tmpList = serverHostsMap.get(tmpIP);
			tmpList.add(tmpDeployConfig);
		}
		
		String sqlFilesZipFile = BilinGameWorldConfig.getWorldReleaseTagRoot(worldName, releaseTag) + "/sqlfiles.zip";
		DeployCommandUtil.UnzipVersionServerFile(worldName, sqlFilesZipFile, BilinGameWorldConfig.appsRoot);
		
		// 添加退出钩子,用于在程序退出时,在版本服务器中 1.添加reload文件; 2.删除skip_zoneId.conf
		DeployCommandUtil.AddHookRemoveSkipFileOnVersionServerOnAppExit(zoneArray, deployProp);
		
		// 在版本服务器添加skip_zoneId.conf文件,给监控中心跳过
		DeployCommandUtil.CreateSkipFileOnVersionServer(zoneArray, deployProp);
		
		for (Entry<String, List<ServerDeployConfig>> hostItem : serverHostsMap.entrySet()){
			List<ServerDeployConfig> tmpList = hostItem.getValue();
			
			Thread t = new Thread(new Runnable() {
				public void run() {
					for (ServerDeployConfig tmpConfig : tmpList){
						int zoneId = tmpConfig.zone_id;
						
						long startTime = System.currentTimeMillis();
						
//					10.	导数据库表
//					a)	用mysql命令导入createdb4user_$worldName.sql到GS的数据库
//					b)	用mysql命令导入dropandcreatedb_$worldName.sql到GS的数据库
//					c)	用mysql命令导入insertdb_$worldName.sql到GS的数据库
						LogUtil.printLog("pubSqlFiles starting|zoneId|" + zoneId);
						pubSqlFiles(worldName, zoneId, releaseTag);
						LogUtil.printLog("pubSqlFiles completely|zoneId|" + zoneId + "|usedtime|" + (System.currentTimeMillis() - startTime));
						
						//改为调用远程的Perl命令
						RemotePerlCommandUtil.execPerlScript(worldName, tmpConfig.server_public_ip, "remoteGameServerDeploy.pl",
								zoneId, releaseTag);
						LogUtil.printLog("OOOOOOOOOOOK|zoneId|" + zoneId);
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
	
	public static void pubSqlFiles(String worldName, int zoneId, String releaseTag) {
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		DeployCommandUtil.ImportGameDatabase(worldName, zoneConfig.zone_id, BilinGameWorldConfig.appsRoot + "/sqlfiles/createdb4user_" + worldName + ".sql");
		DeployCommandUtil.ImportGameDatabase(worldName, zoneConfig.zone_id, BilinGameWorldConfig.appsRoot + "/sqlfiles/dropandcreatedb_" + worldName + ".sql");
		DeployCommandUtil.ImportGameDatabase(worldName, zoneConfig.zone_id, BilinGameWorldConfig.appsRoot + "/sqlfiles/insertdb_" + worldName + ".sql");
	}
	
}

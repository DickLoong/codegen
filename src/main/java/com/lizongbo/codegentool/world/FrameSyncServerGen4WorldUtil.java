package com.lizongbo.codegentool.world;

import java.io.File;

import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.db2java.GenAll;

/**
 * 生成帧同步目录结构和启动/停止文件
 * @author linyaoheng
 *
 */
public class FrameSyncServerGen4WorldUtil {
	public static void main(String[] args) {
		//genWorldFrameSyncServer("bilin_Local", 40001);
	}

	/**
	 * 按世界读取世界目录下的csv生成对应的帧同步目录结构和文件
	 */
	public static void genWorldFrameSyncServer(String worldName, int zoneId) {
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		
		// 开始解析世界内的csv，生成对应的帧同步目录结构和文件
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		genFrameSyncServerBySeq(worldName, zoneId, 1, deployConfig);
		genFrameSyncServerBySeq(worldName, zoneId, 2, deployConfig);
		genFrameSyncServerBySeq(worldName, zoneId, 3, deployConfig);
		genFrameSyncServerBySeq(worldName, zoneId, 4, deployConfig);
	}

	private static void genFrameSyncServerBySeq(String worldName, int zoneId, int seq, 
			ServerDeployConfig deployConfig) {
		String mainClassName = "net.bilinkeji.gecaoshoulie.mgameprotorpc.framesync.FrameSyncServer";
		
		String frameSyncServerFilePath = I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/serverconfs/" + zoneId + "/javaserver_gecaoshoulie_framesync_server" + seq;
		
		String bl_startShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/bl_start.sh", "UTF-8");
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");
		
		I18NUtil.SERVER_TYP serverType = SERVER_TYP.UNKNOWN;
		switch (seq){
		case 1:
			serverType = SERVER_TYP.FRAME_SYNC_SERVER1;
			break;
		case 2:
			serverType = SERVER_TYP.FRAME_SYNC_SERVER2;
			break;
		case 3:
			serverType = SERVER_TYP.FRAME_SYNC_SERVER3;
			break;
		case 4:
			serverType = SERVER_TYP.FRAME_SYNC_SERVER4;
			break;
		}
		
		
		bl_startShText = ServerContainerGenTool.replaceConfigVar(bl_startShText, deployConfig, mainClassName, serverType);
		
		killShText = ServerContainerGenTool.replaceConfigVar(killShText, deployConfig, mainClassName, serverType);

		GameCSV2DB.writeFile(frameSyncServerFilePath + "/bin/bl_start.sh", bl_startShText);
		GameCSV2DB.writeFile(frameSyncServerFilePath + "/bin/kill.sh", killShText);
		
		GameCSV2DB.createDir(frameSyncServerFilePath + "/businesslog");
		GameCSV2DB.createDir(frameSyncServerFilePath + "/conf");

		GameCSV2DB.copyFile(new File(ServerContainerGenTool.javaserverconftempdir + "/endorsed/jmxext_lizongbo.jar"), 
				new File(frameSyncServerFilePath + "/endorsed/jmxext_lizongbo.jar"));

		GameCSV2DB.createDir(frameSyncServerFilePath + "/log");
		GameCSV2DB.createDir(frameSyncServerFilePath + "/WEB-INF");
	}

}

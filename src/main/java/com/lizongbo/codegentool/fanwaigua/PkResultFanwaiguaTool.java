package com.lizongbo.codegentool.fanwaigua;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lizongbo.codegentool.SCPUtil;
import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.tools.LinuxRemoteShellUtil;
import com.lizongbo.codegentool.tools.StringUtil;

public class PkResultFanwaiguaTool {

	public static void main(String[] args) {

		System.setProperty("DEPLOY_SSH_PORT", "13922");
		String startTimeLong = LocalDate.now().toString().replaceAll("-", "") + "000000";
		String csvFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N/cn_release/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
		if (new java.io.File(csvFile).exists()) {

		} else {
			csvFile = "D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N/cn_release/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
		}
		System.out.println("csv==" + csvFile);
		List<String[]> list = CSVUtil.getDataFromCSV2(csvFile);
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String shellCmd = "cat /etc/hosts|grep zone|wc -l";
		shellCmd = "sudo apt-get --yes install ntp";
		shellCmd = "timedatectl";// 查看网络校准时间
		Set<String> userIdSet = new TreeSet<String>();
		for (int k = 4; k < list.size(); k++) {
			String[] arr = list.get(k);
			if (arr.length > 0) {
				String host = CSVUtil.getColValue("server_public_ip", arr, list);
				String innerHost = CSVUtil.getColValue("server_inner_ip", arr, list);
				String zoneId = CSVUtil.getColValue("zone_id", arr, list);
				int platformInt = Integer.parseInt(CSVUtil.getColValue("platform", arr, list));
				int zoneIdInt = Integer.parseInt(zoneId);
				System.out.println("zoneId======" + zoneIdInt);
				if (zoneIdInt > 40000 && zoneIdInt < 40401) {
					String path = "/data/bilin/apps/" + zoneIdInt + "/javaserver_gecaoshoulie_server/businesslog/";
					String classFilePath = "/data/bilin/apps/" + zoneIdInt
							+ "/javaserver_gecaoshoulie_server/WEB-INF/classes/net/bilinkeji/gecaoshoulie/mgameprotorpc/";
					String classFileName = "PkResultReportFanwaigua.class";
					String classFileLocal = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_server/WEB-INF/classes/net/bilinkeji/gecaoshoulie/mgameprotorpc/"
							+ classFileName;
					if (new File(classFileLocal).exists()) {
						SCPUtil.doSCPTo(classFileLocal, userName, password, host, classFilePath + classFileName);
					}
					String javaCmd = "/data/bilin/apps/jdk/bin/java "
							+ "-Djavaserver.home=/data/bilin/apps/40115/javaserver_gecaoshoulie_server/bin/../ "
							+ "-Djavaserver.zoneid=40115 -Djavaserver.internal_ip=10.0.2.12 "
							+ "-Djavaserver.public_id=120.92.77.130 " + "-Djavaserver.public_ip=120.92.77.130 "
							+ "-Djavaserver.frame_sync_server1_port=35115 "
							+ "-Djavaserver.frame_sync_server_hessian1_port=11115 "
							+ "-Djavaserver.frame_sync_server2_port=36115 "
							+ "-Djavaserver.frame_sync_server_hessian2_port=12115 "
							+ "-Djavaserver.frame_sync_server3_port=37115 "
							+ "-Djavaserver.frame_sync_server_hessian3_port=13115 "
							+ "-Djavaserver.frame_sync_server4_port=38115 "
							+ "-Djavaserver.frame_sync_server_hessian4_port=14115 "
							+ "-Djavaserver.scene_map_server=120.92.77.130 " + "-Djavaserver.scene_map_port=39115 "
							+ "-Djavaserver.scene_map_hessian_port=15115 "
							+ "-Djavaserver.javaserver_hessian_port=10115 " + "-Djavaserver.mainport=40115 "
							+ "-Djava.endorsed.dirs=/data/bilin/apps/40115/javaserver_gecaoshoulie_server/bin/..//endorsed "
							+ "-cp /data/bilin/apps/40115/javaserver_gecaoshoulie_server/bin/..//WEB-INF/lib/*"
							+ ":/data/bilin/apps/40115/javaserver_gecaoshoulie_server/bin/..//WEB-INF/classes/"
							+ ":/data/bilin/apps/40115/javaserver_gecaoshoulie_server/bin/..//WEB-INF/dist/* "
							+ "-Dsun.net.http.allowRestrictedHeaders=true -Djava.awt.headless=true "
							+ "-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN "
							+ "-Duser.region=CN -Duser.timezone=Asia/Shanghai -Dio.netty.leakDetectionLevel=paranoid "
							+ "net.bilinkeji.gecaoshoulie.mgameprotorpc.PkResultReportFanwaigua " + path;
					javaCmd = javaCmd.replaceAll("40115", zoneIdInt + "");
					String str = LinuxRemoteShellUtil.runCmd(host, port, userName, password, javaCmd);
					str = str.replaceAll("\n\n", "\n").replaceAll("\n\n", "\n").replaceAll("\n\n", "\n");
					System.out.println(zoneIdInt + "|runedrs|" + str);
					String arrTmp[] = str.split("\n");
					for (String userIdTmp : arrTmp) {
						userIdTmp = userIdTmp.trim();
						if (userIdTmp.length() > 0 && !userIdTmp.startsWith("run")) {
							// int iTmp = Integer.parseInt(userIdTmp);
							userIdSet.add(userIdTmp);
						}
					}
				}
			}
		}

		System.out.println("userInfoSet=====" + userIdSet);
		Set<String> pureUserIdSet = new TreeSet<String>();
		for (String sss : userIdSet) {
			pureUserIdSet.add(sss.split("@")[0]);
		}
		String qrStr = StringUtil.join(";", pureUserIdSet);
		System.out.println("pureUserIdSet=====" + qrStr);
		try {
			String str = LinuxRemoteShellUtil.downloadUrlbyPOST(
					"http://120.92.119.100:8090/gecaoshoulie_operateconsole/stats/CheatingChecking.do",
					"outsourceBanUserIdList=" + qrStr, "quickli", "UTF-8");
			System.out.println(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

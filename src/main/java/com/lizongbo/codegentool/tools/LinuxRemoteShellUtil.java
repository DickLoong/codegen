package com.lizongbo.codegentool.tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.lizongbo.codegentool.LinuxUerInfo;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.MailTest;
import com.lizongbo.codegentool.SCPUtil;
import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;

public class LinuxRemoteShellUtil {

	static String[] hostsIps = new String[] { "120.92.119.7", "120.92.118.161", "120.92.119.123", "120.92.119.140",
			"120.92.119.100", "120.92.20.149", "120.92.35.9", "120.92.116.126", "120.92.116.13", "120.92.119.30",
			"120.92.118.82", "120.92.119.101", "120.92.118.16", "120.92.76.23", "120.92.119.91", "120.92.119.154",
			"120.92.102.164", "120.92.117.161", "120.92.42.192", "120.92.114.81", "120.92.118.52", "120.92.119.29",
			"120.92.118.177", "120.92.117.98", "120.92.115.59", "120.92.77.130", "120.92.93.54", "120.92.102.165",
			"120.92.119.51", "120.92.43.63", "120.92.76.133", "120.92.116.60", "120.92.35.182", "120.92.118.151" };

	// select reg_time_long from tuser4zoneserver_gameplayer where reg_time_long
	// >20170926000000 ;

	public static void main(String[] args) {

		// StringBuilder sb = new StringBuilder();
		// sb.append("\n今日Android登录泰奇用户数" +
		// getTodayLoginUserCount(1).replaceAll("dau", "").trim());
		// sb.append("\n今日iOS登录泰奇用户数" +
		// getTodayLoginUserCount(2).replaceAll("dau", "").trim());
		// sb.append("\n今日Android登录玩家数" + getTodayLoginPlayerCountMap(1));
		// sb.append("\n今日iOS登录玩家数" + getTodayLoginPlayerCountMap(2));
		// System.out.println(sb);
		// meixiaoshiTongji();
		fanwaigua();
	}

	public static void meixiaoshiTongji() {

		StringBuilder sb = new StringBuilder();
		// 再算今天到现在的量，再估算今日总量
		// 再列一下最近两小时每十分钟的进量
		String threeDayAgo = LocalDate.now().plusDays(-3).toString().replaceAll("-", "");
		System.out.println("threeDayAgo==" + threeDayAgo);
		String threeDayAgoStartTime = threeDayAgo + "000000";
		String zuotianStartTime = LocalDate.now().plusDays(-1).toString().replaceAll("-", "") + "000000";
		String zuotianEndTime = LocalDateTime.now().plusDays(-1).toString().replaceAll("-", "").replaceAll("T", "")
				.replaceAll(":", "").substring(0, 12) + "00";

		String todayStartTime = LocalDate.now().toString().replaceAll("-", "") + "000000";
		String todayEndTime = LocalDateTime.now().toString().replaceAll("-", "").replaceAll("T", "").replaceAll(":", "")
				.substring(0, 12) + "00";

		String todayLastHourStartTime = LocalDateTime.now().plusHours(-1).toString().replaceAll("-", "")
				.replaceAll("T", "").replaceAll(":", "").substring(0, 10) + "0000";
		String todayDateStr = LocalDate.now().toString().replaceAll("-", "");
		String zuotianDateStr = LocalDate.now().plusDays(-1).toString().replaceAll("-", "");
		System.out.println("昨日开始" + zuotianStartTime);
		System.out.println("昨日当前" + zuotianEndTime);
		System.out.println("今日开始" + todayStartTime);
		System.out.println("今日当前" + todayEndTime);
		System.out.println("今日上一小时" + todayLastHourStartTime);
		System.out.println("昨天" + zuotianDateStr);
		System.out.println("今天" + todayDateStr);
		int zuotianZhuceZongshu = 0;
		int zuotianZhuceHuanbishu = 0;
		int jintianZhuceHuanbishu = 0;
		// System.exit(0);
		// 先算Android最近三天的量
		sb.append(LocalDateTime.now() + "|截至当前,");

		Map<String, Integer> mapPlayerLoginCountAndroid = getTodayLoginPlayerCountMap(1);
		Map<String, Integer> mapPlayerLoginCountiOS = getTodayLoginPlayerCountMap(2);
		int playerLoginCountAndroid = 0;
		int playerLoginCountiOS = 0;
		for (int a : mapPlayerLoginCountAndroid.values()) {
			playerLoginCountAndroid = playerLoginCountAndroid + a;
		}
		for (int i : mapPlayerLoginCountiOS.values()) {
			playerLoginCountiOS = playerLoginCountiOS + i;
		}
		sb.append("\n今日Android登录泰奇用户数" + getTodayLoginUserCount(1).replaceAll("dau", "").trim() + ";");
		sb.append("\n今日iOS登录泰奇用户数" + getTodayLoginUserCount(1).replaceAll("dau", "").trim() + ";");
		sb.append("\n今日Android登录玩家数" + playerLoginCountAndroid);
		sb.append("\n今日iOS登录玩家数" + playerLoginCountiOS);
		for (int plat = 1; plat <= 2; plat++) {
			String platName = "Android";
			if (plat == 2) {
				platName = "iOS";
			}
			if (true) {
				Map<String, Integer> mapTodayHour = new TreeMap<String, Integer>(Collections.reverseOrder());
				mapTodayHour.putAll(mainShifenzhong(threeDayAgoStartTime, "", 1, plat));

				sb.append("\n" + platName + "最近三天注册玩家数:");
				for (String key : mapTodayHour.keySet()) {
					sb.append("" + key + "\t" + mapTodayHour.get(key) + "\n");
				}
				zuotianZhuceZongshu = mapTodayHour.getOrDefault(zuotianDateStr, 100);
			}
			{
				Map<String, Integer> mapTodayHour = new TreeMap<String, Integer>(Collections.reverseOrder());
				mapTodayHour.putAll(mainShifenzhong(zuotianStartTime, zuotianEndTime, 1, plat));
				mapTodayHour.putAll(mainShifenzhong(todayStartTime, todayEndTime, 1, plat));

				sb.append("|今日(" + todayStartTime + "-" + todayEndTime + ")与昨日(" + zuotianStartTime + "-"
						+ zuotianEndTime + ")的此刻对比:");
				for (String key : mapTodayHour.keySet()) {
					sb.append("" + key + "\t" + mapTodayHour.get(key) + "\n");
				}
				zuotianZhuceHuanbishu = mapTodayHour.getOrDefault(zuotianDateStr, 100);
				jintianZhuceHuanbishu = mapTodayHour.getOrDefault(todayDateStr, 100);
				long yugu = 1L * jintianZhuceHuanbishu * zuotianZhuceZongshu / zuotianZhuceHuanbishu;
				sb.append("昨日总数" + zuotianZhuceZongshu + ",环比昨日" + zuotianZhuceHuanbishu + ",此刻" + jintianZhuceHuanbishu
						+ ",预估今日" + yugu + "\n");
			}
		}
		sb.append("Android每区玩家登录数：\n");
		for (Map.Entry<String, Integer> e : mapPlayerLoginCountAndroid.entrySet()) {
			sb.append(e.getKey() + "\t" + e.getValue() + "\n");
		}
		sb.append("iOS每区玩家登录数：\n");
		for (Map.Entry<String, Integer> e : mapPlayerLoginCountiOS.entrySet()) {
			sb.append(e.getKey() + "\t" + e.getValue() + "\n");
		}
		System.out.println(sb);
		String text = sb.toString();
		String mailto[] = new String[] { "quickli@billionkj.com", "yaoheng@billionkj.com", "wangherong@billionkj.com",
				"gin@billionkj.com", "wubin@billionkj.com", "cys@billionkj.com" };
		MailTest.sendErrorQYWeixin(text.substring(0, 100), mailto);
		MailTest.sendErrorMail(text.substring(0, 100), text, mailto);
		// Map<String, String> mapZuotianHour =
		// mainShifenzhong("20170929000000", "", true);
		// mainzonghe(args);
		// hostsIps = new String[] { "120.92.119.101", "120.92.118.16",
		// "120.92.76.23" };
		// fanwaigua();
		// xiaofeiqueshi();
	}

	public static String getTodayLoginUserCount(int plat) {
		String host = "120.92.118.161";
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String shellCmd = "perl /data/bilin/script/pveCDCheck/pveCDCheck.pl";

		String todayStartTime = LocalDate.now().toString().replaceAll("-", "") + "000000";
		// 查修改数值秒杀的
		String sql = "select count(*) dau from tuser_taiqiuser where last_login_time_long >=" + todayStartTime
				+ " and last_login_client_info_json like \"%Android%\" limit 1";
		if (plat == 2) {
			sql = "select count(*) dau from tuser_taiqiuser where last_login_time_long >=" + todayStartTime
					+ " and last_login_client_info_json not like \"%Android%\" limit 1";

		}
		shellCmd = "mysql -u root -pmysqlpwdbilinkejinet -h 10.0.2.3 mgamedb_gecaoshoulie  -e \'" + sql + "\'";

		// |awk '{print $1}'

		String str = runCmd(host, port, userName, password, shellCmd);
		System.out.println("执行结果:" + str);
		return str;
	}

	public static Map<String, Integer> getTodayLoginPlayerCountMap(int platType) {
		String startTimeLong = LocalDate.now().toString().replaceAll("-", "") + "000000";
		String csvFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N/cn_release/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
		List<String[]> list = CSVUtil.getDataFromCSV2(csvFile);
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String shellCmd = "cat /etc/hosts|grep zone|wc -l";
		shellCmd = "sudo apt-get --yes install ntp";
		shellCmd = "timedatectl";// 查看网络校准时间
		int allCount = 0;
		Map<String, Integer> map = new TreeMap<String, Integer>();
		for (int k = 4; k < list.size(); k++) {
			String[] arr = list.get(k);
			if (arr.length > 0) {
				String host = CSVUtil.getColValue("server_public_ip", arr, list);
				String innerHost = CSVUtil.getColValue("server_inner_ip", arr, list);
				String zoneId = CSVUtil.getColValue("zone_id", arr, list);
				int platformInt = Integer.parseInt(CSVUtil.getColValue("platform", arr, list));
				int zoneIdInt = Integer.parseInt(zoneId);
				System.out.println("服务区id==" + zoneIdInt);
				if (platformInt == platType) {
					String sql = "select count(*) dau from tuser4zoneserver_gameplayer where last_enter_time_long >= "
							+ startTimeLong;
					shellCmd = "mysql -h " + innerHost
							+ " --default-character-set=utf8 -uroot -pmysqlpwdbilinkejinet -P3306 mgamedb_gecaoshoulie_maindb_zone"
							+ zoneId + " -e \"" + sql + "\"";
					String str = runCmd(host, port, userName, password, shellCmd);
					System.out.println("执行结果:" + str);
					int playerLoginCout = Integer.parseInt(str.replaceAll("dau", "").trim());
					allCount = allCount + playerLoginCout;
					map.put(zoneId, playerLoginCout);
				}
			}
		}
		return map;
	}

	public static void xiaofeiqueshi() {
		System.setProperty("DEPLOY_SSH_PORT", "13922");
		for (String host : hostsIps) {
			int port = 13922;
			String userName = "ubuntu";
			String password = "XJliehen!@17";
			String shellCmd = "perl /data/bilin/script/pveCDCheck/pveCDCheck.pl";
			// 查修改数值秒杀的
			shellCmd = "zcat /data/bilin/backup_center/server_log/game_server/2017-10-01/40*/ConsumptionGoodsInfoPbRedisHelper.log.2017-10-01_00.gz "
					+ "|grep MONEY_DIAMOND|grep -E \"2017-10-01T00:0|2017-10-01T00:1\"|awk -F \"|\" '{if($4<0) print $1\" \"$2\" \"$3\" \"$4 }' >/tmp/xiaofei.log";

			// |awk '{print $1}'

			String str = runCmd(host, port, userName, password, shellCmd);
			System.out.println("执行结果:" + str);
			SCPUtil.doSCPFrom(userName, password, host, "/tmp/xiaofei.log", "/tmp/xiaofei" + host + ".log");
			// GameCSV2DB.appendFile("/tmp/zuanshixiaofei.log", str);
		}
	}

	public static void fanwaigua() {
		StringBuilder sb = new StringBuilder();
		for (String host : hostsIps) {
			int port = 13922;
			String userName = "ubuntu";
			String password = "XJliehen!@17";
			String shellCmd = "perl /data/bilin/script/pveCDCheck/pveCDCheck.pl";
			// 查修改数值秒杀的
			shellCmd = "cat /data/bilin/backup_center/server_log/game_server/2017-09-30/40*/PVEreportResultPKLog_ProtoRpcService.log.*|grep 2147483647"
					+ "|awk -F \"\\t\" '{print $2\"_\"$3\" \"$4\" \"$5\" \"$12\" \"$13}'|awk '{print $1}' |sort|uniq -c";
			// 查疑似pve作弊的
			shellCmd = "cat /data/bilin/backup_center/server_log/game_server/2017-09-30/40*/PVEreportResultPKLog_ProtoRpcService.log.*|grep succ |grep PVE"
					+ "|awk -F \"\\t\" '{if($12<500 && (($4>100300 && $4<200000) || ($4>200101)))  print $2\"_\"$3\" \"$4\" \"$5\" \"$12\" \"$13}'";
			// 查排位赛作弊的
			shellCmd = "cat /data/bilin/backup_center/server_log/game_server/2017-09-*/40*/PVEreportResultPKLog_ProtoRpcService.log.*|grep RANKING_SEASON"
					+ "|awk -F \"\\t\" '{if($12<500)  print $2\"_\"$3\" \"$4\" \"$5\" \"$12\" \"$13}'";
			// 查守卫基地
			shellCmd = "cat /data/bilin/backup_center/server_log/game_server/2017-09-*/40*/PVEreportResultPKLog_ProtoRpcService.log.*|grep GUARDBASE"
					+ "|awk -F \"\\t\" '{if($12<500 && $4>40)  print $2\"_\"$3\" \"$4\" \"$5\" \"$12\" \"$13}'";

			// |awk '{print $1}'
			shellCmd = "cat /data/bilin/backup_center/server_log/game_server/2017-*-*/40*/PVEreportResultPKLog_ProtoRpcService.log.*|grep 10.E8"
					+ "|awk -F \"\\t\" '{print $2\"_\"$3\" \"$4\" \"$5\" \"$12\" \"$13}'|awk '{print $1}' |sort|uniq -c";

			shellCmd = "cat /data/bilin/apps/*/javaserver_gecaoshoulie_server/log/ProtoRpcService.log.2017-10-04_1*|"
					+ "grep pkTicketNo|grep \"1.0E8\"|awk -F \"|\" '{print $2}' |awk -F \"=\" '{print $3}'|sort |uniq -c";

			shellCmd = "zcat /data/bilin/backup_center/server_log/game_server/2017-10-02/4*/ProtoRpcService.log.*.gz|grep pkTicketNo|"
					+ "grep \"1.0E8\"|awk -F \"|\" '{print $2}' |awk -F \"=\" '{print $3}'|sort |uniq -c|sort -n -k 1";

			shellCmd = "zcat /data/bilin/backup_center/server_log/game_server/2017-10-02/4*/ProtoRpcService.log.*.gz|grep pkTicketNo|"
					+ "grep \"1.0E8\"|awk -F \"|\" '{print $2}' |awk -F \"=\" '{print $2}'|awk -F \"@\" '{print $1}'|sort|uniq -c|sort -n -k 1|awk '{print $2}'";

			shellCmd = "cat /data/bilin/apps/*/javaserver_gecaoshoulie_server/log/ProtoRpcService.log.2017-10-04_1*|"
					+ "grep pkTicketNo|grep \"1.0E8\"|awk -F \"|\" '{print $2}' |awk -F \"=\" '{print $3}'|sort |uniq -c";
			// 找最近log里的作弊用户id
			shellCmd = "cat /data/bilin/apps/*/javaserver_gecaoshoulie_server/log/ProtoRpcService.log.2017-10-04_*|grep pkTicketNo|"
					+ "grep \"1.0E8\"|awk -F \"|\" '{print $2}' |awk -F \"=\" '{print $2}'|awk -F \"@\" '{print $1}'|sort|uniq -c |sort -n -k 1|awk '{print $2}'|sort";

			String str = runCmd(host, port, userName, password, shellCmd);
			str = str.replaceAll("\n\n", "\n").replaceAll("\n\n", "\n");
			System.out.println("执行结果:" + str);
			sb.append(str);
		}
		String[] arr = sb.toString().split("\n");
		Arrays.sort(arr);
		String qrStr = StringUtil.join(";", arr);
		System.out.println("外挂玩家有：" + qrStr);
		try {
			String str = downloadUrlbyPOST(
					"http://120.92.119.100:8090/gecaoshoulie_operateconsole/stats/CheatingChecking.do",
					"outsourceBanUserIdList=" + qrStr, "quickli", "UTF-8");
			System.out.println(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * 
	 * @param startTimeLong
	 * @param endTimeLong
	 * @param timeType
	 *            1.按天，2，按小时，3，按十分钟
	 * @param platType
	 *            1android，2,iOS
	 * 
	 * @return
	 */
	public static Map<String, Integer> mainShifenzhong(String startTimeLong, String endTimeLong, int timeType,
			int platType) {
		String csvFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N/cn_release/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
		List<String[]> list = CSVUtil.getDataFromCSV2(csvFile);
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String shellCmd = "cat /etc/hosts|grep zone|wc -l";
		shellCmd = "sudo apt-get --yes install ntp";
		shellCmd = "timedatectl";// 查看网络校准时间
		Map<String, String> map = new TreeMap<String, String>();
		for (int k = 4; k < list.size(); k++) {
			String[] arr = list.get(k);
			if (arr.length > 0) {
				String host = CSVUtil.getColValue("server_public_ip", arr, list);
				String innerHost = CSVUtil.getColValue("server_inner_ip", arr, list);
				String zoneId = CSVUtil.getColValue("zone_id", arr, list);
				int platformInt = Integer.parseInt(CSVUtil.getColValue("platform", arr, list));
				int zoneIdInt = Integer.parseInt(zoneId);
				System.out.println("服务区id==" + zoneIdInt);
				if (platformInt == platType) {
					String sql = "select  zone_id,floor(reg_time_long/1000)*10 hourstr ,count(*)   from tuser4zoneserver_gameplayer where reg_time_long >20170926000000  group by zone_id,hourstr order by hourstr;";
					sql = "select  floor(reg_time_long/1000)*10 hourstr ,count(*)   from tuser4zoneserver_gameplayer where reg_time_long>="
							+ startTimeLong;
					if (timeType == 1) {
						sql = "select  floor(reg_time_long/1000000) hourstr ,count(*)   from tuser4zoneserver_gameplayer where reg_time_long>="
								+ startTimeLong;
					}
					if (timeType == 2) {
						sql = "select  floor(reg_time_long/10000) hourstr ,count(*)   from tuser4zoneserver_gameplayer where reg_time_long>="
								+ startTimeLong;
					}
					if (endTimeLong != null && endTimeLong.length() > 0) {
						sql = sql + "  and reg_time_long <" + endTimeLong;
					}
					sql = sql + " group by hourstr order by hourstr";

					// and reg_time_long <=20170927100000 and reg_time_long
					// <=20170929000000

					// select zone_id,floor(reg_time_long/1000) hourstr
					// ,count(*) from tuser4zoneserver_gameplayer where
					// reg_time_long >20170926000000 group by zone_id,hourstr
					// order by hourstr;
					shellCmd = "mysql -h " + innerHost
							+ " --default-character-set=utf8 -uroot -pmysqlpwdbilinkejinet -P3306 mgamedb_gecaoshoulie_maindb_zone"
							+ zoneId + " -e \"" + sql + "\"";
					String str = runCmd(host, port, userName, password, shellCmd);
					System.out.println("执行结果:" + str);
					map.put(zoneId, (str.trim()));
				}
			}
		}
		Map<String, Integer> shifenzhongAllMap = new TreeMap<String, Integer>();

		for (String key : map.keySet()) {
			System.out.println(key + "|value=" + map.get(key));//
		}
		for (String val : map.values()) {
			String[] arr = val.split("\n");
			for (String a : arr) {
				if (a.trim().startsWith("20")) {
					String[] barr = a.split("\t");
					String time = barr[0];
					if (barr.length > 1) {
						int count = Integer.parseInt(barr[1]);
						int newCount = count + shifenzhongAllMap.getOrDefault(time, 0);
						shifenzhongAllMap.put(time, newCount);
					} else {
						System.err.println("出错内容" + a);
					}
				}
			}
		}
		for (String key : shifenzhongAllMap.keySet()) {
			System.out.println("All\t" + key + "\t" + shifenzhongAllMap.get(key));//
		}
		int allCount = 0;
		for (int cc : shifenzhongAllMap.values()) {
			allCount = allCount + cc;
		}
		System.out.println("allCount==" + allCount);
		return shifenzhongAllMap;
	}

	public static void mainzonghe(String[] args) {
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String sql = "select floor(last_login_time_long/1000) hourstr ,count(*) from tuser_taiqiuser where last_login_time_long>=20170926120000 group by hourstr order by hourstr;";
		sql = "select floor(added_time_long/1000)*10 hourstr ,count(*) from tuser_taiqiuser where added_time_long>=20170926120000 group by hourstr order by hourstr;";

		// added_time_long
		String sqlStr = "select count(*) from tuser_taiqiuser limit 1";
		String shellCmd = "mysql -h 10.0.0.4 --default-character-set=utf8 -uroot -pmysqlpwdbilinkejinet -P3306 mgamedb_gecaoshoulie -e \""
				+ sql + "\";";
		String host = "120.92.86.41";
		String str = runCmd(host, port, userName, password, shellCmd);
		System.out.println("执行结果:" + str);
		shellCmd = "mysql -h 10.0.0.4 --default-character-set=utf8 -uroot -pmysqlpwdbilinkejinet -P3306 mgamedb_gecaoshoulie -e \""
				+ sqlStr + "\";";
		str = runCmd(host, port, userName, password, shellCmd);
		System.out.println("执行结果:" + str);
	}

	public static void mainOld(String[] args) {
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String shellCmd = "cat /etc/hosts|grep zone|wc -l";
		shellCmd = "ls /data/bilin/apps/poolcfgfiles/dbpoolcfg/dbpool_4maindb/ -alh|wc -l;"
				// + "rm
				// /data/bilin/apps/poolcfgfiles/dbpoolcfg/dbpool_4maindb/dbpool_4maindb_zone40010.properties;"
				+ "ls /data/bilin/apps/poolcfgfiles/dbpoolcfg/dbpool_4maindb/ -alh|wc -l";
		for (String host : hostsIps) {
			String str = runCmd(host, port, userName, password, shellCmd);
			System.out.println("执行结果:" + str);
		}

	}

	public static void mainNew(String[] args) {
		String csvFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N/cn_release/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
		List<String[]> list = CSVUtil.getDataFromCSV2(csvFile);
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String shellCmd = "cat /etc/hosts|grep zone|wc -l";
		shellCmd = "sudo apt-get --yes install ntp";
		shellCmd = "timedatectl";// 查看网络校准时间
		Map<String, String> map = new TreeMap<String, String>();
		for (int k = 4; k < 70 && k < list.size(); k++) {
			String[] arr = list.get(k);
			if (arr.length > 0) {
				String host = CSVUtil.getColValue("server_public_ip", arr, list);
				String innerHost = CSVUtil.getColValue("server_inner_ip", arr, list);
				String str = runCmd(host, port, userName, password, shellCmd);
				System.out.println("执行结果:" + str);
				map.put(host, (str.trim()));
			}
		}
		for (String key : map.keySet()) {
			System.out.println(key + "\t === HashCode " + map.get(key).hashCode() + "|value=" + map.get(key));//
			System.out.println(key + "|value=" + map.get(key));//
		}
	}

	public static void main4Myaql(String[] args) {
		String[] hostsIps = new String[] { "120.92.119.7", "120.92.118.161", "120.92.119.123", "120.92.119.140",
				"120.92.119.100", "120.92.20.149", "120.92.116.126", "120.92.116.13", "120.92.119.30", "120.92.118.82",
				"120.92.119.101", "120.92.118.16", "120.92.76.23", "120.92.119.91", "120.92.119.154", "120.92.102.164",
				"120.92.117.161", "120.92.42.192", "120.92.114.81", "120.92.118.52", "120.92.119.29", "120.92.118.177",
				"120.92.117.98", "120.92.115.59", "120.92.77.130", "120.92.93.54", "120.92.102.165", "120.92.119.51",
				"120.92.43.63", "120.92.76.133", "120.92.116.60", "120.92.35.182", "120.92.89.146", "120.92.119.169",
				"120.92.18.98", "120.92.115.117", "120.92.88.25", "120.92.116.206", "120.92.118.151" };
		String csvFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N/cn_release/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
		List<String[]> list = CSVUtil.getDataFromCSV2(csvFile);
		int port = 13922;
		String userName = "ubuntu";
		String password = "XJliehen!@17";
		String shellCmd = "cat /etc/hosts|grep zone|wc -l";
		shellCmd = "cat /etc/hosts|grep -v 127.0.0.1";
		shellCmd = "mysql -h10.0.2.5 -P3306 -uroot -pmysqlpwdbilinkejinet -e \"show variables like '%open%'\"";
		// shellCmd = "crontab -l";
		Map<String, Map<String, String>> map = new TreeMap<String, Map<String, String>>();
		for (int k = 4; k < 70 && k < list.size(); k++) {
			String[] arr = list.get(k);
			if (arr.length > 0) {
				String host = CSVUtil.getColValue("server_public_ip", arr, list);
				String innerHost = CSVUtil.getColValue("server_inner_ip", arr, list);
				shellCmd = "mysql -h" + innerHost
						+ " -P3306 -uroot -pmysqlpwdbilinkejinet -e \"show variables like '%open%'\"";
				String str = runCmd(host, port, userName, password, shellCmd);
				map.put(host, getMysqlVariableMap(str));
			}
		}

		for (Map<String, String> mysqlVarMap : map.values()) {

			mysqlVarMap.remove("general_log_file");
			mysqlVarMap.remove("pid_file");
			mysqlVarMap.remove("relay_log_basename");
			mysqlVarMap.remove("relay_log_index");
			mysqlVarMap.remove("server_uuid");
			mysqlVarMap.remove("slow_query_log_file");
			mysqlVarMap.remove("log_error");
			mysqlVarMap.remove("timestamp");
			mysqlVarMap.remove("hostname");
			mysqlVarMap.remove("timestamp");
		}

		Set<String> allVarKeySet = new TreeSet<String>();
		for (String key : map.keySet()) {
			Map<String, String> mysqlVarMap = map.get(key);
			allVarKeySet.addAll(mysqlVarMap.keySet());
		}

		allVarKeySet.remove("general_log_file");
		allVarKeySet.remove("pid_file");
		allVarKeySet.remove("relay_log_basename");
		allVarKeySet.remove("relay_log_index");
		allVarKeySet.remove("server_uuid");
		allVarKeySet.remove("slow_query_log_file");
		allVarKeySet.remove("timestamp");
		allVarKeySet.remove("timestamp");
		allVarKeySet.remove("timestamp");
		allVarKeySet.remove("timestamp");

		for (String key : allVarKeySet) {
			boolean eq = true;
			String oldValue = null;
			for (Map<String, String> mysqlVarMap : map.values()) {
				if (oldValue == null && mysqlVarMap.get(key) != null) {
					oldValue = mysqlVarMap.get(key);
				}
				if (oldValue != null && !oldValue.equals(mysqlVarMap.get(key))) {
					// System.err.println(oldValue + "|noteq|" +
					// mysqlVarMap.get(key));
					eq = false;
					break;
				}
			}
			if (eq) {
				// System.out.println("remove||" + key);
				for (Map<String, String> mysqlVarMap : map.values()) {
					mysqlVarMap.remove(key);
				}
			}
		}
		// for (String host : hostsIps) {
		// String str = runCmd(host, port, userName, password, shellCmd);
		// // System.out.println("执行结果：" + str);
		// map.put(host, str.trim());
		// }

		for (String key : map.keySet()) {
			System.out.println(key + " === HashCode " + map.get(key).hashCode() + "|value=" + map.get(key));//
		}
	}

	public static Map<String, String> getMysqlVariableMap(String str) {
		Map<String, String> map = new TreeMap<String, String>();
		String[] arr = str.split("\n");
		for (String a : arr) {
			String[] barr = a.split("\t");
			// System.out.println(Arrays.toString(barr));
			if (barr.length > 1) {
				map.put(barr[0].trim(), barr[1].trim());
			} else if (barr.length > 0) {
				map.put(barr[0].trim(), "");
			}
		}
		return map;
	}

	public static String runCmd(String host, int port, String userName, String password, String shellCmd) {
		LogUtil.printLog("LinuxRemoteShellUtil.runCmd|" + userName + "@" + host + ":" + port + "/ " + shellCmd);
		try {
			// StringBuilder sb=new StringBuilder();
			JSch jsch = new JSch();
			Session session;
			try {
				session = jsch.getSession(userName, host, port);
				session.setTimeout(5000); // 5秒超时
				// username and password will be given via UserInfo interface.
				UserInfo ui = new LinuxUerInfo(password);
				session.setUserInfo(ui);
				session.connect();
			} catch (Exception e) {
				LogUtil.printLogErr("LinuxRemoteCommandUtil.runCmd connectTimeout #1" + e);

				// 重试一次
				session = null;
				session = jsch.getSession(userName, host, port);
				session.setTimeout(5000); // 5秒超时

				// username and password will be given via UserInfo interface.
				UserInfo ui = new LinuxUerInfo(password);
				session.setUserInfo(ui);
				session.connect();

				LogUtil.printLog("LinuxRemoteCommandUtil.runCmd reconnect successfully");
			}

			boolean ptimestamp = true;

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(shellCmd);

			// get I/O streams for remote scp
			// OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();
			channel.setInputStream(null);
			// channel.setExtOutputStream(System.out);
			// channel.setOutputStream(System.out);
			channel.connect();
			byte[] tmp = new byte[1024];
			StringBuilder sb = new StringBuilder(1024 * 8);
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					String line = new String(tmp, 0, i);
					sb.append(line).append("\n");
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					int exitStatus = channel.getExitStatus();

					// 运行指令出错,打error,发邮件,退出
					if (exitStatus > 0) {
						// String line = "error:" + exitStatus + " ,see
						// http://tldp.org/LDP/abs/html/exitcodes.html";
						// System.err.println(line);
						String errorMsg = "[run remote cmd error],errorCode:" + exitStatus + ",(" + host + "): "
								+ shellCmd;

						LogUtil.printLogErr(errorMsg);
						LogUtil.printLogErr("[remoteServer terminal info]: " + sb);

						// MailTest.sendErrorMail("[run remote cmd error]",
						// errorMsg);
						// System.exit(1);
					}
					break;
				}
			}
			channel.disconnect();
			session.disconnect();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();

		}
		return "err";
	}

	public static String downloadUrlbyPOST(String urlStr, String query, String referer, String encoding)
			throws Exception {
		String line = "";
		StringBuilder sb = new StringBuilder();
		HttpURLConnection httpConn = null;
		try {
			URL url = new URL(urlStr);
			System.out.println(urlStr + "?" + query);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.lizongbo.com", 8080));
			proxy = Proxy.NO_PROXY;
			httpConn = (HttpURLConnection) url.openConnection(proxy);
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setRequestMethod("POST");
			if (referer != null) {
				httpConn.setRequestProperty("Referer", referer);
			}
			httpConn.setRequestProperty("BilinAgent", "quickli");
			httpConn.setConnectTimeout(5000);
			// httpConn.getOutputStream().write(
			// java.net.URLEncoder.encode(query, "UTF-8").getBytes());
			httpConn.getOutputStream().write(query.getBytes());
			httpConn.getOutputStream().flush();
			httpConn.getOutputStream().close();

			BufferedReader in = null;
			if (httpConn.getResponseCode() != 200) {
				System.err.println("error:" + httpConn.getResponseMessage());
				in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), "UTF-8"));
			} else {
				in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
			}
			while ((line = in.readLine()) != null) {
				sb.append(line).append('\n');
			}
			// 关闭连接
			httpConn.disconnect();
			return sb.toString();
		} catch (Exception e) {
			// 关闭连接
			httpConn.disconnect();
			System.out.println(e.getMessage());
			throw e;
		}
	}
}

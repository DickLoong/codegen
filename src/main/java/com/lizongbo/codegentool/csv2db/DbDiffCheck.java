package com.lizongbo.codegentool.csv2db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.gson.Gson;

/**
 * 检查远程的世界表和内网的世界表表结构是否一致
 * 
 * @author quickli
 *
 */
public class DbDiffCheck {

	public static void main(String[] args) throws Exception {
		String urlLocal = "http://10.0.0.16:8090/gecaoshoulie_operateconsole/rootdbdesc.jsp";
		String urlCn = "http://120.92.119.100:8090/gecaoshoulie_operateconsole/rootdbdesc.jsp";
		String jsonLocal = downloadUrl(urlLocal, "UTF-8");
		String jsonCn = downloadUrl(urlCn, "UTF-8");

		Gson gson = new Gson();
		Map mapLocal = gson.fromJson(jsonLocal, Map.class);
		Map mapCn = gson.fromJson(jsonCn, Map.class);
		Map diffMap = getTableDescDiff(mapLocal, mapCn);
		System.out.println("有差异的表为：" + diffMap.keySet());
		for (Object key : diffMap.keySet()) {
			if (String.valueOf(key).endsWith("|4one")) {
				Map map4One = (Map) diffMap.get(key);
				String key4Other = String.valueOf(key).replace("|4one", "|4other");
				// System.out.println(key.getClass() + "key4Other==" +
				// key4Other);
				Map map4Other = (Map) diffMap.get(key4Other);
				// System.out.println(key.getClass() + "map4Other==" +
				// map4Other);
				System.out.println("字段差异" + key + "|" + getTableDescDiff(map4One, map4Other));
			}
			// Object map = diffMap.get(key);
			// System.out.println(key + "||||" + map);
		}
	}

	public static Map getTableDescDiff(Map one, Map other) {
		Map tableMap = new TreeMap();
		if (one == null) {
			one = new HashMap();
		}
		if (other == null) {
			other = new HashMap();
		}
		Set tableNameSet = new TreeSet();
		tableNameSet.addAll(one.keySet());
		tableNameSet.addAll(other.keySet());
		for (Object tabelName : tableNameSet) {
			Object map4one = one.get(tabelName);
			Object map4other = other.get(tabelName);
			boolean eq = false;
			if (map4one != null && map4other != null && map4one.equals(map4other)) {
				eq = true;
			}
			if (!eq) {
				//System.out.println(tabelName + "|4one=" + map4one);
				//System.out.println(tabelName + "|4other=" + map4other);
				tableMap.put(tabelName + "|4one", map4one);
				tableMap.put(tabelName + "|4other", map4other);
			} else {
				// System.err.println("iseq|" + tabelName);
			}
		}
		return tableMap;
	}

	public static String downloadUrl(String urlStr, String encoding) throws Exception {
		// System.out.println("downloadUrl for:" + urlStr + "|" + encoding);
		String line = "";
		StringBuilder sb = new StringBuilder();
		HttpURLConnection httpConn = null;
		try {
			URL url = new URL(urlStr);
			Proxy proxy = Proxy.NO_PROXY;
			httpConn = (HttpURLConnection) url.openConnection(proxy);
			httpConn.setConnectTimeout(5000);
			httpConn.connect();
			BufferedReader in = null;
			if (httpConn.getResponseCode() != 200) {
				System.err.println("error:" + httpConn.getResponseMessage() + "|" + httpConn.getResponseCode() + "|"
						+ httpConn.getHeaderFields());
				in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), "UTF-8"));
			} else {
				in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
			}
			while ((line = in.readLine()) != null) {
				sb.append(line);// .append('\n');
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

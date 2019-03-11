package com.lizongbo.codegentool.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringUtil {

	/** 精确到秒的日期时间格式化的格式字符串 */
	public static final String FMT_DATETIME = "yyyy-MM-dd HH:mm:ss";
	/*
	 * 按天格式化日期
	 */
	public static final String FMT_DAY = "yyyy-MM-dd";
	private static final ThreadLocal<SimpleDateFormat> sdf_FMT_DAY = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return createDaystampFormat();
		}
	};

	private static SimpleDateFormat createDaystampFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(FMT_DAY);
		return sdf;
	}

	private static final ThreadLocal<SimpleDateFormat> sdf_FMT_DATETIME = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return createDateTimeStampFormat();
		}
	};

	private static SimpleDateFormat createDateTimeStampFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(FMT_DATETIME);
		return sdf;
	}

	public static String encodeSQL(String sql) {
		if (sql == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(sql.length() + 64);
		for (int i = 0; i < sql.length(); ++i) {
			char c = sql.charAt(i);
			switch (c) {
			case '\\':
				sb.append("\\\\");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\'':
				sb.append("\'\'");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static <K, V> V safeGetValue(Map<K, V> kvMap, K key) {
		if ((kvMap == null) || (key == null)) {
			return null;
		}
		return kvMap.get(key);
	}

	public static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static String formatDateNow(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new java.util.Date());
	}

	/**
	 * 获取格式化好的当天日期，凌晨0点切换时间戳
	 * 
	 * @return
	 */
	public static String formatDayNow() {
		return formatDayNow(0);
	}

	/**
	 * 获取格式化好的当天日期时间戳，受重置时间点影响的
	 * 
	 * @param offsetHour
	 *            0到23 重置时间的小时，比如5表示凌晨5点重置
	 * @return 比如凌晨5点重置的，则2016.07.13.05:00:00到2016.07.14.04:59:59
	 *         格式化得到的日期时间戳是2016-07-13
	 */
	public static String formatDayNow(int offsetHour) {

		return sdf_FMT_DAY.get().format(new java.util.Date(System.currentTimeMillis() - 1000l * 60 * 60 * offsetHour));
	}

	/**
	 * 到秒的格式化日期
	 * 
	 * @param time
	 * @return
	 */
	public static String formatDateTime(long time) {
		return sdf_FMT_DATETIME.get().format(new java.util.Date(time));
	}

	/**
	 * 获取当前时间的yyyy-MM-dd HH:mm:ss格式的字符串
	 * 
	 * @return
	 */
	public static String currentSqlTimestampStr() {
		return getSqlTimestampStr(System.currentTimeMillis());
	}

	/**
	 * 获取指定时间的yyyy-MM-dd HH:mm:ss格式的字符串
	 * 
	 * @param timeMillis
	 * @return
	 */
	public static String getSqlTimestampStr(long timeMillis) {
		return new java.sql.Timestamp(timeMillis).toString().substring(0, 19);
	}

	@SuppressWarnings("unchecked")
	public static String join(String separator, Collection c) {
		if (c.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		Iterator i = c.iterator();
		sb.append(i.next());
		while (i.hasNext()) {
			sb.append(separator);
			sb.append(i.next());
		}
		return sb.toString();
	}

	public static String join(String separator, String[] s) {
		return joinArray(separator, s);
	}

	public static String joinArray(String separator, Object[] s) {
		if (s == null || s.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(s[0]);
		for (int i = 1; i < s.length; ++i) {
			if (s[i] != null) {
				sb.append(separator);
				sb.append(s[i].toString());
			}
		}
		return sb.toString();
	}

	public static String joinArray(String separator, int[] s) {
		if (s == null || s.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(s[0]);
		for (int i = 1; i < s.length; ++i) {
			sb.append(separator);
			sb.append(s[i]);
		}
		return sb.toString();
	}

	public static String join(String separator, Object... c) {
		return joinArray(separator, c);
	}

	/**
	 * 字符串全量替换
	 * 
	 * @param s
	 *            原始字符串
	 * @param src
	 *            要替换的字符串
	 * @param dest
	 *            替换目标
	 * @return 结果
	 */
	public static String replaceAll(String s, String src, String dest) {
		if (s == null || src == null || dest == null || src.length() == 0)
			return s;
		int pos = s.indexOf(src); // 查找第一个替换的位置
		if (pos < 0)
			return s;
		int capacity = dest.length() > src.length() ? s.length() * 2 : s.length();
		StringBuilder sb = new StringBuilder(capacity);
		int writen = 0;
		for (; pos >= 0;) {
			sb.append(s, writen, pos); // append 原字符串不需替换部分
			sb.append(dest); // append 新字符串
			writen = pos + src.length(); // 忽略原字符串需要替换部分
			pos = s.indexOf(src, writen); // 查找下一个替换位置
		}
		sb.append(s, writen, s.length()); // 替换剩下的原字符串
		return sb.toString();
	}

	public static int toInt(String s) {
		return toInt(s, 0);
	}

	public static int toInt(String s, int def) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return def;
	}

	public static long toLong(String s) {
		return toLong(s, 0);
	}

	public static long toLong(String s, long def) {
		try {
			return Long.parseLong(s.trim());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return def;
	}

	public static float toFloat(String s) {
		return toFloat(s, 0.0f);
	}

	public static float toFloat(String s, float def) {
		try {
			return Float.parseFloat(s.trim());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return def;
	}

	public static double toDouble(String s) {
		return toDouble(s, 0.0f);
	}

	public static double toDouble(String s, double def) {
		try {
			return Double.parseDouble(s.trim());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return def;
	}

	public static short toShort(String s) {
		return toShort(s, (short) 0);
	}

	public static short toShort(String s, short def) {
		try {
			return Short.parseShort(s.trim());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return def;
	}

	public static boolean toBoolean(String s) {
		return toBoolean(s, false);
	}

	public static boolean toBoolean(String s, boolean def) {
		try {
			if (s != null) {
				s = s.trim().toLowerCase();
			}
			return Boolean.valueOf(s);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return def;
	}

	public static String toStr(Throwable th) {
		if (th == null) {
			return "Throwable=NULL";
		}
		StringWriter sw = new StringWriter(1024);
		th.printStackTrace(new PrintWriter(sw));
		sw.flush();
		return sw.toString();
	}

	public static List<String> splitList(String line, String seperator) {
		if (line == null || seperator == null || seperator.length() == 0)
			return null;
		List<String> list = new ArrayList<String>();
		int pos1 = 0;
		int pos2;
		for (;;) {
			pos2 = line.indexOf(seperator, pos1);
			if (pos2 < 0) {
				list.add(line.substring(pos1));
				break;
			}
			list.add(line.substring(pos1, pos2));
			pos1 = pos2 + seperator.length();
		}
		for (int i = list.size() - 1; i >= 0 && list.get(i).length() == 0; --i) {
			list.remove(i);
		}
		return list;
	}

	public static String[] split(String line, String seperator) {
		if (line == null || seperator == null || seperator.length() == 0)
			return null;
		List<String> list = splitList(line, seperator);
		return list.toArray(new String[0]);
	}

	/**
	 * 逗号分割整数数组，分割符号兼容分号和全角逗号
	 * 
	 * @param line
	 * @param def
	 * @return
	 */
	public static int[] splitInt(String line, int def) {
		if (line != null) {
			line = line.trim().replace(';', ',').replace('；', ',').replace('，', ',');
		}
		return splitInt(line, ",", def);
	}

	public static int[] splitInt(String line, String seperator, int def) {
		String[] ss = split(line, seperator);
		int[] r = new int[ss.length];
		for (int i = 0; i < r.length; ++i) {
			r[i] = toInt(ss[i], def);
		}
		return r;
	}

	/**
	 * 竖线和逗号分割得到二维数组,竖线是一级分隔符号，逗号是二级分隔符号
	 * 
	 * @param line
	 * @param def
	 * @return
	 */
	public static int[][] splitInt2d(String line, int def) {
		if (line != null) {
			line = line.trim().replace('｜', '|').replace(';', ',').replace('；', ',').replace('，', ',');
		}
		String[] strs = split(line, "|");
		int[][] ia = new int[strs.length][];
		for (int i = 0; i < strs.length; i++) {
			String str = strs[i];
			ia[i] = splitInt(str, ",", def);
		}
		return ia;
	}

	/**
	 * 逗号分割长整数数组，分割符号兼容分号和全角逗号
	 * 
	 * @param line
	 * @param def
	 * @return
	 */
	public static long[] splitLong(String line, int def) {
		if (line != null) {
			line = line.trim().replace(';', ',').replace('；', ',').replace('，', ',');
		}
		return splitLong(line, ",", def);
	}

	public static long[] splitLong(String line, String seperator, long def) {
		String[] ss = split(line, seperator);
		long[] r = new long[ss.length];
		for (int i = 0; i < r.length; ++i) {
			r[i] = toLong(ss[i], def);
		}
		return r;
	}

	/**
	 * 竖线和逗号分割得到二维数组,竖线是一级分隔符号，逗号是二级分隔符号
	 * 
	 * @param line
	 * @param def
	 * @return
	 */
	public static long[][] splitLong2d(String line, long def) {
		if (line != null) {
			line = line.trim().replace('｜', '|').replace(';', ',').replace('；', ',').replace('，', ',');
		}
		String[] strs = split(line, "|");
		long[][] ia = new long[strs.length][];
		for (int i = 0; i < strs.length; i++) {
			String str = strs[i];
			ia[i] = splitLong(str, ",", def);
		}
		return ia;
	}

	/**
	 * 逗号分割浮点数数组，分割符号兼容分号
	 * 
	 * @param line
	 * @param def
	 * @return
	 */
	public static float[] splitFloat(String line, int def) {
		if (line != null) {
			line = line.trim().replace(';', ',').replace('；', ',').replace('，', ',');
		}
		return splitFloat(line, ",", def);
	}

	public static float[] splitFloat(String line, String seperator, float def) {
		String[] ss = split(line, seperator);
		float[] r = new float[ss.length];
		for (int i = 0; i < r.length; ++i) {
			r[i] = toFloat(ss[i], def);
		}
		return r;
	}

	/**
	 * 竖线和逗号分割得到二维数组,竖线是一级分隔符号，逗号是二级分隔符号
	 * 
	 * @param line
	 * @param def
	 * @return
	 */
	public static float[][] splitFloat2d(String line, float def) {
		if (line != null) {
			line = line.trim().replace('｜', '|').replace(';', ',').replace('；', ',').replace('，', ',');
		}
		String[] strs = split(line, "|");
		float[][] ia = new float[strs.length][];
		for (int i = 0; i < strs.length; i++) {
			String str = strs[i];
			ia[i] = splitFloat(str, ",", def);
		}
		return ia;
	}

	public static String getIpAddress(SocketAddress a) {
		if (a != null && a instanceof InetSocketAddress) {
			InetSocketAddress isa = (InetSocketAddress) a;
			InetAddress ia = isa.getAddress();
			if (ia != null) {
				return ia.getHostAddress();
			}
			// System.out.println(isa.getHostString() + "|" +
			// isa.getAddress().getHostAddress());
		}
		return "null";
	}

	/**
	 * 首字母大写
	 * 
	 * @param s
	 * @return
	 */
	public static String capFirst(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public static String concatRedisKey(Object... strings) {
		StringBuilder sBuffer = new StringBuilder();
		for (Object string : strings) {
			sBuffer.append(String.valueOf(string));
		}
		return sBuffer.toString();
	}

	public static String concatRedisKeyWithSplit(String split, Object... strings) {
		StringBuilder sBuffer = new StringBuilder();
		for (Object string : strings) {
			sBuffer.append(String.valueOf(string)).append(split);
		}
		sBuffer.deleteCharAt(sBuffer.length() - 1);
		return sBuffer.toString();
	}

	public static String concatRedisKeyWithDate(Object... strings) {
		StringBuilder sBuffer = new StringBuilder();
		sBuffer.append(concatRedisKey(strings));
		sBuffer.append(StringUtil.formatDayNow());
		return sBuffer.toString();
	}

	public static String tryZipedHex2Json(String jsonText) throws UnsupportedEncodingException {
		// 需要加判断是否16进制字符串的逻辑来兼容
		if (jsonText.contains("{")) {// 有大括号肯定是json，不用处理啥
			/// 啥也不用干,但是算一下压缩之后10进制字符串有多长
			byte[] strBytes = jsonText.getBytes("UTF-8");
			byte[] strGzipedBytes = gZip(strBytes);
			String hexGzipedStr = HexUtil.bytes2Hex(strGzipedBytes);
			// log.warn("tryZipedHex2Json|字符串是原始json，加载的json字符串长度是" +
			// jsonText.length() + "|json字节长度为" + strBytes.length
			// + "|gzip压缩后的字节长度是" + strGzipedBytes.length +
			// "|gzip压缩之后的16进制字符串长度为：" + hexGzipedStr.length());
			return jsonText;
		} else if (jsonText.contains("g") || jsonText.contains("h") || jsonText.contains("i")) {// 有0-9，a-f之外的字母，肯定是base64的，走base64处理
			// log.warn("tryZipedHex2Json|疑似base64字符串");
		} else if (jsonText.length() % 2 != 1) {// 字符串长度是2的倍数才可能是16进制的
			byte[] strGzipedBytes = HexUtil.hexStr2Bytes(jsonText);
			byte[] strBytes = unGZip(strGzipedBytes);
			String newjsonText = new String(strBytes, "UTF-8");// 还原json字符串
			// log.warn("tryZipedHex2Json|字符串是16进制的，加载的json字符串长度是" +
			// newjsonText.length() + "|json字节长度为" + strBytes.length
			// + "|Gizp压缩后字节长度为" + strGzipedBytes.length +
			// "|存在配置中的gzip压缩之后的16进制字符串长度为：" + jsonText.length());
			return newjsonText;
		} else {
			// log.error("tryZipedHex2Json|不知道是啥格式的字符串了，需要关注一下啊，字符串长度为" +
			// jsonText.length());
		}
		return jsonText;
	}

	public static byte[] gZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(data);
			gzip.finish();
			gzip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***
	 * 解压GZip
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] unGZip(byte[] data) {
		if (data == null || data.length < 1) {
			return data;
		}
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			// logger.error("unGZip|erro|zipLength|"+data.length+"|zipbytes|"+HexUtil.bytes2Hex(data),ex);
			ex.printStackTrace();
		}
		return b;
	}

	/**
	 * 策划配表的颜色表达式解析
	 * 
	 * @param str
	 * @return
	 */
	public static String tryParse2UnityRichText(String str) {
		if (str == null || str.length() < 1 || !str.contains("#") || str.length() < 4) {
			return str;
		}
		// 兼容 #G{{0}} 这样的写法
		for (int i = 0; i < 10; i++) {
			str = replaceAll(str, "{" + i + "}", "[[[" + i + "]]]");
		}

		String richSpaceStr = "<color=$ffffff00>k</color>";
		String richSpaceStrReal = "<color=#ffffff00>k</color>";
		if (str.contains(" ")) {// 为避免空格导致排版换行，用富文本来潜规则处理
			str = replaceAll(str, " ", richSpaceStr);
		}
		// 至少井号加一位颜色加字符，才有可能是需要转换颜色的文本，因此文本长度至少是3
		String[] arr = str.split("#");
		int startIndex = 1;
		if (str.startsWith("#")) {
			startIndex = 0;
		}
		for (int i = startIndex; i < arr.length; i++) {
			String tmp = arr[i];
			if (tmp.length() < 4) {
				continue;
			}
			int aa = tmp.indexOf("{");
			int bb = tmp.indexOf("}");
			if (aa > 0 && bb > aa) {// 是 #Y{汉字} 这样的格式，必须有大括号，才需要转换
				String yanseStr = tmp.substring(0, aa);

				// Debug.LogError ("|TryParse2UnityRichText颜色是" + yanseStr +
				// "|文本是|" + tmp + "|总文本是" + str);
				// <color=yellow>RICH</color>
				if (yanseStr.length() == 1) {
					if ("GBPOR".contains(yanseStr)) {
						StringBuilder sb = new StringBuilder(tmp.length());
						switch (yanseStr) {
						case "G":// 绿色
						{
							sb.append("<color=#00ff00ff>");
							sb.append(tmp.substring(aa + 1, bb).replace(richSpaceStr, " "));
							sb.append("</color>");
							sb.append(tmp.substring(bb + 1));
							break;
						}
						case "B":// 蓝色
						{
							sb.append("<color=#0000ffff>");
							sb.append(tmp.substring(aa + 1, bb).replace(richSpaceStr, " "));
							sb.append("</color>");
							sb.append(tmp.substring(bb + 1));
							break;
						}
						case "P":// 紫色
						{
							sb.append("<color=#ff00ffff>");
							sb.append(tmp.substring(aa + 1, bb).replace(richSpaceStr, " "));
							sb.append("</color>");
							sb.append(tmp.substring(bb + 1));
							break;
						}
						case "O":// 橙色
						{
							sb.append("<color=#ffcc00ff>");
							sb.append(tmp.substring(aa + 1, bb).replace(richSpaceStr, " "));
							sb.append("</color>");
							sb.append(tmp.substring(bb + 1));
							break;
						}
						case "R":// 红色
						{
							sb.append("<color=#ff0000ff>");
							sb.append(tmp.substring(aa + 1, bb).replace(richSpaceStr, " "));
							sb.append("</color>");
							sb.append(tmp.substring(bb + 1));
							break;
						}
						default: {
							// 不转换
							break;
						}
						}
						if (sb.length() > 0) {
							String newTmp = sb.toString();
							// Debug.LogError ("TryParse2UnityRichText|" + i +
							// "|" + tmp + "|转换后得到|" + newTmp);
							arr[i] = newTmp;
						}

					} else {
						// 不是大些字母颜色的，则跳过
					}

				} else if (isHexString(yanseStr) && (yanseStr.length() == 6 || yanseStr.length() == 8)) {
					if (yanseStr.length() == 6) {
						StringBuilder sb = new StringBuilder(tmp.length());

						sb.append("<color=#").append(yanseStr).append("ff>");
						sb.append(tmp.substring(aa + 1, bb).replace(richSpaceStr, " "));
						sb.append("</color>");
						sb.append(tmp.substring(bb + 1));
						String newTmp = sb.toString();
						// Debug.LogError (tmp + "|转换后得到|" + newTmp);
						arr[i] = newTmp;
					}
					if (yanseStr.length() == 8) {
						StringBuilder sb = new StringBuilder(tmp.length());
						sb.append("<color=#").append(yanseStr).append(">");
						sb.append(tmp.substring(aa + 1, bb).replace(richSpaceStr, " "));
						sb.append("</color>");
						sb.append(tmp.substring(bb + 1));
						String newTmp = sb.toString();
						// Debug.LogError (tmp + "|转换后得到|" + newTmp);
						arr[i] = newTmp;
					}
				} else {
					// 也不处理
				}

			}
		}
		for (int i = startIndex; i < arr.length; i++) {
			String tmp = arr[i];
			if (tmp.length() > 0 && !tmp.startsWith("<")) {
				// Debug.LogWarning (i + "|需要补回井号|" + tmp);
				arr[i] = "#" + tmp;
			}
		}

		String strNew = StringUtil.joinArray("", arr);
		for (int i = 0; i < 10; i++) {
			strNew = replaceAll(strNew, "[[[" + i + "]]]", "{" + i + "}");
		}
		strNew = strNew.replace(richSpaceStr, richSpaceStrReal);
		return strNew;
	}

	public static boolean isHexString(String str) {
		if (str == null || str.length() < 1) {
			return true;
		}
		if (str.length() % 2 == 1) {
			// Debug.LogError ("16进制字符串的长度必须是2的倍数|" + str.length());
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			char current = str.charAt(i);
			if (!(Character.isDigit(current) || (current >= 'a' && current <= 'f')
					|| (current >= 'A' && current <= 'F'))) {
				// Debug.LogError ("有非法字符" + current + "|不是16进制了|" + str);
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		String line = "1;2,3｜4;5,6|7，8；9.1";
		int[][] ia = splitInt2d(line, 0);
		System.out.println(line + "==" + Arrays.deepToString(ia));
		long[][] la = splitLong2d(line, 0);
		System.out.println(line + "==" + Arrays.deepToString(la));
		float[][] fa = splitFloat2d(line, 0);
		System.out.println(line + "==" + Arrays.deepToString(fa));
	}
}

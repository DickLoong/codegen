package com.lizongbo.codegentool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.URL;
import java.security.Security;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONException;
import org.json.JSONObject;

import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 发送邮件
 * 
 * @author quickli
 *
 */
public class MailTest {
	static {
//		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	public static void main(String[] args) throws AddressException, MessagingException {
		// sendErrorQYWeixin("测试一下告警消息，以后告警会发企业微信消息给大家", new String[] { "@all"
		// });
	}

	public static void sendErrorMail(String title, String msgText) {
		sendErrorMail(title, msgText, null);
	}

	public static void sendErrorQYWeixin(String msgText, String[] msgto) {
		String at = getAccessToken();
		String qiweixinUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + at;
		JSONObject json = new JSONObject();
		if (msgto == null || msgto.length < 1) {
			msgto = new String[] { "quickli@billionkj.com", "yaoheng@billionkj.com", "langxing.mei@billionkj.com",
					"lihx@billionkj.com", "wangherong@billionkj.com", "liusiyuan@billionkj.com", 
					"ymw@billionkj.com", "gin@billionkj.com", "gfenlele@billionkj.com" };
		}
		try {
			json.put("touser", StringUtil.joinArray("|", msgto));
			json.put("msgtype", "text");
			json.put("agentid", 1000002);
			JSONObject textJo = new JSONObject();
			if (msgText.length() < 1024) {
				textJo.put("content", msgText);
			} else {
				textJo.put("content", msgText.substring(0, 1000) + "......更多详情请查看邮件");
			}
			json.put("text", textJo);
			String rs = downloadUrlbyPOST(qiweixinUrl, json.toString(), null, "UTF-8");
			System.out.println(rs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendErrorMail(String title, String msgText, String[] mailto) {

		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		Properties mailpro = new Properties();
		mailpro.setProperty("mail.smtp.host", "smtp.exmail.qq.com");
		mailpro.put("mail.smtp.port", "465");
		mailpro.put("mail.debug", "true");
		mailpro.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		mailpro.setProperty("mail.smtp.socketFactory.fallback", "false");
		mailpro.setProperty("mail.smtp.socketFactory.port", "465");
		Session session = Session.getDefaultInstance(mailpro);
		session.setDebug(true);
		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress("tapd@billionkj.com"));
			InternetAddress[] ia = new InternetAddress[] { new InternetAddress("quickli@billionkj.com"),
					new InternetAddress("yaoheng@billionkj.com"), new InternetAddress("langxing.mei@billionkj.com"),
					new InternetAddress("lihx@billionkj.com"), new InternetAddress("wangherong@billionkj.com"),
					new InternetAddress("liusiyuan@billionkj.com"), 
					new InternetAddress("wubin@billionkj.com"), new InternetAddress("ymw@billionkj.com"),
					new InternetAddress("gin@billionkj.com"), new InternetAddress("gfenlele@billionkj.com")

			};
			if (mailto == null || mailto.length < 1) {
				mailto = new String[] { "quickli@billionkj.com", "yaoheng@billionkj.com", "langxing.mei@billionkj.com",
						"lihx@billionkj.com", "wangherong@billionkj.com", "liusiyuan@billionkj.com",
						"wubin@billionkj.com", "ymw@billionkj.com", "gin@billionkj.com",
						"gfenlele@billionkj.com" };
			}
			if (mailto != null && mailto.length > 0) {
				ia = new InternetAddress[mailto.length];
				for (int i = 0; i < ia.length; i++) {
					ia[i] = new InternetAddress(mailto[i]);
				}
			}
			msg.setRecipients(Message.RecipientType.TO, ia);
			String localIp = getLocalIpV4();
			msg.setSubject(localIp + "|" + title);
			msg.setText(msgText + "\n from:" + localIp + "|" + System.getProperties().toString() + "|"
					+ System.getenv().toString());
			Transport.send(msg, "tapd@billionkj.com", "Ta@123");
			sendErrorQYWeixin(msgText, mailto);

		} catch (Throwable e) {
			System.out.println("sendErrorMail|fail|" + title + "|" + e);
			e.printStackTrace();
		}

	}

	private static String ipPattern = "(172\\..*)|(10\\..*)|(192\\..*)";

	private static String getLocalAddress(String pattern) {
		return getLocalAddress(Pattern.compile(pattern));
	}

	private static String getLocalAddress(Pattern pattern) {
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();
				Enumeration<InetAddress> en = ni.getInetAddresses();
				// System.out.println(ni + "|getInterfaceAddresses=" +
				// ni.getInterfaceAddresses());
				while (en.hasMoreElements()) {
					InetAddress addr = en.nextElement();
					// System.out.println(ni + "|" + addr);
					String ip = addr.getHostAddress();
					Matcher m = pattern.matcher(ip);
					if (m.matches())
						return ip;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "127.0.0.2";
	}

	/**
	 * 获取本地ipv4的ip地址
	 * 
	 * @return
	 */
	public static String getLocalIpV4() {
		String ipPattern = "(172\\..*)|(10\\..*)|(192\\..*)";
		String ip = getLocalAddress(ipPattern);
		return ip;
	}

	private static String getAccessToken() {
		try {
			String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=WWb03a5a50b54a1fb0&corpsecret=J-L_py24zNWiQFbY5JhFLmNghY6WqUeq0JqgGId-6Ug";
			String text = downloadUrl(url, "UTF-8");
			JSONObject json = new JSONObject(text);
			return json.getString("access_token");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "err";
	}

	public static String downloadUrl(String urlStr, String encoding) throws Exception {
		System.out.println("downloadUrl for:" + urlStr + "|" + encoding);
		String line = "";
		StringBuilder sb = new StringBuilder();
		HttpURLConnection httpConn = null;
		try {
			URL url = new URL(urlStr);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.lizongbo.com", 8080));
			proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7070));
			proxy = Proxy.NO_PROXY;
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

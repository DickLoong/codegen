package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.*;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import org.tmatesoft.svn.util.SVNDebugLogAdapter;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * 封装一下从svn拉取文件的公用方法 ，需要指定版本号拉取整个目录 需要列出单该文件的提交历史纪录
 * 
 * @author lizongbo
 * 
 */
public class SVNUtil {

	public static String svnUser = "quickli";
	public static String svnPass = "quicklipwd";

	/**
	 * @param args
	 * @throws SVNException
	 */
	public static void main(String[] args) throws SVNException {
		String svnUrl = "http://tc-svn.tencent.com/ied/ied_asxy_rep/asxy2_proj/trunk/asxy2client/build/iOSdevTools";
		String username = "quickli";
		String password = "quicklipwd";
		String localDir = "/Users/lizongbo/Documents/workspace/mgamedevtools/tempqqq";
		emptyDir(localDir);
		svnUrl = "https://tc-svn.tencent.com/mqq/mqq_mobileqqgame_rep/jwx_proj/trunk/Doc/%E6%95%B0%E5%80%BC%E8%AE%BE%E5%AE%9A";

		svnUrl = "http://tc-svn.tencent.com/ied/ied_asxy_rep/asxy2_proj/document/csvfilesPublic/Common/TComm%28%E5%9F%BA%E7%A1%80%E9%85%8D%E7%BD%AE%29_Channel%28%E6%B8%A0%E9%81%93%E9%85%8D%E7%BD%AE%29.csv";
		// export(svnUrl, username, password, localDir);
		List<SVNLogEntry> svnLatestLog = showHistory(svnUrl, username, password);
		System.out.println(svnLatestLog);

		/*
		 * export(svnUrl, username, password, localDir, SVNRevision
		 * .create((svnLatestLog != null && svnLatestLog.size() > 3) ?
		 * svnLatestLog .get(2).getRevision() : SVNRevision.HEAD .getNumber()));
		 */
	}

	/**
	 * 删除文件夹，包括当前文件夹及文件夹下所有文件和子文件夹
	 * 
	 * @param localDir
	 */
	public static void emptyDir(String localDir) {
		File dir = new File(localDir);
		if (dir.exists()) {
			System.out.println("emptyDir for:" + dir);
			if (dir.isDirectory()) {
				File f[] = dir.listFiles();
				for (int i = 0; f != null && i < f.length; i++) {
					File file = f[i];
					emptyDir(file.getAbsolutePath());
				}

			}
			dir.delete();
		}
	}

	/**
	 * 通过不同的协议初始化版本库
	 */
	public static void setupLibrary() {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
	}

	/**
	 * 验证登录svn
	 */
	public static SVNClientManager authSvn(String svnRoot, String username, String password) {
		// 初始化版本库
		setupLibrary();

		// 创建库连接
		SVNRepository repository = null;
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnRoot));
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}

		// 身份验证
		ISVNAuthenticationManager authManager = SVNWCUtil

				.createDefaultAuthenticationManager(username, password);

		// 创建身份验证管理器
		repository.setAuthenticationManager(authManager);

		DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
		SVNClientManager clientManager = SVNClientManager.newInstance(options, authManager);
		return clientManager;
	}

	/**
	 * chckou 文件夹
	 * 
	 * @param svnUrl
	 * @param username
	 * @param password
	 * @param localDir
	 * @return
	 * @throws SVNException
	 */
	public static long checkout(String svnUrl, String username, String password, String localDir) throws SVNException {
		SVNClientManager cm = authSvn(svnUrl, username, password);
		SVNURL url = SVNURL.parseURIEncoded(svnUrl);

		return checkout(cm, url, SVNRevision.HEAD, new File(localDir), SVNDepth.INFINITY);
	}

	public static long checkout(SVNClientManager clientManager, SVNURL url, SVNRevision revision, File destPath,
			SVNDepth depth) {

		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		try {
			return updateClient.doCheckout(url, destPath, revision, revision, depth, false);
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 导出svn资源
	 * 
	 * @param svnUrl
	 * @param username
	 * @param password
	 * @param localDir
	 * @return
	 * @throws SVNException
	 */
	public static long export(String svnUrl, String username, String password, String localDir) throws SVNException {
		SVNClientManager cm = authSvn(svnUrl, username, password);
		SVNURL url = SVNURL.parseURIEncoded(svnUrl);
		return export(svnUrl, username, password, localDir, SVNRevision.HEAD);
	}

	public static long export(String svnUrl, String username, String password, String localDir, SVNRevision rev)
			throws SVNException {
		SVNClientManager cm = authSvn(svnUrl, username, password);
		SVNURL url = SVNURL.parseURIEncoded(svnUrl);

		return export(cm, url, rev, new File(localDir), SVNDepth.INFINITY);
	}

	public static long export(SVNClientManager clientManager, SVNURL url, SVNRevision revision, File destPath,
			SVNDepth depth) {
		System.out.println("try|export｜" + url + "|" + revision + "|" + destPath + "|" + depth);

		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setDebugLog(getsvnLogger());
		updateClient.setIgnoreExternals(false);
		try {
			return updateClient.doExport(url, destPath, revision, revision, null, true, depth);
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取历史纪录，但是最多获取10条
	 * 
	 * @param svnUrl
	 * @param username
	 * @param password
	 * @return
	 */
	public static List<SVNLogEntry> showHistory(String svnUrl, String username, String password) {
		return showHistory(svnUrl, username, password, 10);
	}

	public static List<SVNLogEntry> showHistory(String svnUrl, String username, String password, int limit) {
		limit = limit >= 1 ? limit : 10;// 不能小于0
		// SVNUpdateClient updateClient = clientManager.getUpdateClient();
		try {
			SVNURL url = SVNURL.parseURIEncoded(svnUrl);
			System.out.println("showHistory|url==" + url);
			SVNRepository repository = SVNRepositoryFactory.create(url);
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username,
					password.toCharArray());
			repository.setAuthenticationManager(authManager);
			long startRevision = repository
					.getDatedRevision(new java.sql.Timestamp(System.currentTimeMillis() - 1000l * 60 * 30));
			long endRevision = repository.getLatestRevision();
			System.out.println("半小时前的版本号是" + startRevision);
			System.out.println("最新的版本号是" + endRevision);
			final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
			repository.log(new String[] { "" }, startRevision, endRevision, true, true, 100, new ISVNLogEntryHandler() {

				@Override
				public void handleLogEntry(SVNLogEntry e) throws SVNException {
					logEntries.add(e);
				}
			});
			return logEntries;

		} catch (SVNException e) {
			e.printStackTrace();
		}
		return new ArrayList<SVNLogEntry>();
	}

	public static List<SVNLogEntry> showHistory(String svnUrl, String username, String password, long startRevision,
			long endRevision, int limit) {
		limit = limit >= 1 ? limit : 10;// 不能小于0
		// SVNUpdateClient updateClient = clientManager.getUpdateClient();
		try {
			SVNURL url = SVNURL.parseURIEncoded(svnUrl);
			System.out.println("showHistory|url==" + url);
			SVNRepository repository = SVNRepositoryFactory.create(url);
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username,
					password.toCharArray());
			repository.setAuthenticationManager(authManager);

			System.out.println("半小时前的版本号是" + endRevision);
			System.out.println("最新的版本号是" + endRevision);
			final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
			repository.log(new String[] { "" }, startRevision, endRevision, true, true, limit,
					new ISVNLogEntryHandler() {

						@Override
						public void handleLogEntry(SVNLogEntry e) throws SVNException {
							logEntries.add(e);
						}
					});
			return logEntries;

		} catch (SVNException e) {
			e.printStackTrace();
		}
		return new ArrayList<SVNLogEntry>();
	}

	public static Collection<SVNLogEntry> showFileHistory(String svnUrl, String path, String username, String password,
			long version) throws SVNException {

		SVNURL url = SVNURL.parseURIEncoded(svnUrl);
		System.out.println("showHistory|url==" + url);
		SVNRepository repository = SVNRepositoryFactory.create(url);
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username,
				password.toCharArray());
		repository.setAuthenticationManager(authManager);
		String rp = repository.getRepositoryPath(path);
		System.out.println("rp=====" + rp);
		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		SvnOperationFactory operationFactory = new SvnOperationFactory();
		operationFactory.setAuthenticationManager(authManager);
		SvnLog logOperation = operationFactory.createLog();
		logOperation.setSingleTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(svnUrl + rp)));
		logOperation.setRevisionRanges(
				Collections.singleton(SvnRevisionRange.create(SVNRevision.PREVIOUS, SVNRevision.create(version))));
		Collection<SVNLogEntry> logEntries = logOperation.run(null);

		return logEntries;
	}

	private static SVNDebugLogAdapter getsvnLogger() {
		SVNDebugLogAdapter log = new SVNDebugLogAdapter() {

			@Override
			public void log(SVNLogType arg0, Throwable arg1, Level arg2) {
				System.out.println(arg0 + "|" + arg1 + "|" + arg2);
				arg1.printStackTrace();

			}

			@Override
			public void log(SVNLogType arg0, String arg1, Level arg2) {
				System.out.println(arg0 + "|" + arg1 + "|" + arg2);

			}

			@Override
			public void log(SVNLogType arg0, String arg1, byte[] arg2) {
				System.out.println(arg0 + "|" + arg1 + "|" + Arrays.toString(arg2));

			}

		};
		return log;
	}
}

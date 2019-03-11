package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import com.lizongbo.codegentool.GuidCompare;
import com.lizongbo.codegentool.MailTest;
import com.lizongbo.codegentool.csv2db.HashCalc;
import com.lizongbo.codegentool.csv2db.Pair;
import com.lizongbo.codegentool.db2java.GenAll;

public class UnityMetaFileCheckSVNTool {

	static String username = "bilin";
	static String password = "knanFabem";
	static String repoRoot = "http://10.0.0.16:12680/svn/gecaoshoulie_proj/";
	static long lastCheckRevision = -1;
	static String[] checkDirs = new String[] { "trunk/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/",
			"trunk/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/" };
	static String lastCheckedRevisionPropFile = "/tmp/MetaLastCheckedRevision.txt";
	static Properties prop = new Properties();
	static List<String> devUserList = new ArrayList<String>();

	private static List<String> errMailMsgList = new ArrayList<String>();

	/**
	 * 输出并累计出错信息，然后发邮件
	 * 
	 * @param errMsg
	 */
	public static void addErrMailMsgList(String errMsg) {
		if (errMsg == null) {
			return;
		}
		if (errMailMsgList.contains(errMsg)) {
			return;
		}
		System.err.println(errMsg);
		errMailMsgList.add(errMsg);
	}

	public static void main(String[] args) {
		devUserList.add("quickli");
		devUserList.add("ymw");
		devUserList.add("puchengcheng");
		String metaFile = "trunk/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs/saila/saila@attack01.FBX.meta";
		// Map<String, String> map = showMetaGuidSvnHistory(repoRoot, metaFile,
		// username, password);
		// System.out.println(map);
		// System.exit(0);
		if (new File(lastCheckedRevisionPropFile).exists()) {
			try {
				FileInputStream fis = new FileInputStream(lastCheckedRevisionPropFile);
				prop.load(fis);
				System.out.println("prop.load" + prop);
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			for (String checkDir : checkDirs) {
				checkDir(checkDir);
			}

			if (errMailMsgList.size() > 0) {
				String[] msgto = new String[] { "quickli@billionkj.com", "yaoheng@billionkj.com", "ymw@billionkj.com" };
				MailTest.sendErrorMail("unity meta文件检查发现有需要核实的信息", StringUtil.join("\n", errMailMsgList), msgto);
			} else {
				System.out.println("jiancha zhengchang !!!");
			}
			FileOutputStream fos = new FileOutputStream(lastCheckedRevisionPropFile);
			prop.store(fos, "lastcheck@" + LocalDateTime.now());
			System.out.println("prop.store" + prop);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkDir(String checkDir) throws SVNException {
		System.out.println("start" + checkDir);
		String svnUrl = repoRoot + checkDir;
		List<SVNLogEntry> list = showHistory(svnUrl, username, password, 10);
		for (SVNLogEntry e : list) {
			System.out.println("checkDir|" + e.getRevision() + "|" + checkDir);
		}
		for (SVNLogEntry e : list) {
			// System.out.println(e);
			for (Map.Entry<String, SVNLogEntryPath> ee : e.getChangedPaths().entrySet()) {
				SVNLogEntryPath ep = ee.getValue();
				if (ep.getPath().endsWith(".meta") && ep.getType() != SVNLogEntryPath.TYPE_ADDED) {
					// System.out.println(e.getRevision() + "|" + ep);
					if (ep.getType() == SVNLogEntryPath.TYPE_DELETED) {// 如果是删除的meta文件，则需要检查是不是非开发人员删除
						if (!devUserList.contains(e.getAuthor())) {
							addErrMailMsgList(e.getRevision() + "|" + ep.getType() + "|" + e.getAuthor() + "deleted删除了"
									+ ep + ",需要核实");
						}
					} else {
						Map<String, String> map = showMetaGuidSvnHistory(repoRoot, ep.getPath(), username, password);
						Set<String> guidSet = new HashSet<String>();
						guidSet.addAll(map.values());
						if (guidSet.size() > 1) {
							addErrMailMsgList(e.getRevision() + "|" + ep.getType() + "|" + e.getAuthor()
									+ " guidchanged有变化，需要核实：" + ep.getPath() + "|map=" + map);
						}

					}
				}
			}
		}

		System.out.println("ending" + checkDir);
	}

	public static List<SVNLogEntry> showHistory(String svnUrl, String username, String password, int limit)
			throws SVNException {
		limit = limit >= 1 ? limit : 10;// 不能小于0
		SVNURL url = SVNURL.parseURIEncoded(svnUrl);
		System.out.println("showHistory|url==" + url);
		SVNRepository repository = SVNRepositoryFactory.create(url);
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username,
				password.toCharArray());
		repository.setAuthenticationManager(authManager);
		System.out.println("prop======111111111=======" + prop);
		long startRevision = StringUtil.toInt(prop.getProperty(svnUrl), -1);
		if (startRevision < 0) {
			startRevision = repository
					.getDatedRevision(new java.sql.Timestamp(System.currentTimeMillis() - 1000l * 60 * 60 * 24));
		}
		long endRevision = repository.getLatestRevision();
		prop.setProperty(svnUrl, "" + endRevision);
		System.out.println("showHistory startRevision = " + startRevision);
		System.out.println("showHistory endRevision = " + endRevision);

		System.out.println("prop======22222222=======" + prop);
		final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
		if (endRevision <= startRevision) {
			System.out.println("noNewRevisionCommited,so ignore");
			return logEntries;
		}
		repository.log(new String[] { "" }, startRevision, endRevision, true, true, 100, new ISVNLogEntryHandler() {

			@Override
			public void handleLogEntry(SVNLogEntry e) throws SVNException {
				logEntries.add(e);
			}
		});
		return logEntries;
	}

	public static List<Long> showMetaFileSvnHistory(String metaFileSvnUrl, String filePath, String username,
			String password) throws SVNException {
		List<Long> revList = new ArrayList<Long>();
		SVNURL url = SVNURL.parseURIEncoded(metaFileSvnUrl);
		System.out.println("showMetaFileSvnHistory|url==" + url);
		SVNRepository repository = SVNRepositoryFactory.create(url);
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username,
				password.toCharArray());
		repository.setAuthenticationManager(authManager);
		long startRevision = 1;
		long endRevision = repository.getLatestRevision();
		List revisions = new ArrayList();
		try {
			repository.getFileRevisions(filePath, revisions, startRevision, endRevision);
		} catch (Exception e) {
			System.err.println("showMetaFileSvnHistory|" + filePath + e);
		}
		for (Object obj : revisions) {
			if (obj instanceof org.tmatesoft.svn.core.io.SVNFileRevision) {
				SVNFileRevision fr = (SVNFileRevision) obj;
				revList.add(fr.getRevision());
			}
		}
		System.out.println("showMetaFileSvnHistory|" + revList + "|" + filePath);
		return revList;
	}

	public static String exportMetaFile(String metaFileSvnUrl, String username, String password, SVNRevision rev)
			throws SVNException {
		SVNURL url = SVNURL.parseURIEncoded(metaFileSvnUrl);
		// System.out.println("exportMetaFile|rev=" + rev + "|url==" + url);
		SVNRepository repository = SVNRepositoryFactory.create(url);
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username,
				password.toCharArray());
		repository.setAuthenticationManager(authManager);
		SVNClientManager ourClientManager = SVNClientManager.newInstance(null, repository.getAuthenticationManager());
		SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		File dstPath = new File("/tmp/svnkittmp/" + HashCalc.md5(metaFileSvnUrl) + "_" + rev + ".meta");
		dstPath.getParentFile().mkdirs();
		updateClient.doExport(url, dstPath, rev, rev, "\n", true, SVNDepth.FILES);
		String guid = GuidCompare.getGuid(dstPath);
		dstPath.delete();
		return guid;
	}

	public static Map<String, String> showMetaGuidSvnHistory(String repoRoot, String metaFile, String username,
			String password) throws SVNException {
		if (!metaFile.endsWith(".meta")) {
			return null;
		}
		Map<String, String> map = new TreeMap<String, String>();
		List<Long> pair = showMetaFileSvnHistory(repoRoot, metaFile, username, password);
		if (pair.size() < 1) {// 可能是已经删除的文件
			return map;
		}
		String metaFileSvnUrl = repoRoot + metaFile;
		for (Long rev : pair) {
			if (rev > 30000) {/// 30000版本号以前的忽略掉了
				String guidCrt = exportMetaFile(metaFileSvnUrl, username, password, SVNRevision.create(rev));
				map.put("" + rev, guidCrt);
			}
		}
		String guidCrt = exportMetaFile(metaFileSvnUrl, username, password, SVNRevision.HEAD);
		map.put("HEAD", guidCrt);
		return map;
	}
}

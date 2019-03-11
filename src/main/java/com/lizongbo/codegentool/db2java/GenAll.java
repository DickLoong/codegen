package com.lizongbo.codegentool.db2java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.lizongbo.codegentool.CodeGenConsts;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.db2java.dbsql2xml.XmlCodeGen;
import com.lizongbo.codegentool.db2java.dbxml2java.JavaCodeGen;

public class GenAll {

	public static void main(String[] args) throws Exception {

		CodeGenConsts.switchPlat();
		GameCSV2DB.checkIsGapp();
		freemarker.ext.dom.NodeModel.useJaxenXPathSupport();
		String dbName = "laq";
		dbName = CodeGenConsts.PROJDBNAME;
		// CodeGenConsts.fmppDir4DB =
		// "/Users/lizongbo/Documents/workspace/sango_codegentool/WEB-INF/fmpp";
		// CodeGenConsts.fmppDir4DB =
		// "/Users/lizongbo/Documents/workspace/sango_codegentool/WEB-INF/fmpp";

		genAll(dbName);

		String fmppConfigName = "config_" + CodeGenConsts.PROJNAME;
		JavaCodeGen.genJava(CodeGenConsts.fmppDir4ProtoCmd, fmppConfigName,
				CodeGenConsts.PROJPROTO_JAVASRCROOT);
		JavaCodeGen.genJava(CodeGenConsts.fmppDir4ProtoCmd2Unity,
				fmppConfigName, CodeGenConsts.PROJPROTO_UnitySRCROOT);
		// 准备在这里加上java代码格式化
		//com.google.googlejavaformat.java.Main gg;
	}

	public static void genAll(String dbName) throws IOException {

		// 生成之前先把代码存放目录清空
		delDir(new File(".").getAbsoluteFile() + "/WEB-INF/" + "src-gen-"
				+ dbName + "/com", true);
		delDir(new File(new File("."), "crud_" + dbName).getAbsolutePath(),
				true);
		XmlCodeGen.genXml(dbName);
		JavaCodeGen.genJava(CodeGenConsts.fmppDir4DB, "config_" + dbName,
				CodeGenConsts.PROJDBBEANS_JAVASRCROOT);
		if (dbName.equals("db_cobra_hall")
				|| dbName.equals("db_minigame_hall_gift_config_dc")
				|| dbName.equals("db_gp_hall")
				|| dbName.equals("db_mqqgameplat_guessgame")
				|| dbName.equals("db_gp_website") || dbName.equals("charbayes")) {
			System.out.println("try copy file!!!");
			// 只有这两个需要直接复制过去
			String webappRootDir = "D:\\Users\\quickli\\workspace\\crudadmindemo\\crudweb\\crud_"
					+ dbName;
			String webappSourceDir = "D:\\Users\\quickli\\workspace\\mg_qq_com_web_proj\\webapp\\web_mqqgame_admin\\WEB-INF\\src";
			webappSourceDir = "D:\\Users\\quickli\\workspace\\crudadmindemo\\WEB-INF\\src-gen-"
					+ dbName;
			String cruddemoProjectDir = "D:\\Users\\Administrator\\workspace\\crudadmindemo";
			String protocExePath = "D:\\cpplibs\\protoc\\protoc.exe";
			if (new File(protocExePath).exists()) {// 调用命令根据proto文件生成java文件。
				System.out.println("doprotoc");
				Process p = Runtime.getRuntime().exec(
						new String[] {
								protocExePath,
								" --proto_path="
										+ (new File(new File(".")
												.getAbsoluteFile()
												+ "/WEB-INF/"
												+ "src-gen-"
												+ dbName + "/protofiles")),
								" --java_out="
										+ (new File(new File(".")
												.getAbsoluteFile()
												+ "/WEB-INF/"
												+ "src-gen-"
												+ dbName)),
								(new File(new File(".").getAbsoluteFile()
										+ "/WEB-INF/" + "src-gen-" + dbName
										+ "/protofiles"))
										+ "/*.proto" });
			}
			if (new File(cruddemoProjectDir).exists()) {
				webappRootDir = cruddemoProjectDir + "\\crud_" + dbName;
				webappSourceDir = cruddemoProjectDir + "\\WEB-INF\\src-gen-"
						+ dbName;
			}
			File javaFileDir = new File(new File(".").getAbsoluteFile()
					+ "/WEB-INF/" + "src-gen-" + dbName + "/com");
			File protoFileDir = new File(new File(".").getAbsoluteFile()
					+ "/WEB-INF/" + "src-gen-" + dbName + "/protofiles");
			if (dbName.equals("db_cobra_hall") || dbName.equals("charbayes")) {// 其它db的不copy代码了
				System.out.println("try copy "
						+ javaFileDir.getCanonicalPath()
						+ " | to "
						+ (new File(webappSourceDir + "/com"))
								.getCanonicalPath());
				GenAll.copyDir(javaFileDir, new File(webappSourceDir + "/com"));// 把生成的java文件复制过去
				GenAll.copyDir(protoFileDir, new File(webappSourceDir
						+ "/protofiles"));// 把生成的java文件复制过去
			}
			File jspFileDir = new File(new File("."), "crud_" + dbName);
			GenAll.copyDir(jspFileDir, new File(webappRootDir));// 把生成的jsp复制过去
		}
		// 加上复制代码的逻辑
		// dbName = "db_conf_2";
		// XmlCodeGen.genXml(dbName);
		// JavaCodeGen.genJava("config_" + dbName);
		// dbName = "db_adconf_2";
		// XmlCodeGen.genXml(dbName);
		// JavaCodeGen.genJava("config_" + dbName);
	}

	/**
	 * 拷贝一个文件
	 * 
	 * @param src
	 *            原始文件
	 * @param target
	 *            新文件
	 * @return 是否拷贝成功
	 * @throws IOException
	 */
	public static boolean copyFile(File src, File target) throws IOException {
		if ((src == null) || (target == null) || !src.exists()) {
			return false;
		}

		// System.out.println("copyFile|form|" + src.getAbsolutePath() + "|to|"
		// + target.getAbsolutePath());

		if (target.isFile() && target.exists()
				&& target.getName().endsWith(".java")) {// 如果是java文件，则判断，文件内容里是否有not_be_rewrite,有的则不再覆盖
														// not_be_rewrite
			String javasrc = readFile(target.getAbsolutePath(), "UTF-8");
			if ((javasrc != null) && javasrc.contains("not_be_rewrite")) {
				System.out.println("ignore for :" + target);
				return false;
			}
		}
		InputStream ins = new BufferedInputStream(new FileInputStream(src));
		OutputStream ops = new BufferedOutputStream(
				new FileOutputStream(target));
		int b;
		while (-1 != (b = ins.read())) {
			ops.write(b);
		}

		safeClose(ins);
		safeFlush(ops);
		safeClose(ops);
		return target.setLastModified(src.lastModified());
	}

	/**
	 * 自动决定是 copy 文件还是目录
	 * 
	 * @param src
	 *            源
	 * @param target
	 *            目标
	 * @return 是否 copy 成功
	 */
	public static boolean copy(File src, File target) {
		try {
			if (src.isDirectory()) {
				return copyDir(src, target);
			}
			return copyFile(src, target);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 拷贝一个目录
	 * 
	 * @param src
	 *            原始目录
	 * @param target
	 *            新目录
	 * @return 是否拷贝成功
	 * @throws IOException
	 */
	public static boolean copyDir(File src, File target) throws IOException {
		if ((src == null) || (target == null) || !src.exists()) {
			return false;
		}
		if (!src.isDirectory()) {
			throw new IOException(src.getAbsolutePath()
					+ " should be a directory!");
		}
		if (!target.exists()) {
			if (!makeDir(target)) {
				return false;
			}
		}
		boolean re = true;
		File[] files = src.listFiles();
		if (null != files) {
			for (File f : files) {
				if (f.isFile()) {
					re &= copyFile(f, new File(target.getAbsolutePath() + "/"
							+ f.getName()));
				} else {
					re &= copyDir(f, new File(target.getAbsolutePath() + "/"
							+ f.getName()));
				}
			}
		}
		return re;
	}

	/**
	 * 创建新目录，如果父目录不存在，也一并创建。可接受 null 参数
	 * 
	 * @param dir
	 *            目录对象
	 * @return false，如果目录已存在。 true 创建成功
	 * @throws IOException
	 */
	public static boolean makeDir(File dir) {
		if ((null == dir) || dir.exists()) {
			return false;
		}
		return dir.mkdirs();
	}

	/**
	 * 关闭一个可关闭对象，可以接受 null。如果成功关闭，返回 true，发生异常 返回 false
	 * 
	 * @param cb
	 *            可关闭对象
	 * @return 是否成功关闭
	 */
	public static boolean safeClose(Closeable cb) {
		if (null != cb) {
			try {
				cb.close();
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 创建新文件，如果父目录不存在，也一并创建。可接受 null 参数
	 * 
	 * @param f
	 *            文件对象
	 * @return false，如果文件已存在。 true 创建成功
	 * @throws IOException
	 */
	public static boolean createNewFile(File f) throws IOException {
		if ((null == f) || f.exists()) {
			return false;
		}
		makeDir(f.getParentFile());
		return f.createNewFile();
	}

	public static void safeFlush(Flushable fa) {
		if (null != fa) {
			try {
				fa.flush();
			} catch (IOException e) {
			}
		}
	}

	public static String readFile(String path, String encoding) {
		if(path.contains("DbbeansConfig4Proto.proto.md5.txt")){
			System.out.println("DbbeansConfig4Proto.proto.md5.txt meici shengcheng");
			return "nomd5";
		}
		StringBuilder sb = new StringBuilder();
		File readFile;
		try {
			readFile = new File(path);
			// 如果文本文件不存在则返回空串
			if (!readFile.exists()) {
				return "";
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), encoding));

			String data = null;
			while ((data = br.readLine()) != null) {
				sb.append(data).append("\n");
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}

	private static boolean delAllFile(String path) {
		boolean bea = false;
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; tempList != null && i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			} else if (temp.isDirectory()) {
				delAllFile(path + File.separator + tempList[i]);
				delDir(path + File.separator + tempList[i], true);
				bea = true;
			}
		}
		return bea;
	}

	private static void delDir(String folderPath, boolean dirDel) {
		try {
			delAllFile(folderPath);
			if (dirDel) {
				String filePath = folderPath;
				filePath = filePath.toString();
				java.io.File myFilePath = new java.io.File(filePath);
				myFilePath.delete();
			}
		} catch (Exception e) {

		}
	}
}

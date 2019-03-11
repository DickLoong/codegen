package com.lizongbo.codegentool.db2java.dbxml2java;

import java.io.File;

import com.lizongbo.codegentool.CodeGenConsts;
import com.lizongbo.codegentool.db2java.dbsql2xml.XmlCodeGen;

import fmpp.ProcessingException;
import fmpp.progresslisteners.ConsoleProgressListener;
import fmpp.setting.SettingException;
import fmpp.setting.Settings;

public class JavaCodeGen {

	public static void main(String[] args) throws Exception {
		freemarker.ext.dom.NodeModel.useJaxenXPathSupport();
		String fmppConfigName = "config";
		fmppConfigName = "config_mgamedb_sango";
		genJava(CodeGenConsts.fmppDir4DB, fmppConfigName,
				CodeGenConsts.PROJDBBEANS_JAVASRCROOT);
		fmppConfigName = "config_sango";
		
		genJava(CodeGenConsts.fmppDir4ProtoCmd, fmppConfigName,
				CodeGenConsts.PROJPROTO_JAVASRCROOT);

		genJava(CodeGenConsts.fmppDir4ProtoCmd2Unity, fmppConfigName,
				CodeGenConsts.PROJPROTO_UnitySRCROOT);

	}

	public static void genJava(String fmppDir, String fmppConfigName,
			String outputDir) {
		File cfgFile = new File(fmppDir, fmppConfigName + ".fmpp");
		System.out.println("cfgFile ==" + cfgFile);
		File templateDirectory = new File(fmppDir
				+ "/templates/data/ignoredir.fmpp").getParentFile()
				.getParentFile();
		System.out.println("templateDirectory ==" + templateDirectory);
		File outputDirectory = new File(outputDir);
		System.out.println("outputDirectory ==" + outputDirectory);
		// outputDirectory = new File(".");
		try {
			Settings settings = new Settings(new File("."));
			System.out.println("CURRENT DIR == "
					+ new File(".").getAbsolutePath());
			settings.set("sourceRoot", templateDirectory.getAbsolutePath());
			settings.set("outputRoot", outputDirectory.getAbsolutePath());
			settings.load(cfgFile);
			settings.addProgressListener(new ConsoleProgressListener());
			settings.execute();

		} catch (SettingException e) {
			e.printStackTrace();

		} catch (ProcessingException e) {
			e.printStackTrace();
		}
	}
}

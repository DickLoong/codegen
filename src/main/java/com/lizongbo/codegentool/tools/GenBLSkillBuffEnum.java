package com.lizongbo.codegentool.tools;

import java.io.File;
import java.util.List;

import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.db2java.dbsql2xml.DBUtil;

/**
 * 生成技能buff的枚举和基类
 * 
 * @author quickli
 *
 */
public class GenBLSkillBuffEnum {

	public static void main(String[] args) {
		String skillbuffCsv = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TBLSkillBuff(buff配置)_Type(buff类型).csv";
		genBuffEnumcs(skillbuffCsv);
		// genBuffAbstractcs(skillbuffCsv);
		genBuffCsFile(skillbuffCsv);
		genBuffToolcs(skillbuffCsv);
	}

	public static void genBuffEnumcs(String skillbuffCsv) {
		List<String[]> colList = CSVUtil.getDataFromCSV2(skillbuffCsv);
		StringBuilder sb = new StringBuilder();
		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("\n");
		sb.append("namespace Bilinkeji.NewPk.Core\n");
		sb.append("{\n");
		sb.append("	/// <summary>\n");
		sb.append("	/// 技能buff类型，根据 " + new File(skillbuffCsv).getName() + " 来代码生成枚举.\n");
		sb.append("	/// </summary>\n");
		sb.append("	public enum BLSkillBuffType\n");
		sb.append("	{\n");
		// sb.append(" /// <summary>\n");
		// sb.append(" /// 通用 buff \n");
		// sb.append(" /// </summary>\n");
		// sb.append(" BUFFTYPE_COMMON = 0,\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String buffid = CSVUtil.getColValue("buff_type_id", s, colList);
			String buff_name = CSVUtil.getColValue("buff_type_name", s, colList);
			String buff_enum = CSVUtil.getColValue("buff_type_enum", s, colList).toUpperCase();
			sb.append("		/// <summary>\n");
			sb.append("		/// " + buff_name + " buff \n");
			sb.append("		/// </summary>\n");
			sb.append("		BUFFTYPE_" + buff_enum + " = " + buffid + ",\n");
		}
		sb.append("\n");
		sb.append("	}\n");
		sb.append("}\n");

		String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/NewPk/Core/Enum/BLSkillBuffType.cs";

		GameCSV2DB.writeFile(csFilePath, sb.toString(), "UTF-8");
	}

	public static void genBuffAbstractcs(String skillbuffCsv) {
		List<String[]> colList = CSVUtil.getDataFromCSV2(skillbuffCsv);
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String buffid = CSVUtil.getColValue("buff_type_id", s, colList);
			String buff_name = CSVUtil.getColValue("buff_type_name", s, colList);
			String buff_enum = CSVUtil.getColValue("buff_type_enum", s, colList).toUpperCase();
			if (buff_enum == null || buff_enum.trim().length() < 1) {// 只对有枚举的定制buff生成文件
				continue;
			}
			String className = CSVUtil.capFirst(DBUtil.camelName(buff_enum.toLowerCase()));
			StringBuilder sb = new StringBuilder();
			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("using Bilinkeji.NewPk.Core;\n");
			sb.append("\n");
			sb.append("namespace Bilinkeji.NewPk\n");
			sb.append("{\n");
			sb.append("\n");
			sb.append("	/// <summary>\n");
			sb.append("	/// " + buff_name + " buff 执行器的父类\n");
			sb.append("	/// </summary>\n");
			sb.append("	public abstract class " + className + "BaseBLSkillBuffRunner : BLSkillBuffRunner\n");
			sb.append("	{\n");
			sb.append("		\n");
			sb.append("\n");
			sb.append("		public override BLSkillBuffType GetBLSkillBuffType ()\n");
			sb.append("		{\n");
			sb.append("			return BLSkillBuffType.BUFFTYPE_" + buff_enum + ";\n");
			sb.append("		}\n");
			sb.append("	}\n");
			sb.append("}\n");
			String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/NewPk/SkillController/SkillBuffRunners/BaseSkillBuffRunners/"
					+ className + "BaseBLSkillBuffRunner.cs";
			GameCSV2DB.writeFile(csFilePath, sb.toString(), "UTF-8");
		}
	}

	private static void genBuffCsFile(String skillbuffCsv) {
		List<String[]> colList = CSVUtil.getDataFromCSV2(skillbuffCsv);
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String buffid = CSVUtil.getColValue("buff_type_id", s, colList);
			String buff_name = CSVUtil.getColValue("buff_type_name", s, colList);
			String buff_enum = CSVUtil.getColValue("buff_type_enum", s, colList).toUpperCase();
			String className = CSVUtil.capFirst(DBUtil.camelName(buff_enum.toLowerCase()));
			StringBuilder sb = new StringBuilder();
			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("using Bilinkeji.NewPk.Core;\n");
			sb.append("\n");
			sb.append("namespace Bilinkeji.NewPk\n");
			sb.append("{\n");
			sb.append("\n");
			sb.append("	/// <summary>\n");
			sb.append("	/// " + buff_name + " buff 执行器 的实现类\n");
			sb.append("	/// </summary>\n");
			sb.append("	public class " + className + "BLSkillBuffRunner : BLSkillBuffRunner \n");
			sb.append("	{\n");
			sb.append("		\n");
			sb.append("\n");
			// sb.append(" public " + className + "BLSkillBuffRunner (int
			// buffId) : base (buffId)\n");
			sb.append("		public " + className
					+ "BLSkillBuffRunner (int buffId, Role sender) : base (buffId, sender)\n");
			sb.append("		{\n");
			sb.append("		}\n");
			sb.append("		public override void OnUpdate (float deltaTime)\n");
			sb.append("		{\n");
			sb.append("			\n");
			sb.append("		}\n");
			sb.append("		public override BLSkillBuffType GetBLSkillBuffType ()\n");
			sb.append("		{\n");
			sb.append("			return BLSkillBuffType.BUFFTYPE_" + buff_enum + ";\n");
			sb.append("		}\n");
			sb.append("	}\n");
			sb.append("}\n");
			String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/NewPk/SkillController/SkillBuffRunners/"
					+ className + "BLSkillBuffRunner.cs";
			GameCSV2DB.writeFile(csFilePath, sb.toString(), "UTF-8");
		}

	}

	public static void genBuffToolcs(String skillbuffCsv) {
		List<String[]> colList = CSVUtil.getDataFromCSV2(skillbuffCsv);
		StringBuilder sb = new StringBuilder();
		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("using Bilinkeji.NewPk.Core;\n");
		sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto;\n");
		sb.append("\n");
		sb.append("namespace Bilinkeji.NewPk\n");
		sb.append("{\n");
		sb.append("	public class BLSkillBuffRunnerTool\n");
		sb.append("	{\n");
		sb.append("\n");
		sb.append("		public static BLSkillBuffRunner NewBLSkillBuffRunner (int buffId, Role sender)\n");
		sb.append("		{\n");
		sb.append("\n");
		sb.append(
				"			TblskillbuffBuff4Proto buffConfig = TblskillbuffBuff4ProtoArrayCsvHelper.GetById (buffId);\n");
		sb.append("			if (buffConfig == null) {\n");
		sb.append("				return null;\n");
		sb.append("			}\n");
		sb.append("			switch (buffConfig.buffTypeId) {\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String buffid = CSVUtil.getColValue("buff_type_id", s, colList);
			String buff_name = CSVUtil.getColValue("buff_type_name", s, colList);
			String buff_enum = CSVUtil.getColValue("buff_type_enum", s, colList).toUpperCase();
			String className = CSVUtil.capFirst(DBUtil.camelName(buff_enum.toLowerCase()));
			if (buff_enum == null || buff_enum.trim().length() < 1) {// 只对有枚举的定制buff生成文件
				continue;
			}
			sb.append("			case (int)BLSkillBuffType.BUFFTYPE_" + buff_enum + ":\n");
			sb.append("				return new " + className + "BLSkillBuffRunner (buffId, sender);\n");
			sb.append("				break;\n");
			sb.append("\n");
		}

		sb.append("			default: \n");
		sb.append("				return null;\n");
		sb.append("				break;\n");
		sb.append("			}\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append("	}\n");
		sb.append("}\n");
		String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/NewPk/SkillController/SkillBuffRunners/BLSkillBuffRunnerTool.cs";
		GameCSV2DB.writeFile(csFilePath, sb.toString(), "UTF-8");
	}

}

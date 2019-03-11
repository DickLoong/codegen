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
public class GenBLSkillTargetSelectEnum {

	public static void main(String[] args) {
		String skillbuffCsv = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TBLSkill(技能配置)_Targetselect(目标选择).csv";
		genTargetSelectEnumcs(skillbuffCsv);
		// genTargetSelectAbstractcs(skillbuffCsv);
		genTargetSelectCsFile(skillbuffCsv);
		genTargetSelectToolcs(skillbuffCsv);
	}

	public static void genTargetSelectEnumcs(String skillbuffCsv) {
		List<String[]> colList = CSVUtil.getDataFromCSV2(skillbuffCsv);
		StringBuilder sb = new StringBuilder();
		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("namespace Bilinkeji.NewPk.Core\n");
		sb.append("{\n");
		sb.append("\n");
		sb.append("	/// <summary>\n");
		sb.append("	/// 技能目标选择类型的枚举，根据 " + new File(skillbuffCsv).getName() + " 来代码生成枚举\n");
		sb.append("	/// </summary>\n");
		sb.append("	public enum BLSkillTargetSelectType\n");
		sb.append("	{\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String buffid = CSVUtil.getColValue("id", s, colList);
			String buff_name = CSVUtil.getColValue("select_desc", s, colList);
			String buff_enum = CSVUtil.getColValue("enum_name", s, colList).toUpperCase();

			sb.append("		/// <summary>\n");
			sb.append("		/// " + buff_name + " \n");
			sb.append("		/// </summary>\n");
			sb.append("		SELECTTYPE_" + buff_enum + " = " + buffid + ",\n");
			sb.append("		/// <summary>\n");
		}
		sb.append("\n");
		sb.append("	}\n");
		sb.append("}\n");

		String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/NewPk/Core/Enum/BLSkillTargetSelectType.cs";

		GameCSV2DB.writeFile(csFilePath, sb.toString(), "UTF-8");
	}

	private static void genTargetSelectCsFile(String skillbuffCsv) {
		List<String[]> colList = CSVUtil.getDataFromCSV2(skillbuffCsv);
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String buffid = CSVUtil.getColValue("id", s, colList);
			String buff_name = CSVUtil.getColValue("select_desc", s, colList);
			String buff_enum = CSVUtil.getColValue("enum_name", s, colList).toUpperCase();
			String className = CSVUtil.capFirst(DBUtil.camelName(buff_enum.toLowerCase()));
			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("using Bilinkeji.NewPk.Core;\n");
			sb.append("using System.Collections.Generic;\n");
			sb.append("\n");
			sb.append("namespace Bilinkeji.NewPk\n");
			sb.append("{\n");
			sb.append("	/// <summary>\n");
			sb.append("	/// " + buff_name + "的角色选择器\n");
			sb.append("	/// </summary>\n");
			sb.append("	public class " + className + "BLSkillTargetSelector : BLSkillTargetSelector\n");
			sb.append("	{\n");
			sb.append("		public override List<Role> SelectTargetRoles (Role sender)\n");
			sb.append("		{\n");
			sb.append("			return new List<Role> ();\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("		public override BLSkillTargetSelectType GetTargetSelectType ()\n");
			sb.append("		{\n");
			sb.append("			return BLSkillTargetSelectType.SELECTTYPE_" + buff_enum + ";\n");
			sb.append("		}\n");
			sb.append("	}\n");
			sb.append("}\n");

			String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/NewPk/SkillController/SkillTargetSelectors/"
					+ className + "BLSkillTargetSelector.cs";
			if (new File(csFilePath).exists()) {
				return;
			}
			GameCSV2DB.writeFile(csFilePath, sb.toString(), "UTF-8");
		}

	}

	public static void genTargetSelectToolcs(String skillbuffCsv) {
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
		sb.append("	public class BLSkillTargetSelectorTool\n");
		sb.append("	{\n");
		sb.append("\n");
		sb.append(
				"		public static BLSkillTargetSelector NewBLSkillTargetSelector (int selectTypeId, Role sender)\n");
		sb.append("		{\n");
		sb.append("\n");
		sb.append("			switch (selectTypeId) {\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String buffid = CSVUtil.getColValue("id", s, colList);
			String buff_name = CSVUtil.getColValue("select_desc", s, colList);
			String buff_enum = CSVUtil.getColValue("enum_name", s, colList).toUpperCase();
			String className = CSVUtil.capFirst(DBUtil.camelName(buff_enum.toLowerCase()));
			if (buff_enum == null || buff_enum.trim().length() < 1) {// 只对有枚举的定制buff生成文件
				continue;
			}
			sb.append("			case (int)BLSkillTargetSelectType.SELECTTYPE_" + buff_enum + ":\n");
			sb.append("				return new " + className + "BLSkillTargetSelector ();\n");
			sb.append("				break;\n");
			sb.append("							\n");
		}
		sb.append("\n");
		sb.append("			default: \n");
		sb.append("				return null;\n");
		sb.append("				break;\n");
		sb.append("			}\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append("	}\n");
		sb.append("}\n");

		String csFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts/Bilinkeji/NewPk/SkillController/SkillTargetSelectors/BLSkillTargetSelectorTool.cs";
		GameCSV2DB.writeFile(csFilePath, sb.toString(), "UTF-8");
	}

}

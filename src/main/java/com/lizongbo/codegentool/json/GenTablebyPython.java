package com.lizongbo.codegentool.json;

import com.lizongbo.codegentool.db2java.GenAll;

public class GenTablebyPython {

	public static void main(String[] args) {
		String pyPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/src/com/lizongbo/codegentool/templeles/models.py";
		String str = GenAll.readFile(pyPath, "UTF-8");
		String[] classArr = str.split("class ");
		for (int i = 0; i < classArr.length; i++) {
			String classDef = classArr[i];
			if (classDef.contains("models.Model")) {
				String className = classDef.substring(0, classDef.indexOf("("));
				String propArr[] = classDef.split("\n");
				StringBuilder sbName = new StringBuilder();
				StringBuilder sbType = new StringBuilder();
				StringBuilder sbTitle = new StringBuilder();
				for (int j = 0; j < propArr.length; j++) {
					String propDef = propArr[j].trim().replace('\"', '\'');
					if (!propDef.startsWith("#") && propDef.contains("=") && propDef.contains("models.")) {// 非注释字段
						String propName = "";
						String propType = "str";
						String propTitle = "";
						propName = propDef.substring(0, propDef.indexOf("=")).trim();
						propTitle = propDef
								.substring(propDef.indexOf("'") + 1, propDef.indexOf("'", propDef.indexOf("'") + 2))
								.trim();
						if (propDef.contains("BooleanField") || propDef.contains("IntegerField")) {
							propType = "int";
						}
						sbName.append(propName).append("\t");
						sbType.append(propType).append("\t");
						sbTitle.append(propTitle).append("\t");
						// System.out.println(className + "." + propType + " " +
						// propName + ";//" + propTitle);
					}
				}
				System.out.println();
				System.out.println(className);
				System.out.println(sbName);
				System.out.println(sbType);
				System.out.println(sbTitle);
			}

		}

	}

}

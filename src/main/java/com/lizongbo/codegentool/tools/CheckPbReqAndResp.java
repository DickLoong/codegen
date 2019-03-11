package com.lizongbo.codegentool.tools;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lizongbo.codegentool.csv2db.CSVUtil;

/**
 * 检查req 和 resp的协议是否合理
 * 
 * @author quickli
 *
 */
public class CheckPbReqAndResp {

	public static void main(String[] args) {
		String f = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/ProtobufFiles/TprotoCmds";
		checkPbCsv(new File(f));
		for (Map.Entry<String, Integer> e : paramRepeatCount.entrySet()) {
			if (e.getValue() > 4) {
				System.out.println(e);
			}
		}
	}

	static Map<String, Integer> paramRepeatCount = new TreeMap<String, Integer>();

	public static void checkPbCsv(File file) {
		if (file.isDirectory()) {
			File fs[] = file.listFiles();
			for (File f : fs) {
				checkPbCsv(f);
			}
		} else if (file.isFile()) {
			if (file.getName().endsWith("ProtoBufRequest.csv") || file.getName().endsWith("ProtoBufResponse.csv")) {
				List<String[]> colList = CSVUtil.getDataFromCSV2(file.getAbsolutePath());
				for (int i = 5; i < colList.size(); i++) {
					String[] s = colList.get(i);
					String propSeq = CSVUtil.getReqRespColValue("propSeq", s, colList);
					String propOption = CSVUtil.getReqRespColValue("propOption", s, colList);
					String propType = CSVUtil.getReqRespColValue("propType", s, colList);
					String propName = CSVUtil.getReqRespColValue("propName", s, colList);
					String propDefaultValue = CSVUtil.getReqRespColValue("propDefaultValue", s, colList);
					String propDesc = CSVUtil.getReqRespColValue("propDesc", s, colList);
					if (file.getName().endsWith("ProtoBufRequest.csv")) {// 先只看请求的参数
						if (!propType.equals("bytes") && !propType.equals("int32") && !propType.equals("int64")
								&& !propType.equals("float") && !propType.equals("int") && !propType.equals("string")
								&& !propType.endsWith("IntIntMapEntry") && !propType.endsWith("LongIntMapEntry")
								&& !propType.equals("MapEntry") && !propType.endsWith(".MapEntry")) {
							// if (propName.equals("clientTimeLong")) {
							System.out.println(file.getName() + "|propSeq=" + propSeq + ",propOption=" + propOption
									+ ",propType=" + propType + ",propName=" + propName + ",propDefaultValue="
									+ propDefaultValue + ",propDesc=" + propDesc + ",");
							// }

						}
						if (propType.startsWith("int") && propOption.equals("optional")) {

						}

					}

					if (file.getName().endsWith("xxxProtoBufResponse.csv")) {// 再看应答的参数
						if (!propType.equals("bytes") && !propType.equals("int32") && !propType.equals("int64")
								&& !propType.equals("long") && !propType.equals("float") && !propType.equals("int")
								&& !propType.equals("string") && !propType.endsWith("IntIntMapEntry")
								&& !propType.endsWith("LongIntMapEntry") && !propType.equals("MapEntry")
								&& !propType.endsWith(".MapEntry")) {
							int c = paramRepeatCount.getOrDefault(propOption + propType, 0);
							c++;
							paramRepeatCount.put(propOption + propType, c);
							System.out.println(file.getName() + "|propSeq=" + propSeq + ",propOption=" + propOption
									+ ",propType=" + propType + ",propName=" + propName + ",propDefaultValue="
									+ propDefaultValue + ",propDesc=" + propDesc + ",");
						}
					}
				}

			}
		}
	}
}

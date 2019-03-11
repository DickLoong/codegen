package com.lizongbo.codegentool.configverification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.IllegalDataException;

import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.tools.StringUtil;

public class LotteryOddsVerifyUtil {
	public static void main(String[] args) {
		String csvFile = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\csvfiles\\Public\\Common\\TLottery(抽奖配置表)_Recruitequipmentodds(抽卡抽装概率表).csv";
		checkOddis(csvFile);
	}

	public static void checkOddis(String csvFile) {
		List<String[]> odds = CSVUtil.getDataFromCSV2(csvFile);
		Map<Integer, Integer> itemBoxOdds = new HashMap<Integer, Integer>();
		// TODO先验证每个物品池的数量概率对不对
		for (String[] itemOdd : odds) {
			try {
				// 检测是否一个配置项
				Integer.parseInt(CSVUtil.getColValue("id", itemOdd, odds));
			} catch (NumberFormatException ex) {
				continue;
			}
			int[] numStr = StringUtil.splitInt(CSVUtil.getColValue("num_int", itemOdd, odds), 0);
			int[] numOddsStr = StringUtil.splitInt(CSVUtil.getColValue("num_odds_int", itemOdd, odds), 0);
			if (numStr.length != numOddsStr.length) {
				System.err.println("Error:aaa|configItemId:" + CSVUtil.getColValue("id", itemOdd, odds)
						+ "|num_int.length not equals to num_odds_int.length" + csvFile);
				// System.exit(-1);
			}
			int totalOdds = 10000;
			for (int numOdd : numOddsStr) {
				totalOdds -= (numOdd);
			}
			if (totalOdds != 0) {
				System.err.println("Error:bbb|configItemId:" + CSVUtil.getColValue("id", itemOdd, odds)
						+ "|num_odds_int|total Odds Not———————————— Equals To 10000 " + csvFile);
				// System.exit(-1);
			}
			int itemBoxId = Integer.parseInt(CSVUtil.getColValue("itembox_id", itemOdd, odds));
			itemBoxOdds.put(itemBoxId, itemBoxOdds.containsKey(itemBoxId)
					? (itemBoxOdds.get(itemBoxId) + Integer.parseInt(CSVUtil.getColValue("item_odds", itemOdd, odds)))
					: Integer.parseInt(CSVUtil.getColValue("item_odds", itemOdd, odds)));
		}
		// TODO先验证每个物品池的概率对不对
		for (Entry<Integer, Integer> keyValue : itemBoxOdds.entrySet()) {
			if (keyValue.getValue() != 1000000) {
				System.err.println(
						"Error:ccc|itemBoxId:" + keyValue.getKey() + "|total Odds Not———————————————— Equals To 1000000 " + csvFile);
				// System.exit(-1);
			}
		}
		System.out.println("lottery odd check confirmed.");
	}
}

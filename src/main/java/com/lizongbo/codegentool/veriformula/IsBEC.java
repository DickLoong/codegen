package com.lizongbo.codegentool.veriformula;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lizongbo.codegentool.csv2db.CSVUtil;

public class IsBEC  extends DataVerifyFormula{

private static Set<Integer> BECSet = new HashSet<>();
	
	static{
		//TODO 加载背包物品
		{
			String csvpath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TBackpack(背包系统)_Item(物品信息).csv";
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvpath);
			for(String[] singleItem : colList){
				String value = CSVUtil.getColValue("item_id", singleItem, colList);
				try{
					BECSet.add(Integer.parseInt(value));
				}catch(NumberFormatException ex){
					continue;
				}
			}
		}
		//TODO 加载装备
		{
			String csvpath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TEquip(装备信息)_Tequip(装备信息).csv";
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvpath);
			for(String[] singleItem : colList){
				String value = CSVUtil.getColValue("equip_id", singleItem, colList);
				try{
					BECSet.add(Integer.parseInt(value));
				}catch(NumberFormatException ex){
					continue;
				}
			}
		}
		//TODO 加载装备
		{
			String csvpath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TComm(基础配置)_Moneyenumeration(货币枚举).csv";
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvpath);
			for(String[] singleItem : colList){
				String value = CSVUtil.getColValue("money_id", singleItem, colList);
				try{
					BECSet.add(Integer.parseInt(value));
				}catch(NumberFormatException ex){
					continue;
				}
			}
		}		
		
	}
	@Override
	public List<String> calculate(String csvPath) {
		// TODO Auto-generated method stub
		List<String> resultList = this.argsList.stream().map(e -> Boolean.toString(BECSet.contains(Integer.parseInt(e)))).collect(Collectors.toList());
		return resultList;
	}


}

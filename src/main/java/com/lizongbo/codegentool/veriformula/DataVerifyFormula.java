package com.lizongbo.codegentool.veriformula;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public abstract class DataVerifyFormula {
	protected List<String> argsList = new LinkedList<>(); 
	private static Map<String,Class<? extends DataVerifyFormula>> formulaMap = new HashMap<>();
	
	static{
		regFormula(Equals.class);
		regFormula(Sum.class);
		regFormula(IsBEC.class);
		regFormula(GetValueByColName.class);
		regFormula(Each.class);
		regFormula(IndexAt.class);
	}
	
	abstract public List<String> calculate(String csvPath);
	public void addArgs(String arg){
		argsList.add(arg);
	}
	public void addArgs(List<String> args){
		this.argsList.addAll(args);
	}
	
	public static void regFormula(Class<? extends DataVerifyFormula> formula){
		formulaMap.put(formula.getSimpleName(), formula);
	}
	
	public static boolean formulaVerifyCalculate(String formulaExpression,String csvPath) throws InstantiationException, IllegalAccessException{
		StringBuilder formulaScanner = new StringBuilder();
		Stack<DataVerifyFormula> calculationStack = new Stack<>();
		for(int i = 0 ; i < formulaExpression.length();i ++){
			char scanCursor = formulaExpression.charAt(i);
			if(scanCursor == '('){
				//扫出了公式
				DataVerifyFormula formula = (DataVerifyFormula) formulaMap.get(formulaScanner.toString()).newInstance();
				calculationStack.push(formula);
				formulaScanner = new  StringBuilder();
			}else if(scanCursor == ','){
				//扫出了参数
				if(formulaScanner.length() > 0){
					calculationStack.peek().addArgs(formulaScanner.toString());
				}
				formulaScanner = new  StringBuilder();
			}else if(scanCursor == ')'){
				//扫出了运算符
				//将最后一次扫入的参数add入参数表
				if(formulaScanner.length() > 0){
					calculationStack.peek().addArgs(formulaScanner.toString());
				}
				//算出当前层的结果，然后pop掉计算栈最高一层的运算，把运算结果加入到运算栈的peek的参数栈中
				DataVerifyFormula peekFormula  = calculationStack.pop();
				List<String> resultList = peekFormula.calculate(csvPath);
				//如果栈空了，进行最后的计算
				if(calculationStack.isEmpty()){
					//TODO return result
					return new Boolean(true).toString().equals(resultList.get(0));
				}else{
					calculationStack.peek().addArgs(resultList);
				}
				formulaScanner = new  StringBuilder();
			}else{
				formulaScanner.append(scanCursor);
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
//		String testFormula = "IsBEC(IndexAt(0,;,Each(|,GetValueByColName(item_reward))))";
		String testFormula = "IsBEC(GetValueByColName(item3))";
		try {
			System.out.println(formulaVerifyCalculate(testFormula, "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TLottery(抽奖配置表)_Itembag(道具包).csv"));
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

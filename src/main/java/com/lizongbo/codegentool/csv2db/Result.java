package com.lizongbo.codegentool.csv2db;

public  class Result<T> {

	private T holdingObject;
	private int errorCode;
	public boolean isSuccess() {
		return null != holdingObject;
	}
	
	public  Result(T object) {
		holdingObject = object;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Result CreateErrorResult(int errorCode) {
		Result result = new Result(null);
		result.errorCode = errorCode;
		return result;
	}
	
	public T getObject() {
		return holdingObject;
	}

	public int getErrorCode() {
		return errorCode;
	}
	
	
}

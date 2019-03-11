package com.lizongbo.codegentool;

import com.jcraft.jsch.UserInfo;

public class LinuxUerInfo implements UserInfo {
	private String username = "root";
	private String password = "";

	public LinuxUerInfo() {
	}

	public LinuxUerInfo(String password) {
		super();
		this.password = password;
	}

	public LinuxUerInfo(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassphrase(String arg0) {
		return true;
	}

	@Override
	public boolean promptPassword(String arg0) {
		return true;
	}

	@Override
	public boolean promptYesNo(String arg0) {
		return true;
	}

	@Override
	public void showMessage(String arg0) {
		System.out.println(arg0);

	}

}

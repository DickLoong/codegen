package com.lizongbo.codegentool.csv2db;

import java.io.*;
import java.security.*;

public class HashCalc {

	public static final char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };
	public static final String[] hashTypes = new String[] { "MD2", "MD5", "SHA1", "SHA-256", "SHA-384", "SHA-512" };

	public static void main(String[] args) throws Exception {
		// /args = new String[] {"D:\\lizongbo\\HashCalc2.02H\\HashCalc.exe”};
		if (args == null || args.length < 1) {
			System.out.println("示例： java com.lizongbo.util.HashCalc D:\\lizongbo\\HashCalc2.02H\\HashCalc.exe");
			System.exit(1);
		}
		String fileName = args[0];
		System.out.println("需要获取hash的文件为：　" + fileName);
		java.util.List<MessageDigest> mds = new java.util.ArrayList<MessageDigest>();
		for (String hashType : hashTypes) {
			MessageDigest md = MessageDigest.getInstance(hashType);
			mds.add(md);
		}
		InputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			byte[] buffer = new byte[1024];
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0) {
				for (MessageDigest md : mds) {
					md.update(buffer, 0, numRead);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		for (MessageDigest md : mds) {
			System.out.println(md.getAlgorithm() + "== " + toHexString(md.digest()));

		}
	}

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public static String md5(byte[] bytes) {

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			return toHexString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "no";
	}

	public static String md5(String str) {
		if (str != null) {
			try {
				return md5(str.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 针对文件进行md5
	 * 
	 * @param f
	 * @return
	 */
	public static String md5(File f) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] buffer = new byte[1024];
			int numRead = 0;
			MessageDigest md = MessageDigest.getInstance("MD5");
			while ((numRead = fis.read(buffer)) > 0) {
				md.update(buffer, 0, numRead);
			}
			return toHexString(md.digest());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return "null";
	}
}

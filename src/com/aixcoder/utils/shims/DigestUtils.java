package com.aixcoder.utils.shims;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

	public static String getMD5(String content) {
		byte[] bytes;
		try {
			bytes = content.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return md5Hex(bytes);
	}

	public static String md5Hex(byte[] data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte[] thedigest = md.digest(data);
		return Hex.encodeHexString(thedigest);
	}
}

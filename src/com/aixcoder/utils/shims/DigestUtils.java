package com.aixcoder.utils.shims;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
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

package com.github.trganda.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAUtils {

  public static final String SHA_1 = "SHA-1";
  public static final String SHA_256 = "SHA-256";

  public static String SHA(final String strText, final String strType) {
    byte[] byteBuffer = SHA(strText.getBytes(), strType);

    StringBuilder strHexString = new StringBuilder();
    for (byte b : byteBuffer) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        strHexString.append('0');
      }
      strHexString.append(hex);
    }

    return strHexString.toString();
  }

  public static byte[] SHA(final byte[] strText, final String strType) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance(strType);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Impossible", e);
    }
    messageDigest.update(strText);
    return messageDigest.digest();
  }
}

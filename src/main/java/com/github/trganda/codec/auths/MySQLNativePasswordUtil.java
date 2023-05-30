package com.github.trganda.codec.auths;

import com.github.trganda.codec.auths.Handshake;
import io.netty.buffer.ByteBuf;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Calculates a password hash for {@code mysql_native_password} authentication. */
public class MySQLNativePasswordUtil {

  public static byte[] hashPassword(String password, ByteBuf saltBuf) {
    byte[] salt = new byte[saltBuf.readableBytes()];
    saltBuf.readBytes(salt);
    return hashPassword(password, salt);
  }

  /**
   * Calculates a hash of the user's password.
   *
   * @param password the user's password
   * @param salt the salt send from the server in the {@link Handshake} packet.
   * @return the hashed password
   */
  public static byte[] hashPassword(String password, byte[] salt) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");

      byte[] hashedPassword = md.digest(password.getBytes());

      md.reset();
      byte[] doubleHashedPassword = md.digest(hashedPassword);

      md.reset();
      md.update(salt, 0, 20);
      md.update(doubleHashedPassword);

      byte[] hash = md.digest();
      for (int i = 0; i < hash.length; i++) {
        hash[i] = (byte) (hash[i] ^ hashedPassword[i]);
      }
      return hash;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}

package com.github.trganda.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

public final class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final ZoneOffset BEIJING_TIMEZONE = ZoneOffset.of("+8");

    public static LocalDateTime getLocalDateTimeNow() {
        return LocalDateTime.now(BEIJING_TIMEZONE);
    }

    public static byte[] generateRandomAsciiBytes(int numBytes) {
        byte[] s = new byte[numBytes];
        new SecureRandom().nextBytes(s);
        for (int i = 0; i < s.length; i++) {
            s[i] &= 0x7f;
            if (s[i] == 0 || s[i] == 36) s[i]++;
        }
        return s;
    }

    public static long getJVMUptime() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        return bean.getUptime() / 1000;
    }

    public static byte[] hexToBytes(String hex) {
        String s = hex.replace(" ", "");
        int len = s.length();
        if ((len & 1) != 0) throw new IllegalArgumentException("Odd number of characters.");

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] =
                (byte)
                    ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static boolean compareDigest(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(
            a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public static String scramble411(String passwordSha1Hex, byte[] seedAsBytes) {
        byte[] passwordSha1 = hexToBytes(passwordSha1Hex);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(SHAUtils.SHA_1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Impossible", e);
        }

        byte[] passwordHashStage2 = md.digest(passwordSha1);
        md.reset();

        md.update(seedAsBytes);
        md.update(passwordHashStage2);

        byte[] toBeXord = md.digest();

        int numToXor = toBeXord.length;

        for (int i = 0; i < numToXor; i++) {
            toBeXord[i] = (byte) (toBeXord[i] ^ passwordSha1[i]);
        }

        return Base64.getEncoder().encodeToString(toBeXord);
    }

    public static byte[] payload(String name) {
        try (InputStream fis = Utils.class.getClassLoader().getResourceAsStream(name);) {
            assert fis != null;
            byte[] bytes = new byte[fis.available()];
            int cnt = fis.read(bytes);
            if (cnt == -1) {
                return new byte[0];
            }
            return bytes;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return new byte[0];
        }
    }
}

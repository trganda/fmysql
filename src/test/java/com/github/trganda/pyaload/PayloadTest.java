package com.github.trganda.pyaload;

import com.github.trganda.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PayloadTest {
    @Test
    public void payloadTest() {
        byte[] bytes = Utils.payload("cc5.bin");
        int len = bytes.length;
        System.out.println(len);
        System.out.println(Arrays.toString(bytes));

        char[] chars = new char[len/2 + 1];
        for (int i = 0; i < (len / 2); i++) {
            chars[i] = byteToChar(bytes[i], bytes[i+1]);
        }

        char[] temp = new char[0];
        if (len % 2 == 0) {
            temp = Arrays.copyOfRange(chars, 0, len/2);
        } else {
            int last = len / 2;
            chars[last] = (char) (0xFF00 | ((bytes[len-1] & 0xFF) << 8));
            temp = chars;
        }
        System.out.println(Arrays.toString(temp));
        String ret = new String(temp);
        System.out.println(ret);

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeCharSequence(ret, StandardCharsets.UTF_8);

        byte[] outbytes = new byte[buf.readableBytes()];
        buf.readBytes(outbytes);

        System.out.println(Arrays.toString(outbytes));
    }

    public static char byteToChar(byte h, byte l) {
        int hi = (h & 0xFF) << 8;
        int lo = l & 0xFF;
        return (char) (hi | lo);
    }
}

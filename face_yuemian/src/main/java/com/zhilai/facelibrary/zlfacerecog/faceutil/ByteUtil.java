package com.zhilai.facelibrary.zlfacerecog.faceutil;

import java.util.Arrays;
import java.util.Locale;

public class ByteUtil {

    public static byte xor(byte[] data, int start, int end) {

        if (start >= end || end > data.length) {
            return 0;
        }

        byte ret = 0;
        for (int i = start; i <= end; i++) {
            ret ^= data[i];
        }

        return ret;
    }

    public static String bytesToHexString(byte[] src, String split) {

        StringBuilder stringBuilder = new StringBuilder();
        if (src == null) {
            return null;
        }
        if (src.length <= 0) {
            return "";
        }

        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv.toUpperCase(Locale.getDefault()));

            if (i == src.length - 1)
                break;

            stringBuilder.append(split);
        }

        return stringBuilder.toString();
    }

    public static String bytesToHexStringLimit(byte[] src, String split, int reserve) {
        if (src == null) {
            return null;
        }
        if (src.length > reserve) {
            return bytesToHexString(Arrays.copyOfRange(src, 0, reserve), split) + "...";
        } else {
            return bytesToHexString(src, split);
        }
    }

    //这个函数将float转换成byte[]
        public static byte[] float2byte(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;
    }

}

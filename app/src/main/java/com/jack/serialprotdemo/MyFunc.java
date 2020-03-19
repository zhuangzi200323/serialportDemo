package com.jack.serialprotdemo;

public class MyFunc {
    static public int isOdd(int num) {
        return num & 0x1;
    }

    static public int HexToInt(String inHex) {
        inHex = inHex.replaceAll(" ", "");
        return Integer.parseInt(inHex, 16);
    }

    static public byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    static public String Byte2Hex(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    //计算前面6位的异或校验值
    public static byte getXor(byte[] datas) {
        byte temp = datas[0];

        for (int i = 1; i < 6; i++) {
            temp ^= datas[i];
        }

        return temp;
    }

    //计算校验和
    public static byte getSum(byte[] datas) {
        byte sum = (byte) 0;

        for (int i = 0; i < datas.length - 1; i++) {
            sum += datas[i];
        }

        return (byte) ~sum;
    }

    //-------------------------------------------------------

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    static public String ByteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (int i = 0; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    static public String ByteArrToHex(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
        }
        return strBuilder.toString();
    }

    static public byte[] HexToByteArr(String inHex) {
        inHex = inHex.replaceAll(" ", "");

        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
}
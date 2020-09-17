package com.jack.utils;

/**
 * byte转string，hex转byte，byte轩hex等等相关操作类
 * @author jack
 */
public class ByteStringHexUtils {
	public static String printByteToString(byte[] f) {
		if(f==null) {
			return "";
		}
		int num = f.length;
		if(num == 0) {
			return "";
		}
		String result = "";
		String temp = "";
		for(int i = 0; i<num; i++) {
			if (i != num - 1) {
				temp = Integer.toHexString(f[i] & 0xff);
			} else {
				temp = Integer.toHexString(f[i] & 0xff);
			}
			if (temp.length() == 1){
				result += "0" + temp + " ";
			}else{
				result += temp + " ";
			}
		}
		return result;
	}

	public static String ByteToString(byte[] f) {
		if(f==null) {
			return "";
		}
		int num = f.length;
		if(num == 0) {
			return "";
		}
		String result = "";
		String temp = "";
		for(int i = 0; i<num; i++) {
			if (i != num - 1) {
				temp = Integer.toHexString(f[i] & 0xff);
			} else {
				temp = Integer.toHexString(f[i] & 0xff);
			}
			if (temp.length() == 1){
				result += "0" + temp;
			}else{
				result += temp;
			}
		}
		return result;
	}

	public static byte HexToByte(String inHex) {
		return (byte) Integer.parseInt(inHex, 16);
	}

	public static String Byte2Hex(Byte inByte) {
		return String.format("%02x", inByte).toUpperCase();
	}

	public static int isOdd(int num) {
		return num & 0x1;
	}

	public static byte[] HexToByteArr(String inHex) {
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


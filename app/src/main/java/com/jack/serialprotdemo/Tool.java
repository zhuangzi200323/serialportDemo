package com.jack.serialprotdemo;

public class Tool {
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
}


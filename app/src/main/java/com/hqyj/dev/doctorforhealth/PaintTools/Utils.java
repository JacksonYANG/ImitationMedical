package com.hqyj.dev.doctorforhealth.PaintTools;

public class Utils {
	/**
	 * 将整形数组转化成字节流
	 * @param intArr
	 * @return
	 */
	public static byte[] convertIntArrToByteArr(int[] intArr) {

		int byteNum = intArr.length * 4;
		byte[] byteArr = new byte[byteNum];

		int curInt = 0;
		for (int j = 0, k = 0; j < intArr.length; j++, k += 4) {
			curInt = intArr[j];
			byteArr[k] = (byte) ((curInt >>> 24) & 0xFF);
			byteArr[k + 1] = (byte) ((curInt >>> 16) & 0xFF);
			byteArr[k + 2] = (byte) ((curInt >>> 8) & 0xFF);
			byteArr[k + 3] = (byte) ((curInt >>> 0) & 0xFF);
		}

		return byteArr;

	}


	/**
	 * 将字节流转化成整形数组
	 * @param byteArr
	 * @return
	 */
	public static int[] convertByteArrToIntArr(byte[] byteArr) {

		int remained = 0;
		int intNum = 0;

		remained = byteArr.length % 4;
		if (remained != 0) {
			throw new RuntimeException();
		}

		// 把字节数组转化为int[]后保留的个数.
		intNum = byteArr.length / 4;

		//
		int[] intArr = new int[intNum];

		int ch1, ch2, ch3, ch4;
		for (int j = 0, k = 0; j < intArr.length; j++, k += 4) {

			ch1 = byteArr[k];
			ch2 = byteArr[k + 1];
			ch3 = byteArr[k + 2];
			ch4 = byteArr[k + 3];

			// 以下内容用于把字节的8位, 不按照正负, 直接放到int的后8位中.
			if (ch1 < 0) {
				ch1 = 256 + ch1;
			}
			if (ch2 < 0) {
				ch2 = 256 + ch2;
			}
			if (ch3 < 0) {
				ch3 = 256 + ch3;
			}
			if (ch4 < 0) {
				ch4 = 256 + ch4;
			}

			intArr[j] = (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
		}

		return intArr;
	}
}

package com.bluetooth.modbus.snrtools2.uitls;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * 对数字和字节进行转换。<br>
 * 基础知识：<br>
 * 假设数据存储是以大端模式存储的：<br>
 * byte: 字节类型 占8位二进制 00000000<br>
 * char: 字符类型 占2个字节 16位二进制 byte[0] byte[1]<br>
 * int : 整数类型 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]<br>
 * long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5]
 * byte[6] byte[7]<br>
 * float: 浮点数(小数) 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]<br>
 * double: 双精度浮点数(小数) 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4]
 * byte[5] byte[6] byte[7]<br>
 */
public class NumberBytes
{
	/**
	 * 左补位，右对齐
	 * 
	 * @param oriStr
	 *            原字符串
	 * @param len
	 *            目标字符串长度
	 * @param alexin
	 *            补位字符
	 * @return 目标字符串
	 */
	public static String padLeft(String oriStr, int len, char alexin)
	{
		String str = "";
		int strlen = oriStr.length();
		if (strlen < len)
		{
			for (int i = 0; i < len - strlen; i++)
			{
				str = str + alexin;
			}
		}
		str = str + oriStr;
		return str;
	}

	/**
	 * 右补位，左对齐
	 *
	 * @param oriStr
	 *            原字符串
	 * @param len
	 *            目标字符串长度
	 * @param alexin
	 *            补位字符
	 * @return 目标字符串
	 */
	public static String padRight(String oriStr, int len, char alexin)
	{
		String str = "";
		int strlen = oriStr.length();
		if (strlen < len)
		{
			for (int i = 0; i < len - strlen; i++)
			{
				str = str + alexin;
			}
		}
		str = oriStr + str;
		return str;
	}

	/**
	 * 将一个2位字节数组转换为char字符。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return char字符
	 */
	public static char bytesToChar(byte[] b)
	{
		char c = (char) ((b[0] << 8) & 0xFF00L);
		c |= (char) (b[1] & 0xFFL);
		return c;
	}

	public static String byte2Char(byte[] b)
	{
		/**
		 *  通过ByteToCharConverter类转换不可行，sun.io.*包属于内部API，已经不可用
		 *  ByteToCharConverter converter = ByteToCharConverter.getConverter("gb2312");
		 *  char c[] = converter.convertAll(b);
		 */

		Charset charSet = Charset.forName("GB2312");
		ByteBuffer byteBuffer = ByteBuffer.allocate(b.length);
		byteBuffer.put(b);
		byteBuffer.flip();
		CharBuffer charBuffer = charSet.decode(byteBuffer);
		return charBuffer.toString().trim();
	}

	/**
	 * 将一个8位字节数组转换为双精度浮点数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 双精度浮点数
	 */
	public static double bytesToDouble(byte[] b) {
		return Double.longBitsToDouble(bytesToLong(b));
	}

	/**
	 * 将一个4位字节数组转换为浮点数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 浮点数
	 */
	public static float bytesToFloat(byte[] b) {
		return Float.intBitsToFloat(bytesToInt(b));
	}

	/**
	 * 将一个4位字节数组转换为4整数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 整数
	 */
	public static int bytesToInt(byte[] b)
	{
		int i = (b[0] << 24) & 0xFF000000;
		i |= (b[1] << 16) & 0xFF0000;
		i |= (b[2] << 8) & 0xFF00;
		i |= b[3] & 0xFF;
		return i;
	}

	/**
	 * 将一个8位字节数组转换为长整数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 长整数
	 */
	public static long bytesToLong(byte[] b)
	{
		long l = ((long) b[0] << 56) & 0xFF00000000000000L;
		// 如果不强制转换为long，那么默认会当作int，导致最高32位丢失
		l |= ((long) b[1] << 48) & 0xFF000000000000L;
		l |= ((long) b[2] << 40) & 0xFF0000000000L;
		l |= ((long) b[3] << 32) & 0xFF00000000L;
		l |= ((long) b[4] << 24) & 0xFF000000L;
		l |= ((long) b[5] << 16) & 0xFF0000L;
		l |= ((long) b[6] << 8) & 0xFF00L;
		l |= (long) b[7] & 0xFFL;
		return l;
	}

	/**
	 * 将一个char字符转换位字节数组（2个字节），b[0]存储高位字符，大端
	 * 
	 * @param c
	 *            字符（java char 2个字节）
	 * @return 代表字符的字节数组
	 */
	public static byte[] charToBytes(char c)
	{
		byte[] b = new byte[8];
		b[0] = (byte) (c >>> 8);
		b[1] = (byte) c;
		return b;
	}

	/**
	 * 将一个双精度浮点数转换位字节数组（8个字节），b[0]存储高位字符，大端
	 * 
	 * @param d
	 *            双精度浮点数
	 * @return 代表双精度浮点数的字节数组
	 */
	public static byte[] doubleToBytes(double d) {
		return longToBytes(Double.doubleToLongBits(d));
	}

	/**
	 * 将一个浮点数转换为字节数组（4个字节），b[0]存储高位字符，大端
	 * 
	 * @param f
	 *            浮点数
	 * @return 代表浮点数的字节数组
	 */
	public static byte[] floatToBytes(float f) {
		return intToBytes(Float.floatToIntBits(f));
	}

	/**
	 * 将一个整数转换位字节数组(4个字节)，b[0]存储高位字符，大端
	 * 
	 * @param i
	 *            整数
	 * @return 代表整数的字节数组
	 */
	public static byte[] intToBytes(int i)
	{
		byte[] b = new byte[4];
		b[0] = (byte) (i >>> 24);
		b[1] = (byte) (i >>> 16);
		b[2] = (byte) (i >>> 8);
		b[3] = (byte) i;
		return b;
	}

	/**
	 * 将一个长整数转换位字节数组(8个字节)，b[0]存储高位字符，大端
	 * 
	 * @param l
	 *            长整数
	 * @return 代表长整数的字节数组
	 */
	public static byte[] longToBytes(long l)
	{
		byte[] b = new byte[8];
		b[0] = (byte) (l >>> 56);
		b[1] = (byte) (l >>> 48);
		b[2] = (byte) (l >>> 40);
		b[3] = (byte) (l >>> 32);
		b[4] = (byte) (l >>> 24);
		b[5] = (byte) (l >>> 16);
		b[6] = (byte) (l >>> 8);
		b[7] = (byte) (l);
		return b;
	}

	// 将十六进制字符串转换为float
	public static float hexStrToFloat(String str)
	{
		float result = 0;
		try {
			int temp = Integer.parseInt(str.trim(), 16);
			result = Float.intBitsToFloat(temp);
		} catch (NumberFormatException e) {
			long ltemp = Long.parseLong(str.trim(), 16);
			result = Float.intBitsToFloat((int) ltemp);
		}
		return result;
	}

	// 将十六进制字符串转换为Long
	public static long hexStrToLong(String str)
	{
		long result = 0;
		try {
			result = Long.parseLong(str.trim(), 16);
		} catch (NumberFormatException e) {
		}
		return result;
	}

	/**
	 * 四舍五入，保留几位位小数
	 * @param f
	 * @param count 保留几位
	 * @return
	 */
	public static float scaleFloat(float f,int count)
	{
		BigDecimal b = new BigDecimal(f);
		float f1 = b.setScale(count, BigDecimal.ROUND_HALF_UP).floatValue();
		// b.setScale(2, BigDecimal.ROUND_HALF_UP) 表明四舍五入，保留两位小数
		return f1;
	}
	
	/**
	 * 保证小数位数的字符串
	 * @param str
	 * @param count 保留几位
	 * @return
	 */
	public static String scaleString(String str,int count)
	{
		String s = str;
		if(s!=null && s.contains(".")){
			switch(s.substring(s.lastIndexOf(".")+1).length()){
			case 0:
				s += "000";
				break;
			case 1:
				s += "00";
				break;
			case 2:
				s += "0";
				break;
			}
		}
		return s;
	}

	/**
	 * 使用java正则表达式去掉多余的.与0
	 * @param s
	 * @return
	 */
	public static String subZeroAndDot(String s)
	{
		try {
			if(s.indexOf(".") > 0){
				s = s.replaceAll("0+?$", "");//去掉多余的0
				s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
			}
		}catch (Exception e){

		}
		return s;
	}
}

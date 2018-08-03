package com.bluetooth.modbus.snrtools2;

import android.content.Context;
import android.os.Environment;

import com.bluetooth.modbus.snrtools2.common.SNRApplication;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;

public class Constans {
	
	/** 指定型号名称起始字段*/
	public final static String DEVICE_NAME_START = "Sinier";

	/** 重试次数*/
	public final static int RETRY_COUNT = 3;

	/** 连接设备中*/
	public final static int CONNECTING_DEVICE = 0X10001;
	/** 设备连接成功*/
	public final static int CONNECT_DEVICE_SUCCESS = 0X10002;
	/** 设备连接失败*/
	public final static int CONNECT_DEVICE_FAILED = 0X10003;
	/** 设备未连接*/
	public final static int NO_DEVICE_CONNECTED = 0X10004;
	/** 设备返回信息*/
	public final static int DEVICE_RETURN_MSG = 0X10005;
	/** 连接已经关闭*/
	public final static int CONNECT_IS_CLOSED = 0X10006;
	/** 连接堵塞*/
	public final static int CONNECT_IS_JIM = 0X10007;
	/** 通讯开始*/
	public final static int CONTACT_START = 0X10008;
	/** 不合法的返回信息*/
	public final static int ERROR_START = 0X10009;
	/** 连接超时*/
	public final static int TIME_OUT = 0X1000A;
	
	public static class PasswordLevel{
		/** 等级1 ，无密码*/
		public static String LEVEL_0 = "";
		/** 等级1，基本参数密码 ，出厂密码100000*/
		public static String LEVEL_1 = "100000";
		/** 等级2，高级参数密码 ，出厂密码200000*/
		public static String LEVEL_2 = "200000";
		/** 等级3，传感器参数密码 ，出厂密码300000*/
		public static String LEVEL_3 = "300000";
		/** 等级4，转换器密码 ，出厂密码400000*/
		public static String LEVEL_4 = "400000";
//		/** 等级5，超级密码 ，固定值270427*/
//		public static long LEVEL_5 = 270427;
//		/** 等级6，总量清零密码,出厂密码5210*/
//		public static long LEVEL_6 = 5210;
	}
	
	public static class Directory{
		public static final String DOWNLOAD = AppStaticVar.mApplication.getFilesDir()+"/";
	}
}

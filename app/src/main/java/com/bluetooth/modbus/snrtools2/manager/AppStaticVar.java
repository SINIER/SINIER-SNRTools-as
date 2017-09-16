package com.bluetooth.modbus.snrtools2.manager;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.bluetooth.modbus.snrtools2.MyObservable;
import com.bluetooth.modbus.snrtools2.bean.Parameter;
import com.bluetooth.modbus.snrtools2.bean.ProductInfo;

public class AppStaticVar {
	public final static Byte[] locks = new Byte[0];
	public static Context mApplication;
	/** 每次连接获取的产品配置信息*/
	public static ProductInfo mProductInfo;
	/** 取得默认的蓝牙适配器 */
	public static BluetoothAdapter mBtAdapter;
	/** 当前连接的蓝牙地址*/
	public static String mCurrentAddress;
	/** 当前连接的蓝牙名称*/
	public static String mCurrentName;
	/** 蓝牙socket*/
	public static BluetoothSocket mSocket;
	/** 密码等级*/
	public static int PASSWORD_LEVEAL = -1;
	/** 参数列表*/
	public static List<Parameter> mParamList;
	/** 正向总量参数位置*/
	public static int ZXZLPosition = -1;
	/** 反向总量参数位置*/
	public static int FXZLPosition = -1;
	/** 是否是主动断开*/
	public static boolean isExit = false;
	/** 用于通知密码页面开始发送指令*/
	public static MyObservable mObservable;
	public static boolean isSNRMainPause = false;
	public static boolean isCheckPwdPause = false;
	/** 是否是英文模式*/
	public static boolean isEnglish = false;
	/** 当前同步的编号*/
	public static int currentSyncIndex;
	/** 当前变量的编号*/
	public static int currentVarIndex;
	/** 当前主界面项的编号*/
	public static int currentMainIndex;
}

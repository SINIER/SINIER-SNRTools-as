package com.bluetooth.modbus.snrtools2.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.bluetooth.modbus.snrtools2.bean.ProductInfo;

public class AppStaticVar {
	public final static Byte[] locks = new Byte[0];
	public static Context mApplication;
	/** 每次连接获取的产品配置信息*/
	public static ProductInfo mProductInfo;
	/** 取得默认的蓝牙适配器 */
	public static BluetoothAdapter mBtAdapter;
	/** 上次连接成功的蓝牙地址*/
	public static String mLastSuccessAddress;
	/** 当前连接的蓝牙地址*/
	public static String mCurrentAddress;
	/** 当前连接的蓝牙名称*/
	public static String mCurrentName;
	/** 蓝牙socket*/
	public static BluetoothSocket mSocket;
	/** 密码等级*/
	public static int PASSWORD_LEVEAL = -1;
	/** 是否是主动断开*/
	public static boolean isExit = false;
	public static boolean isSNRMainPause = false;
	/** 是否是中文模式*/
	public static boolean isChinese = true;
	/** 进入程序时，蓝牙是否开启*/
	public static boolean isBluetoothOpen = false;
	/** 当前同步的编号*/
	public static int currentSyncIndex;
	/** 当前主界面变量和配置的编号*/
	public static int currentVarIndex;
	/** 剩余重试次数*/
	public static int retryCount;
}

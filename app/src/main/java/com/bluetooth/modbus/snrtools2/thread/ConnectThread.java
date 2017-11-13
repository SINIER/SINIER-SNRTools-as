package com.bluetooth.modbus.snrtools2.thread;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bluetooth.modbus.snrtools2.Constans;
import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

public class ConnectThread extends Thread { 		
	private BluetoothDevice mDevice;
	private Handler mHanlder;
	
	public ConnectThread(BluetoothDevice device,Handler hanlder) {
		this.mDevice = device;
		this.mHanlder = hanlder;
	}
	
	public void run() {
		try {
			//创建一个Socket连接：只需要服务器在注册时的UUID号
			if(mHanlder == null){
				return ;
			}
			if(mDevice == null){
				Message msg = new Message();
				msg.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg14);
				msg.what = Constans.CONNECT_DEVICE_FAILED;
				mHanlder.sendMessage(msg);
				return;
			}
			if(AppStaticVar.mSocket == null || !((AppStaticVar.mLastSuccessAddress+"").equals(AppStaticVar.mCurrentAddress+""))) {
				AppStaticVar.mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			}
			//连接
			Message msg2 = new Message(); 
			msg2.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_progressmsg3)+AppStaticVar.mCurrentName;
			msg2.what = Constans.CONNECTING_DEVICE;
			mHanlder.sendMessage(msg2);
			if(!AppStaticVar.mSocket.isConnected()) {
				AppStaticVar.mSocket.connect();
			}
			Message msg = new Message();
			msg.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg16);
			msg.what = Constans.CONNECT_DEVICE_SUCCESS;
			mHanlder.sendMessage(msg);
		} 
		catch (IOException e) 
		{
			Log.e("connect", "", e);
			CrashReport.postCatchedException(e);
			AppStaticVar.mSocket = null;
			Message msg = new Message();
			msg.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg14);
			msg.what = Constans.CONNECT_DEVICE_FAILED;
			mHanlder.sendMessage(msg);
		} 
	}
}

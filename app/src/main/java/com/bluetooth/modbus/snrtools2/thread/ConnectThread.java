package com.bluetooth.modbus.snrtools2.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bluetooth.modbus.snrtools2.Constans;
import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
				connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg14), Constans.CONNECT_DEVICE_FAILED);
				return;
			}
			if(mDevice.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
				AppUtil.closeBLE();
				if (AppStaticVar.mSocket == null || !((AppStaticVar.mLastSuccessAddress + "").equals(AppStaticVar.mCurrentAddress + ""))) {
					AppStaticVar.mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				}
				//连接
				connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_progressmsg3)+AppStaticVar.mCurrentName, Constans.CONNECTING_DEVICE);
				if(!AppStaticVar.mSocket.isConnected()) {
					AppStaticVar.mSocket.connect();
				}
				connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg16), Constans.CONNECT_DEVICE_SUCCESS);
			}else {
				//连接
				connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_progressmsg3)+AppStaticVar.mCurrentName, Constans.CONNECTING_DEVICE);

				final Timer connect_timer = new Timer();
				connect_timer.schedule(new TimerTask() {
					@Override
					public void run() {
						//连接超时
						connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg14), Constans.CONNECT_DEVICE_FAILED);
					}
				},60000*5);

				if(AppStaticVar.mSocket != null){
					AppStaticVar.mSocket.close();
					AppStaticVar.mSocket = null;
				}

				if (AppStaticVar.mGatt == null || !((AppStaticVar.mLastSuccessAddress + "").equals(AppStaticVar.mCurrentAddress + ""))
					|| !AppUtil.checkBLEHasConnected()) {
					AppUtil.closeBLE();
					mDevice.connectGatt(AppStaticVar.mApplication, false, new BluetoothGattCallback() {
						@Override
						public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
							super.onConnectionStateChange(gatt, status, newState);
							if(newState == BluetoothProfile.STATE_CONNECTED){
								AppStaticVar.mGatt = gatt;
								AppStaticVar.mGatt.discoverServices();
							}
						}

						@Override
						public void onServicesDiscovered(BluetoothGatt gatt, int status) {
							// 发现GATT服务
							if (status == BluetoothGatt.GATT_SUCCESS) {
								if(AppUtil.checkBLEHasConnected()) {
									connect_timer.cancel();
									connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg16), Constans.CONNECT_DEVICE_SUCCESS);
								}else {
									connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg14), Constans.CONNECT_DEVICE_FAILED);
								}
							}
						}

						@Override
						public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
							// 收到数据
							AppStaticVar.cacheData.append(CRC16.getBufHexStr(characteristic.getValue()).toLowerCase());
						}
					});
				}else {
					connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg16), Constans.CONNECT_DEVICE_SUCCESS);
				}
			}

		} 
		catch (IOException e) 
		{
			Log.e("connect", "", e);
			CrashReport.postCatchedException(e);
			if (AppStaticVar.mSocket != null)
			{
				try
				{
					AppStaticVar.mSocket.close();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				AppStaticVar.mSocket = null;
			}
			if (AppStaticVar.mGatt != null)
			{
				try
				{
					AppStaticVar.mGatt.close();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				AppStaticVar.mGatt = null;
			}
			connectResult(AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg14), Constans.CONNECT_DEVICE_FAILED);
		} 
	}

	private void connectResult(String strMsg, int connectDeviceStatus) {
		Message msg = new Message();
		msg.obj = strMsg;
		msg.what = connectDeviceStatus;
		mHanlder.sendMessage(msg);
	}
}

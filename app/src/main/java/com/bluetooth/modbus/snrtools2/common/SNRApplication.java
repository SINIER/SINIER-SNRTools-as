package com.bluetooth.modbus.snrtools2.common;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;

import com.bluetooth.modbus.snrtools2.Constans;
import com.bluetooth.modbus.snrtools2.MyObservable;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.tencent.bugly.crashreport.CrashReport;

/**
 *
 */
public class SNRApplication extends Application {
	
	public void onCreate() {
		super.onCreate();
		AppStaticVar.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		AppStaticVar.mObservable = new MyObservable();
		Constans.mApplicationContext = getApplicationContext();
		CrashReport.initCrashReport(getApplicationContext(), "d0f956be79", false);
	}
	
}

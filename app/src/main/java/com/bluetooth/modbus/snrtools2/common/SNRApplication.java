package com.bluetooth.modbus.snrtools2.common;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;

import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Locale;

/**
 *
 */
public class SNRApplication extends Application {
	
	public void onCreate() {
		super.onCreate();
		AppStaticVar.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		AppStaticVar.mApplication = getApplicationContext();
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		AppStaticVar.isChinese = language.endsWith("zh");
		CrashReport.initCrashReport(getApplicationContext(), "d0f956be79", false);
	}
	
}

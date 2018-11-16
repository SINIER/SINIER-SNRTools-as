package com.bluetooth.modbus.snrtools2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;

import java.util.Locale;

public class LocaleChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(String.valueOf(intent.getAction()).equals(Intent.ACTION_LOCALE_CHANGED)) {
            Log.e("LocaleChangeReceiver","Language change");
            Locale locale = context.getResources().getConfiguration().locale;
            String language = locale.getLanguage();
            AppStaticVar.isChinese = language.endsWith("zh");
        }
    }
}

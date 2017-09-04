package com.bluetooth.modbus.snrtools2.uitls;

import android.os.Handler;
import android.os.Message;

import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cchen on 2017/8/8.
 */

public class CmdUtils {

    public static void sendCmd(String cmd, final CmdListener listener){
        synchronized (AppStaticVar.locks) {
            if(listener != null){
                listener.start();
            }
            if (AppStaticVar.mSocket == null) {
                if(listener != null){
                    listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg14));
                    listener.finish();
                }
                return;
            }
            try {
                OutputStream os = AppStaticVar.mSocket.getOutputStream();
                byte[] sendB = CRC16.getSendBuf(cmd.replaceAll("0x","").replaceAll(" ",""));
                os.write(sendB);
                os.flush();

            } catch (IOException e) {
                if(listener != null){
                    listener.timeOut(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg15));
                    listener.finish();
                }
                return;
            }

            final Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what){
                        case 1:
                            if(listener != null){
                                listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg10));
                                listener.finish();
                            }
                            break;
                        case 2:
                            if (listener != null) {
                                listener.result(msg.obj.toString());
                            }
                            break;
                        case 3:
                            if (listener != null) {
                                listener.result("Error Data!");
                            }
                            break;
                        case 4:
                            if (listener != null) {
                                listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg10));
                            }
                            break;
                        case 5:
                            if(listener != null) {
                                listener.finish();
                            }
                            break;
                        case 6:
                            if(listener != null){
                                listener.timeOut(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg3));
                                listener.finish();
                            }
                            break;
                    }
                }
            };

            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    handler.sendEmptyMessage(6);
                }
            }, 10000);

            Timer readtimer = new Timer();
            readtimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputStream mmInStream = null;
                    try {
                        if (AppStaticVar.mSocket == null) {
                            handler.sendEmptyMessage(1);
                            return;
                        }
                        mmInStream = AppStaticVar.mSocket.getInputStream();
                        byte[] buffer = new byte[10240];
                        int bytes;
                        if ((bytes = mmInStream.read(buffer)) != -1) {
                            byte[] buf_data = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                buf_data[i] = buffer[i];
                            }
                            if (CRC16.checkBuf(buf_data)) {
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = CRC16.getBufHexStr(buf_data);
                                handler.sendMessage(msg);
                            } else {
                                System.out.println("==未通过CRC校验==" + CRC16.getBufHexStr(buf_data));
                                handler.sendEmptyMessage(3);
                            }
                        }
                    } catch (IOException e1) {
                        try {
                            if (mmInStream != null) {
                                mmInStream.close();
                            }
                            handler.sendEmptyMessage(4);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    } finally {
                        timer.cancel();
                        handler.sendEmptyMessage(5);
                    }
                }
            }, 300);
        }
    }
}

package com.bluetooth.modbus.snrtools2.uitls;

import android.os.Handler;
import android.os.Message;

import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cchen on 2017/8/8.
 */

public class CmdUtils {

    public static void sendCmd(final String cmd, final CmdListener listener){
        synchronized (AppStaticVar.locks) {
            sendCmd(cmd,listener,true);
        }
    }

    private static void sendCmd(final String cmd, final CmdListener listener,boolean isNew){
        synchronized (AppStaticVar.locks) {
            if(isNew) {
                AppStaticVar.retryCount = 5;
            }
            if(listener != null&&isNew){
                listener.start();
            }
            if (AppStaticVar.mSocket == null) {
                if(listener != null){
                    listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg14));
                    listener.finish();
                }
                return;
            }
            if(AppStaticVar.retryCount!=5){
                CrashReport.postCatchedException(new Throwable("开始第"+(5-AppStaticVar.retryCount)+"次尝试"+cmd));
            }
            try {
                OutputStream os = AppStaticVar.mSocket.getOutputStream();
                byte[] sendB = CRC16.getSendBuf(cmd.replaceAll("0x","").replaceAll(" ",""));
                os.write(sendB);
                os.flush();

            } catch (Exception e) {
                CrashReport.postCatchedException(e);
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
                                listener.finish();
                            }
                            break;
                        case 3:
                            if (listener != null) {
                                listener.failure(msg.obj.toString());
                                listener.finish();
                            }
                            break;
                        case 4:
                            if (listener != null) {
                                listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg10));
                                listener.finish();
                            }
                            break;
//                        case 5:
//                            if(listener != null) {
//                                listener.finish();
//                            }
//                            break;
                        case 6:
                            if(listener != null){
                                listener.timeOut(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg3));
                                listener.finish();
                            }
                            break;
                        case 7:
                            sendCmd(cmd,listener,false);
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

            AppStaticVar.readCount = 10;
            final Timer readtimer = new Timer();
            readtimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputStream mmInStream = null;
                    try {
                        if (AppStaticVar.mSocket == null) {
                            handler.sendEmptyMessage(1);
                            readtimer.cancel();
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
                                if(AppStaticVar.retryCount!=5) {
                                    CrashReport.postCatchedException(new Throwable("第"+(5-AppStaticVar.retryCount)+"次尝试成功"+cmd));
                                }
//                                if(AppStaticVar.retryCount>0&&AppStaticVar.retryCount!=5){
//                                    handler.sendEmptyMessage(5);
//                                }
                            } else {
                                String emsg = "==未通过CRC校验==\n发送命令" + cmd + "\n返回值\n" + CRC16.getBufHexStr(buf_data);
                                System.out.println(emsg);
                                if(AppStaticVar.retryCount>0){
                                    if(AppStaticVar.retryCount!=5) {
                                        CrashReport.postCatchedException(new Throwable("第"+(5-AppStaticVar.retryCount)+"次尝试"+emsg));
                                    }
                                    AppStaticVar.retryCount--;
                                    handler.sendEmptyMessageDelayed(7,500);
                                }else {
                                    Message msg = new Message();
                                    msg.what = 3;
                                    msg.obj = emsg;
                                    handler.sendMessage(msg);
                                    CrashReport.postCatchedException(new Throwable("重试结束"+emsg));
                                }
//                                handler.sendEmptyMessage(3);
                            }
                            readtimer.cancel();
                        }else {
                            if(AppStaticVar.readCount>0) {
                                AppStaticVar.readCount--;
                            }else {
                                handler.sendEmptyMessage(6);
                            }
                        }
                    } catch (IOException e1) {
                        readtimer.cancel();
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
//                        if(AppStaticVar.retryCount>0&&AppStaticVar.retryCount!=5){
//                            sendCmd(cmd,listener,false);
//                        }
//                        else {
//                            handler.sendEmptyMessage(5);
//                        }
                    }
                }
            }, 300,300);
        }
    }
}

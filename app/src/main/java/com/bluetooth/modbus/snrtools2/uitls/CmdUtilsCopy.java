package com.bluetooth.modbus.snrtools2.uitls;

import android.os.Handler;
import android.os.Message;

import com.bluetooth.modbus.snrtools2.Constans;
import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cchen on 2017/8/8.
 */

public class CmdUtilsCopy {

    public static void sendCmd(final String cmd,final int backValueCount, final CmdListener listener){
        synchronized (AppStaticVar.locks) {
            sendCmd(cmd,backValueCount,listener,true);
        }
    }

    private static void sendCmd(final String cmd, final int backValueCount, final CmdListener listener, final boolean isNew){
        synchronized (AppStaticVar.locks) {
            System.out.println("====================\n\n\n\n");
            System.out.println("====================开始时间=="+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
            if(isNew) {
                AppStaticVar.retryCount = Constans.RETRY_COUNT;
                System.out.println("=========================收到待发送命令"+cmd);
            }else {
                System.out.println("=========================收到一条重试命令"+cmd);
            }
            if(listener != null&&isNew){
                listener.start();
            }
            if (AppStaticVar.mSocket == null) {
                if(listener != null){
                    listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg14));
                    listener.finish();
                }
                System.out.println("=========================socket连接断开");
                return;
            }
            if(AppStaticVar.retryCount!=Constans.RETRY_COUNT){
//                CrashReport.postCatchedException(new Throwable("开始第"+(Constans.RETRY_COUNT-AppStaticVar.retryCount)+"次尝试"+cmd));
            }
            try {
                OutputStream os = AppStaticVar.mSocket.getOutputStream();
                byte[] sendB = CRC16.getSendBuf(cmd.replaceAll("0x","").replaceAll(" ",""));
                os.write(sendB);
                os.flush();
                System.out.println("=========================命令发送"+cmd);
            } catch (Exception e) {
                System.out.println("=========================命令发送失败"+e.toString());
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
                            sendCmd(cmd,backValueCount,listener,false);
                            break;
                    }
                }
            };

            final StringBuilder stringBuilder = new StringBuilder();
            final Timer readtimer = new Timer();
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    System.out.println("=========================读取数据超时"+cmd);
                    System.out.println("====================超时时间=="+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
                    String emsg = "==读取数据超时==\n发送命令" + cmd;
                    System.out.println(emsg);
                    readtimer.cancel();
                    if (AppStaticVar.retryCount > 0) {
                        System.out.println("=========================发送重试"+cmd);
                        if (AppStaticVar.retryCount != Constans.RETRY_COUNT) {
//                            CrashReport.postCatchedException(new Throwable("第" + (Constans.RETRY_COUNT - AppStaticVar.retryCount) + "次尝试" + emsg));
                        }
                        AppStaticVar.retryCount--;
                        handler.sendEmptyMessageDelayed(7, 500);
                    } else {
                        System.out.println("=========================命令重试失败"+cmd);
                        Message msg = new Message();
                        msg.what = 3;
                        msg.obj = emsg;
                        handler.sendMessage(msg);
//                        CrashReport.postCatchedException(new Throwable("重试结束" + emsg));
                    }
                }
            }, 2000);
            readtimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputStream mmInStream = null;
                    try {
                        if (AppStaticVar.mSocket == null) {
                            readtimer.cancel();
                            timer.cancel();
                            handler.sendEmptyMessage(1);
                            return;
                        }
                        System.out.println("=========================开始等待响应"+cmd);
                        mmInStream = AppStaticVar.mSocket.getInputStream();
                        byte[] buffer = new byte[10240];
                        int bytes;
                        if ((bytes = mmInStream.read(buffer)) != -1) {
                            byte[] buf_data = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                buf_data[i] = buffer[i];
                            }
                            System.out.println("=========================收到响应值"+CRC16.getBufHexStr(buf_data)+"==="+cmd);
                            stringBuilder.append(CRC16.getBufHexStr(buf_data));
                            System.out.println("=========================当前值"+stringBuilder.toString()+"==="+cmd);
                            if(stringBuilder.length()>=backValueCount){
                                int index = stringBuilder.indexOf(cmd.replaceAll("0x","").replaceAll(" ","").substring(0,8));
                                if(index>-1) {
                                    stringBuilder.delete(0,index);
                                    if(stringBuilder.length()==backValueCount) {
                                        System.out.println("=========================响应值校验通过==值" + stringBuilder.toString());
                                        System.out.println("=========================响应值校验通过==命令" + cmd);
                                        readtimer.cancel();
                                        timer.cancel();
                                        Message msg = new Message();
                                        msg.what = 2;
                                        msg.obj = stringBuilder.toString();
                                        handler.sendMessage(msg);
                                        if (AppStaticVar.retryCount != Constans.RETRY_COUNT) {
//                                            CrashReport.postCatchedException(new Throwable("第" + (Constans.RETRY_COUNT - AppStaticVar.retryCount) + "次尝试成功" + cmd));
                                        }
                                    }
                                }
                            }


                        }
                    } catch (IOException e1) {
                        readtimer.cancel();
                        timer.cancel();
                        System.out.println("=========================接受响应值失败"+ e1.toString());
                        try {
                            if (mmInStream != null) {
                                mmInStream.close();
                            }
                            handler.sendEmptyMessage(4);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    } finally {
                    }
                }
            }, 5,1);
        }
    }
}

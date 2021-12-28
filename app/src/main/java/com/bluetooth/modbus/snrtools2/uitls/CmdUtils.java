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
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/*******************************************************************************
 *  文 件 名: CmdUtils.java
 *
 *  程序功能: 蓝牙数据收发数据帧处理
 *  创 建 人: cchen
 *  创建时间: 2017年08月08日
 *  说    明:
 *------修改历史-----------------------------------------
 *  版   本：
 *  修 改 人:
 *  修改时间:
 *  修改说明:
 *******************************************************************************/
public class CmdUtils
{
    static StringBuilder stringBuilder = new StringBuilder();

    static Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            CmdListener listener = (CmdListener) ((HashMap<String,Object>)msg.obj).get("listener");
            switch (msg.what){
                case 1:
                    if(listener != null){
                        listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg10));
                        listener.finish();
                    }
                    break;
                case 2:
                    if (listener != null) {
                        listener.result(((HashMap<String,Object>)msg.obj).get("msg").toString());
                        listener.finish();
                    }
                    break;
                case 3:
                    if (listener != null) {
                        listener.failure(((HashMap<String,Object>)msg.obj).get("msg").toString());
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
                    String cmd = ((HashMap<String,Object>)msg.obj).get("cmd").toString();
                    int backValueCount = AppUtil.parseToInt(((HashMap<String,Object>)msg.obj).get("backValueCount").toString(),0);
                    sendCmd(cmd,backValueCount,listener,false);
                    break;
            }
        }
    };

     /*******************************************************************************
      * 函数名称: sendCmd
      * 功能描述: 蓝牙数据发送程序
      * 输入变量: cmd-数据包内容
      *          backValueCount-重发次数
      *          listener-监听处理函数
      * 返 回 值: 无
      * 全局变量:
      * 调用模块:
      * 说    明:
      * 注    意:
      *******************************************************************************/
    public static void sendCmd(final String cmd,final int backValueCount, final CmdListener listener)
    {
        synchronized (AppStaticVar.locks)
        {
            sendCmd(cmd,backValueCount,listener,true);
        }
    }

     /*******************************************************************************
     * 函数名称: sendCmd
     * 功能描述: 蓝牙数据发送程序
      * 输入变量: cmd-数据包内容
      *          backValueCount-重发次数
      *          listener-监听处理函数
      *          isNew-新数据包
     * 返 回 值: 无
     * 全局变量:
     * 调用模块:
     * 说    明:
     * 注    意:
     *******************************************************************************/
    private static void sendCmd(final String cmd, final int backValueCount, final CmdListener listener, final boolean isNew)
    {
        // 锁定以下进程
        synchronized (AppStaticVar.locks)
        {
            System.out.println("====================\n\n\n\n");
            System.out.println("====================开始时间=="+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
            if(isNew)
            {
                AppStaticVar.retryCount = Constans.RETRY_COUNT;
                System.out.println("=========================收到待发送命令"+cmd);
            }
            else
            {
                System.out.println("=========================收到一条重试命令"+cmd);
            }
            // 监听开始函数
            if(listener != null&&isNew)
            {
                listener.start();
            }
            if (AppStaticVar.mSocket == null && AppStaticVar.mGatt == null)
            {
                if(listener != null)
                {
                    listener.connectFailure(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg14));
                    listener.finish();
                }
                System.out.println("=========================socket连接断开");
                return;
            }
            // =======================================================================================
            // 数据发送
            if(AppStaticVar.retryCount!=Constans.RETRY_COUNT)
            {
//                CrashReport.postCatchedException(new Throwable("开始第"+(Constans.RETRY_COUNT-AppStaticVar.retryCount)+"次尝试"+cmd));
            }
            try
            {
                byte[] sendB = CRC16.getSendBuf(cmd.replaceAll("0x", "").replaceAll(" ", ""));
                if(AppStaticVar.mSocket != null) {
                    // 新建发送缓冲区
                    OutputStream os = AppStaticVar.mSocket.getOutputStream();
                    // 数据包加入CRC数据
                    // 数据包发送
                    os.write(sendB);
                    os.flush();
                }else if(AppStaticVar.mGatt != null){
                    if(!AppUtil.sendData(sendB)){
                        System.out.println("=========================命令发送失败-BLE");
                        CrashReport.postCatchedException(new Exception("命令发送失败-BLE"));
                        if(listener != null)
                        {
                            listener.timeOut(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg15));
                            listener.finish();
                        }
                    }
                }
                System.out.println("=========================命令发送"+cmd);
            }
            catch (Exception e)
            {
                System.out.println("=========================命令发送失败"+e.toString());
                CrashReport.postCatchedException(e);
                if(listener != null)
                {
                    listener.timeOut(AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg15));
                    listener.finish();
                }
                return;
            }
            // =======================================================================================
            // 数据接收
            // timer 用法说明
            // 第一种方法：设定指定任务task在指定延迟delay后执行
            // schedule(TimerTask task, long delay)
            // 第二种方法：设定指定任务task在指定延迟delay后进行固定延迟peroid的执行
            // schedule(TimerTask task, long delay, long period)
            // 第三种方法：设定指定任务task在指定延迟delay后进行固定频率peroid的执行。
            // scheduleAtFixedRate(TimerTask task, long delay, long period)
            // 第四种方法：安排指定的任务task在指定的时间firstTime开始进行重复的固定速率period执行．
            // scheduleAtFixedRate(TimerTask task,Date firstTime,long period)
            stringBuilder.delete(0,stringBuilder.length());
            final HashMap<String,Object> obj = new HashMap<>();
            obj.put("listener",listener);
            final Timer read_timer = new Timer();
            final Timer read_timeout_timer = new Timer();

            // 读取超时定时器
            // 2000ms后如无数据，执行超时定时器处理任务
            read_timeout_timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    // 读数据超时处理任务
                    System.out.println("=========================读取数据超时"+cmd);
                    System.out.println("====================超时时间=="+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
                    String emsg = "==读取数据超时==\n发送命令" + cmd;
                    System.out.println(emsg);
                    // 关闭读数据定时器
                    read_timer.cancel();
                    if (AppStaticVar.retryCount > 0)
                    {
                        // 重新发送
                        System.out.println("=========================发送重试"+cmd);
                        if (AppStaticVar.retryCount != Constans.RETRY_COUNT)
                        {
//                            CrashReport.postCatchedException(new Throwable("第" + (Constans.RETRY_COUNT - AppStaticVar.retryCount) + "次尝试" + emsg));
                        }
                        AppStaticVar.retryCount--;
                        obj.put("cmd",cmd);
                        obj.put("backValueCount",backValueCount);
                        Message sendMsg = new Message();
                        sendMsg.what = 7;
                        sendMsg.obj = obj;
                        handler.sendMessageDelayed(sendMsg, 200);
                    }
                    else
                    {
                        // 发送失败
                        obj.put("msg",emsg);
                        System.out.println("=========================命令重试失败"+cmd);
                        Message msg = new Message();
                        msg.what = 3;
                        msg.obj = obj;
                        handler.sendMessage(msg);
//                        CrashReport.postCatchedException(new Throwable("重试结束" + emsg));
                    }
                }
            }, 2000);
            // 读数据定时器
            // 等待5ms后，以5ms为周期执行
            read_timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    InputStream mmInStream = null;
                    try
                    {
                        if (AppStaticVar.mSocket == null && AppStaticVar.mGatt == null)
                        {
                            read_timer.cancel();
                            read_timeout_timer.cancel();
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = obj;
                            handler.sendMessage(msg);
                            return;
                        }
                        System.out.println("=========================开始等待响应"+cmd);

                        if(AppStaticVar.mSocket != null) {
                            mmInStream = AppStaticVar.mSocket.getInputStream();
                            byte[] buffer = new byte[10240];
                            int bytes;
                            if ((bytes = mmInStream.read(buffer)) != -1) {
                                byte[] buf_data = new byte[bytes];
                                for (int i = 0; i < bytes; i++) {
                                    buf_data[i] = buffer[i];
                                }
                                System.out.println("=========================收到响应值" + CRC16.getBufHexStr(buf_data) + "===" + cmd);
                                stringBuilder.append(CRC16.getBufHexStr(buf_data).toLowerCase());
                                System.out.println("=========================当前值" + stringBuilder.toString() + "===" + cmd);
                                if (stringBuilder.length() >= backValueCount) {
                                    int index = stringBuilder.indexOf(cmd.replaceAll("0x", "").replaceAll(" ", "").substring(0, 8).toLowerCase());
                                    if (index > -1) {
                                        stringBuilder.delete(0, index);
                                        if (stringBuilder.length() == backValueCount) {
                                            System.out.println("=========================响应值校验通过==值" + stringBuilder.toString());
                                            System.out.println("=========================响应值校验通过==命令" + cmd);
                                            read_timer.cancel();
                                            read_timeout_timer.cancel();
                                            obj.put("msg", stringBuilder.toString());
                                            Message msg = new Message();
                                            msg.what = 2;
                                            msg.obj = obj;
                                            handler.sendMessage(msg);
                                            if (AppStaticVar.retryCount != Constans.RETRY_COUNT) {
//                                            CrashReport.postCatchedException(new Throwable("第" + (Constans.RETRY_COUNT - AppStaticVar.retryCount) + "次尝试成功" + cmd));
                                            }
                                        }
                                    }
                                }
                            } else {
                                System.out.println("=========================收到的数据为0====");
                            }
                        }else if(AppStaticVar.mGatt != null){
                            System.out.println("=========================收到响应值" + AppStaticVar.cacheData.toString() + "===" + cmd);
                            stringBuilder.append(AppStaticVar.cacheData.toString().toLowerCase());
                            AppStaticVar.cacheData = new StringBuilder();
                            System.out.println("=========================当前值" + stringBuilder.toString() + "===" + cmd);
                            if(stringBuilder.toString().contains("0143000c")){
                                System.out.println("=====");
                            }
                            if (stringBuilder.length() >= backValueCount) {
                                int index = stringBuilder.indexOf(cmd.replaceAll("0x", "").replaceAll(" ", "").substring(0, 8).toLowerCase());
                                if (index > -1) {
                                    stringBuilder.delete(0, index);
                                    if (stringBuilder.length() == backValueCount) {
                                        System.out.println("=========================响应值校验通过==值" + stringBuilder.toString());
                                        System.out.println("=========================响应值校验通过==命令" + cmd);
                                        read_timer.cancel();
                                        read_timeout_timer.cancel();
                                        obj.put("msg", stringBuilder.toString());
                                        Message msg = new Message();
                                        msg.what = 2;
                                        msg.obj = obj;
                                        handler.sendMessage(msg);
                                        if (AppStaticVar.retryCount != Constans.RETRY_COUNT) {
//                                            CrashReport.postCatchedException(new Throwable("第" + (Constans.RETRY_COUNT - AppStaticVar.retryCount) + "次尝试成功" + cmd));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException e1)
                    {
                        read_timer.cancel();
                        read_timeout_timer.cancel();
                        System.out.println("=========================接受响应值失败"+ e1.toString());
                        try
                        {
                            if (mmInStream != null)
                            {
                                mmInStream.close();
                            }
                            Message msg = new Message();
                            msg.what = 4;
                            msg.obj = obj;
                            handler.sendMessage(msg);
                        }
                        catch (IOException e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                    finally
                    {
                    }
                }
            }, 10,10);
        }
    }
}

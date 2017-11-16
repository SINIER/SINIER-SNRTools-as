package com.bluetooth.modbus.snrtools2.uitls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.ab.util.AbSharedUtil;
import com.bluetooth.modbus.snrtools2.Constans;
import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.bean.Command;
import com.bluetooth.modbus.snrtools2.bean.CommandRead;
import com.bluetooth.modbus.snrtools2.bean.CommandWrite;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.db.Value;
import com.bluetooth.modbus.snrtools2.db.Var;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.tencent.bugly.crashreport.CrashReport;

public class AppUtil {

    /**
     * 检查是否已经开启蓝牙，如果没有开启会弹出开启蓝牙界面
     */
    public static boolean checkBluetooth(Context context,boolean showDialog) {
        // If BT is not on, request that it be enabled.
        if (AppStaticVar.mBtAdapter == null) {
            AppStaticVar.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (AppStaticVar.mBtAdapter == null) {
            Toast.makeText(context, context.getResources().getString(R.string.string_tips_msg14), Toast.LENGTH_LONG).show();
            return false;
        }
        if (!AppStaticVar.mBtAdapter.isEnabled()) {
            if(showDialog) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableIntent);
            }
            return false;
        }
        return true;
    }

    public static boolean checkBluetooth(Context context) {
        return checkBluetooth(context,true);
    }

    public static void closeBluetooth() {
        if (AppStaticVar.mBtAdapter != null)
            AppStaticVar.mBtAdapter.disable();
    }

    /**
     * msg.what ==Constans.NO_DEVICE_CONNECTED 与设备连接失败，请返回重新连接！ msg.what
     * ==Constans.CONNECT_IS_JIM 与设备通讯堵塞，通讯失败！
     */
    public static void modbusWriteNew(String className, final Handler handler, Command command, int waittime) {
        synchronized (AppStaticVar.locks) {
            System.out.println("=====" + className);
            Message msg = new Message();
            msg.what = Constans.CONTACT_START;
            msg.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg15);
//			handler.sendMessage(msg);
            if (AppStaticVar.mSocket == null) {
                Message message = new Message();
                message.what = Constans.NO_DEVICE_CONNECTED;
                message.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg14);
//				handler.sendMessage(message);
                return;
            }
            try {
                OutputStream os = AppStaticVar.mSocket.getOutputStream();
                System.out.println("======发送命令=====" + command.getSendString());
                byte[] sendB = new byte[8];
                sendB[0] = 0x01;
                sendB[1] = 0x6a;
                sendB[2] = 0x00;
                sendB[3] = 0x04;
                sendB[4] = 0x00;
                sendB[5] = 0x00;
                sendB[6] = 0x58;
                sendB[7] = 0x02;
//				byte[] sendB = CRC16.getSendBuf(command.getSendString());
                synchronized (os) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    os.write(sendB);
                    os.flush();
                }

            } catch (IOException e) {
                Message message = new Message();
                message.what = Constans.CONNECT_IS_JIM;
                message.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg15);
//				handler.sendMessage(message);
                e.printStackTrace();
                return;
            }

//			final Timer timer = new Timer();
//			timer.schedule(new TimerTask() {
//
//				@Override
//				public void run() {
//					Message message = new Message();
//					message.what = Constans.TIME_OUT;
//					message.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg3);
//					handler.sendMessage(message);
//					return;
//				}
//			}, waittime);

            Timer readtimer = new Timer();
            readtimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputStream mmInStream = null;
                    try {
                        if (AppStaticVar.mSocket == null) {
                            return;
                        }
                        mmInStream = AppStaticVar.mSocket.getInputStream();
                        byte[] buffer = new byte[10240];
                        int bytes;
                        if ((bytes = mmInStream.read(buffer)) != -1) {
                            byte[] buf_data = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                buf_data[i] = buffer[i];
                            }//if (CRC16.checkBuf(buf_data)) {
                            for (int i = 0; i < buf_data.length / 2; i++) {
                                byte[] b = new byte[2];
                                b[0] = buf_data[i * 2];
                                b[1] = buf_data[i * 2 + 1];
                                System.out.println("==========" + NumberBytes.bytesToChar(b));
                            }
                            String result = CRC16.getBufHexStr(buf_data);
                        }
                    } catch (IOException e1) {
                        try {
                            if (mmInStream != null) {
                                mmInStream.close();
                            }
                            Message msg = new Message();
                            msg.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg10);
                            msg.what = Constans.CONNECT_IS_CLOSED;
                            if (handler != null) {
//								handler.sendMessage(msg);
                            }
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    } finally {
//						timer.cancel();
                    }
                }
            }, 1000);
        }
    }

    /**
     * msg.what ==Constans.NO_DEVICE_CONNECTED 与设备连接失败，请返回重新连接！ msg.what
     * ==Constans.CONNECT_IS_JIM 与设备通讯堵塞，通讯失败！
     */
    public static void modbusWrite(String className, final Handler handler, CommandRead command, int waittime) {
        synchronized (AppStaticVar.locks) {
            System.out.println("=====" + className);
            Message msg = new Message();
            msg.what = Constans.CONTACT_START;
            msg.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_tips_msg15);
            handler.sendMessage(msg);
            if (AppStaticVar.mSocket == null) {
                Message message = new Message();
                message.what = Constans.NO_DEVICE_CONNECTED;
                message.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg14);
                handler.sendMessage(message);
                return;
            }
            try {
                OutputStream os = AppStaticVar.mSocket.getOutputStream();
                int count = 0;
                if (command instanceof CommandWrite) {
                    count = 6 + 1 + ((CommandWrite) command).getContentMap().size();
                } else if (command instanceof CommandRead) {
                    count = 6;
                }
                String[] totalTemp = new String[count];
                int i = 0;
                totalTemp[i++] = command.getDeviceId();
                totalTemp[i++] = command.getCommandNo();

                totalTemp[i++] = command.getStartAddressH();
                totalTemp[i++] = command.getStartAddressL();

                totalTemp[i++] = command.getCountH();
                totalTemp[i++] = command.getCountL();
                if (command instanceof CommandWrite) {
                    totalTemp[i++] = ((CommandWrite) command).getByteCount();
                    for (int j = 0; j < ((CommandWrite) command).getContentMap().size(); j++) {
                        totalTemp[i++] = ((CommandWrite) command).getContentMap().get(j + "");
                    }
                }
                String cmd = "";
                for (int ii = 0; ii < totalTemp.length; ii++) {
                    cmd += totalTemp[ii];
                }
                System.out.println("======发送命令=====" + cmd);
                byte[] sendB = CRC16.getSendBuf2(totalTemp);
                synchronized (os) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    os.write(sendB);
                    os.flush();
                }

            } catch (IOException e) {
                Message message = new Message();
                message.what = Constans.CONNECT_IS_JIM;
                message.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg15);
                handler.sendMessage(message);
                e.printStackTrace();
                return;
            }

            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Message message = new Message();
                    message.what = Constans.TIME_OUT;
                    message.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg3);
                    handler.sendMessage(message);
                    return;
                }
            }, waittime);

            Timer readtimer = new Timer();
            readtimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputStream mmInStream = null;
                    try {
                        if (AppStaticVar.mSocket == null) {
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
                                msg.obj = CRC16.getBufHexStr(buf_data);
                                msg.what = Constans.DEVICE_RETURN_MSG;
                                if (handler != null)
                                    handler.sendMessage(msg);
                            } else {
                                System.out.println("==未通过CRC校验==" + CRC16.getBufHexStr(buf_data));
                                Message msg = new Message();
                                msg.what = Constans.ERROR_START;
                                if (handler != null)
                                    handler.sendMessage(msg);
                            }
                        }
                    } catch (IOException e1) {
                        try {
                            if (mmInStream != null) {
                                mmInStream.close();
                            }
                            Message msg = new Message();
                            msg.obj = AppStaticVar.mApplication.getResources().getString(R.string.string_error_msg10);
                            msg.what = Constans.CONNECT_IS_CLOSED;
                            if (handler != null)
                                handler.sendMessage(msg);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    } finally {
                        timer.cancel();
                    }
                }
            }, 1000);
        }
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 拷贝数据库到sd卡
     *
     * @deprecated <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     */
    public static void copyDataBaseToSD() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return;
        }
        File dbFile = new File(AppStaticVar.mApplication.getDatabasePath("db") + ".db");
        File file = new File(Environment.getExternalStorageDirectory(), "db.db");

        FileChannel inChannel = null, outChannel = null;

        try {
            file.createNewFile();
            inChannel = new FileInputStream(dbFile).getChannel();
            outChannel = new FileOutputStream(file).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                    inChannel = null;
                }
                if (outChannel != null) {
                    outChannel.close();
                    outChannel = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 转换为浮点型数据
     *
     * @param s
     * @param def
     * @return
     */
    public static long parseToLong(String s, long def) {
        if (s == null || s.length() == 0) {
            return def;
        }
        try {
            s = s.trim();
            return Long.parseLong(s);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 转换为浮点型数据
     *
     * @param s
     * @param def
     * @return
     */
    public static float parseToFloat(String s, float def) {
        if (s == null || s.length() == 0) {
            return def;
        }
        try {
            s = s.trim();
            return Float.parseFloat(s);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 转换为double数据
     *
     * @param s
     * @param def
     * @return
     */
    public static double parseToDouble(String s, double def) {
        if (s == null || s.length() == 0) {
            return def;
        }
        try {
            s = s.trim();
            return Double.parseDouble(s);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 转换为boolean数据
     *
     * @param d
     * @param def
     * @return
     */
    public static boolean parseToDouble(Boolean d, boolean def) {
        if (d == null) {
            return def;
        }
        try {
            return d.booleanValue();
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 转换为double数据
     *
     * @param d
     * @param def
     * @return
     */
    public static double parseToDouble(Double d, double def) {
        if (d == null) {
            return def;
        }
        try {
            return d.doubleValue();
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 转换为Double数据
     *
     * @param s
     * @return 如果可以转返回Double，否则返回null
     */
    public static Double parseToDouble(String s) {
        try {
            s = s.trim();
            return Double.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转换为整型数据
     *
     * @param s
     * @param def
     * @return
     */
    public static int parseToInt(String s, int def) {
        if (s == null || s.length() == 0)
            return def;
        try {
            s = s.trim();
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;// size is error
        }
    }

    /**
     * 转换为整型数据
     *
     * @param s
     * @param def
     * @return
     */
    public static int parseHexStrToInt(String s, int def) {
        if (s == null || s.length() == 0)
            return def;
        try {
            s = s.trim();
            return Integer.parseInt(s, 16);
        } catch (Exception e) {
            return def;// size is error
        }
    }

    /**
     * 转换为整型数据
     *
     * @param obj
     * @param def
     * @return
     */
    public static int parseObjToInt(Object obj, int def) {
        if (obj == null)
            return def;
        try {

            if (obj instanceof Double) {
                return ((Double) obj).intValue();
            }
            return ((Integer) obj).intValue();

        } catch (Exception ex) {
        }
        return def;
    }

//    public static String getValueByType(Var var, String str) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, true,false,0);
//    }
//
//    public static String getValueByType(Var var, String str, int youxiaoCount) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, true,true, youxiaoCount);
//    }
//
//    public static String getValueByType(Var var, String str, boolean isShowUnit) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, isShowUnit, false,0);
//    }
//    public static String getValueByType(Var var, String str, int youxiaoCount, boolean isShowUnit) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, isShowUnit,true, youxiaoCount);
//    }
//
//    public static String getValueByType(Param var, String str) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, true, false,0);
//    }
//
//    public static String getValueByType(Param var, String str, int youxiaoCount) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, true,true, youxiaoCount);
//    }
//
//    public static String getValueByType(Param var, String str, boolean isShowUnit) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, isShowUnit,false, 0);
//    }
//
//    public static String getValueByType(Param var, String str, int youxiaoCount, boolean isShowUnit) {
//        if (var == null) {
//            return "";
//        }
//        return getValueByType(var.getType(), var.getUnit(), var.getCount(), str, isShowUnit,true, youxiaoCount);
//    }

    public static String getValueByType(String type, String unit, String count, String str, boolean isShowUnit) {
        String value = "";
        if ("0".equals(type + "")) {
            String dealStr = str.substring(2, 4) + str.substring(0, 2);
            long i = Long.parseLong(dealStr) + Long.parseLong(unit, 16);
            value = DBManager.getInstance().getStr(NumberBytes.padLeft(Long.toHexString(i), 4, '0'));
        } else if ("1".equals(type + "")) {
            //小于等于32767直接显示  大于32767则减去65536
            String dealStr = str.substring(2, 4) + str.substring(0, 2);
            long v = Long.parseLong(dealStr, 16);
            value = String.valueOf(v <= 32767 ? v : (v - 65536)) + (isShowUnit ? "" + DBManager.getInstance().getStr(unit) : "");
        } else if ("2".equals(type + "")) {
            //0-65536
            String dealStr = str.substring(2, 4) + str.substring(0, 2);
            long v = Long.parseLong(dealStr, 16);
            value = String.valueOf(v) + "" + DBManager.getInstance().getStr(unit);
        } else if ("3".equals(type + "")) {
            //小于等于32767直接显示  大于32767则减去65536  然后根据参数来确定小数点位数
            String dealStr = str.substring(2, 4) + str.substring(0, 2);
            long v = Long.parseLong(dealStr, 16);
            v = v <= 32767 ? v : (v - 65536);
            value = String.valueOf(v / Math.pow(10, AppUtil.parseToInt(count, 0))) + (isShowUnit ? "" + DBManager.getInstance().getStr(unit) : "");
        } else if ("4".equals(type + "")) {
            //0-65536  然后根据参数来确定小数点位数
            String dealStr = str.substring(2, 4) + str.substring(0, 2);
            long v = Long.parseLong(dealStr, 16);
            value = String.valueOf(v/Math.pow(10,AppUtil.parseToInt(count,0)))+(isShowUnit?""+DBManager.getInstance().getStr(unit):"");
        } else if ("5".equals(type + "")) {
            //小于等于2147483647直接显示  大于2147483647则减去4294967296
            String dealStr = str.substring(6, 8) + str.substring(4, 6) + str.substring(2, 4) + str.substring(0, 2);
            long v = Long.parseLong(dealStr, 16);
            value = String.valueOf(v <= 2147483647L ? v : (v - 4294967296L)) + (isShowUnit ? "" + DBManager.getInstance().getStr(unit) : "");
        } else if ("6".equals(type + "")) {
            //0-4294967295
            String dealStr = str.substring(6, 8) + str.substring(4, 6) + str.substring(2, 4) + str.substring(0, 2);
            long v = Long.parseLong(dealStr, 16);
            value = String.valueOf(v) + (isShowUnit ? "" + DBManager.getInstance().getStr(unit) : "");
        } else if ("7".equals(type + "")) {
            //float
            String dealStr = str.substring(6, 8) + str.substring(4, 6) + str.substring(2, 4) + str.substring(0, 2);
            long i = Long.parseLong(dealStr, 16);
            float v = Float.intBitsToFloat((int) (i <= 2147483647L ? i : (i - 4294967296L)));
            if (AppUtil.parseToInt(count,0)==0) {
                value = dealNoCountResult(String.valueOf(v), 5) + (isShowUnit ? "" + DBManager.getInstance().getStr(unit) : "");
            }else {
                value = String.valueOf(AppUtil.numFormatter(v,AppUtil.parseToInt(count,0)))+(isShowUnit?""+DBManager.getInstance().getStr(unit):"");
            }
//            value = String.valueOf(v) + (isShowUnit ? "" + DBManager.getInstance().getStr(unit) : "");
        } else if ("8".equals(type + "")) {
            //ip地址
            String str1 = Long.parseLong(str.substring(6, 8), 16) + "";
            String str2 = Long.parseLong(str.substring(4, 6), 16) + "";
            String str3 = Long.parseLong(str.substring(2, 4), 16) + "";
            String str4 = Long.parseLong(str.substring(0, 2), 16) + "";
            value = str1 + "." + str2 + "." + str3 + "." + str4;
        } else if ("9".equals(type + "")) {
            //字符串
            String dealStr = str.substring(2, 4) + str.substring(0, 2);
            value = DBManager.getInstance().getStr(dealStr);
        }
        return value;
    }

    public static String dealNoCountResult(String value, int youxiaoCount) {
        if (youxiaoCount <= 0) {
            youxiaoCount = 5;
        }
        double v = Double.parseDouble(value);
        if (Math.abs(v) >= Math.pow(10, youxiaoCount - 1)) {
            return (long) v + "";
        } else {
            return significand(v, youxiaoCount) + "";
        }
    }

    /** 保留几位小数*/
    public static float numFormatter(float val, int count)
    {
        BigDecimal b = new BigDecimal(val);
        val = b.setScale(count < 0 ? 2 : count, RoundingMode.HALF_UP).floatValue();
        return val;
    }

    /**
     * 保留几位有效数字
     *
     * @param oldDouble
     * @param scale
     * @return
     */
    public static double significand(double oldDouble, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "scale指定的精度为非负值");
        }
        /**
         * RoundingMode：舍入模式
         * UP：远离零方向舍入的舍入模式；
         * DOWN：向零方向舍入的舍入模式；
         * CEILING： 向正无限大方向舍入的舍入模式；
         * FLOOR：向负无限大方向舍入的舍入模式；
         * HALF_DOWN：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向下舍入；
         * HALF_UP：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向上舍入；
         * HALF_EVEN：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向相邻的偶数舍入;(在重复进行一系列计算时,此舍入模式可以将累加错误减到最小)
         * UNNECESSARY：用于断言请求的操作具有精确结果的舍入模式，因此不需要舍入。
         */
        RoundingMode rMode = RoundingMode.DOWN;
        //rMode=RoundingMode.FLOOR;
        //下面这种情况，其实和FLOOR一样的。
//        if(oldDouble>0){
//            rMode=RoundingMode.DOWN;
//        }else{
//            rMode= RoundingMode.UP;
//        }
        //此处的scale表示的是，几位有效位数
        BigDecimal b = new BigDecimal(Double.toString(oldDouble), new MathContext(scale, rMode));
        return b.doubleValue();
    }

    public static String getWriteValueByType(String type, String count, String str) {
        String value = "";
        try {


            if ("0".equals(type + "")) {
                String temp = NumberBytes.padLeft(str, 4, '0');
                value = temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("1".equals(type + "")) {
                //大于0直接使用  小于0加上65536
                long l = (long) Double.parseDouble(str);
                if (l < 0) {
                    l += 65536L;
                }
                String temp = NumberBytes.padLeft(Long.toHexString(l), 4, '0');
                value = temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("2".equals(type + "")) {
                //0-65536
                long l = (long) Double.parseDouble(str);
                String temp = NumberBytes.padLeft(Long.toHexString(l), 4, '0');
                value = temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("3".equals(type + "")) {
                //根据参数来确定小数点位数   然后大于0直接使用  小于0加上65536
                long l = (long) (Double.parseDouble(str) * Math.pow(10, AppUtil.parseToInt(count, 0)));
                if (l < 0) {
                    l += 65536L;
                }
                String temp = NumberBytes.padLeft(Long.toHexString(l), 4, '0');
                value = temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("4".equals(type + "")) {
                //0-65536  然后根据参数来确定小数点位数
                long l = (long) (Double.parseDouble(str) * Math.pow(10, AppUtil.parseToInt(count, 0)));
                String temp = NumberBytes.padLeft(Long.toHexString(l), 4, '0');
                value = temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("5".equals(type + "")) {
                //大于0直接使用  小于0加上4294967296
                long l = (long) Double.parseDouble(str);
                if (l < 0) {
                    l += 4294967296L;
                }
                String temp = NumberBytes.padLeft(Long.toHexString(l), 8, '0');
                value = temp.substring(6, 8) + temp.substring(4, 6) + temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("6".equals(type + "")) {
                //0-4294967295
                long l = (long) Double.parseDouble(str);
                String temp = NumberBytes.padLeft(Long.toHexString(l), 8, '0');
                value = temp.substring(6, 8) + temp.substring(4, 6) + temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("7".equals(type + "")) {
                //float
                String temp = NumberBytes.padLeft(Integer.toHexString(Float.floatToIntBits(Float.parseFloat(str))), 8, '0');
                value = temp.substring(6, 8) + temp.substring(4, 6) + temp.substring(2, 4) + temp.substring(0, 2);
            } else if ("8".equals(type + "")) {
                //ip地址
                String[] ips = str.split("\\.");
                String ip1 = NumberBytes.padLeft(Long.toHexString(Long.parseLong(ips[3])), 2, '0');
                String ip2 = NumberBytes.padLeft(Long.toHexString(Long.parseLong(ips[2])), 2, '0');
                String ip3 = NumberBytes.padLeft(Long.toHexString(Long.parseLong(ips[1])), 2, '0');
                String ip4 = NumberBytes.padLeft(Long.toHexString(Long.parseLong(ips[0])), 2, '0');
                value = ip1 + ip2 + ip3 + ip4;
            }
        } catch (Exception e) {
            CrashReport.postCatchedException(e);
        }
        return value;
    }

    public static String getValue(String key, String defalutStr) {
        Value value = DBManager.getInstance().getValue(key);
        return value == null ? defalutStr : value.getValue();
    }

    public static void saveValue(String key, String value) {
        Value value1 = new Value();
        value1.setBtAddress(AppStaticVar.mCurrentAddress);
        value1.setKey(key);
        value1.setValue(value);
        DBManager.getInstance().saveValue(value1);
    }
}

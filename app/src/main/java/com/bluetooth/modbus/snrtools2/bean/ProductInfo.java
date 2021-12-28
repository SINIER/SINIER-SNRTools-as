package com.bluetooth.modbus.snrtools2.bean;

import android.text.TextUtils;

import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cchen on 2017/8/6.
 */

public class ProductInfo implements Serializable
{
    /** DWORD-4	编译时间（注1）*/
    public String pdBuildTime;//
    /** WORD-2	产品型号字符串（注2）*/
    public String pdModel;//
    /** WORD-2	版本信息字符串（注2）*/
    public String pdVersion;//
    /** WORD-2	预留*/
    public String ext;//
    /** WORD-2	产品配置CRC（注3）*/
    public String pdCfgCrc;//
    /** BYTE-1	主界面显示项数量*/
    public long pdDispMainCount;//
    /** BYTE-1	变量数量*/
    public long pdVarCount;//
    /** WORD-2	参数数量*/
    public long pdParCount;//
    /** BYTE-1	参数组数量*/
    public long pdParGroupCount;//
    /** BYTE-1	命令数量*/
    public long pdCmdCount;//
    /** WORD-2	字符串数量（注2）*/
    public long pdStringCount;//
    /** DWORD-4	普通用户密码*/
    public long pdPasswordUser;//
    /** DWORD-4	高级用户密码*/
    public long pdPasswordAdvance;//
    /** DWORD-4	传感器级用户密码*/
    public long pdPasswordSensor;//
    /** DWORD-4	转换器级用户密码*/
    public long pdPasswordFactory;//
    /** DWORD-4	显示板配置字（注4）*/
    public String pdDispConfig;//
    /** DWORD-4	出厂序列号*/
    public long pdSN;//
    /** crc+model*/
    public String crcModel;//

    public static ProductInfo buildModel(String info)
    {
        if(!TextUtils.isEmpty(info))
        {
            ProductInfo productInfo = new ProductInfo();
            String[] strings = new String[info.length()/2];
            if(strings.length!=52)
            {
                return null;
            }
            for(int i=0;i<info.length()/2;i++)
            {
                strings[i] = info.substring(i*2,(i+1)*2);
            }
            //编译时间
            long time = NumberBytes.hexStrToLong(strings[9]+strings[8]+strings[7]+strings[6]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try
            {
                long l2000 = sdf.parse("2000-01-01 00:00:00").getTime();
                productInfo.pdBuildTime = sdf.format(new Date(time*1000+l2000));
            }
            catch (Exception e)
            {
                CrashReport.postCatchedException(e);
                return null;
            }
            /** 产品型号字符串*/
            productInfo.pdModel = strings[11]+strings[10];
            /** 版本信息字符串*/
            productInfo.pdVersion = strings[13]+strings[12];
            /** 预留*/
            productInfo.ext = strings[15]+strings[14];
            /** 产品配置CRC*/
            productInfo.pdCfgCrc = strings[17]+strings[16];
            /** BYTE-1	主界面显示项数量*/
            productInfo.pdDispMainCount = NumberBytes.hexStrToLong(strings[18]);
            /** BYTE-1	变量数量*/
            productInfo.pdVarCount = NumberBytes.hexStrToLong(strings[19]);
            /** WORD-2	参数数量*/
            productInfo.pdParCount = NumberBytes.hexStrToLong(strings[21]+strings[20]);
            /** BYTE-1	参数组数量*/
            productInfo.pdParGroupCount = NumberBytes.hexStrToLong(strings[22]);
            /** BYTE-1	命令数量*/
            productInfo.pdCmdCount = NumberBytes.hexStrToLong(strings[23]);
            /** WORD-2	字符串数量（注2）*/
            productInfo.pdStringCount = NumberBytes.hexStrToLong(strings[25]+strings[24]);
            /** DWORD-4	普通用户密码*/
            productInfo.pdPasswordUser = NumberBytes.hexStrToLong(strings[29]+strings[28]+strings[27]+strings[26]);
            /** DWORD-4	高级用户密码*/
            productInfo.pdPasswordAdvance = NumberBytes.hexStrToLong(strings[33]+strings[32]+strings[31]+strings[30]);
            /** DWORD-4	传感器级用户密码*/
            productInfo.pdPasswordSensor = NumberBytes.hexStrToLong(strings[37]+strings[36]+strings[35]+strings[34]);
            /** DWORD-4	转换器级用户密码*/
            productInfo.pdPasswordFactory = NumberBytes.hexStrToLong(strings[41]+strings[40]+strings[39]+strings[38]);
            /** DWORD-4	显示板配置字（注4）*/
            productInfo.pdDispConfig = NumberBytes.padLeft(Long.toBinaryString(NumberBytes.hexStrToLong(strings[45]+strings[44]+strings[43]+strings[42])),16,'0');//
            /** DWORD-4	出厂序列号*/
            productInfo.pdSN= NumberBytes.hexStrToLong(strings[49]+strings[48]+strings[47]+strings[46]);
            productInfo.crcModel = productInfo.pdCfgCrc;//暂时认为crc是唯一标识
//            productInfo.crcModel = productInfo.pdCfgCrc+DBManager.getInstance().getStr(productInfo.pdModel);
            return productInfo;
        }
        return null;
    }
}

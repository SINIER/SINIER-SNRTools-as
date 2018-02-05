package com.bluetooth.modbus.snrtools2.bean;

import java.io.Serializable;

/**
 * 创建人 cchen
 * 创建日期 2017/12/17
 */

public class LogInfo implements Serializable {

    public String timeStr;
    public String hexNo;
    public int time;
    public String code;
    public String content;

    @Override
    public boolean equals(Object obj) {
        return ((LogInfo)obj).hexNo.equals(this.hexNo);
    }
}

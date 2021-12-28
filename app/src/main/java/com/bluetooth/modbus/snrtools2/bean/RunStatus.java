package com.bluetooth.modbus.snrtools2.bean;

import java.io.Serializable;

/**
 * 创建人 cchen
 * 创建日期 2017/12/17
 */

public class RunStatus implements Serializable
{

    public boolean isError;
    /** 0-正常运行
     1-上电复位
     2-模拟运行
     3-工程调试
     4-
     5-
     6-
     7-故障停止*/
    public String workModel;
    public int errorCount;
    public int logCount;
}

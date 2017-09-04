package com.bluetooth.modbus.snrtools2.listener;

public interface CmdListener {

    void start();

    void result(String result);

    void failure(String msg);

    void connectFailure(String msg);

    void timeOut(String msg);

    void finish();
}

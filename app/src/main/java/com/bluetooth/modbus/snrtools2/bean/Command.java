package com.bluetooth.modbus.snrtools2.bean;

import java.io.Serializable;

public class Command implements Serializable{

	public String cmd="00";
	public String no="00";
	public String data1="00";
	public String data2="00";
	public String data3="00";
	public String data4="00";

	public Command(){

	}

	public Command(String cmd){

	}

	public String getSendString(){
		return cmd+no+data1+data2+data3+data4;
	}
}

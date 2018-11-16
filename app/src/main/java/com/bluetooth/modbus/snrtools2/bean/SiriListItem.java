package com.bluetooth.modbus.snrtools2.bean;

import android.text.TextUtils;

public class SiriListItem {
	private String addr;
	private String message;
	private boolean isSiri;

	public SiriListItem(String addr,String msg, boolean siri) {
		this.addr = addr;
		message = msg;
		isSiri = siri;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSiri() {
		return isSiri;
	}

	public void setSiri(boolean isSiri) {
		this.isSiri = isSiri;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	@Override
	public boolean equals(Object obj) {
		SiriListItem item = (SiriListItem) obj;
		if(TextUtils.isEmpty(addr)){
			return false;
		}
		return addr.equals(item.getAddr()+"");
	}
}

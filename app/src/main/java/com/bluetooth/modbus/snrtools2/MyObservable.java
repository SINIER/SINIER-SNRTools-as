package com.bluetooth.modbus.snrtools2;

import java.util.Observable;

public class MyObservable extends Observable {

	
	public void notifyObservers(){
		setChanged();
		super.notifyObservers();
	}
	
	public void notifyObservers(Object o){
		setChanged();
		super.notifyObservers(o);
	}
}

package com.robotsmanagement.ui.list;

import java.util.Observable;

import android.util.Log;

public class CustomListItem extends Observable {
	String robotName;
	String ip;
	ConnectionStatus connectionStatus;

	public CustomListItem(String robotName, String ip) {
		super();
		this.robotName = robotName;
		this.ip = ip;
		connectionStatus = ConnectionStatus.CONNECTING;
	}

	public String getRobotName() {
		return robotName;
	}

	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isConnected() {
		return connectionStatus == ConnectionStatus.CONNECTED;
	}

	public void setConnectionStatus(ConnectionStatus status) {
		this.connectionStatus = status;
		Log.d("ITEM UPDATE", "Set up status:  " + status.name());
		setChanged();
		notifyObservers(this);
	}

}

package com.robotsmanagement.core.gps;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.robotsmanagement.model.list.CustomListItem;

public class GPSLaunchTask extends AsyncTask<CustomListItem, Void, Void> {
	private static final String CLASS_TAG = GPSLaunchTask.class.getName();
	private final static String login = "panda";
	private final static String password = "panda2013";
	private static final String GPS_STARTING_COMMAND = "gpsd -b /dev/ttyUSB0";
	private static final String GPS_LOGGING_COMMAND = "gpsctl -f -n /dev/ttyUSB0";

	@Override
	protected Void doInBackground(CustomListItem... params) {
		try {
			Log.i(CLASS_TAG, "GPS Adress: " + params[0].getIp());
			excecuteCommand(params[0].getIp(), GPS_STARTING_COMMAND);
			Log.i(CLASS_TAG, "GPS run sucessfully.");
			excecuteCommand(params[0].getIp(), GPS_LOGGING_COMMAND);
			params[0].setGpsLaunched(true);
		} catch (JSchException e) {
			Log.e(CLASS_TAG, "Error trying to connect to GPS", e);
		} catch (IOException e) {
			Log.e(CLASS_TAG, "Error trying to connect to GPS", e);
		} catch (NullPointerException e) {
			Log.e(CLASS_TAG, "Error trying to connect to GPS", e);
		}
		
		return null;
	}
	
	private void excecuteCommand(String address, String command) throws JSchException, IOException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(login, address, 22);
	    session.setPassword(password);
	    session.setConfig("StrictHostKeyChecking", "no");
	    session.connect(8000);
	    
	    ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
        channel.connect();
        channel.disconnect();
	    session.disconnect();

	}

	
	
}

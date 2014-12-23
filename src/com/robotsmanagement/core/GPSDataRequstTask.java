package com.robotsmanagement.core;

import java.io.IOException;
import java.io.InputStream;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class GPSDataRequstTask extends AsyncTask<String, Void, Void> {
	private final static String login = "panda";
	private final static String password = "panda2013";
	private static final String GPS_STARTING_COMMAND = "gpsd -b /dev/ttyUSB0";
	private static final String GPS_LOGGING_COMMAND = "gpsctl -f -n /dev/ttyUSB0";
	private static final String GPS_EXTRACT_COMMAND = "cat /dev/ttyUSB0 | head";
	private final MainActivity mainActivity;
	
	public GPSDataRequstTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	@Override
	protected Void doInBackground(String... params) {
		InputStream inputReader = null;
		try {
			Log.i("GPS Adress:", params[0]);
			handleSth(params[0], GPS_STARTING_COMMAND);
			Log.i("GPS", "GPS run sucessfully.");
			handleSth(params[0], GPS_LOGGING_COMMAND);
			inputReader = handleSth(params[0], GPS_EXTRACT_COMMAND);
			Log.i("GPS", "Starting to get GPSdata");
			new GPSLogFileReader(mainActivity, inputReader);
		} catch (JSchException e) {
			Log.e("GPS", "Error trying to connect to GPS", e);
		} catch (IOException e) {
			Log.e("GPS", "Error trying to connect to GPS", e);
		} catch (NullPointerException e) {
			Log.e("GPS", "Error trying to connect to GPS", e);
		}
		return null;
	}
	
	private InputStream handleSth(String address, String command) throws JSchException, IOException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(login, address, 22);
	    session.setPassword(password);
	    session.setConfig("StrictHostKeyChecking", "no");
	    session.connect(8000);
	    
	    ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
		InputStream reader = channel.getInputStream();
        channel.connect();
        try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			Log.e("INTER SLEEP GPS", "Interruted during sleep wihtin GPS task");
		}
        channel.disconnect();
	    session.disconnect();
	    
	    return reader;
	}

	
	
}

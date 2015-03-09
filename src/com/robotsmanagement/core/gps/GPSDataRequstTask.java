package com.robotsmanagement.core.gps;

import java.io.IOException;
import java.io.InputStream;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.robotsmanagement.core.MainActivity;
import com.robotsmanagement.model.list.CustomListItem;

public class GPSDataRequstTask extends AsyncTask<Void, Void, Void> {
	private static final String CLASS_TAG = GPSDataRequstTask.class.getName();
	private final static String login = "panda";
	private final static String password = "panda2013";
	private static final String GPS_EXTRACT_COMMAND = "cat /dev/ttyUSB0 | head";
	private final GPSLogFileReader gpsDataParser;
	private final CustomListItem item;
	
	public GPSDataRequstTask(MainActivity mainActivity, CustomListItem item) {
		gpsDataParser = new GPSLogFileReader(mainActivity, item);
		this.item = item;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		InputStream commandResultStream = null;
		try {
			Log.i(CLASS_TAG, "GPS started logging info...");
			commandResultStream = excecuteCommand(item.getIp(), GPS_EXTRACT_COMMAND);
			Log.i(CLASS_TAG, "Starting to get GPSdata");
			gpsDataParser.parseLocation(commandResultStream);
		} catch (JSchException e) {
			Log.e(CLASS_TAG, "Error trying to connect to GPS", e);
		} catch (IOException e) {
			Log.e(CLASS_TAG, "Error trying to connect to GPS", e);
		} catch (NullPointerException e) {
			Log.e(CLASS_TAG, "Error trying to connect to GPS", e);
		}
		
		return null;
	}
	
	private InputStream excecuteCommand(String address, String command) throws JSchException, IOException {
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
			Log.e("SLEEP COMMAND", "Interruted during sleep wihtin GPS task");
		}
        channel.disconnect();
	    session.disconnect();
	    
	    return reader;
	}

	
	
}

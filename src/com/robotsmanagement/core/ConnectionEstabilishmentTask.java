package com.robotsmanagement.core;

import java.io.IOException;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.drivetopoint.DriveToPointProxy;
import pl.edu.agh.amber.hokuyo.HokuyoProxy;
import pl.edu.agh.amber.location.LocationProxy;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;
import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.ui.list.CustomListItem;

public class ConnectionEstabilishmentTask extends
		AsyncTask<CustomListItem, Void, Void> {

	@Override
	protected Void doInBackground(CustomListItem... params) {
		Log.i("CONNECTION TASK",
				"Setting up connection for " + params[0].getIp());

		
		try {
			AmberClient client = new AmberClient(params[0].getIp(), 26233);
			params[0].setClient(client);
			params[0].setHokuyoProxy(new HokuyoProxy(new AmberClient(params[0].getIp(), 26233), 0));
			params[0].setDtpProxy(new DriveToPointProxy(new AmberClient(params[0].getIp(), 26233), 0));
			params[0].setLocationProxy(new LocationProxy(new AmberClient(params[0].getIp(), 26233), 0));
			params[0].setRoboclawProxy(new RoboclawProxy(new AmberClient(params[0].getIp(), 26233), 0));
		} catch (IOException e) {
			Log.e("CONNECTION TASK", "Unable to connect to robot: " + e);
		}

		return null;
	}

}

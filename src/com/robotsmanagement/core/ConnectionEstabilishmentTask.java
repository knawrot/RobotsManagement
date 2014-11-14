package com.robotsmanagement.core;

import java.io.IOException;

import pl.edu.agh.amber.common.AmberClient;

import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.ui.list.ConnectionStatus;
import com.robotsmanagement.ui.list.CustomListItem;

public class ConnectionEstabilishmentTask extends
		AsyncTask<CustomListItem, Void, Void> {
	CustomListItem listItem;

	@Override
	protected Void doInBackground(CustomListItem... params) {
		Log.d("CONNECTION TASK",
				"Setting up connection for " + params[0].getIp());
		
		listItem = params[0];
		try {
			listItem.setClient(new AmberClient(params[0].getIp(), 26233));
		} catch (IOException e) {
			System.out.println("Unable to connect to robot: " + e);
		}
		
		listItem.setConnectionStatus(ConnectionStatus.CONNECTED);
		return null;
	}

	// @Override
	// protected void onPostExecute(Void result) {
	//
	// }

}

package com.robotsmanagement.core;

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
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listItem.setConnectionStatus(ConnectionStatus.CONNECTED);
		return null;
	}

	// @Override
	// protected void onPostExecute(Void result) {
	//
	// }

}

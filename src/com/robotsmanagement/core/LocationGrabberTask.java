package com.robotsmanagement.core;

import java.io.IOException;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.location.LocationCurrent;
import pl.edu.agh.amber.location.LocationProxy;
import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.ui.list.CustomListItem;

public class LocationGrabberTask extends
		AsyncTask<CustomListItem, Void, Void> {
	
	private static final String tag = LocationGrabberTask.class.getName();
	CustomListItem listItem;
	AmberClient client;

	@Override
	protected Void doInBackground(CustomListItem... params) {
		Log.d("LOCATION TASK",
				"Acquiring position info from " + params[0].getIp());
		
		listItem = params[0];
		client = listItem.getClient();
		LocationProxy locationProxy = new LocationProxy(client, 0);
		
		try {
			LocationCurrent lok = locationProxy.getCurrentLocation();
			lok.waitAvailable();
			
			//TODO tu trzeba bedzie ustawic jakies znaczniki na mapie
			Log.i(tag, String.format("Current location: X: %e, Y: %e, Alfa: %e, P: %e, TimeStamp: %e",
					lok.getX(), lok.getY(), lok.getAngle(),
					lok.getP(), lok.getTimeStamp()));
		} catch (IOException e) {
			Log.e(tag, "Error in sending a command: " + e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//TODO tu raczej niepotrzebne ale gdzies trzeba to bedzie zrobic
			//client.terminate();
		}

		return null;
	}
}

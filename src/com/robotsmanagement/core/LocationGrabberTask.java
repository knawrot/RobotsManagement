package com.robotsmanagement.core;

import java.io.IOException;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.location.LocationCurrent;
import pl.edu.agh.amber.location.LocationProxy;
import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.ui.list.CustomListItem;
import com.robotsmanagement.ui.list.Point;

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
			lok.waitAvailable(3000);
			
			if(!lok.isAvailable())
				return null;
				
			params[0].setLocation(new Point(lok.getX(), lok.getY()));
			
			Log.d(tag, String.format("Current location: X: %e, Y: %e, Alfa: %e, P: %e, TimeStamp: %e",
					lok.getX(), lok.getY(), lok.getAngle(),
					lok.getP(), lok.getTimeStamp()));
		} catch (IOException e) {
			Log.e(tag, "Error in sending a command: " + e);
		} catch (Exception e) {
			Log.e(tag, "Error :c");
			e.printStackTrace();
		} finally {
			//TODO tu raczej niepotrzebne ale gdzies trzeba to bedzie zrobic
			//client.terminate();
		}

		return null;
	}
}

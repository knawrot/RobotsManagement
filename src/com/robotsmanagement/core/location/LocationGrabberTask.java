package com.robotsmanagement.core.location;

import java.io.IOException;

import pl.edu.agh.amber.drivetopoint.Point;
import pl.edu.agh.amber.location.LocationCurrent;
import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.model.list.ConnectionStatus;
import com.robotsmanagement.model.list.CustomListItem;

public class LocationGrabberTask extends
		AsyncTask<CustomListItem, Void, Void> {
	
	private static final String CLASS_TAG = LocationGrabberTask.class.getName();

	@Override
	protected Void doInBackground(CustomListItem... params) {
		Log.d(CLASS_TAG, "Getting robot's location data..." + params[0].getIp());
		
		try {
			LocationCurrent lok = params[0].getLocationProxy().getCurrentLocation();
			lok.waitAvailable(3000);
			
			if(!lok.isAvailable()) {
				Log.w(CLASS_TAG, "Could not get robot location data.");
				params[0].setConnectionStatus(ConnectionStatus.DISCONNECTED);
				return null;
			}
				
			params[0].setLocation(new Point(lok.getX(), lok.getY(), 0));
			params[0].setAngle(lok.getAngle());
			
			Log.d(CLASS_TAG, String.format("Current location: X: %e, Y: %e, Alfa: %e, P: %e, TimeStamp: %e",
					lok.getX(), lok.getY(), lok.getAngle(),
					lok.getP(), lok.getTimeStamp()));
			
			params[0].setConnectionStatus(ConnectionStatus.CONNECTED);
		} catch (IOException e) {
			params[0].setConnectionStatus(ConnectionStatus.DISCONNECTED);
			Log.e(CLASS_TAG, "Error in sending a command:");
			e.printStackTrace();
		} catch (Exception e) {
			params[0].setConnectionStatus(ConnectionStatus.DISCONNECTED);
			Log.e(CLASS_TAG, "Error acquiring location.");
			e.printStackTrace();
		}

		return null;
	}
}

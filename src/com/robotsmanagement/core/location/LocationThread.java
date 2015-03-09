package com.robotsmanagement.core.location;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.core.MainActivity;
import com.robotsmanagement.model.list.ConnectionStatus;
import com.robotsmanagement.model.list.CustomListItem;

public class LocationThread extends Thread {
	
	private static final String CLASS_TAG = LocationThread.class.getName();
	private final MainActivity activity;

	public LocationThread(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {
		Map<CustomListItem, AsyncTask<CustomListItem, Void, Void>> map = 
				new HashMap<CustomListItem, AsyncTask<CustomListItem, Void, Void>>();
		
		Log.i(CLASS_TAG, "Starting location thread...");

		while(!activity.getLocationThread().isInterrupted()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/* asking for location and waiting for response */
			for(CustomListItem item : activity.getItems()) {
				if(map.containsKey(item) && map.get(item).getStatus() != AsyncTask.Status.FINISHED)
					continue;
				else if(item.getConnectionStatus() != ConnectionStatus.DISCONNECTED)
					map.put(item, new LocationGrabberTask().execute(item));
			}
		}
		
		Log.i(CLASS_TAG, "Location thread has been shut down.");
	}

}

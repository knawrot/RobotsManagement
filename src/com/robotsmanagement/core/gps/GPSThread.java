package com.robotsmanagement.core.gps;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.core.MainActivity;
import com.robotsmanagement.model.list.ConnectionStatus;
import com.robotsmanagement.model.list.CustomListItem;

public class GPSThread extends Thread {
	private static final String CLASS_TAG = GPSThread.class.getName();
	private final MainActivity activity;

	public GPSThread(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {
		Map<CustomListItem, AsyncTask<Void, Void, Void>> map = 
				new HashMap<CustomListItem, AsyncTask<Void, Void, Void>>();
		
		Log.i(CLASS_TAG, "Running GPS thread...");

		while (!activity.getGpsThread().isInterrupted()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for (CustomListItem item : activity.getItems()) {
				if (map.containsKey(item) && map.get(item).getStatus() != AsyncTask.Status.FINISHED)
					continue;
				else if (item.getConnectionStatus() == ConnectionStatus.CONNECTED) {
					if (!item.isGpsLaunched())
						new GPSLaunchTask().execute(item);
					else
						map.put(item, new GPSDataRequstTask(activity, item).execute());
				}
			}
		}
		
		Log.i(CLASS_TAG, "GPS thread has been shut down.");
	}
	
}

package com.robotsmanagement.core;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.ui.list.CustomListItem;

public class LocationThread extends Thread {
	
	private static final String tag = LocationThread.class.getName();
	private MainActivity activity;

	LocationThread(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {
		Map<CustomListItem, AsyncTask<CustomListItem, Void, Void>> map = 
				new HashMap<CustomListItem, AsyncTask<CustomListItem, Void, Void>>();
		
		Log.i(tag, "Uruchamianie w¹tku lokalizacji");

		try {
			while(!activity.getLocationThread().isInterrupted()) {
				Thread.sleep(800);
			
				// odpytywanie robotów o lokalizacjê, czekamy na odpowiedŸ zanim ponowimy zapytanie
				for(CustomListItem item : activity.getItems()) {
					if(map.containsKey(item) && map.get(item).getStatus() != AsyncTask.Status.FINISHED)
						continue;
					else
						map.put(item, new LocationGrabberTask().execute(item));
				}
			}
		} catch (InterruptedException e) {
			Log.i(tag, "W¹tek lokalizacji zosta³ zakoñczony.");
		}
	}
}

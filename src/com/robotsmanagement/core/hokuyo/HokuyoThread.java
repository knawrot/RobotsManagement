package com.robotsmanagement.core.hokuyo;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.core.MainActivity;
import com.robotsmanagement.model.list.ConnectionStatus;
import com.robotsmanagement.model.list.CustomListItem;

public class HokuyoThread  extends Thread {
	
	private static final String CLASS_TAG = HokuyoThread.class.getName();
	private final MainActivity activity;

	public HokuyoThread(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {
		Map<CustomListItem, AsyncTask<CustomListItem, Void, Void>> map = 
				new HashMap<CustomListItem, AsyncTask<CustomListItem, Void, Void>>();
		
		Log.i(CLASS_TAG, "Starting Hokuyo thread...");

		while(!activity.getHokuyoThread().isInterrupted()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/* asking for location */
			for(CustomListItem item : activity.getItems()) {
				if(item.isHokuyoRunning() != true)
					continue;
				
				if(map.containsKey(item) && map.get(item).getStatus() != AsyncTask.Status.FINISHED)
					continue;
				else if(item.getConnectionStatus() != ConnectionStatus.DISCONNECTED)
					map.put(item, new HokuyoSensorTask().execute(item));
			}
		}
	}

}

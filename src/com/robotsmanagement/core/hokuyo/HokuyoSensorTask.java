package com.robotsmanagement.core.hokuyo;

import java.io.IOException;

import pl.edu.agh.amber.hokuyo.Scan;
import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.model.list.CustomListItem;

public class HokuyoSensorTask extends
		AsyncTask<CustomListItem, Void, Void> {

	private static final String CLASS_TAG = HokuyoSensorTask.class.getName();

	@Override
	protected Void doInBackground(CustomListItem... params) {
		Log.d(CLASS_TAG, "Getting info about collisions..." + params[0].getIp());
		
		try {
			Scan singleScan = params[0].getHokuyoProxy().getSingleScan();
			singleScan.waitAvailable(1000);
			
			if(singleScan.isAvailable()) {
				Log.v(CLASS_TAG, singleScan.getPoints().toString());
				params[0].setScan(singleScan);
			}
		} catch (IOException e) {
			Log.e(CLASS_TAG, "Error in sending a command: " + e);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

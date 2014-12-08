package com.robotsmanagement.core;

import java.io.IOException;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.hokuyo.HokuyoProxy;
import pl.edu.agh.amber.hokuyo.Scan;
import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.ui.list.CustomListItem;

public class HokuyoSensorTask extends
		AsyncTask<CustomListItem, Void, Void> {
	CustomListItem listItem;
	AmberClient client;

	@Override
	protected Void doInBackground(CustomListItem... params) {
		Log.i("HOKUYO TASK",
				"Acquiring collision info from " + params[0].getIp());
		
		client = params[0].getClient();
		HokuyoProxy hokuyoProxy = new HokuyoProxy(client, 0);
		
		try {
			// Synchronous receiving
			Scan singleScan = hokuyoProxy.getSingleScan();
			Log.i("HOKUYO TASK", singleScan.getPoints().toString());
			params[0].setScan(singleScan);
			
//			Log.i("HOKUYO TASK", "Now registering cyclic data listener...");
//			Thread.sleep(1000);
			// Asynchronous receiving (with listener)
//			hokuyoProxy.registerMultiScanListener(new CyclicDataListener<Scan>() {
//				@Override
//				public void handle(Scan data) {
//					try {
//						Log.i("HOKUYO TASK", data.getPoints().toString());
//					} catch (Exception e) {
//						Log.e("HOKUYO TASK", "Exception occurred: " + e);
//					}
//				}
//			});
		} catch (IOException e) {
			Log.e("HOKUYO TASK", "Error in sending a command: " + e);
//		} catch (InterruptedException e) {
//			Log.e("HOKUYO TASK", "Interrupted");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

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
		Log.d("HOKUYO TASK",
				"Acquiring position info from " + params[0].getIp());
		
		listItem = params[0];
		client = listItem.getClient();
		HokuyoProxy hokuyoProxy = new HokuyoProxy(client, 0);
		
		try {
			// Synchronous receiving
			Scan singleScan = hokuyoProxy.getSingleScan();
			System.err.println(singleScan.getPoints());
			System.out.println("Now registering cyclic data listener...");
			Thread.sleep(1000);
			// Asynchronous receiving (with listener)
			hokuyoProxy.registerMultiScanListener(new CyclicDataListener<Scan>() {
				@Override
				public void handle(Scan data) {
					try {
						System.out.println(data.getPoints());
					} catch (Exception e) {
						System.err.println("Exception occurred: " + e);
					}
				}
			});
		} catch (IOException e) {
			System.out.println("Error in sending a command: " + e);
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

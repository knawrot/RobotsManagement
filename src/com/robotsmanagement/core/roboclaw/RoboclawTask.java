package com.robotsmanagement.core.roboclaw;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.model.list.CustomListItem;

public class RoboclawTask extends
		AsyncTask<Integer, Void, Void> {

	private static final String CLASS_TAG = RoboclawTask.class.getName();
	private final CustomListItem item;

	public RoboclawTask(CustomListItem item) {
		this.item = item;
	}
	
	@Override
	protected Void doInBackground(Integer... params) {
		Log.d(CLASS_TAG, "Sending Roboclaw command to " + item.getIp());
		
		try {
			item.getRoboclawProxy().sendMotorsCommand(params[0], params[1], params[2], params[3]);
		} catch (IOException e) {
			Log.e(CLASS_TAG, "Error sending command.");
		} catch (Exception e) {
			Log.e(CLASS_TAG, "Error sending command.");
		}
		return null;
	}

}

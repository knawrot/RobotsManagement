package com.robotsmanagement.core.drivetopoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.agh.amber.drivetopoint.Point;
import android.os.AsyncTask;
import android.util.Log;

import com.robotsmanagement.model.list.CustomListItem;

public class DriveToPointTask extends
		AsyncTask<CustomListItem, Void, Void> {
	
	private static final String CLASS_TAG = DriveToPointTask.class.getName();
	private Point location;

	public DriveToPointTask() {	}
	
	public DriveToPointTask(Point destination) {
		this.location = destination;
	}

	@Override
	protected Void doInBackground(CustomListItem... params) {
		Log.d(CLASS_TAG, params[0].getIp() + " - driving to pointed location...");

        try {
        	params[0].getDtpProxy().setTargets(new ArrayList<Point>());
        	List<Point> target;
        	if(location != null)
        		target = Arrays.asList(location);
        	else
        		target = Arrays.asList();
        	
            params[0].getDtpProxy().setTargets(target);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
	}

}

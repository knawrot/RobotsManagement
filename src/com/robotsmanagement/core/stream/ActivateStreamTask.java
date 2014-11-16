package com.robotsmanagement.core.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bytedeco.javacv.FFmpegFrameGrabber;

import com.example.robotsmanagement.R;
import com.robotsmanagement.ui.list.CustomListItem;

//import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ActivateStreamTask extends AsyncTask<CustomListItem, Void, FFmpegFrameGrabber>
		implements OnClickListener {
	
	private final static String tag = ActivateStreamTask.class.getName();

	private FFmpegFrameGrabber grabber;
	//private String packageName;
	private TaskDelegate delegate;
	private File extStorage;
	
	public ActivateStreamTask(TaskDelegate delegate, //String packageName, 
			Resources resources, File extStorage) {
		//this.packageName = packageName;
		this.delegate = delegate;
		this.extStorage = extStorage;
		
		InputStream in = null;
	    OutputStream out = null;
	    try {
	        in = resources.openRawResource(R.raw.stream);
	        out = new FileOutputStream(extStorage.toString() + "/stream.sdp");

	        byte[] buffer = new byte[65536 * 2];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	        Log.i(tag, "SDP file copied to SD card");
	    } catch (Exception e) {
	        Log.e(tag, "Couldn't copy SDP file to SD card");
	        e.printStackTrace();
	    }
	}
	
	@Override
	protected FFmpegFrameGrabber doInBackground(CustomListItem... param) {
		
		try {
			Log.i(tag, "Inicjalizowanie strumieniowania wideo...");
			grabber = FFmpegFrameGrabber.createDefault(new File(extStorage.toString(), "stream.sdp"));
			Log.i(tag, "Oczekiwanie na po³¹czenie ze strumieniem wideo...");
			grabber.start();
			Log.i(tag, "Pomyœlnie po³¹czono ze strumieniem.");
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			Log.w(tag, "Nie uda³o siê po³¹czyæ ze strumieniem wideo.");
			e.printStackTrace();
		}
		
		return grabber;
	}

	@Override    
	protected void onPostExecute(FFmpegFrameGrabber grabber) {   
        delegate.streamActivationResult(grabber);
    }

	@Override
	public void onClick(View v) { 
		this.execute();
	}
}

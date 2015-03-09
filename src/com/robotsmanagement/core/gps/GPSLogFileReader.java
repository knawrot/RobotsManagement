package com.robotsmanagement.core.gps;

import java.io.IOException;
import java.io.InputStream;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.robotsmanagement.core.MainActivity;
import com.robotsmanagement.model.list.CustomListItem;

public class GPSLogFileReader implements SentenceListener {
	private static final String CLASS_TAG = GPSLogFileReader.class.getName();
	private final MainActivity mainActivity;
	private final CustomListItem item;
	private SentenceReader reader;

	public GPSLogFileReader(MainActivity mainActivity, CustomListItem item) {
		this.mainActivity = mainActivity;
		this.item = item;
	}
	
	public void parseLocation(InputStream stream) throws IOException {
		reader = new SentenceReader(stream);
		reader.addSentenceListener(this, SentenceId.GGA);
		reader.start();
	}

	@Override
	public void readingPaused() {
		// TODO Auto-generated method stub
	}

	@Override
	public void readingStarted() {
		// TODO Auto-generated method stub
	}


	@Override
	public void readingStopped() {
		// TODO Auto-generated method stub
	}


	@Override
	public void sentenceRead(SentenceEvent event) {
		final GGASentence s = (GGASentence) event.getSentence();

		Log.i(CLASS_TAG, "Position: " 
				+ String.valueOf(s.getPosition().getLatitude())
				+ " " + s.getPosition().getLatitudeHemisphere().name()
				+ ", "
				+ String.valueOf(s.getPosition().getLongitude())
				+ " " + s.getPosition().getLongitudeHemisphere().name());
		
		mainActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mainActivity.setRobotGMapRepresentation(item, s.getPosition().getLatitude(), s.getPosition().getLongitude());
				item.setGMapsLocation(new LatLng(s.getPosition().getLatitude(), s.getPosition().getLongitude()));
			}
			
		});
	}


}

package com.robotsmanagement.core;

import java.io.IOException;
import java.io.InputStream;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import android.util.Log;

public class GPSLogFileReader implements SentenceListener {
	private final SentenceReader reader;
	private final MainActivity mainActivity;

	public GPSLogFileReader(MainActivity mainActivity, InputStream stream) throws IOException {
		reader = new SentenceReader(stream);
		reader.addSentenceListener(this, SentenceId.GGA);
		reader.start();
		this.mainActivity = mainActivity;
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

		Log.i("GPS", "Position: " 
				+ String.valueOf(s.getPosition().getLatitude())
				+ " " + s.getPosition().getLatitudeHemisphere().name()
				+ ", "
				+ String.valueOf(s.getPosition().getLongitude())
				+ " " + s.getPosition().getLongitudeHemisphere().name());
		mainActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mainActivity.setMark(s.getPosition().getLatitude(), s.getPosition().getLongitude());
			}
		});
	}


}

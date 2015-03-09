package com.robotsmanagement.core;

import android.util.Log;

public class StopperThread extends Thread {
	private static final String CLASS_TAG = StopperThread.class.getName();
	private final Thread thread;
	
	public StopperThread(Thread thread) {
		this.thread = thread;
	}
	
	@Override
	public void run() {
		boolean wait = true;
		while (wait) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				Log.e(CLASS_TAG, "Thread interrupted.");
			}
		}
	}
}

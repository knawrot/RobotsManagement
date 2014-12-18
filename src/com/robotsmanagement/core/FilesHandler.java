package com.robotsmanagement.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class FilesHandler {
	private static final int MAX_BUFFER_SIZE = 10000;
	private static Context mainContext;
	
	public static final void setContext(Context context) {
		mainContext = context;
	}
	
	public static final String readFromFile(String filename) {
		FileInputStream file = null;
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		String result = null;
		
        try {
			file = mainContext.openFileInput(filename);
			while (file.read(buffer) != -1) {
				result = new String(buffer, "UTF-8");
			}
			Log.d("EXCEPTION HANDLER", "Read info about exception");
		} catch (FileNotFoundException e) {
			Log.e("EXCEPTION HANDLER", "Fatal error - could not locate errors file. " + e);
		} catch (IOException e) {
			Log.e("EXCEPTION HANDLER", "Fatal error - could not save to file. " + e);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					Log.e("EXCEPTION HANDLER", "Error closing file. " + e);
				}
			}
		}
        
        return result;
	}
	
	public static final boolean saveTofile(String filename, String output) {
		FileOutputStream file = null;
		boolean success = false;
		
        try {
			file = mainContext.openFileOutput(filename, Context.MODE_PRIVATE);
			file.write(output.toString().getBytes());
			Log.d("EXCEPTION HANDLER", "Saved info about exception");
			success = true;
		} catch (FileNotFoundException e) {
			Log.e("EXCEPTION HANDLER", "Fatal error - could not locate errors file. " + e);
		} catch (IOException e) {
			Log.e("EXCEPTION HANDLER", "Fatal error - could not save to file. " + e);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					Log.e("EXCEPTION HANDLER", "Error closing file. " + e);
				}
			}
		}
        
        return success;
	}
	
	public static final boolean fileExists(String filename) {
		try {
			mainContext.openFileInput(filename).read();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public static final void deleteFile(String filename) {
		mainContext.deleteFile(filename);
	}

}

package com.robotsmanagement.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler {
	private static final String CLASS_TAG = ExceptionHandler.class.getName();
	private static final String ERROR_DESC_INTRO_STR = "Poprzednie uruchomienie aplikacji zostalo"
						+ " zatrzymane, poniewaz wykryto blad:\n";
	public static final String ERROR_TAG = "error_description";
	public static final String ERRORS_FILE = "errors.txt";

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
        StringBuilder errorReport = new StringBuilder();
        errorReport.append(ERROR_DESC_INTRO_STR);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        ex.printStackTrace(pw);
        String msg = sw.getBuffer().toString();
        errorReport.append(msg);
        Log.e(CLASS_TAG, "Uncaught Exception: " + msg);
        
        FilesHandler.saveTofile(ERRORS_FILE, errorReport.toString());

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);

        
	}

}

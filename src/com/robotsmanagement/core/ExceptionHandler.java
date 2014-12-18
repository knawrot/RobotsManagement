package com.robotsmanagement.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler {
	public static final String ERROR_TAG = "error_description";
	public static final String ERRORS_FILE = "errors.txt";
	Context parentActivityContext;
	
	public ExceptionHandler(Context parentActivityContext) {
		this.parentActivityContext = parentActivityContext;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("Aplikacja zostanie zatrzymana, poniewaz wykryto blad:\n");
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        ex.printStackTrace(pw);
        String msg = sw.getBuffer().toString();
        errorReport.append(msg);
        Log.e("ExceptionHandler", "Niewylapany Exception: " + msg);
        
        FilesHandler.saveTofile(ERRORS_FILE, errorReport.toString());
//        Intent intent = new Intent(parentActivityContext, ErrorLoggingActivity.class);
//        intent.putExtra("error_description", errorReport.toString());
//        Log.d("starting activity", String.valueOf(parentActivityContext == null));
//        parentActivityContext.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);

        
	}

}

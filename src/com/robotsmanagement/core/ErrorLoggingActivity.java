package com.robotsmanagement.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.robotsmanagement.R;

public class ErrorLoggingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error_logging_screen);
		
		Intent creationIntent = getIntent();
		String errorDesc = creationIntent.getStringExtra(ExceptionHandler.ERROR_TAG);
		((TextView) findViewById(R.id.errorDesc)).setText(errorDesc);
		
	}

}

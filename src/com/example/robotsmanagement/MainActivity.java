package com.example.robotsmanagement;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/* 
 * 1. Skalowanie mapy to nie jest najlepsze wyjscie w naszej sytuacji (powinien byc z gory znany
 * obszar ktory sie pokazuje w widoku mapy)
 * 2. Mniejsze przyciski
 * 3. Pochowac kolory/rozmiary tekstow itd. do *.xml jakas zmiana
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        View imageView = (ImageView) findViewById(R.id.mapComponent);
        imageView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				return true;
			}
		});
    }
}

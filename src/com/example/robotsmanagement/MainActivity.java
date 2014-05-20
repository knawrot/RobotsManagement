package com.example.robotsmanagement;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/* 
 * 1. Skalowanie mapy to nie jest najlepsze wyjscie w naszej sytuacji (powinien byc z gory znany
 * obszar ktory sie pokazuje w widoku mapy)
 * 2. Mniejsze przyciski
 * 3. Pochowac kolory/rozmiary tekstow itd. do *.xml jakas zmiana
 */

public class MainActivity extends Activity {

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Canvas canvas;
	private Thread renderThread;
	private boolean renderFlag;
	
	private float x = 00.0f;
	private float y = 00.0f;
	private float zoom = 15.0f;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
			JsonMapRenderer.load(getApplicationContext(), "second_floor_rooms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        this.surfaceView = (SurfaceView) findViewById(R.id.mapComponent);
        this.surfaceHolder = this.surfaceView.getHolder();
        this.surfaceHolder.addCallback(surfaceHolderCallback);
        
        setUpListeners();
        
        this.renderFlag = false;
        this.renderThread = new Thread(renderingLoop);
        this.renderThread.start();
    }
    
    private void setUpListeners() {
    	SurfaceView imageView = (SurfaceView) findViewById(R.id.mapComponent);
        imageView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.i("Aplikacja", "Wspolrzedna X: " + event.getX() +
							", Y: " + event.getY());
					moveRobot(event.getX(), event.getY());
				}
				return true;
			}
		});
        
        final ListView list = (ListView) findViewById(R.id.robotsList); 
        list.setOnItemClickListener(new OnItemClickListener() {
        	View currentlySelected = null;

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//Log.i("Aplikacja", (String) list.getItemAtPosition(position)); 
				//TODO: uaktywnij ikony z szarego koloru jesli jeszcze nie sa aktywne
				view.setBackgroundColor(getResources().getColor(R.color.GRASS_GREEN));
				if(currentlySelected != null) 
					currentlySelected.setBackgroundColor(0);
				currentlySelected = view;
			}
		});
    }
    
    public void moveRobot(float x, float y) {
    	translateToMapCords(x, y);
    	//TODO: dalsze gunwo
    }
    
    private void translateToMapCords(float x, float y) {
    	//TODO: gunwo, jakas skala musi byc zdefiniowana or so
    }
    
    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			renderFlag = true;
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private Runnable renderingLoop = new Runnable() {
		
		@Override
		public void run() {
			boolean interrupedInternally = false;
            
	        while(!renderThread.isInterrupted() && !interrupedInternally) {
	            try {
	            	Thread.sleep(40);
	            } catch(InterruptedException ie) {
	            	interrupedInternally = true;
	            }
	            if(!renderFlag)
	            	continue;
	             
	            try {
	                canvas = surfaceHolder.lockCanvas();
	                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SCREEN);
					JsonMapRenderer.draw(canvas, x, y, zoom);
	                surfaceHolder.unlockCanvasAndPost(canvas);
	            } catch(Exception e) {
	            	interrupedInternally = true;
	            }
	        }
		}
	};
}

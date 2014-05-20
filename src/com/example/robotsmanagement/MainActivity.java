package com.example.robotsmanagement;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

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
	
	private float x = 10.0f;
	private float y = 10.0f;
	private float zoom = 20.0f;
	
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
        
        surfaceView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				return true;
			}
		});
        
        this.renderFlag = false;
        this.renderThread = new Thread(renderingLoop);
        this.renderThread.start();
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

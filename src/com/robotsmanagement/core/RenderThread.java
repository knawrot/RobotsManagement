package com.robotsmanagement.core;

import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.Log;

import com.robotsmanagement.R;
import com.robotsmanagement.ui.list.CustomListItem;
import com.robotsmanagement.ui.map.JsonMapRenderer;

public class RenderThread extends Thread {

	private static final String tag = RenderThread.class.getName();
	private static final String map = "mapa_laboratorium";
	
	private static float x = -1.0f;
	private static float y = -1.0f;
	private static float zoom = 15.0f;
	
	private final MainActivity activity;
	private Canvas canvas;
	private CustomListItem lastItem;

	RenderThread(MainActivity activity) {
		this.activity = activity;
		lastItem = null;

		try {
			JsonMapRenderer.load(activity.getApplicationContext(), map);
		} catch (IOException e) {
			Log.e("MAP LOADER", "Nie udalo sie zaladowac mapy");
		}
	}
	
	@Override
	public void run() {
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		
		while(!activity.getRenderThread().isInterrupted()) {
			canvas = activity.getSurfaceHolder().lockCanvas();

			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SCREEN);
			
			// rysowanie mapy
			JsonMapRenderer.draw(canvas, x, y, zoom);
			
			// oznaczanie lokalizacji robotów na mapie 
			for(CustomListItem item : activity.getItems()) {
				if(activity.getSelectedItem() != item) {
					item.draw(canvas, x, y, zoom);
				} else if(item == lastItem) {
					item.draw(canvas, x, y, zoom, paint, false);
				} else {
					item.draw(canvas, x, y, zoom, paint, true);
					lastItem = item;
				}
			}
			
			activity.getSurfaceHolder().unlockCanvasAndPost(canvas);
		}
		
		Log.d(tag, "Watek renderowania zostal zamkniety");
	}

	public void setDefaultZoom() {
		zoom = activity.findViewById(R.id.mapComponent).getHeight() / (JsonMapRenderer.getMapHeight() + 2);
		Log.d(tag, "Ustawianie domyslnego przyblizenia mapy: " + zoom);
	}

	public void setX(float _x) {
		x = _x;
	}

	public void setY(float _y) {
		y = _y;
	}

	public void setZoom(float _zoom) {
		zoom = _zoom;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZoom() {
		return zoom;
	}
}

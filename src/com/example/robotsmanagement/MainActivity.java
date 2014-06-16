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
 * TODO:
 * 1. Mniejsze przyciski
 */

public class MainActivity extends Activity {

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Canvas canvas;
	private Thread renderThread;
	private boolean renderFlag;
	private static final int NO_GESTURE = -1;
	private static final int PINCH_ZOOM = 0;
	private static final int DRAG_MOVE = 1;

	private int androidGesture = NO_GESTURE;
	private float x = 0.0f;
	private float y = 0.0f;
	private float startX = 0.0f;
	private float startY = 0.0f;
	private float zoom = 15.0f;
	private float oldDist = 0.0f;
	private float newDist = 0.0f;

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
					Log.i("Aplikacja", "Wspolrzedna X: " + event.getX()
							+ ", Y: " + event.getY());
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
				// Log.i("Aplikacja", (String)
				// list.getItemAtPosition(position));
				// TODO: uaktywnij ikony z szarego koloru jesli jeszcze nie sa
				// aktywne
				view.setBackgroundColor(getResources().getColor(
						R.color.GRASS_GREEN));
				if (currentlySelected != null)
					currentlySelected.setBackgroundColor(0);
				currentlySelected = view;
			}
		});

		surfaceView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					startX = event.getX();
					startY = event.getY();
					androidGesture = DRAG_MOVE;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = calDistBtwFingers(event);
					if (oldDist > 10f) {
						androidGesture = PINCH_ZOOM;
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					androidGesture = NO_GESTURE;
					break;
				case MotionEvent.ACTION_MOVE:
					if (androidGesture == DRAG_MOVE) {
						Log.d("EVENT", "DRAG");
						x += (event.getX() - startX) * 0.1f;
						y += (event.getY() - startY) * 0.1f;
					} else if (androidGesture == PINCH_ZOOM) {
						Log.d("EVENT", "ZOOM");
						newDist = calDistBtwFingers(event);
						if (newDist > 10f) {
							zoom *= newDist / oldDist;
							// x = ?
							// y = ?
						}
					}

					break;
				}
				return true;
			}
		});

	}

	private float calDistBtwFingers(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	public void moveRobot(float x, float y) {
		translateToMapCords(x, y);
		// TODO: dalsze gunwo
	}

	private void translateToMapCords(float x, float y) {
		// TODO: gunwo, jakas skala musi byc zdefiniowana or so
	}

	private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

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

	private final Runnable renderingLoop = new Runnable() {

		@Override
		public void run() {
			boolean interrupedInternally = false;
			// canvas.save();

			while (!renderThread.isInterrupted() && !interrupedInternally) {
				try {
					Thread.sleep(40);
				} catch (InterruptedException ie) {
					interrupedInternally = true;
				}
				if (!renderFlag)
					continue;

				try {
					canvas = surfaceHolder.lockCanvas();
					canvas.drawColor(0, PorterDuff.Mode.CLEAR);
					canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SCREEN);
					JsonMapRenderer.draw(canvas, x, y, zoom);
					surfaceHolder.unlockCanvasAndPost(canvas);
				} catch (Exception e) {
					interrupedInternally = true;
				}
			}
		}
	};
}

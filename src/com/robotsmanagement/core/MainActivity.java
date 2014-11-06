package com.robotsmanagement.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robotsmanagement.R;
import com.robotsmanagement.ui.list.ConnectionStatus;
import com.robotsmanagement.ui.list.CustomListAdapter;
import com.robotsmanagement.ui.list.CustomListItem;
import com.robotsmanagement.ui.map.JsonMapRenderer;

public class MainActivity extends Activity implements Observer {

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Canvas canvas;
	private Thread renderThread;
	private boolean renderFlag;
	private ListView list;
	private final ArrayList<CustomListItem> items = new ArrayList<CustomListItem>();
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

		setUpRobotsList();
		setUpListeners();

		this.renderFlag = false;
		this.renderThread = new Thread(renderingLoop);
		this.renderThread.start();
	}

	private void setUpRobotsList() {
		items.add(new CustomListItem("Damian", "255.255.255.0"));
		items.add(new CustomListItem("Robert", "192.168.1.2"));
		ArrayAdapter<CustomListItem> adapter = new CustomListAdapter(this,
				R.layout.custom_list_item, items);
		list = (ListView) findViewById(R.id.robotsList);
		list.setAdapter(adapter);
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

		ImageButton addRobButton = (ImageButton) findViewById(R.id.addRobotButton);
		addRobButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// (new AddRobotDialog()).show();
				// items.add("Cos nowego");
				// adapter.notifyDataSetChanged();
				LayoutInflater layInf = LayoutInflater.from(MainActivity.this);
				final View dialogView = layInf.inflate(
						R.layout.add_robot_dialog, null);

				AlertDialog.Builder addDialogBuilder = new AlertDialog.Builder(
						MainActivity.this);
				addDialogBuilder.setView(dialogView);
				addDialogBuilder
						.setTitle("Dodaj robota")
						.setPositiveButton(R.string.addRobotAccept,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										EditText nameInput = (EditText) dialogView
												.findViewById(R.id.nameEditText);
										EditText ipInput = (EditText) dialogView
												.findViewById(R.id.ipEditText);

										String name = nameInput.getText()
												.toString();
										String ip = ipInput.getText()
												.toString();
										CustomListItem newItem = new CustomListItem(
												name, ip);
										items.add(newItem);
										newItem.addObserver(MainActivity.this);
										((BaseAdapter) list.getAdapter())
												.notifyDataSetChanged();
										Toast.makeText(MainActivity.this,
												"Dodano robota",
												Toast.LENGTH_LONG).show();
										new ConnectionEstabilishmentTask()
												.execute(newItem);
									}
								})
						.setNegativeButton(R.string.addRobotCancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										// TODO: prawdopodbnie nie rob nic,
										// ewentualnie
										// dialog o bledzie czy cus
									}
								});

				AlertDialog addDialog = addDialogBuilder.create();
				addDialog.show();
				// addDialog.getWindow().setLayout(450, 400);

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

	@Override
	public void update(Observable observable, Object data) {
		CustomListItem item = (CustomListItem) data;
		if (item.isConnected()) {
			Log.d("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.CONNECTED.name());
			View listElemToEdit = list.getChildAt(items.indexOf(item));
			TextView progressBar = (TextView) listElemToEdit
					.findViewById(R.id.robotName);
			ViewGroup parent = (ViewGroup) progressBar.getParent();
			if (parent != null) {
				Log.d("CONNECTION RESULT", progressBar.getText().toString());
				parent.removeView(progressBar);
			}
			// parent.removeView(progressBar);
		} else {
			Log.d("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.DISCONNECTED.name());
		}
	}

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

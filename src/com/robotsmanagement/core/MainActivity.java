package com.robotsmanagement.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.robotsmanagement.R;
import com.robotsmanagement.core.stream.StreamRequestListener;
import com.robotsmanagement.ui.list.ConnectionStatus;
import com.robotsmanagement.ui.list.CustomListAdapter;
import com.robotsmanagement.ui.list.CustomListItem;
import com.robotsmanagement.ui.map.JsonMapRenderer;

public class MainActivity extends Activity implements Observer {

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private RenderThread renderThread;
	private LocationThread locationThread;
	private ListView list;
	private CustomListItem selectedItem;
	private final ArrayList<CustomListItem> items = new ArrayList<CustomListItem>();
	private static final int NO_GESTURE = -1;
	private static final int PINCH_ZOOM = 0;
	private static final int DRAG_MOVE = 1;
	private int androidGesture = NO_GESTURE;
	private float startX = 0.0f;
	private float startY = 0.0f;
	private float oldDist = 0.0f;
	private float newDist = 0.0f;
	private boolean surfaceCreated = false;

	private Handler guiUpdatesHandler;

	public ArrayList<CustomListItem> getItems() {
		return items;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) {
	    	Log.e("Vitamio loader", "Nie udalo sie zaladowac biblioteki streamingu wideo.");
	        return;
	    }
	        
		setContentView(R.layout.activity_main);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getApplicationContext()));
		FilesHandler.setContext(getApplicationContext());
		GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.googleMap))
		        .getMap();
	    
	    if (map != null) {
	      Marker hamburg = map.addMarker(new MarkerOptions().position(new LatLng(53.558, 9.927))
	          .title("Hamburg"));
	    }
	    else
	    	Log.e("GOOGLE MAPS", "NULL :c");
		if (FilesHandler.fileExists(ExceptionHandler.ERRORS_FILE)) {
//	        Intent intent = new Intent(this, ErrorLoggingActivity.class);
//	        Log.d("starting activity", "starting new activity");
//	        startActivity(intent);
			AlertDialog.Builder errorReportDialog = new AlertDialog.Builder(this);
			errorReportDialog.setMessage(FilesHandler.readFromFile(ExceptionHandler.ERRORS_FILE));
			errorReportDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					FilesHandler.deleteFile(ExceptionHandler.ERRORS_FILE);
					Log.d("FILE", String.valueOf(FilesHandler.fileExists(ExceptionHandler.ERRORS_FILE)));
				}
			});
			errorReportDialog.create().show();
		} else {
			// load shared preferences
		}
		
		try {
			JsonMapRenderer
					.load(getApplicationContext(), "second_floor_rooms2");
		} catch (IOException e) {
			Log.e("MAP LOADER", "Jeb�o w chuj");
		}

		this.surfaceView = (SurfaceView) findViewById(R.id.mapComponent);
		this.surfaceHolder = this.surfaceView.getHolder();
		this.surfaceHolder.addCallback(surfaceHolderCallback);

		setUpRobotsList();
		setUpListeners();

		guiUpdatesHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO: handle null pointer excep
				Log.d("HANDLING MESSAGE", "Handling message");
				View listElemToEdit = (View) msg.obj;
				try {
					ProgressBar progressBar = (ProgressBar) listElemToEdit
							.findViewById(R.id.progressBar);
					progressBar.setVisibility(View.INVISIBLE);
					TextView offlineStatus = (TextView) listElemToEdit
							.findViewById(R.id.offlineTextView);
					TextView onlineStatus = (TextView) listElemToEdit
							.findViewById(R.id.onlineTextView);
					ImageView offlineImage = (ImageView) listElemToEdit
							.findViewById(R.id.offlineImgView);
					ImageView onlineImage = (ImageView) listElemToEdit
							.findViewById(R.id.onlineImgView);
					if (msg.what == 0) {
						offlineImage.setVisibility(View.VISIBLE);
						offlineStatus.setVisibility(View.VISIBLE);
						onlineImage.setVisibility(View.INVISIBLE);
						onlineStatus.setVisibility(View.INVISIBLE);
					} else if (msg.what == 1) {
						offlineImage.setVisibility(View.INVISIBLE);
						offlineStatus.setVisibility(View.INVISIBLE);
						onlineImage.setVisibility(View.VISIBLE);
						onlineStatus.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Log.e("List item initialization", "error: ", e);
				}
			}

		};
		
//		CustomListItem newItem = new CustomListItem(
//				"Mock", "196.23.22.11");
//		items.add(newItem);
	}

	private void setUpRobotsList() {
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
				// TODO: uaktywnij ikony z szarego koloru jesli jeszcze nie sa
				// aktywne; blad przy ponownym (potrojnym dotknieciu tego samego
				// itemu)
				selectedItem = (CustomListItem) parent
						.getItemAtPosition(position);

				view.setBackgroundColor(getResources().getColor(
						R.color.GRASS_GREEN));
				if (currentlySelected != null)
					currentlySelected.setBackgroundColor(0);
				currentlySelected = view;
			}
		});

		surfaceView.setOnTouchListener(new OnTouchListener() {
			float middleX = 0.0f;
			float middleY = 0.0f;

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
						middleX = (event.getX(0) + event.getX(1)) / 2;
						middleY = (event.getY(0) + event.getY(1)) / 2;
						Log.i("----------------",
								"P(" + Float.toString(middleX) + ","
										+ Float.toString(middleY) + ")");
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					androidGesture = NO_GESTURE;
					middleX = 0.0f;
					middleY = 0.0f;
					break;
				case MotionEvent.ACTION_MOVE:
					if (androidGesture == DRAG_MOVE) {

						Log.i("DRAG_MEASUREMENT",
								"P(" + Float.toString(event.getX()) + ","
										+ Float.toString(event.getY()) + ")");
						Log.i("DRAG_MEASUREMENT",
								"Przesuniecie(" + Float.toString(startX) + ","
										+ Float.toString(startY) + ")");
						renderThread.setX(renderThread.getX()
								- (event.getX() - startX) * 0.05f);
						renderThread.setY(renderThread.getY()
								- (event.getY() - startY) * 0.05f);
						startX = event.getX();
						startY = event.getY();
					} else if (androidGesture == PINCH_ZOOM) {
						newDist = calDistBtwFingers(event);
						if (newDist > 10f) {
							renderThread.setZoom(renderThread.getZoom()
									* newDist / oldDist);
							Log.i("ZOOM_MEASUREMENT",
									"zoom="
											+ Float.toString(renderThread
													.getZoom()));
							Log.i("ZOOM_MEASUREMENT",
									"Old scale: P("
											+ Float.toString(renderThread
													.getX())
											+ ","
											+ Float.toString(renderThread
													.getY()) + ")");
							float tmpx = (middleX)
									/ (renderThread.getZoom() * 10);
							float tmpy = (middleY)
									/ (renderThread.getZoom() * 10);
							tmpx = tmpx * 33.0f / surfaceView.getHeight();
							tmpy = tmpy * 45.0f / surfaceView.getWidth();
							Log.i("ZOOM_MEASUREMENT",
									"Vector: P[" + Float.toString(tmpx) + ","
											+ Float.toString(tmpy) + "]");
							if (newDist > oldDist) {
								renderThread.setX(renderThread.getX() + tmpx);
								renderThread.setY(renderThread.getY() + tmpy);
							} else {
								renderThread.setX(renderThread.getX() - tmpx);
								renderThread.setY(renderThread.getY() - tmpy);
							}
							Log.i("ZOOM_MEASUREMENT",
									"New scale: P("
											+ Float.toString(renderThread
													.getX())
											+ ","
											+ Float.toString(renderThread
													.getY()) + ")");
							oldDist = newDist;
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
										if (name == null || ip == null
												|| name.equals("")
												|| ip.equals(""))
											return;
										CustomListItem newItem = new CustomListItem(
												name, ip);
										newItem.addObserver(MainActivity.this);
										items.add(newItem);
										((BaseAdapter) list.getAdapter())
										.notifyDataSetChanged();
										Toast.makeText(MainActivity.this,
												"Dodano robota",
												Toast.LENGTH_LONG).show();
										new ConnectionEstabilishmentTask()
												.execute(newItem);
									}
								})
						.setNegativeButton(R.string.addRobotCancel, null);

				AlertDialog addDialog = addDialogBuilder.create();
				addDialog.show();

			}
		});
		
	

		ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new StreamRequestListener(this));

		ImageButton sonarButton = (ImageButton) findViewById(R.id.colliDrawButton);
		sonarButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// if(getList().getSelectedItemPosition() ==
				// AdapterView.INVALID_POSITION)
				// return;
				
				if(getSelectedItem() != null) {
					if(!getSelectedItem().isHokuyoRunning())
						(new HokuyoSensorTask()).execute(getSelectedItem());
	
					getSelectedItem().setHokuyoRunning(!getSelectedItem().isHokuyoRunning());
				}
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

	@Override
	public void update(Observable observable, Object data) {
		CustomListItem item = (CustomListItem) data;
		View listElemToEdit;
		/* there are some undefined delays when updating the ListView,
		 as a reference is not available - the following handles it */
		while ((listElemToEdit = list.getChildAt(items.indexOf(item))) == null);
		
		if (item.isConnected()) {
			Log.i("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.CONNECTED.name());
			Message updateMsg = guiUpdatesHandler.obtainMessage(1,
					listElemToEdit);
			updateMsg.sendToTarget();
		} else {
			Log.i("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.DISCONNECTED.name());
			Message updateMsg = guiUpdatesHandler.obtainMessage(0,
					listElemToEdit);
			updateMsg.sendToTarget();
		}
	}

	private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i("SurfaceHolder", "wywo�anie surfaceDestroyed()");
			stop(renderThread);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i("SurfaceHolder", "wywo�anie surfaceCreated()");
			renderThread.start();
			surfaceCreated = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i("SurfaceHolder", "wywo�anie surfaceChanged()");
		}
	};

	// zarz�dzanie w�tkami renderowania i lokalizacji:

	@Override
	protected void onPause() {
		//konieczne, bo inaczej aplikacja sie wypierdziela przy zamknieciu
		super.onPause();
		Log.i("SurfaceHolder", "wywo�anie onPause()");
		stop(renderThread);
		stop(locationThread);
		HokuyoSensorTask.closeListeners();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("SurfaceHolder", "wywo�anie onResume()");

		locationThread = new LocationThread(this);
		//locationThread.start();

		renderThread = new RenderThread(this);
		if (surfaceCreated && !renderThread.isAlive())
			renderThread.start();
	}

	private void stop(Thread thread) {
		boolean wait = true;
		while (wait) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public CustomListItem getSelectedItem() {
		return selectedItem;
	}

	public RenderThread getRenderThread() {
		return renderThread;
	}

	public LocationThread getLocationThread() {
		return locationThread;
	}

	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	public ListView getList() {
		return list;
	}

}

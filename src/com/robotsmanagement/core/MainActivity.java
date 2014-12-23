package com.robotsmanagement.core;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import pl.edu.agh.amber.drivetopoint.Point;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.net.Uri;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robotsmanagement.R;
import com.robotsmanagement.core.stream.StreamRequestListener;
import com.robotsmanagement.ui.list.ConnectionStatus;
import com.robotsmanagement.ui.list.CustomListAdapter;
import com.robotsmanagement.ui.list.CustomListItem;

public class MainActivity extends Activity implements Observer {

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private RenderThread renderThread;
	private LocationThread locationThread;
	private HokuyoThread hokuyoThread;
	private MediaController mediaController;
	private ListView list;
	private CustomListItem selectedItem;
	private View coloredItem;
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
	private GPSDataRequstTask gpsTask;
	private Handler guiUpdatesHandler;
	private ImageButton cameraButton;
	private ImageButton sonarButton;
	private ImageButton moreInfoButton;
	private GoogleMap map;

	public ArrayList<CustomListItem> getItems() {
		return items;
	}

	public void setMark(double latitude, double longitude) {
		Log.i("DODAWANIE MARKA", String.valueOf(latitude) + " " + String.valueOf(longitude));
		map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	        
		mediaController = new MediaController(this);
		mediaController.setAnchorView(findViewById(R.id.video_view));
	        
		setContentView(R.layout.activity_main);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getApplicationContext()));
		FilesHandler.setContext(getApplicationContext());
		if (FilesHandler.fileExists(ExceptionHandler.ERRORS_FILE)) {
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
		
		surfaceView = (SurfaceView) findViewById(R.id.mapComponent);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceHolderCallback);
		cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setEnabled(false);
		sonarButton = (ImageButton) findViewById(R.id.colliDrawButton);
		sonarButton.setEnabled(false);
		moreInfoButton = (ImageButton) findViewById(R.id.stopButton);
		moreInfoButton.setEnabled(false);
		
		setUpRobotsList();
		setUpListeners();

		gpsTask = new GPSDataRequstTask(MainActivity.this);
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
					else if (msg.what == 2) {
						offlineImage.setVisibility(View.INVISIBLE);
						offlineStatus.setVisibility(View.INVISIBLE);
						onlineImage.setVisibility(View.INVISIBLE);
						onlineStatus.setVisibility(View.INVISIBLE);
						progressBar.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Log.e("List item initialization", "error: ", e);
				}
			}

		};
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.googleMap))
		        .getMap();
	    findViewById(R.id.googleMap).setVisibility(View.GONE);
	    if (map == null) {
	    	Toast.makeText(MainActivity.this, "Google Maps nie sa dostepne!", Toast.LENGTH_LONG).show();
	    	Log.e("GOOGLE MAPS", "NULL :c");
	      
	    }
	    else {
	    	map.setMyLocationEnabled(true);
	    	Location myLocation = map.getMyLocation();
	    	LatLng myGeoCoord;
	    	if (myLocation == null) {
	    		myGeoCoord = new LatLng(50.068127,19.912709);
	    		Toast.makeText(MainActivity.this, "Nie mozna ustalic polozenia urzadzenia, centruje na D17.", Toast.LENGTH_LONG).show();
	    	}
	    	else {
	    		myGeoCoord = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
	    	}
	    	CameraPosition myPostionOnMap = new CameraPosition.Builder().
	    			target(myGeoCoord).
	    			zoom(15).
	    			build();
	    	map.animateCamera(CameraUpdateFactory.newCameraPosition(myPostionOnMap));
	    	map.setOnMapClickListener(new OnMapClickListener() {
				
				@Override
				public void onMapClick(LatLng arg0) {
					if (selectedItem != null)
						map.addMarker(new MarkerOptions().position(arg0)
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
				}
			});
	    	// wzwolac jakis task ktorz bd mial updatowana liste Markow i bedzie je usuwal i dodawal
	    	// LUB
	    	// listenerLocation i kazdego tak udpateowac
	    }
	    
	
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

//		Switch mapSwitch = (Switch) findViewById(R.id.mapSwitch);
//		mapSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				FrameLayout layout = (FrameLayout) MainActivity.this.findViewById(R.id.mapContainer);
//				Log.i("CHILD COUNT", String.valueOf(layout.getChildCount()));
//				while (layout.getChildAt(0) == null || layout.getChildAt(1) == null || layout.getChildAt(2) == null);
//				if (isChecked) {
//					layout.getChildAt(1).bringToFront();
//					layout.getChildAt(1).setVisibility(View.VISIBLE);
//					layout.getChildAt(0).setVisibility(View.INVISIBLE);
//				} else {
//					layout.getChildAt(0).bringToFront();
//					layout.getChildAt(0).setVisibility(View.VISIBLE);
//					layout.getChildAt(1).setVisibility(View.GONE);
//				}
//				layout.getChildAt(2).bringToFront();
//			}
//		});
		
		list.setOnItemClickListener(new OnItemClickListener() {
			View currentlySelected = null;

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO: uaktywnij ikony z szarego koloru jesli jeszcze nie sa
				// aktywne; blad przy ponownym (potrojnym dotknieciu tego samego
				// itemu)

				view.setBackgroundColor(getResources().getColor(
						R.color.GRASS_GREEN));
				if (currentlySelected != null)
					currentlySelected.setBackgroundColor(0);
				if (currentlySelected == view) {
					selectedItem = null;
					cameraButton.setEnabled(false);
					sonarButton.setEnabled(false);
					moreInfoButton.setEnabled(false);
				}
				else {
					selectedItem = (CustomListItem) parent.getItemAtPosition(position);
					cameraButton.setEnabled(true);
					sonarButton.setEnabled(true);
					moreInfoButton.setEnabled(true);
				}
					
				currentlySelected = view;
			}
		});
		
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedItem = (CustomListItem) parent.getItemAtPosition(position);

				view.setBackgroundColor(getResources().getColor(
						R.color.GRASS_GREEN));
				
				if(coloredItem != null && coloredItem != view)
					coloredItem.setBackgroundColor(0);
				
				coloredItem = view;
				
				LayoutInflater layInf = LayoutInflater.from(MainActivity.this);
				final View dialogView = layInf.inflate(
						R.layout.manage_robots_dialog, null);
				
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
						MainActivity.this);
				dialogBuilder.setView(dialogView);
				dialogBuilder
						.setTitle(selectedItem.getRobotName())
						.setPositiveButton(R.string.manageRobotsReconnect,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										selectedItem.setConnectionStatus(ConnectionStatus.CONNECTING);
									}
								})
						.setNeutralButton(R.string.manageRobotsDelete, 
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										items.remove(selectedItem);
										((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
										(new Thread() {
											@Override
											public void run() {
												selectedItem.getClient().terminate();
											}
										}).start();
									}
								})
						.setNegativeButton(R.string.manageRobotsCancel, null);

				AlertDialog addDialog = dialogBuilder.create();
				addDialog.show();
				return false;
			}
		});

		surfaceView.setOnTouchListener(new OnTouchListener() {
			float middleX = 0.0f;
			float middleY = 0.0f;
			private long clickTimer;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					startX = event.getX();
					startY = event.getY();
					androidGesture = DRAG_MOVE;
					clickTimer = System.currentTimeMillis();
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
					if(System.currentTimeMillis() - clickTimer < 200) {
						Log.i("ON_CLICK", "Wspolrzedna X: " + event.getX()
								+ ", Y: " + event.getY());
						moveRobot(event.getX(), event.getY());
					}
					break;
				case MotionEvent.ACTION_POINTER_UP:
					androidGesture = NO_GESTURE;
					middleX = 0.0f;
					middleY = 0.0f;
					break;
				case MotionEvent.ACTION_MOVE:
					if (androidGesture == DRAG_MOVE &&
							System.currentTimeMillis() - clickTimer >= 200) {
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
										new GPSDataRequstTask(MainActivity.this).execute(ip);
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
				getSelectedItem().setHokuyoRunning(!getSelectedItem().isHokuyoRunning());
			}
		});
		
		ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new OnClickListener() {
			
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
//				(new DriveToPointTask()).execute(getSelectedItem());
				(new RoboclawTask(getSelectedItem())).execute(100, 100, 100, 100);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new RoboclawTask(getSelectedItem())).execute(100, -100, 100, -100);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new RoboclawTask(getSelectedItem())).execute(-100, 100, -100, 100);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new RoboclawTask(getSelectedItem())).execute(-100, -100, -100, -100);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	private float calDistBtwFingers(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	@Override
	public void update(Observable observable, Object data) {
		CustomListItem item = (CustomListItem) data;
		View listElemToEdit;
		/* there are some undefined delays when updating the ListView,
		 as a reference is not available - the following handles it */
		while ((listElemToEdit = list.getChildAt(items.indexOf(item))) == null);
		
		if (item.getConnectionStatus() == ConnectionStatus.CONNECTED) {
			Log.i("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.CONNECTED.name());
			Message updateMsg = guiUpdatesHandler.obtainMessage(1,
					listElemToEdit);
			updateMsg.sendToTarget();
		} else if(item.getConnectionStatus() == ConnectionStatus.CONNECTING) {
			Log.i("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.CONNECTING.name());
			Message updateMsg = guiUpdatesHandler.obtainMessage(2,
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
			Log.i("SurfaceHolder", "wywo³anie surfaceDestroyed()");
			stop(renderThread);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i("SurfaceHolder", "wywo³anie surfaceCreated()");
			renderThread.setDefaultZoom();
			renderThread.start();
			surfaceCreated = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i("SurfaceHolder", "wywo³anie surfaceChanged()");
		}
	};
	
	//////////////////////////////
	// Odbieranie strumienia wideo 

	public void playVideo(final String videoUrl) {
		// Get the URL from String VideoURL
		final VideoView videoView = (VideoView) findViewById(R.id.video_view);
		Uri video = Uri.parse(String.format(videoUrl, getSelectedItem().getIp()));
		videoView.setMediaController(mediaController);
		videoView.setVideoURI(video);
		videoView.requestFocus();
		videoView.start();
	}
	
	public void stopVideo() {
		VideoView videoView = (VideoView) findViewById(R.id.video_view);
		videoView.stopPlayback();
		videoView.setVideoURI(null);
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// Sterowanie ruchem robota i translacja wspolrzednych dotyku na wspolrzedne mapy

	public void moveRobot(float x, float y) {
		if(getSelectedItem() == null)
			return;

		Log.i("MOVE_ROBOT", "Wysyanie rozkazu przemieszczenie sie do robota");
		// DriveToPoint pobiera wspolrzedne w milimetrach
		Point coords = translateToMapCords(x, y);
		Point coordsInMiliMs = new Point(coords.x * 1000, coords.y * 1000, 0);
		getSelectedItem().setDestination(coords);
		(new DriveToPointTask(coordsInMiliMs)).execute(getSelectedItem());
	}

	private Point translateToMapCords(float x, float y) {
		float newX = x / renderThread.getZoom() + renderThread.getX();
		float newY = y / renderThread.getZoom() + renderThread.getY();
		Log.d("TRANSLATE_TO_MAP_COORDS", newX + " " + newY);
		return new Point(newX, newY, 0);
	}
	
	//////////////////////////////////////////////////
	// zarz¹dzanie w¹tkami renderowania i lokalizacji:

	@Override
	protected void onPause() {
		//konieczne, bo inaczej aplikacja sie wypierdziela przy zamknieciu
		super.onPause();
		Log.i("SurfaceHolder", "wywo³anie onPause()");
		stop(renderThread);
		stop(locationThread);
		stop(hokuyoThread);
	}

	@Override
	protected void onStop() {
		super.onStop();
		stop(locationThread);
		stop(hokuyoThread);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("SurfaceHolder", "wywo³anie onResume()");

		locationThread = new LocationThread(this);
		locationThread.start();

		hokuyoThread = new HokuyoThread(this);
		hokuyoThread.start();

		renderThread = new RenderThread(this);
		if (surfaceCreated && !renderThread.isAlive())
			renderThread.start();
	}
	
	public void stop(Thread thread) {
		if(thread != null && thread.isAlive())
			(new StopperThread(thread)).start();
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

	public HokuyoThread getHokuyoThread() {
		return hokuyoThread;
	}

	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	public ListView getList() {
		return list;
	}

	public MediaController getMediaCtrl() {
		return mediaController;
	}
}

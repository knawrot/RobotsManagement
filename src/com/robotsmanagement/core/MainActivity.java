package com.robotsmanagement.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pl.edu.agh.amber.drivetopoint.Point;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Switch;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.robotsmanagement.R;
import com.robotsmanagement.core.drivetopoint.DriveToPointTask;
import com.robotsmanagement.core.gps.GPSThread;
import com.robotsmanagement.core.hokuyo.HokuyoThread;
import com.robotsmanagement.core.location.LocationThread;
import com.robotsmanagement.core.map.JsonMapRenderer;
import com.robotsmanagement.core.roboclaw.RoboclawTask;
import com.robotsmanagement.core.stream.StreamRequestListener;
import com.robotsmanagement.model.list.ConnectionStatus;
import com.robotsmanagement.model.list.CustomListAdapter;
import com.robotsmanagement.model.list.CustomListItem;

public class MainActivity extends Activity implements Observer {
	private static final String CLASS_TAG = MainActivity.class.getName();
	private static final int NO_GESTURE = -1;
	private static final int PINCH_ZOOM = 0;
	private static final int DRAG_MOVE = 1;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private RenderThread renderThread;
	private LocationThread locationThread;
	private HokuyoThread hokuyoThread;
	private GPSThread gpsThread;
	private Map<CustomListItem, Marker> robotDestinationMarks;
	private Map<CustomListItem, Marker> robotLocationMarks;
	private MediaController mediaController;
	private ListView list;
	private CustomListItem selectedItem;
	private View currentlySelected;
	private List<CustomListItem> items;
	private ImageButton cameraButton;
	private ImageButton sonarButton;
	private ImageButton moreInfoButton;
	private ImageButton arrowUpButton;
	private ImageButton arrowDownButton;
	private ImageButton arrowLeftButton;
	private ImageButton arrowRightButton;
	private GoogleMap gMap;
	private boolean gMapActive;
	private int androidGesture = NO_GESTURE;
	private float startX = 0.0f;
	private float startY = 0.0f;
	private float oldDist = 0.0f;
	private float newDist = 0.0f;
	private boolean surfaceCreated;
	private Handler guiUpdatesHandler;
	private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolderCallbackImpl();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/* standard Android startup initialization */
		super.onCreate(savedInstanceState);	        
		setContentView(R.layout.activity_main);
		
		/* bounding references to GUI elements */
		mediaController = new MediaController(this);
		mediaController.setAnchorView(findViewById(R.id.video_view));
		surfaceView = (SurfaceView) findViewById(R.id.mapComponent);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceHolderCallback);
		cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setEnabled(false);
		sonarButton = (ImageButton) findViewById(R.id.colliDrawButton);
		sonarButton.setEnabled(false);
		moreInfoButton = (ImageButton) findViewById(R.id.stopButton);
		moreInfoButton.setEnabled(false);
		arrowUpButton = (ImageButton) findViewById(R.id.upButton);
		arrowUpButton.setEnabled(false);
		arrowDownButton = (ImageButton) findViewById(R.id.downButton);
		arrowDownButton.setEnabled(false);
		arrowLeftButton = (ImageButton) findViewById(R.id.leftButton);
		arrowLeftButton.setEnabled(false);
		arrowRightButton = (ImageButton) findViewById(R.id.rightButton);
		arrowRightButton.setEnabled(false);
		currentlySelected = null;
		
		/* some lists initialization */
		items = new ArrayList<CustomListItem>();
		robotDestinationMarks = new HashMap<CustomListItem, Marker>();
		robotLocationMarks = new HashMap<CustomListItem, Marker>();
				
		/* doing all remaining GUI initialization */
		setUpRobotsList();
		setUpListeners();
		
		/* setting up a handler used in Observer pattern */
		guiUpdatesHandler = new GuiChangesHandler(getApplicationContext(), getResources().getColor(R.color.OFFLINE_COLOR));
		
		/* Google Maps initializations */
		gMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.googleMap))
		        .getMap();
	    findViewById(R.id.googleMap).setVisibility(View.GONE);
	    if (gMap == null) {
	    	Toast.makeText(MainActivity.this, "Google Maps nie sa dostepne!", Toast.LENGTH_LONG).show();
	    	Log.e(CLASS_TAG, "Google Maps are not available");
	      
	    }
	    else {
	    	gMap.setMyLocationEnabled(true);
	    	Location myLocation = gMap.getMyLocation();
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
	    	gMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPostionOnMap));
	    	gMap.setOnMapClickListener(new OnMapClickListener() {
				
				@Override
				public void onMapClick(LatLng arg0) {
					if (selectedItem != null) {
						Marker newMarker = gMap.addMarker(new MarkerOptions().position(arg0)
								.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
						
						if (robotDestinationMarks.containsKey(selectedItem))
							robotDestinationMarks.get(selectedItem).remove();
						robotDestinationMarks.put(selectedItem, newMarker);
						
						moveRobotToGeoCoord(arg0.latitude, arg0.longitude);
					}
						
				}
			});
	    }

	    
		/* setting context for save and load files operations */
		FilesHandler.setContext(getApplicationContext());
	    
		/* unexpected problems handling... */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		/* ... as well as providing a user with latest crash report */
		if (FilesHandler.fileExists(ExceptionHandler.ERRORS_FILE)) {
			AlertDialog.Builder errorReportDialog = new AlertDialog.Builder(this);
			errorReportDialog.setMessage(FilesHandler.readFromFile(ExceptionHandler.ERRORS_FILE));
			errorReportDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					FilesHandler.deleteFile(ExceptionHandler.ERRORS_FILE);
					Log.d(CLASS_TAG, "File deleted? " + String.valueOf(FilesHandler.fileExists(ExceptionHandler.ERRORS_FILE)));
				}
			});
			errorReportDialog.create().show();
		}
		
		
		/* startup values */
	    gMapActive = false;
	    surfaceCreated = false;
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
					Log.i(CLASS_TAG, "X: " + event.getX()
							+ ", Y: " + event.getY());
					moveRobotToPosition(event.getX(), event.getY());
				}
				return true;
			}
		});

		
		Switch mapSwitch = (Switch) findViewById(R.id.mapSwitch);
		mapSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				FrameLayout layout = (FrameLayout) MainActivity.this.findViewById(R.id.mapContainer);
				if (isChecked) {
					stopD17Threads();
					startGMapsThreads();
					findViewById(R.id.googleMap).setVisibility(View.VISIBLE);
					findViewById(R.id.mapComponent).setVisibility(View.INVISIBLE);
					gMapActive = true;
				} else {
					stopGMapsThreads();
					startD17Threads();
					findViewById(R.id.googleMap).setVisibility(View.INVISIBLE);
					findViewById(R.id.mapComponent).setVisibility(View.VISIBLE);
					gMapActive = false;
				}
				layout.bringChildToFront(findViewById(R.id.mapSwitch));
				layout.requestLayout();
				layout.invalidate();
			}
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				

				view.setBackgroundColor(getResources().getColor(
						R.color.GRASS_GREEN));
				if (currentlySelected != null)
					currentlySelected.setBackgroundColor(0);
				if (currentlySelected == view) {
					selectedItem = null;
					currentlySelected = null;
					cameraButton.setEnabled(false);
					sonarButton.setEnabled(false);
					moreInfoButton.setEnabled(false);
					arrowUpButton.setEnabled(false);
					arrowDownButton.setEnabled(false);
					arrowLeftButton.setEnabled(false);
					arrowRightButton.setEnabled(false);
				}
				else {
					selectedItem = (CustomListItem) parent.getItemAtPosition(position);
					currentlySelected = view;
					if (((CustomListItem) parent.getItemAtPosition(position)).isConnected()) {
						if (gMapActive) {
							CameraPosition myPostionOnMap = new CameraPosition.Builder().
					    			target(selectedItem.getGMapsLocation()).
					    			zoom(25).
					    			build();
							gMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPostionOnMap));
						}
						cameraButton.setEnabled(true);
						sonarButton.setEnabled(true);
						moreInfoButton.setEnabled(true);
						arrowUpButton.setEnabled(true);
						arrowDownButton.setEnabled(true);
						arrowLeftButton.setEnabled(true);
						arrowRightButton.setEnabled(true);
					}
				}
					
			}
		});
		
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setBackgroundColor(getResources().getColor(
						R.color.GRASS_GREEN));
				if (currentlySelected != null && currentlySelected != view)
					currentlySelected.setBackgroundColor(0);
				if (currentlySelected != view) {
					selectedItem = (CustomListItem) parent.getItemAtPosition(position);
					currentlySelected = view;
					if (((CustomListItem) parent.getItemAtPosition(position)).isConnected()) {
						cameraButton.setEnabled(true);
						sonarButton.setEnabled(true);
						moreInfoButton.setEnabled(true);
						arrowUpButton.setEnabled(true);
						arrowDownButton.setEnabled(true);
						arrowLeftButton.setEnabled(true);
						arrowRightButton.setEnabled(true);
					}
				}

				final Dialog dialog = new Dialog(MainActivity.this);
				dialog.setContentView(R.layout.manage_robots_dialog);
				dialog.setTitle("Operacje dla " + selectedItem.getRobotName());
				dialog.findViewById(R.id.manageRobotsRcBut).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						selectedItem.setConnectionStatus(ConnectionStatus.CONNECTING);
						
						dialog.dismiss();
					}
					
				});
				dialog.findViewById(R.id.manageRobotsDelBut).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						items.remove(selectedItem);
						((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
						(new Thread() {
							@Override
							public void run() {
								selectedItem.getClient().terminate();
								selectedItem = null;
							}
						}).start();
						MainActivity.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								currentlySelected = null;
								cameraButton.setEnabled(false);
								sonarButton.setEnabled(false);
								moreInfoButton.setEnabled(false);
								arrowUpButton.setEnabled(false);
								arrowDownButton.setEnabled(false);
								arrowLeftButton.setEnabled(false);
								arrowRightButton.setEnabled(false);
							}
						});
						
						dialog.dismiss();
					}
				});
				dialog.findViewById(R.id.manageRobotsCanBut).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				
				dialog.show();
				
				
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
						Log.i(CLASS_TAG,
								"P(" + Float.toString(middleX) + ","
										+ Float.toString(middleY) + ")");
					}
					break;
				case MotionEvent.ACTION_UP:
					if(System.currentTimeMillis() - clickTimer < 200) {
						Log.i(CLASS_TAG, "X: " + event.getX()
								+ ", Y: " + event.getY());
						moveRobotToPosition(event.getX(), event.getY());
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
						Log.i(CLASS_TAG, "DRAG_MEASUREMENT: " +
								"P(" + Float.toString(event.getX()) + ","
										+ Float.toString(event.getY()) + ")");
						Log.i(CLASS_TAG, "DRAG_MEASUREMENT " + 
								"P(" + Float.toString(startX) + ","
										+ Float.toString(startY) + ")");
						renderThread.setX(renderThread.getX()
								- (event.getX() - startX) * 0.6f / renderThread.getZoom());
						renderThread.setY(renderThread.getY()
								- (event.getY() - startY) * 0.6f / renderThread.getZoom());
						startX = event.getX();
						startY = event.getY();
					} else if (androidGesture == PINCH_ZOOM) {
						newDist = calDistBtwFingers(event);
						if (newDist > 10f) {
							renderThread.setZoom(renderThread.getZoom()
									* newDist / oldDist);
							Log.i(CLASS_TAG, "ZOOM_MEASUREMENT " +
									"zoom="
											+ Float.toString(renderThread
													.getZoom()));
							Log.i(CLASS_TAG, "ZOOM_MEASUREMENT " +
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
							Log.i(CLASS_TAG, "ZOOM_MEASUREMENT " + 
									"Vector: P[" + Float.toString(tmpx) + ","
											+ Float.toString(tmpy) + "]");
							if (newDist > oldDist) {
								renderThread.setX(renderThread.getX() + tmpx);
								renderThread.setY(renderThread.getY() + tmpy);
							} else {
								renderThread.setX(renderThread.getX() - tmpx);
								renderThread.setY(renderThread.getY() - tmpy);
							}
							Log.i(CLASS_TAG, "ZOOM_MEASUREMENT " +
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
										if (robotExists(name, ip)) {
											Toast.makeText(MainActivity.this,
													"Taki robot ju¿ istnieje!",
													Toast.LENGTH_LONG).show();
											return;
										}
										
										CustomListItem newItem = new CustomListItem(
												name, ip);
										newItem.addObserver(MainActivity.this);
										items.add(newItem);
										((BaseAdapter) list.getAdapter())
										.notifyDataSetChanged();
										Toast.makeText(MainActivity.this,
												"Dodano robota",
												Toast.LENGTH_LONG).show();
										new ConnectionEstabilishmentTask().execute(newItem);
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
				(new DriveToPointTask()).execute(getSelectedItem());
			}
		});

		arrowUpButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getSelectedItem() != null) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						(new RoboclawTask(getSelectedItem())).execute(200, 200, 200, 200);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
					}
				}
				
				return false; 
			}
			
		});
		
		arrowLeftButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getSelectedItem() != null) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						(new RoboclawTask(getSelectedItem())).execute(-200, 200, -200, 200);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
					}
				}
				return false;
			}
		});
		
		arrowDownButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getSelectedItem() != null) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						(new RoboclawTask(getSelectedItem())).execute(-200, -200, -200, -200);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
					}
				}
				
				return false;
			}
		});
		
		arrowRightButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getSelectedItem() != null) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						(new RoboclawTask(getSelectedItem())).execute(200, -200, 200, -200);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						(new RoboclawTask(getSelectedItem())).execute(0, 0, 0, 0);
					}
				} 
				
				return false;
			}
		});
	}
	
	
	
	/* ------------------------------- */
	/* ----- SOME SIMPLE METHODS ----- */

	private float calDistBtwFingers(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}
	
	private boolean robotExists(String name, String ip) {
		for (CustomListItem listItem : items) {
			if (listItem.getRobotName().equals(name) || listItem.getIp().equals(ip))
				return true;
		}
		return false;
	}

	
	
	/* --------------------------------------------------------- */
	/* ----- UPDATE FOR CHANGES IN LIST (OBSERVER) ------------- */
	
	@Override
	public void update(Observable observable, Object data) {
		CustomListItem item = (CustomListItem) data;
		View listElemToEdit;
		/* there are some undefined delays when updating the ListView,
		 hence a reference is not available - the following handles it */
		while ((listElemToEdit = list.getChildAt(items.indexOf(item))) == null);
		
		if (item.getConnectionStatus() == ConnectionStatus.CONNECTED) {
			Log.i(CLASS_TAG, "Status of connection: "
					+ ConnectionStatus.CONNECTED.name());
			Message updateMsg = guiUpdatesHandler.obtainMessage(1,
					listElemToEdit);
			updateMsg.sendToTarget();
		} else if(item.getConnectionStatus() == ConnectionStatus.CONNECTING) {
			Log.i(CLASS_TAG, "Status of connection: "
					+ ConnectionStatus.CONNECTING.name());
			Message updateMsg = guiUpdatesHandler.obtainMessage(2,
					listElemToEdit);
			updateMsg.sendToTarget();
		} else {
			Log.i(CLASS_TAG, "Status of connection: "
					+ ConnectionStatus.DISCONNECTED.name());
			Message updateMsg = guiUpdatesHandler.obtainMessage(0,
					listElemToEdit);
			updateMsg.sendToTarget();
		}
	}

	
	
	/* -------------------------- */
	/* ----- VIDEO HANDLING ----- */
	
	public void playVideo(final String videoUrl) {
		/* Get the URL from String VideoURL */
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
	
	
	
	/* ------------------------------------------ */
	/* ----- MOVEMENT TO CONCRETE DIRECTION ----- */
	
	protected void moveRobotToGeoCoord(double latitude, double longitude) {
		Log.i(CLASS_TAG, "Sending 'move' (GPS) command to robot... "
				+ "(" + latitude + "," + longitude + ")");
		
		//TODO: Once the functionality is provided, add implementation here
	}

	public void moveRobotToPosition(float x, float y) {
		if(getSelectedItem() == null)
			return;

		Log.i(CLASS_TAG, "Sending 'move' command to robot...");
		Point coords = translateToMapCords(x, y);
		Point coordsInMiliMs = new Point(coords.x, coords.y, JsonMapRenderer.TARGET_RADIUS);
		getSelectedItem().setDestination(coords);
		(new DriveToPointTask(coordsInMiliMs)).execute(getSelectedItem());
	}

	private Point translateToMapCords(float x, float y) {
		float newX = x / renderThread.getZoom() + renderThread.getX();
		float newY = y / renderThread.getZoom() + renderThread.getY();
		Log.d(CLASS_TAG, "TRANSLATE_TO_MAP_COORDS" + newX + " " + newY);
		return new Point(newX, newY, 0);
	}
	
	
	
	/* ----------------------------------------------- */
	/* ----- RENDER AND LOCATION THREAD HANDLING ----- */
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(CLASS_TAG, "onPause()");
		stopD17Threads();
		stopGMapsThreads();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(CLASS_TAG, "onStop()");
		stopD17Threads();
		stopGMapsThreads();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(CLASS_TAG, "onResume()");

		if (gMapActive)
			startGMapsThreads();
		else
			startD17Threads();
		
	}
	
	private void startD17Threads() {
		locationThread = new LocationThread(this);
		locationThread.start();

		hokuyoThread = new HokuyoThread(this);
		hokuyoThread.start();
		
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceHolderCallback);

		renderThread = new RenderThread(this);
		if (surfaceCreated && !renderThread.isAlive())
			renderThread.start();
		
	}
	
	private void startGMapsThreads() {
		gpsThread = new GPSThread(this);
		gpsThread.start();
	}
	
	private void stopD17Threads() {
		stop(renderThread);
		stop(locationThread);
		stop(hokuyoThread);
	}
	
	private void stopGMapsThreads() {
		stop(gpsThread);
	}
	
	public void stop(Thread thread) {
		if(thread != null && thread.isAlive())
			(new StopperThread(thread)).start();
	}

	
	
	/* ------------------------------- */
	/* ----- GETTERS AND SETTERS ----- */
	
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

	public GPSThread getGpsThread() {
		return gpsThread;
	}

	public void setGpsThread(GPSThread gpsThread) {
		this.gpsThread = gpsThread;
	}

	public ListView getList() {
		return list;
	}

	public MediaController getMediaCtrl() {
		return mediaController;
	}
	
	public List<CustomListItem> getItems() {
		return items;
	}
	
	public void setRobotGMapRepresentation(CustomListItem item, double latitude, double longitude) {
		Log.i(CLASS_TAG, "Setting GMap localization..." + String.valueOf(latitude) + " " + String.valueOf(longitude));
		if (robotLocationMarks.containsKey(item))
			robotLocationMarks.get(item).remove();
		robotLocationMarks.put(item, gMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
	}
	
	
	
	/* ------------------------------------- */
	/* ----- DEPENDENT PRIVATE CLASSES ----- */
	
	private class SurfaceHolderCallbackImpl implements SurfaceHolder.Callback {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(CLASS_TAG, "wywo³anie surfaceDestroyed()");
			stop(renderThread);
			surfaceCreated = false;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(CLASS_TAG, "surfaceCreated()");
			renderThread = new RenderThread(MainActivity.this);
			renderThread.setDefaultZoom();
			renderThread.start();
			surfaceCreated = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i(CLASS_TAG, "surfaceChanged()");
		}
	}
	
	private static class GuiChangesHandler extends Handler {
		private final Context applicationContext;
		private final int disabledTextColor;
		
		public GuiChangesHandler(Context context, int disabledTextColor) {
			super();
			applicationContext = context;
			this.disabledTextColor = disabledTextColor;
		}

		@Override
		public void handleMessage(Message msg) {
			Log.d(CLASS_TAG, "Handling message");
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
				TextView robotName = (TextView) listElemToEdit
						.findViewById(R.id.robotName);
				TextView robotIp = (TextView) listElemToEdit
						.findViewById(R.id.robotIp);
				if (msg.what == 0) {
					offlineImage.setVisibility(View.VISIBLE);
					offlineStatus.setVisibility(View.VISIBLE);
					onlineImage.setVisibility(View.INVISIBLE);
					onlineStatus.setVisibility(View.INVISIBLE);
					robotName.setTextColor(disabledTextColor);
					robotIp.setTextColor(disabledTextColor);
				} else if (msg.what == 1) {
					offlineImage.setVisibility(View.INVISIBLE);
					offlineStatus.setVisibility(View.INVISIBLE);
					onlineImage.setVisibility(View.VISIBLE);
					onlineStatus.setVisibility(View.VISIBLE);
					robotName.setTextAppearance(applicationContext, R.attr.textAppearanceListItem);
					robotIp.setTextAppearance(applicationContext, R.attr.textAppearanceListItemSmall);
				}
				else if (msg.what == 2) {
					offlineImage.setVisibility(View.INVISIBLE);
					offlineStatus.setVisibility(View.INVISIBLE);
					onlineImage.setVisibility(View.INVISIBLE);
					onlineStatus.setVisibility(View.INVISIBLE);
					progressBar.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				Log.e(CLASS_TAG, "List item initialization ", e);
			}
		}

	}
}

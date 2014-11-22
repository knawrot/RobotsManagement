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

import com.example.robotsmanagement.R;
import com.robotsmanagement.core.stream.StreamRequestListener;
import com.robotsmanagement.core.stream.TaskDelegate;
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
	private final ArrayList<CustomListItem> items = new ArrayList<CustomListItem>();
	private static final int NO_GESTURE = -1;
	private static final int PINCH_ZOOM = 0;
	private static final int DRAG_MOVE = 1;
	private int androidGesture = NO_GESTURE;
	private float startX = 0.0f;
	private float startY = 0.0f;
	private float oldDist = 0.0f;
	private float newDist = 0.0f;

	private Handler guiUpdatesHandler;

	public ArrayList<CustomListItem> getItems() {
		return items;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		try {
			JsonMapRenderer.load(getApplicationContext(), "second_floor_rooms");
		} catch (IOException e) {
			Log.e("MAP LOADER", "Jeb³o w chuj");
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
			}

		};
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
				// TODO: no cos tu jednak nie chodzi...
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
						Log.i("EVENT", "DRAG");
						Log.i("DRAG_MEASUREMENT",
								"(" + Float.toString(event.getX() - startX)
										+ ","
										+ Float.toString(event.getY() - startY)
										+ ")");
						renderThread.setX(renderThread.getX() + (event.getX() - startX) * 0.005f);
						renderThread.setY(renderThread.getY() + (event.getY() - startY) * 0.005f);
					} else if (androidGesture == PINCH_ZOOM) {
						Log.i("EVENT", "ZOOM");
						newDist = calDistBtwFingers(event);
						if (newDist > 10f) {
							renderThread.setZoom(renderThread.getZoom() * newDist / oldDist);
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
						.setNegativeButton(R.string.addRobotCancel, null);

				AlertDialog addDialog = addDialogBuilder.create();
				addDialog.show();

			}
		});

		ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new StreamRequestListener(this, (TaskDelegate) renderThread));
		
		ImageButton sonarButton = (ImageButton) findViewById(R.id.colliDrawButton);
		sonarButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(getList().getSelectedItemPosition() == AdapterView.INVALID_POSITION)
						return;

				// TODO switch to drawing hokuyo info instead of map
				(new HokuyoSensorTask()).execute(getSelectedItem());			
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
		if (item.isConnected()) {
			Log.i("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.CONNECTED.name());
			View listElemToEdit = list.getChildAt(items.indexOf(item));
			Message updateMsg = guiUpdatesHandler.obtainMessage(1,
					listElemToEdit);
			updateMsg.sendToTarget();
		} else {
			Log.i("CONNECTION RESULT", "Status of connection: "
					+ ConnectionStatus.DISCONNECTED.name());
			View listElemToEdit = list.getChildAt(items.indexOf(item));
			Message updateMsg = guiUpdatesHandler.obtainMessage(0,
					listElemToEdit);
			updateMsg.sendToTarget();
		}
	}

	private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i("SurfaceHolder", "wywo³anie surfaceDestroyed()");
			renderThread.interrupt();
			try {
				renderThread.join();
			} catch (InterruptedException e) {
				Log.w("SurfaceHolder", "Nie uda³o siê poprawnie zamkn¹æ w¹tku renderowania.");
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i("SurfaceHolder", "wywo³anie surfaceCreated()");
			renderThread.start();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Log.i("SurfaceHolder", "wywo³anie surfaceChanged()");
		}
	};

	// zarz¹dzanie w¹tkami renderowania i lokalizacji:
	
	@Override
	protected void onPause() {
		super.onPause();
		
		renderThread.interrupt();
		try {
			renderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		locationThread.interrupt();
		try {
			locationThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		locationThread = new LocationThread(this);
		locationThread.start();
		
		renderThread = new RenderThread(this);
	}

	public CustomListItem getSelectedItem() {
		if(getList().getSelectedItemPosition() == AdapterView.INVALID_POSITION)
			return null;
		else
			return getItems().get(getList().getSelectedItemPosition());
	}

	public Thread getRenderThread() {
		return renderThread;
	}

	public Thread getLocationThread() {
		return locationThread;
	}

	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	public ListView getList() {
		return list;
	}
	
}

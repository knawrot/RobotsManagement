package com.robotsmanagement.core;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2RGBA;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
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
import com.robotsmanagement.core.stream.ActivateStreamTask;
import com.robotsmanagement.core.stream.TaskDelegate;
import com.robotsmanagement.ui.list.ConnectionStatus;
import com.robotsmanagement.ui.list.CustomListAdapter;
import com.robotsmanagement.ui.list.CustomListItem;
import com.robotsmanagement.ui.map.JsonMapRenderer;

public class MainActivity extends Activity implements Observer, TaskDelegate {

	private FFmpegFrameGrabber grabber;
	private IplImage grabbedImage;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Canvas canvas;
	private Thread renderThread;
	private boolean renderFlag;
	private boolean videoStream;
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

		this.videoStream = false;
		this.renderFlag = false;
		this.renderThread = new Thread(renderingLoop);
		this.renderThread.start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					for (CustomListItem item : getItems()) {
						(new LocationGrabberTask()).execute(item);
					}
				}
			}
		}).start();

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
						x += (event.getX() - startX) * 0.005f;
						y += (event.getY() - startY) * 0.005f;
					} else if (androidGesture == PINCH_ZOOM) {
						Log.i("EVENT", "ZOOM");
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
		cameraButton.setOnClickListener(new ActivateStreamTask(this, // getPackageName(),
				getResources(), Environment.getExternalStorageDirectory()));

		ImageButton sonarButton = (ImageButton) findViewById(R.id.colliDrawButton);
		sonarButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO switch to drawing hokuyo info instead of map

				// (new HokuyoSensorTask()).execute(items.get(0));
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
				if (!renderFlag)
					continue;

				try {
					Paint paint = new Paint();
					canvas = surfaceHolder.lockCanvas();

					canvas.drawColor(0, PorterDuff.Mode.CLEAR);
					canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SCREEN);
					JsonMapRenderer.draw(canvas, x, y, zoom);

					// TODO draw robots location
					if (videoStream && (grabbedImage = grabber.grab()) != null) {
						IplImage img = IplImage.create(grabbedImage.width(),
								grabbedImage.height(), IPL_DEPTH_8U, 4);
						cvCvtColor(grabbedImage, img, CV_BGR2RGBA);
						Bitmap bmp = Bitmap.createBitmap(img.width(),
								img.height(), Config.ARGB_8888);
						bmp.copyPixelsFromBuffer(img.getByteBuffer());
						canvas.drawBitmap(
								bmp,
								null,
								new Rect(0, 0, bmp.getWidth(), bmp.getHeight()),
								paint);
					}
					surfaceHolder.unlockCanvasAndPost(canvas);
				} catch (Exception e) {
					Log.e("RENDERING", "Error drawing frame!");
					e.printStackTrace();
					interrupedInternally = true;
				}
			}
		}
	};

	@Override
	public void streamActivationResult(FFmpegFrameGrabber result) {
		this.grabber = result;
		this.videoStream = true;
	}

}

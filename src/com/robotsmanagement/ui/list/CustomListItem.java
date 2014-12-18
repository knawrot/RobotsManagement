package com.robotsmanagement.ui.list;

import java.io.IOException;
import java.util.Observable;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.hokuyo.HokuyoProxy;
import pl.edu.agh.amber.hokuyo.MapPoint;
import pl.edu.agh.amber.hokuyo.Scan;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class CustomListItem extends Observable {
	
	private final static int DOT_SIZE = 5;
	
	private String robotName;
	private String ip;
	private ConnectionStatus connectionStatus;
	private AmberClient client;
	private Point location;
	private Scan scan;
	private HokuyoProxy hokuyoProxy;
	private boolean hokuyoRunning;

	public CustomListItem(String robotName, String ip) {
		super();
		this.robotName = robotName;
		this.ip = ip;
		connectionStatus = ConnectionStatus.CONNECTING;
	}

	public AmberClient getClient() {
		return client;
	}

	public void setClient(AmberClient client) {
		this.client = client;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public String getRobotName() {
		return robotName;
	}

	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isConnected() {
		return connectionStatus == ConnectionStatus.CONNECTED;
	}

	public void setConnectionStatus(ConnectionStatus status) {
		this.connectionStatus = status;
		Log.d("ITEM UPDATE", "Set up status:  " + status.name());
		setChanged();
		notifyObservers(this);
	}
	
	public void draw(Canvas canvas, float x, float y, float zoom) {
		Paint paint = new Paint();
		paint.setColor(Color.LTGRAY);
		
		if(location == null)
			return;

		int newX = (int) ((location.getX() - x) * zoom);
		int newY = (int) ((location.getY() - y) * zoom);
		
		canvas.drawArc(new RectF(newX - DOT_SIZE, newY - DOT_SIZE, 
				newX + DOT_SIZE, newY + DOT_SIZE), 0, 360, true, paint);
		
		try {
			if(hokuyoRunning) {
				Paint scannerPaint = new Paint();
				scannerPaint.setColor(Color.RED);
				
				for(MapPoint point : scan.getPoints()) {
					float x1 = (float) (newX + point.getDistance() * Math.cos(point.getAngle()));
					float y1 = (float) (newY + point.getDistance() * Math.sin(point.getAngle()));
					
					//TODO nie wiem czy te +1 sa potrzebne
					canvas.drawArc(new RectF(x1, y1, x1 + 1, y1 + 1), 0, 360, true, scannerPaint);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Scan getScan() {
		return scan;
	}

	public void setScan(Scan scan) {
		this.scan = scan;
	}

	public boolean isHokuyoRunning() {
		return hokuyoRunning;
	}

	public void setHokuyoRunning(boolean hokuyoRunning) {
		if(hokuyoRunning == false) {
			scan = null;
			
			if(hokuyoProxy != null) {
				try {
					hokuyoProxy.unsubscribe();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		this.hokuyoRunning = hokuyoRunning;
	}

	public HokuyoProxy getHokuyoProxy() {
		return hokuyoProxy;
	}

	public void setHokuyoProxy(HokuyoProxy hokuyoProxy) {
		this.hokuyoProxy = hokuyoProxy;
	}

}

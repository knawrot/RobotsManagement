package com.robotsmanagement.model.list;

import java.util.Observable;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.drivetopoint.DriveToPointProxy;
import pl.edu.agh.amber.drivetopoint.Point;
import pl.edu.agh.amber.hokuyo.HokuyoProxy;
import pl.edu.agh.amber.hokuyo.MapPoint;
import pl.edu.agh.amber.hokuyo.Scan;
import pl.edu.agh.amber.location.LocationProxy;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.robotsmanagement.core.map.JsonMapRenderer;

public class CustomListItem extends Observable {
	private static final String CLASS_TAG = CustomListItem.class.getName();
	private final static int DOT_SIZE = 5;
	private String robotName;
	private String ip;
	private ConnectionStatus connectionStatus;
	private AmberClient client;
	private Point location;
	private Point destination;
	private LatLng gMapsLocation;
	private boolean gpsLaunched;
	private Scan scan;
	private boolean hokuyoRunning;
	private HokuyoProxy hokuyoProxy;
	private DriveToPointProxy dtpProxy;
	private LocationProxy locationProxy;
	private RoboclawProxy roboclawProxy;
	private double angle;

	public CustomListItem(String robotName, String ip) {
		super();
		this.robotName = robotName;
		this.ip = ip;
		connectionStatus = ConnectionStatus.CONNECTING;
		hokuyoRunning = false;
	}
	
	public void draw(Canvas canvas, float x, float y, float zoom) {
		Paint paint = new Paint();
		paint.setColor(Color.LTGRAY);
		
		if(location == null)
			return;
		
		float newX = (float) ((location.x - x) * zoom);
		float newY = (float) ((location.y - y) * zoom);
		
		canvas.drawArc(new RectF(newX - DOT_SIZE, newY - DOT_SIZE, 
				newX + DOT_SIZE, newY + DOT_SIZE), 0, 360, true, paint);
		
		float y1 = 0;
		float x1 = 10;

		y1 = (float) ((float) DOT_SIZE * 2 * Math.sin(angle));
		x1 = (float) ((float) DOT_SIZE * 2 * Math.cos(angle));

		canvas.drawLine(newX, newY, x1 + newX, y1 + newY, paint);
	}

	public void draw(Canvas canvas, float x, float y, float zoom, Paint paint, boolean refresh) {		
		if(location == null)
			return;		
		
		float newX = (float) ((location.x - x) * zoom);
		float newY = (float) ((location.y - y) * zoom);
		
		canvas.drawArc(new RectF(newX - DOT_SIZE, newY - DOT_SIZE, 
				newX + DOT_SIZE, newY + DOT_SIZE), 0, 360, true, paint);
		
		float y2 = 0;
		float x2 = 10;

		y2 = (float) ((float) DOT_SIZE * 3 * Math.sin(angle));
		x2 = (float) ((float) DOT_SIZE * 3 * Math.cos(angle));

		canvas.drawLine(newX, newY, x2 + newX, y2 + newY, paint);

		if(destination != null) {
			float destX = (float) ((destination.x - x) * zoom);
			float destY = (float) ((destination.y - y) * zoom);
			
			if(JsonMapRenderer.drawPath(canvas, x, y, zoom, location, destination, refresh)) {
				canvas.drawLine(destX - DOT_SIZE, destY - DOT_SIZE, destX + DOT_SIZE, destY + DOT_SIZE, paint);
				canvas.drawLine(destX - DOT_SIZE, destY + DOT_SIZE, destX + DOT_SIZE, destY - DOT_SIZE, paint);
			}
		}
		
		try {
			if(scan == null)
				return;
			if(hokuyoRunning == false)
				return;
			
			Paint scannerPaint = new Paint();
			scannerPaint.setColor(Color.RED);
			
			for(MapPoint point : scan.getPoints()) {
				float x1 = (float) (newX + point.getDistance() * zoom
						* Math.cos(Math.toRadians(point.getAngle()) + angle) / 1000);
				float y1 = (float) (newY + point.getDistance() * zoom
						* Math.sin(Math.toRadians(point.getAngle()) + angle) / 1000);

				canvas.drawPoint(x1, y1, scannerPaint);
			}
		} catch (Exception e) {
			Log.e(CLASS_TAG, "Error drawing canvas");
		}
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

	public LatLng getGMapsLocation() {
		return gMapsLocation;
	}

	public void setGMapsLocation(LatLng gMapsLocation) {
		this.gMapsLocation = gMapsLocation;
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
	
	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public Scan getScan() {
		return scan;
	}

	public void setScan(Scan scan) {
		this.scan = scan;
	}

	public HokuyoProxy getHokuyoProxy() {
		return hokuyoProxy;
	}

	public void setHokuyoProxy(HokuyoProxy hokuyoProxy) {
		this.hokuyoProxy = hokuyoProxy;
	}

	public DriveToPointProxy getDtpProxy() {
		return dtpProxy;
	}

	public void setDtpProxy(DriveToPointProxy dtpProxy) {
		this.dtpProxy = dtpProxy;
	}

	public LocationProxy getLocationProxy() {
		return locationProxy;
	}

	public void setLocationProxy(LocationProxy locationProxy) {
		this.locationProxy = locationProxy;
	}

	public void setDestination(Point destination) {
		this.destination = destination;
	}

	public boolean isHokuyoRunning() {
		return hokuyoRunning;
	}

	public void setHokuyoRunning(boolean hokuyoRunning) {
		this.hokuyoRunning = hokuyoRunning;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getAngle() {
		return angle;
	}

	public RoboclawProxy getRoboclawProxy() {
		return roboclawProxy;
	}

	public void setRoboclawProxy(RoboclawProxy roboclawProxy) {
		this.roboclawProxy = roboclawProxy;
	}

	public boolean isGpsLaunched() {
		return gpsLaunched;
	}

	public void setGpsLaunched(boolean gpsLaunched) {
		this.gpsLaunched = gpsLaunched;
	}

}

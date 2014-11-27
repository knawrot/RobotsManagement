package com.robotsmanagement.core;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2RGBA;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.Log;

import com.robotsmanagement.core.stream.TaskDelegate;
import com.robotsmanagement.ui.list.CustomListItem;
import com.robotsmanagement.ui.map.JsonMapRenderer;

public class RenderThread extends Thread implements TaskDelegate {

	private static final String tag = RenderThread.class.getName();
	
	private MainActivity activity;
	private IplImage grabbedImage;
	private FFmpegFrameGrabber grabber;
	private Canvas canvas;
	private boolean videoStream;
	private float x = 0.0f;
	private float y = 0.0f;
	private float zoom = 15.0f;

	RenderThread(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {
		while(!activity.getRenderThread().isInterrupted()) {
			canvas = activity.getSurfaceHolder().lockCanvas();

			// rysowanie mapy
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SCREEN);
			JsonMapRenderer.draw(canvas, x, y, zoom);
			
			// oznaczanie lokalizacji robotów na mapie 
			for(CustomListItem item : activity.getItems())
				item.draw(canvas, x, y, zoom);
				
			// wyœwietlanie strumienia wideo z kamery robota
			try {
				if(videoStream && (grabbedImage = grabber.grab()) != null) {
					IplImage img = IplImage.create(grabbedImage.width(), grabbedImage.height(), IPL_DEPTH_8U, 4);
					cvCvtColor(grabbedImage, img, CV_BGR2RGBA);
					Bitmap bmp = Bitmap.createBitmap(img.width(), img.height(), Config.ARGB_8888);
					bmp.copyPixelsFromBuffer(img.getByteBuffer());
					canvas.drawBitmap(bmp, null, new Rect(0, 0, bmp.getWidth()/2, bmp.getHeight()/2), new Paint());
				}
			} catch(Exception e) {
				Log.e(tag, "Error drawing camera frame!");
				e.printStackTrace();
			}
			
			activity.getSurfaceHolder().unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public void streamActivationResult(FFmpegFrameGrabber result) {
		this.grabber = result;
		
		if(result != null)
			this.videoStream = true;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
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

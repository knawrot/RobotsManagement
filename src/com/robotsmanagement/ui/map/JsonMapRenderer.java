package com.robotsmanagement.ui.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class JsonMapRenderer {

	private static Map<String, Object> mapEntities;
	
	public static void load(Context context, String filename) throws IOException {
		InputStream in = context.getResources().openRawResource(
	            context.getResources().getIdentifier(filename,
	            "raw", context.getPackageName()));
		
		mapEntities = JsonMapReader.readJsonStream(in);
	}
	
	@SuppressWarnings("unchecked")
	public static void draw(Canvas canvas, float x, float y, float zoom) {
		Paint paint = new Paint();
//		Matrix transform = new Matrix();
		
		for(JsonEntityWall jme : (List<JsonEntityWall>) mapEntities.get("walls")) {
			paint.setColor(Color.parseColor("#".concat(jme.getColor().substring(2))));
			
			float newX1 = (jme.getFrom().x - x) * zoom;
			float newY1 = (jme.getFrom().y - y) * zoom;
			float newX2 = (jme.getTo().x - x) * zoom;
			float newY2 = (jme.getTo().y - y) * zoom;
			
			canvas.drawLine(newX1, newY1, newX2, newY2, paint);
		}
		
//		for(JsonEntityGate jeg : (List<JsonEntityGate>) mapEntities.get("gates")) {			
//			float newX1 = (jeg.getFrom().x - x) * zoom;
//			float newY1 = (jeg.getFrom().y - y) * zoom;
//			float newX2 = (jeg.getTo().x - x) * zoom;
//			float newY2 = (jeg.getTo().y - y) * zoom;
//			
//			float dist = (float) Math.sqrt(Math.pow((newX1 - newX2), 2.0) + Math.pow((newY1 - newY2), 2.0));
//			float angle = (float) Math.toDegrees(Math.atan2(newY2 - newY1, newX2 - newX1));
//
//			paint.setColor(Color.LTGRAY);
//			canvas.drawArc(new RectF(newX1 - dist, newY1 - dist, newX1 + dist, newY1 + dist), 
//					angle, -90, true, paint);
//			paint.setColor(Color.BLACK);
//			canvas.drawArc(new RectF(newX1 - dist + 1, newY1 - dist + 1, newX1 + dist - 1, newY1 + dist - 1), 
//					angle, -90, true, paint);
//			
//			float[] pts = { newX2, newY2 };
//			transform.setRotate(-90, newX1, newY1);
//			transform.mapPoints(pts);
//			
//			paint.setColor(Color.LTGRAY);
//			canvas.drawLine(newX1, newY1, pts[0], pts[1], paint);
//		}
	}
	
	private JsonMapRenderer() { };
	
}

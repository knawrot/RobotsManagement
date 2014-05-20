package com.example.robotsmanagement;

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
		
		for(JsonEntityWall jme : (List<JsonEntityWall>) mapEntities.get("walls")) {
			paint.setColor(Color.parseColor("#".concat(jme.getColor().substring(2))));
			
			float newX1 = (jme.getFrom().x - x) * zoom;
			float newY1 = (jme.getFrom().y - y) * zoom;
			float newX2 = (jme.getTo().x - x) * zoom;
			float newY2 = (jme.getTo().y - y) * zoom;
			
			canvas.drawLine(newX1, newY1, newX2, newY2, paint);
		}
	}
	
	private JsonMapRenderer() { };
	
}

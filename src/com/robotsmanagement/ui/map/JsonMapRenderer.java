package com.robotsmanagement.ui.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.amber.drivetopoint.Point;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;


public class JsonMapRenderer {

	private static Map<String, Object> mapEntities;
	private static Map<JsonEntityNode, Map<JsonEntityNode, Float>> adjacencyMap;
	private static Map<JsonEntityNode, JsonEntityNode> path;
	private static long lastCalculation;
	
	public static void load(Context context, String filename) throws IOException {
		InputStream in = context.getResources().openRawResource(
	            context.getResources().getIdentifier(filename,
	            "raw", context.getPackageName()));
		
		mapEntities = JsonMapReader.readJsonStream(in);
		adjacencyMap = generateAdjacencyMap();
		lastCalculation = System.currentTimeMillis();
	}
	
	@SuppressWarnings("unchecked")
	public static float getMapHeight() {
		if(!mapEntities.containsKey("walls"))
			return 15.0f;
		
		float maxY = 0;
		
		for(JsonEntityWall wall : (List<JsonEntityWall>) mapEntities.get("walls")) {
			if(maxY < wall.getFrom().y)
				maxY = wall.getFrom().y;
			if(maxY < wall.getTo().y)
				maxY = wall.getTo().y;
		}
		
		return maxY;
	}
	
	@SuppressWarnings("unchecked")
	public static void draw(Canvas canvas, float x, float y, float zoom) {
		Paint paint = new Paint();
//		Matrix transform = new Matrix();

		if(mapEntities.containsKey("walls")) {
			for(JsonEntityWall jew : (List<JsonEntityWall>) mapEntities.get("walls")) {
				paint.setColor(Color.parseColor("#".concat(jew.getColor().substring(2))));
				
				float newX1 = (jew.getFrom().x - x) * zoom;
				float newY1 = (jew.getFrom().y - y) * zoom;
				float newX2 = (jew.getTo().x - x) * zoom;
				float newY2 = (jew.getTo().y - y) * zoom;
				
				canvas.drawLine(newX1, newY1, newX2, newY2, paint);
			}
		}
		
		if(mapEntities.containsKey("gates")) {
			for(JsonEntityGate jeg : (List<JsonEntityGate>) mapEntities.get("gates")) {	
				paint.setColor(Color.GRAY);
				
				float newX1 = (jeg.getFrom().x - x) * zoom;
				float newY1 = (jeg.getFrom().y - y) * zoom;
				float newX2 = (jeg.getTo().x - x) * zoom;
				float newY2 = (jeg.getTo().y - y) * zoom;
				
				canvas.drawLine(newX1, newY1, newX2, newY2, paint);
				
//				float dist = (float) Math.sqrt(Math.pow((newX1 - newX2), 2.0) + Math.pow((newY1 - newY2), 2.0));
//				float angle = (float) Math.toDegrees(Math.atan2(newY2 - newY1, newX2 - newX1));
//	
//				canvas.drawArc(new RectF(newX1 - dist, newY1 - dist, newX1 + dist, newY1 + dist), 
//						angle, -90, true, paint);
//				paint.setColor(Color.BLACK);
//				canvas.drawArc(new RectF(newX1 - dist + 1, newY1 - dist + 1, newX1 + dist - 1, newY1 + dist - 1), 
//						angle, -90, true, paint);
//				
//				float[] pts = { newX2, newY2 };
//				transform.setRotate(-90, newX1, newY1);
//				transform.mapPoints(pts);
//				
//				paint.setColor(Color.GRAY);
//				canvas.drawLine(newX1, newY1, pts[0], pts[1], paint);
			}
		}
//		
		// widok na potrzeby debugowania dijkstry:
//				
//		if(mapEntities.containsKey("nodes")) {
//			for(JsonEntityNode jen : (List<JsonEntityNode>) mapEntities.get("nodes")) {
//				paint.setColor(Color.CYAN);
//				
//				float newX3 = (jen.getPosition().x - x) * zoom;
//				float newY3 = (jen.getPosition().y - y) * zoom;
//				
//				canvas.drawArc(new RectF(newX3 - 2, newY3 - 2, newX3 + 2, newY3 + 2), 
//						0, 360, true, paint);
//
//				paint.setColor(Color.YELLOW);
//				for(JsonEntityNode jen2 : adjacencyMap.get(jen).keySet()) {
//					float newX4 = (jen2.getPosition().x - x) * zoom;
//					float newY4 = (jen2.getPosition().y - y) * zoom;
//					
//					canvas.drawLine(newX3, newY3, newX4, newY4, paint);
//				}
//			}
//		}
	}

	public static void drawPath(Canvas canvas, float x, float y, float zoom, 
			Point from, Point to, boolean refresh) {
		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		
		JsonEntityNode jenFrom = new JsonEntityNode("From", null, null, 
				new PointF((float) from.x, (float) from.y));
		JsonEntityNode jenTo = new JsonEntityNode("To", null, null, 
				new PointF((float) to.x, (float) to.y));
		
		long time = System.currentTimeMillis();
		if(time - lastCalculation > 500 || refresh)
			path = calculatePath(jenFrom, jenTo);
	
		if(path == null)
			return;
		
		JsonEntityNode next = jenTo;
		
		while(next != jenFrom) {
			JsonEntityNode prev = path.get(next); 
			
			if(prev == null)
				break;
			
			float newX1 = (float) ((prev.getPosition().x - x) * zoom);
			float newY1 = (float) ((prev.getPosition().y - y) * zoom);
			float newX2 = (float) ((next.getPosition().x - x) * zoom);
			float newY2 = (float) ((next.getPosition().y - y) * zoom);
			
			next = prev;
			
			canvas.drawLine(newX1, newY1, newX2, newY2, paint);
		}
	};
	
	@SuppressWarnings("unchecked")
	private static boolean checkPathIntersectionWithWalls(JsonEntityNode n1, JsonEntityNode n2) {
		boolean intersects = false;
		
		if(mapEntities.containsKey("walls")) {
			for(JsonEntityWall jew : (List<JsonEntityWall>) mapEntities.get("walls")) {
				if(JsonMapGraphUtil.intersects(n1, n2, jew)) {
					intersects = true;
					break;
				}
			}
		}
		
		return intersects;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<JsonEntityNode, Map<JsonEntityNode, Float>> generateAdjacencyMap() {
		Map<JsonEntityNode, Map<JsonEntityNode, Float>> adjacencyMap = 
				new HashMap<JsonEntityNode, Map<JsonEntityNode, Float>>();
		
		if(mapEntities.containsKey("nodes")) {
			for(JsonEntityNode jen1 : (List<JsonEntityNode>) mapEntities.get("nodes")) {
				Map<JsonEntityNode, Float> tmp = new HashMap<JsonEntityNode, Float>();
				
				for(JsonEntityNode jen2 : (List<JsonEntityNode>) mapEntities.get("nodes")) {
					if(jen1 != jen2 && !checkPathIntersectionWithWalls(jen1, jen2))
						tmp.put(jen2, JsonMapGraphUtil.calculateDistance(jen1, jen2));
				}

				adjacencyMap.put(jen1, tmp);
			}
		}
		
		return adjacencyMap;
		
	}
	
	@SuppressWarnings("unchecked")
	private static Map<JsonEntityNode, JsonEntityNode> calculatePath(JsonEntityNode jenFrom, JsonEntityNode jenTo) {
		Map<JsonEntityNode, JsonEntityNode> prev = null;
		
		if(mapEntities.containsKey("nodes")) {
			
			Map<JsonEntityNode, Float> tmp = new HashMap<JsonEntityNode, Float>();

			for(JsonEntityNode jen : (List<JsonEntityNode>) mapEntities.get("nodes")) {
				if(jenTo != jen && !checkPathIntersectionWithWalls(jenTo, jen))
					adjacencyMap.get(jen).put(jenTo, JsonMapGraphUtil.calculateDistance(jenTo, jen));
			}
			
			adjacencyMap.put(jenTo, new HashMap<JsonEntityNode, Float>());
			
			for(JsonEntityNode jen : (List<JsonEntityNode>) mapEntities.get("nodes")) {
				if(jenFrom != jen && !checkPathIntersectionWithWalls(jenFrom, jen))
					tmp.put(jen, JsonMapGraphUtil.calculateDistance(jenFrom, jen));
			}

			adjacencyMap.put(jenFrom, tmp);
			
			prev = JsonMapGraphUtil.dijkstra(jenFrom, jenTo, adjacencyMap);
			
			adjacencyMap.remove(jenFrom);
			
			for(JsonEntityNode jen : (List<JsonEntityNode>) mapEntities.get("nodes")) {
				if(adjacencyMap.get(jen).containsKey(jenTo))
					adjacencyMap.get(jen).remove(jenTo);
			}
			
			adjacencyMap.remove(jenTo);
		}
		
		return prev;
	}

	private JsonMapRenderer() { }
	
}

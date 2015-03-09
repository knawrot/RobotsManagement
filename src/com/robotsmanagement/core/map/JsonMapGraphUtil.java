package com.robotsmanagement.core.map;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.robotsmanagement.model.map.JsonEntityNode;
import com.robotsmanagement.model.map.JsonEntityWall;


public class JsonMapGraphUtil {
	private static Map<JsonEntityNode, Float> d;
	
	private static boolean belongsTo(float xx, float xy, float yx, float yy, float zx, float zy)    {
		float det;
		 
		det = xx*yy + yx*zy + zx*xy - zx*yy - xx*zy - yx*xy;
		if (det != 0)
			return false;
		else {
			if ((Math.min(xx, yx) <= zx) && (zx <= Math.max(xx, yx)) &&
					(Math.min(xy, yy) <= zy) && (zy <= Math.max(xy, yy)))
				return true;
			else
				return false;
		}
	}
	 
	private static float det_matrix(float xx, float xy, float yx, float yy, float zx, float zy) {
		return (xx*yy + yx*zy + zx*xy - zx*yy - xx*zy - yx*xy);
	}
	 
	public static boolean intersects(JsonEntityNode n1, JsonEntityNode n2, JsonEntityWall w) {
		if(belongsTo(n1.getPosition().x, n1.getPosition().y, n2.getPosition().x, n2.getPosition().y, w.getFrom().x, w.getFrom().y))
			return true;
		else if(belongsTo(n1.getPosition().x, n1.getPosition().y, n2.getPosition().x, n2.getPosition().y, w.getTo().x, w.getTo().y))
			return true; 
		else if(belongsTo(w.getFrom().x, w.getFrom().y, w.getTo().x, w.getTo().y, n1.getPosition().x, n1.getPosition().y))
			return true; 
		else if(belongsTo(w.getFrom().x, w.getFrom().y, w.getTo().x, w.getTo().y, n2.getPosition().x, n2.getPosition().y))
			return true; 
		else if((det_matrix(n1.getPosition().x, n1.getPosition().y, n2.getPosition().x, n2.getPosition().y, w.getFrom().x, w.getFrom().y))
				* (det_matrix(n1.getPosition().x, n1.getPosition().y, n2.getPosition().x, n2.getPosition().y, w.getTo().x, w.getTo().y)) >= 0)
			return false; 
		else if((det_matrix(w.getFrom().x, w.getFrom().y, w.getTo().x, w.getTo().y, n1.getPosition().x, n1.getPosition().y))
				* (det_matrix(w.getFrom().x, w.getFrom().y, w.getTo().x, w.getTo().y, n2.getPosition().x, n2.getPosition().y)) >= 0)
			return false;
		else
			return true;
	}

	public static Float calculateDistance(JsonEntityNode n1, JsonEntityNode n2) {
		return (float) Math.sqrt(Math.pow(n1.getPosition().x - n2.getPosition().x, 2)
				+ Math.pow(n1.getPosition().y - n2.getPosition().y, 2));
	}

	public static Map<JsonEntityNode, JsonEntityNode> dijkstra(JsonEntityNode jenFrom, JsonEntityNode jenTo,
			Map<JsonEntityNode, Map<JsonEntityNode, Float>> adjacencyMap) {
		Map<JsonEntityNode, JsonEntityNode> prev = new HashMap<JsonEntityNode, JsonEntityNode>();

		Queue<JsonEntityNode> q = new PriorityQueue<JsonEntityNode>(adjacencyMap.keySet().size(), 
				new Comparator<JsonEntityNode>() {

			@Override
			public int compare(JsonEntityNode n1, JsonEntityNode n2) {
				if(d.get(n1) > d.get(n2))
					return 1;
				else if(d.get(n1) < d.get(n2))
					return -1;
				else
					return 0;
			}
			
		});
		
		d = new HashMap<JsonEntityNode, Float>();
		for(JsonEntityNode jen : adjacencyMap.keySet())
			d.put(jen, Float.POSITIVE_INFINITY);
		d.put(jenFrom, Float.valueOf(0));
		for(JsonEntityNode jen : adjacencyMap.keySet())
			q.add(jen);
		
		while(!q.isEmpty()) {
			JsonEntityNode jen = q.remove();
			
			for(JsonEntityNode jen2 : adjacencyMap.get(jen).keySet()) {
				if(d.get(jen2) > d.get(jen) + adjacencyMap.get(jen).get(jen2)) {
					d.put(jen2, d.get(jen) + adjacencyMap.get(jen).get(jen2));
					q.remove(jen2);
					q.add(jen2);
					prev.put(jen2, jen);
				}
			}
		}
		
		return prev;
	}
	
	private JsonMapGraphUtil() { }
	
}

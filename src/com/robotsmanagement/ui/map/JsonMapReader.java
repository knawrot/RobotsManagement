package com.robotsmanagement.ui.map;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

public class JsonMapReader {
	public static Map<String, Object> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMapEntities(reader);
		} finally {
			reader.close();
		}
	}

	public static Map<String, Object> readMapEntities(JsonReader reader) throws IOException {
		Map<String, Object> mapEnitities = new HashMap<String, Object>();
		
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(name.equals("walls")) {
				List<JsonEntityWall> walls = new ArrayList<JsonEntityWall>();
				
				reader.beginArray();
				while(reader.hasNext()) {
					walls.add(readWall(reader));
				}
				reader.endArray();
				mapEnitities.put("walls", walls);
			} else if(name.equals("gates")) {
				List<JsonEntityGate> gates = new ArrayList<JsonEntityGate>();
				
				reader.beginArray();
				while(reader.hasNext()) {
					gates.add(readGate(reader));
				}
				reader.endArray();
				mapEnitities.put("gates", gates);
			} else if(name.equals("nodes")) {
				List<JsonEntityNode> nodes = new ArrayList<JsonEntityNode>();
				
				reader.beginArray();
				while(reader.hasNext()) {
					nodes.add(readNode(reader));
				}
				reader.endArray();
				mapEnitities.put("nodes", nodes);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return mapEnitities;
	}
	
	private static JsonEntityGate readGate(JsonReader reader) throws IOException {
		String id = null;
		String type = null;
		String kind = null;
		Double blocked = -1.0;
		PointF from = null;
		PointF to = null;
		
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(name.equals("id")) {
				id = reader.nextString();
			} else if(name.equals("type")) {
				type = reader.nextString();
			} else if(name.equals("kind")) {
				kind = reader.nextString();
			} else if(name.equals("blocked")) {
				blocked = reader.nextDouble();
			} else if(name.equals("from") && reader.peek() != JsonToken.NULL) {
				from = readDoublesXY(reader);
			} else if(name.equals("to") && reader.peek() != JsonToken.NULL) {
				to = readDoublesXY(reader);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return new JsonEntityGate(id, type, kind, blocked, from, to);
	}

	public static JsonEntityWall readWall(JsonReader reader) throws IOException {
		String id = null;
		String type = null;
		String color = null;
		Double width = -1.0;
		Double height = -1.0;
		PointF from = null;
		PointF to = null;
		
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(name.equals("type")) {
				type = reader.nextString();
			} else if(name.equals("id")) {
				id = reader.nextString();
			} else if(name.equals("width")) {
				width = reader.nextDouble();
			} else if(name.equals("height")) {
				height = reader.nextDouble();
			} else if(name.equals("color")) {
				color = reader.nextString();
			} else if(name.equals("from") && reader.peek() != JsonToken.NULL) {
				from = readDoublesXY(reader);
			} else if(name.equals("to") && reader.peek() != JsonToken.NULL) {
				to = readDoublesXY(reader);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return new JsonEntityWall(id, type, color, width, height, from, to);
	}

	public static JsonEntityNode readNode(JsonReader reader) throws IOException {
		String id = null;
		String type = null;
		String kind = null;
		PointF position = null;
		
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(name.equals("type")) {
				type = reader.nextString();
			} else if(name.equals("id")) {
				id = reader.nextString();
			} else if(name.equals("kind")) {
				kind = reader.nextString();
			} else if(name.equals("position") && reader.peek() != JsonToken.NULL) {
				position = readDoublesXY(reader);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return new JsonEntityNode(id, type, kind, position);
	}

	public static PointF readDoublesXY(JsonReader reader) throws IOException {
		float x = -1.0f;
		float y = -1.0f;
		
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("x")) {
				x = (float) reader.nextDouble();
			} else if(name.equals("y")) {
				y = (float) reader.nextDouble();
			}
		}
		reader.endObject();
		return new PointF(x, y);
	}
}

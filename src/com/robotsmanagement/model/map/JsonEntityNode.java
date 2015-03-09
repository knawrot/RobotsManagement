package com.robotsmanagement.model.map;

import android.graphics.PointF;

public class JsonEntityNode {
	private String id;
	private String type;
	private String kind;
	private PointF position;
	
	public JsonEntityNode(String id, String type, String kind, PointF position) {
		super();
		this.id = id;
		this.type = type;
		this.kind = kind;
		this.position = position;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public PointF getPosition() {
		return position;
	}

	public void setPosition(PointF position) {
		this.position = position;
	}
	
}

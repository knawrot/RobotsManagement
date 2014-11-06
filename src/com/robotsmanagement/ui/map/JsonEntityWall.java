package com.robotsmanagement.ui.map;

import android.graphics.PointF;

public class JsonEntityWall {
	private String id;
	private String type;
	private String color;
	private Double width;
	private Double height;
	private PointF from;
	private PointF to;
	
	public JsonEntityWall(String id, String type, String color, Double width,
			Double height, PointF from, PointF to) {
		super();
		this.id = id;
		this.type = type;
		this.color = color;
		this.width = width;
		this.height = height;
		this.from = from;
		this.to = to;
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
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public Double getWidth() {
		return width;
	}
	
	public void setWidth(Double width) {
		this.width = width;
	}
	
	public Double getHeight() {
		return height;
	}
	
	public void setHeight(Double height) {
		this.height = height;
	}
	
	public PointF getFrom() {
		return from;
	}

	public void setFrom(PointF from) {
		this.from = from;
	}

	public PointF getTo() {
		return to;
	}

	public void setTo(PointF to) {
		this.to = to;
	}
}

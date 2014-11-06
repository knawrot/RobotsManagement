package com.robotsmanagement.ui.map;

import android.graphics.PointF;

public class JsonEntityGate {
	private String id;
	private String type;
	private String kind;
	private Double blocked;
	private PointF from;
	private PointF to;

	public JsonEntityGate(String id, String type, String kind, Double blocked,
			PointF from, PointF to) {
		super();
		this.id = id;
		this.type = type;
		this.kind = kind;
		this.blocked = blocked;
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

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Double getBlocked() {
		return blocked;
	}

	public void setBlocked(Double blocked) {
		this.blocked = blocked;
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

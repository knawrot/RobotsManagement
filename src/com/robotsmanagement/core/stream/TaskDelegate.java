package com.robotsmanagement.core.stream;

import org.bytedeco.javacv.FFmpegFrameGrabber;

public interface TaskDelegate {
	public void streamActivationResult(FFmpegFrameGrabber result);
}

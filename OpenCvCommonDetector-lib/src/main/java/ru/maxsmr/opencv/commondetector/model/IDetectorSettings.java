package ru.maxsmr.opencv.commondetector.model;

import ru.maxsmr.opencv.commondetector.model.graphic.Point;

import java.util.List;


public interface IDetectorSettings {

	DETECTOR_SENSITIVITY getSensitivity();

	/** @return frame to detect - when using detector with preview frames */
	int getFrameToDetect();

	/** @return take photo or use YUV data - when using detector with preview frames */
	boolean takePhoto();

	/** @return frames to analyze - when using detector with video */
	int getFramesToAnalyze();

	/** is source image should be converted to gray color space or not */
	boolean grayscale();

	/** area of source image for detection */
	List<Point> getRegion();

	/** debug mode flag: if set, video without detection will be proceed */
	boolean debugMode();
}

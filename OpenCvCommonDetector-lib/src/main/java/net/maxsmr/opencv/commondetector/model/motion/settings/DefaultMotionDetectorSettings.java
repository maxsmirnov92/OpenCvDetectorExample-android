package net.maxsmr.opencv.commondetector.model.motion.settings;

import net.maxsmr.opencv.commondetector.model.DetectorSensivity;

public class DefaultMotionDetectorSettings {

	public final static DetectorSensivity DEFAULT_SENSITIVITY = DetectorSensivity.MEDIUM;

	public final static int DEFAULT_FRAME_TO_DETECT = 3; // CameraHelper.FRAME_TO_DETECT_MOTION_DEFAULT;
	public final static boolean DEFAULT_TAKE_PHOTO = true;
	public final static int DEFAULT_FRAMES_TO_ANALYZE = 20; // CameraOperationsManager.MOTION_DETECTOR_FRAMES_TO_ANALYZE_DEFAULT;

	public final static boolean DEFAULT_DEBUG_MODE = false;

	public final static double DEFAULT_PIXEL_THRESHOLD_RATIO = 0.01; // SimpleMotionDetector.DEFAULT_PIXEL_THRESHOLD_RATIO;

	public final static boolean DEFAULT_GRAYSCALE = true; // BaseDetector.DEFAULT_GRAYSCALE
	public final static int DEFAULT_MORPH_KERNEL_SIZE = 0; // BaseDetector.DEFAULT_MORPH_KERNEL_SIZE

	public static final int DEFAULT_HISTORY = 3; // BackgroundSubtractorDetector.DEFAULT_HISTORY;
	public static final int DEFAULT_MIXTURES = 4; // BackgroundSubtractorDetector.DEFAULT_MIXTURES;
	public final static double DEFAULT_BACKGROUND_RATIO = 0.8; // BackgroundSubtractorDetector.DEFAULT_BACKGROUND_RATIO;
	public final static double DEFAULT_NOISE_SIGMA = 0.0; // BackgroundSubtractorDetector.DEFAULT_NOISE_SIGMA;
	public final static double DEFAULT_LEARING_RATE = 0.1; // BackgroundSubtractorDetector.DEFAULT_LEARNING_RATE;
	public final static double DEFAULT_MIN_CONTOUR_AREA_RATIO = 0.01; // BackgroundSubtractorDetector.DEFAULT_MIN_CONTOUR_AREA_RATIO;
}

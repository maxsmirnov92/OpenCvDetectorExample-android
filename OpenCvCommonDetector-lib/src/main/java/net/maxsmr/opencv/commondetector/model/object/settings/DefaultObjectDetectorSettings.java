package net.maxsmr.opencv.commondetector.model.object.settings;

import net.maxsmr.opencv.commondetector.model.DetectorSensivity;

public class DefaultObjectDetectorSettings {

    public final static DetectorSensivity DEFAULT_SENSITIVITY = DetectorSensivity.MEDIUM;

    public final static int DEFAULT_FRAME_TO_DETECT = 10; // CameraHelper.FRAME_TO_DETECT_OBJECT_DEFAULT;
    public final static boolean DEFAULT_TAKE_PHOTO = true;
    public final static int DEFAULT_FRAMES_TO_ANALYZE = 20; // CameraOperationsManager.OBJECT_DETECTOR_FRAMES_TO_ANALYZE_DEFAULT;

    public final static boolean DEFAULT_DEBUG_MODE = false;

    public final static OBJECT_TYPE DEFAULT_OBJECT_TYPE = OBJECT_TYPE.UNKNOWN;

    public final static boolean DEFAULT_GRAYSCALE = true; // AbstractClassifierDetector.DEFAULT_GRAYSCALE
}

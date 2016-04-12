package ru.maxsmr.opencv.detectorexample.app;

import ru.maxsmr.opencv.commondetector.model.DETECTOR_SENSITIVITY;
import ru.maxsmr.opencv.commondetector.model.motion.settings.MotionDetectorSettings;
import ru.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;
import ru.maxsmr.opencv.commondetector.model.object.settings.ObjectDetectorSettings;

public class DefaultSettings {

    public static MotionDetectorSettings generateDefaultMotionDetectorSettings() {
        MotionDetectorSettings s = new MotionDetectorSettings();
        s.setGrayscale(true);
        s.setFramesToAnalyze(100);
        s.setSensitivity(DETECTOR_SENSITIVITY.HIGH);
        s.setMinContourAreaRatio(0.001);
        return s;
    }

    public static ObjectDetectorSettings generateDefaultObjectDetectorSettings() {
        ObjectDetectorSettings s = new ObjectDetectorSettings();
        s.setGrayscale(true);
        s.setType(OBJECT_TYPE.HUMAN);
        s.setFramesToAnalyze(30);
        s.setSensitivity(DETECTOR_SENSITIVITY.HIGH);
        return s;
    }

}

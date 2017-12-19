package net.maxsmr.opencv.detectorexample.app;

import net.maxsmr.opencv.commondetector.model.DetectorSensivity;
import net.maxsmr.opencv.commondetector.model.motion.settings.MotionDetectorSettings;
import net.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;
import net.maxsmr.opencv.commondetector.model.object.settings.ObjectDetectorSettings;

public class DefaultSettings {

    public static MotionDetectorSettings generateDefaultMotionDetectorSettings() {
        MotionDetectorSettings s = new MotionDetectorSettings();
        s.setGrayscale(true);
        s.setFramesToAnalyze(100);
        s.setSensitivity(DetectorSensivity.HIGH);
        s.setMinContourAreaRatio(0.001);
        return s;
    }

    public static ObjectDetectorSettings generateDefaultObjectDetectorSettings() {
        ObjectDetectorSettings s = new ObjectDetectorSettings();
        s.setGrayscale(true);
        s.setType(OBJECT_TYPE.HUMAN);
        s.setFramesToAnalyze(30);
        s.setSensitivity(DetectorSensivity.HIGH);
        return s;
    }

}

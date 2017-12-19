package net.maxsmr.opencv.commondetector.motion;

import java.io.File;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public interface IDetector {

	Mat detect(CvCameraViewFrame frame);

	Mat detect(Mat source, List<Point> region);

	boolean isDetected();

	void setContourThickness(int thickness);

	void setContourColor(Scalar color);

	boolean grayscale();

	int getMorphKernelSize();

	File getSavedFramesDir();
}

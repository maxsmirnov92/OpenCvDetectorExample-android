package ru.maxsmr.opencv.commondetector.object.cascade;

import ru.maxsmr.opencv.commondetector.model.object.info.ObjectDetectFrameInfo;
import ru.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

public class BaseClassifierDetector extends AbstractClassifierDetector {

	/** the main cascade classifier; loads in constructor */
	private CascadeClassifier baseClassifier;

	private boolean loadBaseClassifier(File f) {
		baseClassifier = loadClassifier(f);
		return isClassifierLoaded(baseClassifier);
	}

	private OBJECT_TYPE objectType;

	public OBJECT_TYPE getObjectType() {
		return objectType;
	}

	public void setObjectType(OBJECT_TYPE objectType) {
		if (objectType == null) {
			throw new NullPointerException("objectType is null");
		}
		this.objectType = objectType;
	}

	public BaseClassifierDetector(File baseClassifierFile, OBJECT_TYPE objectType) {
		loadBaseClassifier(baseClassifierFile);
		setObjectType(objectType);
	}

	@Override
	public synchronized ObjectDetectFrameInfo detect(Mat frame, Size scaleSize, List<Point> region) {
		return AbstractClassifierDetector.detect(baseClassifier, objectType, frame, scaleSize, region, getContourColor(), grayscale(),
				getSavedFramesDir());
	}

}

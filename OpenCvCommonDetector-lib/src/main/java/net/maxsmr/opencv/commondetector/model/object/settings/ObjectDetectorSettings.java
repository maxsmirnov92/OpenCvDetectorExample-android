package net.maxsmr.opencv.commondetector.model.object.settings;

import net.maxsmr.opencv.commondetector.model.DetectorSensivity;
import net.maxsmr.opencv.commondetector.model.IDetectorSettings;
import net.maxsmr.opencv.commondetector.model.graphic.Point;

import java.io.Serializable;
import java.util.List;


public class ObjectDetectorSettings implements IDetectorSettings, Serializable {

	private static final long serialVersionUID = -7521203372790806032L;
	
	DetectorSensivity sensitivity = DefaultObjectDetectorSettings.DEFAULT_SENSITIVITY;

	@Override
	public DetectorSensivity getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(DetectorSensivity sensitivity) {
		if (sensitivity != null) {
			this.sensitivity = sensitivity;
		}
	}

	
	int frameToDetect = DefaultObjectDetectorSettings.DEFAULT_FRAME_TO_DETECT;

	@Override
	public int getFrameToDetect() {
		return frameToDetect;
	}

	public void setFrameToDetect(int frame) {
		if (frame >= 0) {
			this.frameToDetect = frame;
		}
	}

	
	boolean takePhoto = DefaultObjectDetectorSettings.DEFAULT_TAKE_PHOTO;

	@Override
	public boolean takePhoto() {
		return takePhoto;
	}

	public void setTakePhoto(boolean toggle) {
		this.takePhoto = toggle;
	}

	
	int framesToAnalyze = DefaultObjectDetectorSettings.DEFAULT_FRAMES_TO_ANALYZE;

	@Override
	public int getFramesToAnalyze() {
		return framesToAnalyze;
	}

	public void setFramesToAnalyze(int frames) {
		if (frames >= 1) {
			this.framesToAnalyze = frames;
		}
	}

	
	boolean grayscale = DefaultObjectDetectorSettings.DEFAULT_GRAYSCALE;

	@Override
	public boolean grayscale() {
		return grayscale;
	}

	public void setGrayscale(boolean toggle) {
		this.grayscale = toggle;
	}

	
	boolean debug = DefaultObjectDetectorSettings.DEFAULT_DEBUG_MODE;

	@Override
	public boolean debugMode() {
		return debug;
	}

	public void setDebugMode(boolean toggle) {
		this.debug = toggle;
	}
	
	List<Point> region;

	@Override
	public List<Point> getRegion() {
		return region;
	}

	public void setRegion(List<Point> region) {
		this.region = region;
	}

	
	OBJECT_TYPE type = DefaultObjectDetectorSettings.DEFAULT_OBJECT_TYPE;

	public OBJECT_TYPE getType() {
		return type;
	}

	public void setType(OBJECT_TYPE type) {
		if (type != null)
			this.type = type;
	}

	public ObjectDetectorSettings() {
	}

	public ObjectDetectorSettings(DetectorSensivity sensitivity, int frameToDetect, boolean takePhoto, int framesToAnalyze,
                                  boolean debugMode, boolean grayscale, List<Point> region, OBJECT_TYPE type) {
		setSensitivity(sensitivity);
		setFrameToDetect(frameToDetect);
		setTakePhoto(takePhoto);
		setFramesToAnalyze(framesToAnalyze);
		setDebugMode(debugMode);
		setGrayscale(grayscale);
		setRegion(region);
		setType(type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (debug ? 1231 : 1237);
		result = prime * result + frameToDetect;
		result = prime * result + framesToAnalyze;
		result = prime * result + (grayscale ? 1231 : 1237);
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((sensitivity == null) ? 0 : sensitivity.hashCode());
		result = prime * result + (takePhoto ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectDetectorSettings other = (ObjectDetectorSettings) obj;
		if (debug != other.debug)
			return false;
		if (frameToDetect != other.frameToDetect)
			return false;
		if (framesToAnalyze != other.framesToAnalyze)
			return false;
		if (grayscale != other.grayscale)
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (sensitivity != other.sensitivity)
			return false;
		if (takePhoto != other.takePhoto)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectDetectorSettings [sensitivity=" + sensitivity + ", frameToDetect=" + frameToDetect + ", takePhoto=" + takePhoto
				+ ", framesToAnalyze=" + framesToAnalyze + ", grayscale=" + grayscale + ", debug=" + debug + ", region=" + region
				+ ", type=" + type + "]";
	}

}

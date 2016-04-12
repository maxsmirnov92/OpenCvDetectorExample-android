package ru.maxsmr.opencv.commondetector.model.motion.settings;

import ru.maxsmr.opencv.commondetector.model.DETECTOR_SENSITIVITY;
import ru.maxsmr.opencv.commondetector.model.IDetectorSettings;
import ru.maxsmr.opencv.commondetector.model.graphic.Point;

import java.io.Serializable;
import java.util.List;


public class MotionDetectorSettings implements IDetectorSettings, Serializable {

	private static final long serialVersionUID = -2328381359551570129L;

	DETECTOR_SENSITIVITY sensitivity = DefaultMotionDetectorSettings.DEFAULT_SENSITIVITY;

	@Override
	public DETECTOR_SENSITIVITY getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(DETECTOR_SENSITIVITY sensitivity) {
		if (sensitivity != null) {
			this.sensitivity = sensitivity;
		}
	}

	
	int frameToDetect = DefaultMotionDetectorSettings.DEFAULT_FRAME_TO_DETECT;

	@Override
	public int getFrameToDetect() {
		return frameToDetect;
	}

	public void setFrameToDetect(int frame) {
		if (frame >= 0) {
			this.frameToDetect = frame;
		}
	}

	
	boolean takePhoto = DefaultMotionDetectorSettings.DEFAULT_TAKE_PHOTO;

	@Override
	public boolean takePhoto() {
		return takePhoto;
	}

	public void setTakePhoto(boolean toggle) {
		this.takePhoto = toggle;
	}

	
	int framesToAnalyze = DefaultMotionDetectorSettings.DEFAULT_FRAMES_TO_ANALYZE;

	@Override
	public int getFramesToAnalyze() {
		return framesToAnalyze;
	}

	public void setFramesToAnalyze(int frames) {
		if (frames >= 2) {
			this.framesToAnalyze = frames;
		}
	}

	
	boolean debug = DefaultMotionDetectorSettings.DEFAULT_DEBUG_MODE;

	@Override
	public boolean debugMode() {
		return debug;
	}

	public void setDebugMode(boolean toggle) {
		this.debug = toggle;
	}

	
	boolean grayscale = DefaultMotionDetectorSettings.DEFAULT_GRAYSCALE;

	@Override
	public boolean grayscale() {
		return grayscale;
	}

	public void setGrayscale(boolean toggle) {
		this.grayscale = toggle;
	}

	
	int morphKernelSize = DefaultMotionDetectorSettings.DEFAULT_MORPH_KERNEL_SIZE;

	public int getMorphKernelSize() {
		return morphKernelSize;
	}

	public void setMorphKernelSize(int size) {
		if (size >= 0)
			morphKernelSize = size;
	}

	
	double pixelThresholdRatio = DefaultMotionDetectorSettings.DEFAULT_PIXEL_THRESHOLD_RATIO;

	public double getPixelThresholdRatio() {
		return pixelThresholdRatio;
	}

	public void setPixelThresholdRatio(double pixelThresholdRatio) {
		if (pixelThresholdRatio > 0 && pixelThresholdRatio < 1)
			this.pixelThresholdRatio = pixelThresholdRatio;
	}

	List<Point> region;

	@Override
	public List<Point> getRegion() {
		return region;
	}

	public void setRegion(List<Point> region) {
		this.region = region;
	}

	
	int history = DefaultMotionDetectorSettings.DEFAULT_HISTORY;

	public int getHistory() {
		return history;
	}

	public void setHistory(int history) {
		if (history > 0)
			this.history = history;
	}

	
	int mixtures = DefaultMotionDetectorSettings.DEFAULT_MIXTURES;

	public int getMixtures() {
		return mixtures;
	}

	public void setMixtures(int mixtures) {
		if (mixtures > 0)
			this.mixtures = mixtures;
	}

	
	double backgroundRatio = DefaultMotionDetectorSettings.DEFAULT_BACKGROUND_RATIO;

	public double getBackgroundRatio() {
		return backgroundRatio;
	}

	public void setBackgroundRatio(double backgroundRatio) {
		if (backgroundRatio > 0 && backgroundRatio <= 1)
			this.backgroundRatio = backgroundRatio;
	}

	
	double noiseSigma = DefaultMotionDetectorSettings.DEFAULT_NOISE_SIGMA;

	public double getNoiseSigma() {
		return noiseSigma;
	}

	public void setNoiseSigma(double noiseSigma) {
		if (noiseSigma >= 0)
			this.noiseSigma = noiseSigma;
	}

	
	double learningRate = DefaultMotionDetectorSettings.DEFAULT_LEARING_RATE;

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(double learningRate) {
		if (learningRate >= 0) {
			this.learningRate = learningRate;
		}
	}

	
	double minContourAreaRatio = DefaultMotionDetectorSettings.DEFAULT_MIN_CONTOUR_AREA_RATIO;

	public double getMinContourAreaRatio() {
		return minContourAreaRatio;
	}

	public void setMinContourAreaRatio(double ratio) {
		if (ratio >= 0 && ratio <= 1)
			this.minContourAreaRatio = ratio;
	}

	public MotionDetectorSettings() {
	}

	public MotionDetectorSettings(DETECTOR_SENSITIVITY sensitivity, int frameToDetect, boolean takePhoto, int framesToAnalyze,
			boolean debugMode, boolean grayscale, int morphKernelSize, double pixelThresholdRatio, List<Point> region, int history,
			int mixtures, double backgroundRatio, double noiseSigma, double learningRate, double minContourAreaRatio) {
		setSensitivity(sensitivity);
		setFrameToDetect(frameToDetect);
		setTakePhoto(takePhoto);
		setFramesToAnalyze(framesToAnalyze);
		setDebugMode(debugMode);
		setGrayscale(grayscale);
		setMorphKernelSize(morphKernelSize);
		setPixelThresholdRatio(pixelThresholdRatio);
		setRegion(region);
		setHistory(history);
		setMixtures(mixtures);
		setBackgroundRatio(backgroundRatio);
		setNoiseSigma(noiseSigma);
		setLearningRate(learningRate);
		setMinContourAreaRatio(minContourAreaRatio);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(backgroundRatio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (debug ? 1231 : 1237);
		result = prime * result + frameToDetect;
		result = prime * result + framesToAnalyze;
		result = prime * result + (grayscale ? 1231 : 1237);
		result = prime * result + history;
		temp = Double.doubleToLongBits(learningRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minContourAreaRatio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + mixtures;
		result = prime * result + morphKernelSize;
		temp = Double.doubleToLongBits(noiseSigma);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(pixelThresholdRatio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((sensitivity == null) ? 0 : sensitivity.hashCode());
		result = prime * result + (takePhoto ? 1231 : 1237);
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
		MotionDetectorSettings other = (MotionDetectorSettings) obj;
		if (Double.doubleToLongBits(backgroundRatio) != Double.doubleToLongBits(other.backgroundRatio))
			return false;
		if (debug != other.debug)
			return false;
		if (frameToDetect != other.frameToDetect)
			return false;
		if (framesToAnalyze != other.framesToAnalyze)
			return false;
		if (grayscale != other.grayscale)
			return false;
		if (history != other.history)
			return false;
		if (Double.doubleToLongBits(learningRate) != Double.doubleToLongBits(other.learningRate))
			return false;
		if (Double.doubleToLongBits(minContourAreaRatio) != Double.doubleToLongBits(other.minContourAreaRatio))
			return false;
		if (mixtures != other.mixtures)
			return false;
		if (morphKernelSize != other.morphKernelSize)
			return false;
		if (Double.doubleToLongBits(noiseSigma) != Double.doubleToLongBits(other.noiseSigma))
			return false;
		if (Double.doubleToLongBits(pixelThresholdRatio) != Double.doubleToLongBits(other.pixelThresholdRatio))
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
		return true;
	}

	@Override
	public String toString() {
		return "MotionDetectorSettings [sensitivity=" + sensitivity + ", frameToDetect=" + frameToDetect + ", takePhoto=" + takePhoto
				+ ", framesToAnalyze=" + framesToAnalyze + ", debug=" + debug + ", grayscale=" + grayscale + ", morphKernelSize="
				+ morphKernelSize + ", pixelThresholdRatio=" + pixelThresholdRatio + ", region=" + region + ", history=" + history
				+ ", mixtures=" + mixtures + ", backgroundRatio=" + backgroundRatio + ", noiseSigma=" + noiseSigma + ", learningRate="
				+ learningRate + ", minContourAreaRatio=" + minContourAreaRatio + "]";
	}

}

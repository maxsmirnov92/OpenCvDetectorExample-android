package ru.maxsmr.opencv.commondetector.motion;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.maxsmr.opencv.commondetector.utils.DetectorHelper;

import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.opencv.commondetector.utils.OpenCvUtils;

public final class BackgroundSubtractorDetector extends BaseDetector implements IDetector {

	private static final Logger logger = LoggerFactory.getLogger(BackgroundSubtractorDetector.class);

	public static final int DEFAULT_HISTORY = 3;
	public static final int DEFAULT_MIXTURES = 4;
	public static final double DEFAULT_BACKGROUND_RATIO = 0.8;
	public static final double DEFAULT_NOISE_SIGMA = 10.0;

	public static final double DEFAULT_LEARNING_RATE = 0.1;
	private double learningRate;

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(double learningRate) {
		if (learningRate >= 0)
			this.learningRate = learningRate;
		else
			throw new IllegalArgumentException("incorrect learningRate parameter: " + learningRate);
	}

	public static final double DEFAULT_MIN_CONTOUR_AREA_RATIO = 0.01;
	private double minContourAreaRatio;

	public double getMinContourAreaRatio() {
		return minContourAreaRatio;
	}

	public void setMinContourAreaRatio(double ratio) {
		if (ratio >= 0 && ratio <= 1)
			this.minContourAreaRatio = ratio;
		else
			throw new IllegalArgumentException("incorrect minContourAreaRatio parameter: " + minContourAreaRatio);
	}

	/** ring image buffer */
	private Mat buf = null;
	private final Mat fgMask = new Mat();

	private final BackgroundSubtractorMOG bg;

	public BackgroundSubtractorDetector() {
		this.bg = new BackgroundSubtractorMOG(DEFAULT_HISTORY, DEFAULT_MIXTURES, DEFAULT_BACKGROUND_RATIO, DEFAULT_NOISE_SIGMA);
		setLearningRate(DEFAULT_LEARNING_RATE);
	}

	public BackgroundSubtractorDetector(double backgroundRatio, double learningRate) {

		if (backgroundRatio <= 0 || backgroundRatio > 1)
			throw new IllegalArgumentException("incorrect backgroundRatio parameter: " + backgroundRatio);

		this.bg = new BackgroundSubtractorMOG(DEFAULT_HISTORY, DEFAULT_MIXTURES,
				(backgroundRatio > 0 && backgroundRatio <= 1) ? backgroundRatio : DEFAULT_BACKGROUND_RATIO, DEFAULT_NOISE_SIGMA);

		setLearningRate(learningRate);
	}

	public BackgroundSubtractorDetector(int history, int mixtures, double backgroundRatio, double noiseSigma, double learningRate,
			double minContourAreaRatio) {

		if (history <= 0)
			throw new IllegalArgumentException("incorrect history parameter: " + history);

		if (mixtures <= 0)
			throw new IllegalArgumentException("incorrect mixtures parameter: " + mixtures);

		if (backgroundRatio <= 0 || backgroundRatio > 1)
			throw new IllegalArgumentException("incorrect backgroundRatio parameter: " + backgroundRatio);

		if (noiseSigma < 0)
			throw new IllegalArgumentException("incorrect noiseSigma parameter: " + noiseSigma);

		this.bg = new BackgroundSubtractorMOG(history > 0 ? history : DEFAULT_HISTORY, mixtures > 0 ? mixtures : DEFAULT_MIXTURES,
				(backgroundRatio > 0 && backgroundRatio <= 1) ? backgroundRatio : DEFAULT_BACKGROUND_RATIO, noiseSigma >= 0 ? noiseSigma
						: DEFAULT_NOISE_SIGMA); // noiseSigma <= 1

		setLearningRate(learningRate);
		setMinContourAreaRatio(minContourAreaRatio);
	}

	/** @param source 1, 3 or 4 channel mat */
	@Override
	public synchronized Mat detect(Mat source, List<Point> region) throws NullPointerException, IllegalArgumentException {
		// logger.debug("detect(), source=" + source + ", region=" + region);

		if (source == null)
			throw new NullPointerException("source mat is null");

		if (source.empty())
			throw new IllegalArgumentException("source mat is empty");

		if (!(source.channels() == 1 || source.channels() == 3 || source.channels() == 4))
			throw new IllegalArgumentException("incorrect source mat channels number: " + source.channels());

		if (getSavedFramesDir() != null) {
			logger.debug("saving source frame " + source.width() + "x" + source.height() + "...");

			long time = new Date().getTime();
			FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", getSavedFramesDir().getAbsolutePath() + File.separator
					+ SOURCE_FRAMES_DIR);
			Highgui.imwrite(getSavedFramesDir().getAbsolutePath() + File.separator + SOURCE_FRAMES_DIR + File.separator + "frame_"
					+ dateFormatter.format(time) + ".png", source);
		}

		// get current frame size
		Size size = source.size();

		// allocate images at the beginning or
		// reallocate them if the frame size is changed
		if (buf == null || buf.width() != size.width || buf.height() != size.height) {
			if (buf == null) {
				buf = new Mat(size, CvType.CV_8UC1);
				buf = Mat.zeros(size, CvType.CV_8UC1);
			}
		}

		switch (source.channels()) {
		case 4:
			if (grayscale()) {
				logger.debug("converting RGBA color space to gray...");
				Imgproc.cvtColor(source, buf, Imgproc.COLOR_RGBA2GRAY);
			} else
				buf = source.clone();
			break;
		case 3:
			if (grayscale()) {
				logger.debug("converting RGB color space to gray...");
				Imgproc.cvtColor(source, buf, Imgproc.COLOR_RGB2GRAY);
			} else
				buf = source.clone();
			break;
		case 1:
			source.copyTo(buf);
			break;
		}

		if (getMorphKernelSize() > 0) {
			logger.debug("applying morphology (kernel size:" + getMorphKernelSize() + ")...");
			Mat morph = OpenCvUtils.doMorphology(buf, grayscale(), getMorphKernelSize());
			buf.release();
			buf = morph;
		}

		if (buf == null || buf.empty())
			throw new RuntimeException("pre-processing failed: image is null or empty");

		if ((grayscale() || getMorphKernelSize() > 0) && getSavedFramesDir() != null) {
			logger.debug("saving pre-processed frame " + buf.width() + "x" + buf.height() + "...");

			long time = new Date().getTime();
			FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", getSavedFramesDir().getAbsolutePath() + File.separator
					+ PRE_PROCESSED_FRAMES_DIR);
			Highgui.imwrite(getSavedFramesDir().getAbsolutePath() + File.separator + PRE_PROCESSED_FRAMES_DIR + File.separator + "frame_"
					+ dateFormatter.format(time) + ".png", buf);
		}

		bg.apply(buf, fgMask, learningRate); // apply() exports a gray image by definition

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Imgproc.findContours(fgMask, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		// logContours(contours, source.cols(), source.rows());

		List<MatOfPoint> filteredContours = DetectorHelper.filterContours(contours, region);
		// logContours(filteredContours, source.cols(), source.rows());

		if (filteredContours != null && filteredContours.size() > 0) {

			Imgproc.drawContours(source, filteredContours, -1, contourColor, contourThickness);
			targetDetected = DetectorHelper.findTotalContourAreaRatio(filteredContours, source.cols(), source.rows()) >= minContourAreaRatio;

			if (targetDetected) {
				if (getSavedFramesDir() != null) {
					logger.debug("saving detected frame " + source.width() + "x" + source.height() + "...");

					long time = new Date().getTime();
					FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", getSavedFramesDir().getAbsolutePath()
							+ File.separator + DETECTED_FRAMES_DIR);
					Highgui.imwrite(getSavedFramesDir().getAbsolutePath() + File.separator + DETECTED_FRAMES_DIR + File.separator
							+ "frame_" + dateFormatter.format(time) + ".png", source);
				}
			}

		} else {

			targetDetected = false;
		}

		return source;
	}
}

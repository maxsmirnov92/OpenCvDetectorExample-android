package ru.maxsmr.opencv.commondetector.object.cascade;

import ru.maxsmr.opencv.commondetector.utils.DetectorHelper;
import ru.maxsmr.opencv.commondetector.model.graphic.Point;
import ru.maxsmr.opencv.commondetector.model.graphic.Rect;
import ru.maxsmr.opencv.commondetector.model.object.info.ObjectDetectFrameInfo;
import ru.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.opencv.commondetector.utils.OpenCvUtils;


public class CarClassifierDetector extends AbstractClassifierDetector {

	private static final Logger logger = LoggerFactory.getLogger(CarClassifierDetector.class);

	private List<File> mainClassifierFiles;

	public void setMainClassifierFiles(List<File> files) {
		mainClassifierFiles = files;
	}

	/** the main cascade classifier; loads during detect() */
	private CascadeClassifier mainClassifier;

	private boolean loadMainClassifier(File f) {
		mainClassifier = loadClassifier(f);
		return isClassifierLoaded(mainClassifier);
	}

	/** a test classifier; loads in constructor */
	private CascadeClassifier checkClassifier;

	public boolean loadCheckClassifier(File f) {
		checkClassifier = loadClassifier(f);
		return isClassifierLoaded(checkClassifier);
	}

	public CarClassifierDetector(List<File> mainClassifierFiles, File checkClassifierFile) {
		super();
		logger.debug("CarClassifierDetector(), mainClassifierFiles=" + mainClassifierFiles + ", checkClassifierFile=" + checkClassifierFile);

		// loadMainClassifiers(mainClassifierFiles);
		setMainClassifierFiles(mainClassifierFiles);

		loadCheckClassifier(checkClassifierFile);
	}

	// /** FIXME is it optimal size to scale? */
	// private static final Size scaleSize = new Size(300, 150);

	private static final Scalar[] detectedCarsColors = new Scalar[] { new Scalar(0, 0, 255), new Scalar(0, 255, 0), new Scalar(255, 0, 0),
			new Scalar(255, 255, 0), new Scalar(255, 0, 255), new Scalar(128, 128, 128), new Scalar(0, 128, 128), new Scalar(128, 128, 0),
			new Scalar(128, 0, 128) };

	@Override
	public synchronized ObjectDetectFrameInfo detect(Mat frame, Size scaleSize, List<org.opencv.core.Point> region) {
		logger.debug("detect(), frame=" + frame + ", scaleSize=" + scaleSize + ", region=" + region);

		if (!isClassifierLoaded(checkClassifier))
			throw new RuntimeException("checkClassifier is not loaded");

		if (mainClassifierFiles == null || mainClassifierFiles.isEmpty())
			throw new RuntimeException("mainClassifierFiles is null or empty");

		if (frame == null)
			throw new NullPointerException("frame mat is null");

		if (frame.empty())
			throw new IllegalArgumentException("frame mat is empty");

		if (!(frame.channels() == 1 || frame.channels() == 3 || frame.channels() == 4))
			throw new IllegalArgumentException("incorrect frame mat channels number: " + frame.channels());

		final long startTime = System.currentTimeMillis();

		if (getSavedFramesDir() != null) {
			logger.debug("saving source frame " + frame.width() + "x" + frame.height() + "...");

			long time = new Date().getTime();
			FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", getSavedFramesDir().getAbsolutePath() + File.separator
					+ SOURCE_FRAMES_DIR);
			Highgui.imwrite(getSavedFramesDir().getAbsolutePath() + File.separator + SOURCE_FRAMES_DIR + File.separator + "frame_"
					+ dateFormatter.format(time) + ".png", frame);
		}

		if (scaleSize != null && (frame.size().width > scaleSize.width || frame.size().height > scaleSize.height))
			Imgproc.resize(frame, frame, scaleSize);

		if (grayscale()) {
			Mat frameGray = new Mat(frame.size(), CvType.CV_8UC1);

			switch (frame.channels()) {
			case 4:
				logger.debug("converting RGBA color space to gray...");
				Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_RGBA2GRAY);
				break;
			case 3:
				logger.debug("converting RGB color space to gray...");
				Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_RGB2GRAY);
				break;
			case 1:
				frame.copyTo(frameGray);
				break;
			}

			frame = frameGray;
		}

		if (frame.channels() == 1)
			Imgproc.equalizeHist(frame, frame); // frameGray, frameGray

		if (frame == null || frame.empty())
			throw new RuntimeException("pre-processing failed: image is null or empty");

		if (grayscale() && getSavedFramesDir() != null) {
			logger.debug("saving pre-processed frame " + frame.width() + "x" + frame.height() + "...");

			long time = new Date().getTime();
			FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", getSavedFramesDir().getAbsolutePath() + File.separator
					+ PRE_PROCESSED_FRAMES_DIR);
			Highgui.imwrite(getSavedFramesDir().getAbsolutePath() + File.separator + PRE_PROCESSED_FRAMES_DIR + File.separator + "frame_"
					+ dateFormatter.format(time) + ".png", frame);
		}

		// number of total cars detected
		int detectedCarsCount = 0;
		List<Rect> detectedCars = new ArrayList<Rect>();

		Mat outputImage = new Mat(frame.size(), frame.type());
		frame.copyTo(outputImage);

		for (File f : mainClassifierFiles) {

			if (!FileHelper.isFileCorrect(f)) {
				logger.error("incorrect main classifier file: " + f);
				continue;
			}

			if (!loadMainClassifier(f)) {
				logger.error("failed loading main classifier");
				continue;
			}

			MatOfRect mainCars = new MatOfRect();
			mainClassifier.detectMultiScale(frame, mainCars); // frameGray

			if (mainCars.empty()) {
				logger.debug("no main cars detected by this main classifier (file: " + f + ")");
				continue;
			}

			int colorIndex = 0;

			for (org.opencv.core.Rect mainCar : mainCars.toList()) {

				if (mainCar == null || mainCar.size().width == 0 || mainCar.size().height == 0) {
					logger.error("incorrect main car rect: " + mainCar);
					continue;
				}

				if (region != null && !region.isEmpty() && !DetectorHelper.isRectInPolygon(mainCar, region)) {
					logger.warn("main car rect " + mainCar + " is out or region");
					continue;
				}

				Mat roiImg = new Mat(frame, mainCar); // frameGray

				MatOfRect nestedCars = new MatOfRect();

				// getting points for bouding a rectangle over the car detected by main
				int x0 = mainCar.x;
				int y0 = mainCar.y;
				int width = mainCar.width;
				int height = mainCar.height;
				int x1 = mainCar.x + width - 1;
				int y1 = mainCar.y + height - 1;

				checkClassifier.detectMultiScale(roiImg, nestedCars);

				if (nestedCars.empty()) {
					logger.debug("main car rect " + mainCar + " was rejected by check classifier (no detected nested cars)");
					continue;
				} else
					logger.debug("nested cars (" + nestedCars.toList().size()
							+ ") were detected by by check classifier with main car rect " + mainCar);

				// testing the detected car by main using nested cars detected by checkcascade
				for (org.opencv.core.Rect nestedCar : nestedCars.toList()) {

					if (nestedCar == null || nestedCar.size().width == 0 || nestedCar.size().height == 0) {
						logger.error("incorrect nested car rect: " + nestedCar);
						continue;
					}

					// getting center points for bouding a circle over the car detected by checkcascade
					Point center = new Point();
					center.x = (int) Math.round(mainCar.x + nestedCar.x + nestedCar.width * 0.5);
					center.y = (int) Math.round(mainCar.y + nestedCar.y + nestedCar.height * 0.5);

					// if center of bounding circle is inside the rectangle boundary over a threshold the car is
					// certified
					if (center.x > (x0 + 15) && center.x < (x1 - 15) && center.y > (y0 + 15) && center.y < (y1 - 15)) {

						// drawing boundary rectangle over the final result
						Core.rectangle(outputImage, new org.opencv.core.Point(x0, y0), new org.opencv.core.Point(x1, y1),
								detectedCarsColors[colorIndex % detectedCarsColors.length], getContourThickness());

						detectedCars.add(new Rect(x0, y0, width, height));
						detectedCarsCount++;
					}
				}

				colorIndex++;
			}

			mainClassifier = null;
		}

		// frameGray.release();

		List<org.opencv.core.Rect> cvDetectedCars = null;

		if (!detectedCars.isEmpty()) {

			cvDetectedCars = new ArrayList<org.opencv.core.Rect>();

			for (Rect car : detectedCars) {
				if (car == null)
					continue;
				cvDetectedCars.add(new org.opencv.core.Rect(car.x, car.y, car.width, car.height));
			}
			cvDetectedCars = DetectorHelper.filterRects(cvDetectedCars, region);

		} else
			cvDetectedCars = null;

		if (cvDetectedCars != null && !cvDetectedCars.isEmpty()) {

			detectedCars.clear();

			for (org.opencv.core.Rect car : cvDetectedCars) {
				if (car == null)
					continue;
				detectedCars.add(new Rect(car.x, car.y, car.width, car.height));
			}
		} else
			detectedCars = null;

		if (detectedCarsCount > 0) {
			if (getSavedFramesDir() != null) {
				logger.debug("saving detected frame " + outputImage.width() + "x" + outputImage.height() + "...");

				long time = new Date().getTime();
				FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", getSavedFramesDir().getAbsolutePath()
						+ File.separator + DETECTED_FRAMES_DIR);
				Highgui.imwrite(getSavedFramesDir().getAbsolutePath() + File.separator + DETECTED_FRAMES_DIR + File.separator + "frame_"
						+ dateFormatter.format(time) + ".png", outputImage);
			}
		}

		// GraphicUtils.getBitmapData(OpenCvUtils.convertMatToBitmap(outputImage, false))

		ObjectDetectFrameInfo info = new ObjectDetectFrameInfo(OpenCvUtils.convertMatToByteArray(outputImage), outputImage.type(),
				outputImage.cols(), outputImage.rows(), detectedCarsCount > 0, OBJECT_TYPE.CAR, detectedCars, System.currentTimeMillis()
						- startTime);
		outputImage.release();
		return info;
	}

}

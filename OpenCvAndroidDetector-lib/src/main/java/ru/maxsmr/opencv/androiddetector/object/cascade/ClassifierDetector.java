package ru.maxsmr.opencv.androiddetector.object.cascade;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;


import ru.maxsmr.opencv.androiddetector.OpenCvInit;
import ru.maxsmr.opencv.androiddetector.object.AbstractObjectDetector;
import ru.maxsmr.opencv.androiddetector.object.cascade.classifiers.ClassifierResInit;
import ru.maxsmr.opencv.commondetector.model.DETECTOR_SENSITIVITY;
import ru.maxsmr.opencv.commondetector.model.graphic.Point;
import ru.maxsmr.opencv.commondetector.model.object.info.ObjectDetectFrameInfo;
import ru.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;
import ru.maxsmr.opencv.commondetector.object.cascade.AbstractClassifierDetector;
import ru.maxsmr.opencv.commondetector.object.cascade.BaseClassifierDetector;
import ru.maxsmr.opencv.commondetector.object.cascade.CarClassifierDetector;


import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.commonutils.graphic.GraphicUtils;
import ru.maxsmr.opencv.commondetector.utils.OpenCvUtils;

public class ClassifierDetector extends AbstractObjectDetector {

	private static final Logger logger = LoggerFactory.getLogger(ClassifierDetector.class);

	public static final OBJECT_TYPE DEFAULT_OBJECT_TYPE = OBJECT_TYPE.UNKNOWN;
	private OBJECT_TYPE objectType = DEFAULT_OBJECT_TYPE;

	private AbstractClassifierDetector classifierDetector;

	public OBJECT_TYPE getObjectType() {
		return objectType;
	}

	/**
	 * @param defaultClassifier use custom xml classifier with BaseClassifierDetector; if null it will be taken from
	 *            resources
	 */
	public synchronized void setObjectType(OBJECT_TYPE objectType, File defaultClassifier) throws IOException, InterruptedException {

		if (objectType == null)
			throw new NullPointerException("objectType is null");

		File classifierFile = null;

		if (!FileHelper.isFileCorrect(defaultClassifier)) {

			switch (objectType) {

			case CAR:

				classifierDetector = new CarClassifierDetector(ClassifierResInit.getInstance(mContext).getCarMainClassifiers(),
						ClassifierResInit.getInstance(mContext).getCarCheckClassifier());
				break;

			case HUMAN:

//				if (!FileHelper.copyRawFile(mContext, R.raw.hogcascade_pedestrians,
//						classifierFile = new File(mContext.getFilesDir(), OBJECT_TYPE.HUMAN.name() + ".xml"),
//						FileHelper.FILE_PERMISSIONS_ALL))
//					throw new RuntimeException("copy resource " + R.raw.hogcascade_pedestrians + " failed");

				break;

			default:

				throw new IllegalArgumentException("incorrect objectType: " + objectType);
			}
		} else {
			classifierFile = defaultClassifier;
		}

		this.objectType = objectType;

		if (classifierDetector == null) {

			if (classifierFile == null)
				throw new RuntimeException("classifier file was not initialized");

			classifierDetector = new BaseClassifierDetector(classifierFile, objectType);
		}

		classifierDetector.setContourColor(getContourColor());
		classifierDetector.setContourThickness(getContourThickness());
		classifierDetector.setGrayscale(grayscale);
		classifierDetector.setSavedFramesDir(savedFramesDir);
	}

	private boolean grayscale = AbstractClassifierDetector.DEFAULT_GRAYSCALE;

	public boolean grayscale() {
		return grayscale;
	}

	public void setGrayscale(boolean toggle) {
		this.grayscale = toggle;

		if (classifierDetector != null)
			classifierDetector.setGrayscale(grayscale);
	}

	private File savedFramesDir;

	public boolean setSavedFramesDir(File savedFramesDir) {

		if (savedFramesDir == null) {
			return false;
		}

		if (!FileHelper.isDirExists(savedFramesDir.getAbsolutePath())) {
			logger.warn("directory " + savedFramesDir + " not exists, creating...");
			if (FileHelper.createNewDir(savedFramesDir.getAbsolutePath()) == null) {
				logger.error("can't create dir");
				return false;
			}
		}

		this.savedFramesDir = savedFramesDir;

		if (classifierDetector != null)
			classifierDetector.setSavedFramesDir(savedFramesDir);

		return true;
	}

	private final Context mContext;

	// int defaultClassifierResourceId, String defaultClassifierName
	public ClassifierDetector(Context ctx, OBJECT_TYPE objectType, File defaultClassifierFile, boolean grayscale, File savedFramesDir)
			throws IOException, InterruptedException {
		logger.debug("ClassifierDetector(), objectType=" + objectType + ", defaultClassifierFile=" + defaultClassifierFile + ", grayscale="
				+ grayscale + ", savedFramesDir=" + savedFramesDir);

		mContext = ctx;

		setObjectType(objectType, defaultClassifierFile);
		setGrayscale(grayscale);

		setSavedFramesDir(savedFramesDir);
	}

	private Mat lastFrame;

	@Override
	public Bitmap getLastFrame() {
		return OpenCvUtils.convertMatToBitmap(lastFrame, false);
	}

	@Override
	protected boolean updateLastFrame(Bitmap frame) {

		if (frame == null || GraphicUtils.getBitmapByteCount(frame) == 0)
			return false;

		Mat mat = OpenCvUtils.convertBitmapToMat(frame);

		if (mat != null && !mat.empty()) {

			if (lastFrame != null)
				lastFrame.release();

			lastFrame = mat;
			return true;
		}

		return false;
	}

	@Override
	public synchronized ObjectDetectFrameInfo detectObjectByByteArray(byte[] data, boolean isRgb, int yuvFormat, int imageWidth,
			int imageHeight, DETECTOR_SENSITIVITY sensitivity, List<Point> region) {
		// logger.debug("detectObjectByByteArray(), isRgb=" + isRgb + ", yuvFormat=" + yuvFormat + ", imageWidth=" +
		// imageWidth + ", imageHeight=" + imageHeight + ", region=" + region);

		if (!OpenCvInit.getInstance().isOpenCvManagerLoaded()) {
			throw new RuntimeException("OpenCV Manager is not loaded");
		}

		if (data == null)
			throw new NullPointerException("image data is null");

		if (data.length == 0)
			throw new IllegalArgumentException("image data is empty");

		if (sensitivity == null || sensitivity == DETECTOR_SENSITIVITY.NONE) {
			logger.debug("sensitivity is null or NONE, no need to detect object");
			return null;
		}

		if (imageWidth <= 0 || imageHeight <= 0) {
			throw new IllegalArgumentException("incorrect image size: " + imageWidth + "x" + imageHeight);
		}

		final Bitmap frameBitmap;

		if (!isRgb) {

			if (!(yuvFormat == ImageFormat.NV16 || yuvFormat == ImageFormat.NV21 || yuvFormat == ImageFormat.YUY2 || yuvFormat == ImageFormat.YV12)) {
				throw new IllegalArgumentException("image format is not YUV: " + yuvFormat);
			}

			frameBitmap = GraphicUtils.createBitmapByByteArray(GraphicUtils.convertYuvToJpeg(data, yuvFormat, imageWidth, imageHeight), 1);

		} else {

			frameBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGB_565);
			ByteBuffer frameBuffer = ByteBuffer.wrap(data);
			frameBitmap.copyPixelsFromBuffer(frameBuffer);
		}

		if (frameBitmap == null || GraphicUtils.getBitmapByteCount(frameBitmap) == 0)
			throw new RuntimeException("frameBitmap is null or empty");

		// if (savedFramesDir != null) {
		// logger.debug("saving source frame " + imageWidth + "x" + imageHeight + "...");
		// FileHelper.writeCompressedBitmapToFile(frameBitmap, Bitmap.CompressFormat.PNG,
		// "frame_" + dateFormatter.format(new Date().getTime()), savedFramesDir.getAbsolutePath() + File.separator
		// + SOURCE_FRAME_DIR);
		// }

		Mat frame = OpenCvUtils.convertBitmapToMat(frameBitmap);

		// if (grayscale) {
		// logger.debug("converting color space to gray...");
		// Mat frameGray = OpenCvUtils.colorToGray(frame);
		// frame.release();
		// frame = frameGray;
		// }
		//
		// if (frame == null || frame.empty()) {
		// throw new RuntimeException("pre-processing failed: frame is null or empty");
		// }
		//
		// if (grayscale && savedFramesDir != null) {
		// Bitmap preBitmap = OpenCvUtils.convertMatToBitmap(frame, false);
		// logger.debug("saving source pre-processed frame...");
		// FileHelper.writeCompressedBitmapToFile(preBitmap, Bitmap.CompressFormat.PNG,
		// "frame_" + dateFormatter.format(new Date().getTime()), savedFramesDir.getAbsolutePath() + File.separator
		// + PRE_PROCESSED_FRAME_DIR);
		// preBitmap.recycle();
		// }

		final List<org.opencv.core.Point> cvRegion;

		if (region != null) {
			cvRegion = new ArrayList<org.opencv.core.Point>();
			for (Point p : region) {
				if (p == null) {
					continue;
				}
				cvRegion.add(new org.opencv.core.Point(p.x, p.y));
			}
		} else {
			cvRegion = null;
		}

		logger.debug("detecting objects by matrix " + frame.cols() + "x" + frame.rows() + "...");
		ObjectDetectFrameInfo objInfo = classifierDetector == null ? null : classifierDetector.detect(frame, null, cvRegion);

		Bitmap resultBitmap = null;

		if (objInfo == null || !objInfo.detected()) {
			resultBitmap = OpenCvUtils.convertMatToBitmap(frame, false);
			frame.release();
			if (updateLastFrame(resultBitmap))
				resultBitmap.recycle();
			else
				logger.error("incorrect last frame");

		} else {

			frame.release();

			if (objInfo.getSceneImage() != null && objInfo.getSceneImage().length != 0 && objInfo.getType() >= 0 && objInfo.getWidth() > 0
					&& objInfo.getHeight() > 0) {

				Mat resultMat = OpenCvUtils.convertByteArrayToMat(objInfo.getSceneImage(), objInfo.getWidth(), objInfo.getHeight(),
						objInfo.getType());

				// resultBitmap = Bitmap.createBitmap(objInfo.getWidth(), objInfo.getHeight(), Bitmap.Config.RGB_565);
				// ByteBuffer imageBuffer = ByteBuffer.wrap(objInfo.getSceneImage());
				// resultBitmap.copyPixelsFromBuffer(imageBuffer);

				resultBitmap = OpenCvUtils.convertMatToBitmap(resultMat, false);

				if (updateLastFrame(resultBitmap)) {

					// if (savedFramesDir != null) {
					// logger.debug("saving detected frame " + resultBitmap.getWidth() + "x" + resultBitmap.getHeight()
					// + "...");
					// FileHelper.writeCompressedBitmapToFile(resultBitmap, Bitmap.CompressFormat.PNG,
					// "frame_" + dateFormatter.format(new Date().getTime()), savedFramesDir.getAbsolutePath() +
					// File.separator
					// + DETECTED_FRAME_DIR);
					// }
					resultMat.release();
					resultBitmap.recycle();
				} else
					logger.error("incorrect last frame");

			} else
				logger.error("incorrect last frame data (length: " + (objInfo.getSceneImage() != null ? objInfo.getSceneImage().length : 0)
						+ ")" + " or size (" + objInfo.getWidth() + "x" + objInfo.getHeight() + ") or type (" + objInfo.getType() + ")");
		}

		return objInfo;
	}

	@Override
	protected void beforeVideoDetect(File videoFile, int framesCount) {
	}

	@Override
	protected void afterVideoDetect(File videoFile, int framesCount) {
	}

}

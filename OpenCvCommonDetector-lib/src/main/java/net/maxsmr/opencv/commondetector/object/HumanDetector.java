package net.maxsmr.opencv.commondetector.object;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;

import net.maxsmr.commonutils.data.FileHelper;
import net.maxsmr.commonutils.graphic.GraphicUtils;
import net.maxsmr.opencv.commondetector.model.object.info.ObjectDetectFrameInfo;
import net.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;

import net.maxsmr.opencv.commondetector.utils.OpenCvUtils;

public class HumanDetector {

	private static final Logger logger = LoggerFactory.getLogger(HumanDetector.class);

	public static ObjectDetectFrameInfo findHuman(Mat imgScene, Scalar rectColor, Scalar fontColor) {
		logger.debug("findHuman(), imgScene=" + imgScene + ", rectColor=" + rectColor + ", fontColor=" + fontColor);

		if (imgScene.empty()) {
			logger.error("scene image is empty");
			return null;
		}
		logger.debug("imgScene: " + imgScene.toString());

		Mat imgSceneGray = new Mat(imgScene.size(), CvType.CV_8UC1);
		logger.debug("imgSceneGray: " + imgSceneGray.toString());

		logger.debug("converting from ARGB to GRAY...");
		Imgproc.cvtColor(imgScene, imgSceneGray, Imgproc.COLOR_RGBA2GRAY);

		final long startCalcTime = System.currentTimeMillis();
		long execTime;

		HOGDescriptor hog = new HOGDescriptor();
		MatOfFloat descriptors = HOGDescriptor.getDefaultPeopleDetector();
		hog.setSVMDetector(descriptors);

		MatOfRect locations = new MatOfRect();
		MatOfDouble weights = new MatOfDouble();

		logger.debug("starting detection...");
		hog.detectMultiScale(imgSceneGray, locations, weights);
		logger.debug("locations count: " + locations.rows());

		boolean detectionResult = false;

		Mat imgResult = imgSceneGray.clone();

		if (rectColor == null) {
			rectColor = OpenCvUtils.COLOR_BLACK;
		}

		if (fontColor == null) {
			fontColor = OpenCvUtils.COLOR_BLACK;
		}

		if (locations.rows() > 0) {

			Point rectPoint1 = new Point();
			Point rectPoint2 = new Point();
			Point fontPoint = new Point();

			List<Rect> rectangles = locations.toList();
			int i = 0;
			List<Double> weightList = weights.toList();

			for (Rect rect : rectangles) {

				logger.info("human detected: [" + rect.x + "," + rect.y + "]");

				float weight = weightList.get(i++).floatValue();

				rectPoint1.x = rect.x;
				rectPoint1.y = rect.y;
				rectPoint2.x = rect.x + rect.width;
				rectPoint2.y = rect.y + rect.height;

				fontPoint.x = rect.x;
				fontPoint.y = rect.y - 4;

				Core.rectangle(imgResult, rectPoint1, rectPoint2, rectColor, 2);
				Core.putText(imgResult, String.format("%1.2f", weight), fontPoint, Core.FONT_HERSHEY_PLAIN,
						OpenCvUtils.getFontScaleByImgSize(imgSceneGray.cols(), imgSceneGray.rows(), null), fontColor, 2, Core.LINE_AA,
						false);
			}

			detectionResult = true;

		} else {

			logger.info("no humans detected");
			detectionResult = false;
		}

		execTime = System.currentTimeMillis() - startCalcTime;
		logger.info("Algorithm duration: " + execTime + " ms");

		Point fontPoint = new Point();
		fontPoint.x = 15;
		fontPoint.y = imgResult.rows() - 20;

		Core.putText(imgResult, "Processing time:" + execTime + " ms | width:" + imgResult.cols() + " height:" + imgResult.rows(),
				fontPoint, Core.FONT_HERSHEY_PLAIN, OpenCvUtils.getFontScaleByImgSize(imgSceneGray.cols(), imgSceneGray.rows(), null),
				fontColor, 2, Core.LINE_AA, false);

		logger.debug("imgResult: " + imgResult.toString());

		List<net.maxsmr.opencv.commondetector.model.graphic.Rect> locations2 = null;

		if (!locations.empty()) {

			locations2 = new ArrayList<net.maxsmr.opencv.commondetector.model.graphic.Rect>();

			for (Rect rect : locations.toList()) {

				if (rect == null)
					continue;

				locations2.add(new net.maxsmr.opencv.commondetector.model.graphic.Rect(rect.x, rect.y, rect.width, rect.height));
			}
		}

		// GraphicUtils.getBitmapData(OpenCvUtils.convertMatToBitmap(imgResult, false))

		return new ObjectDetectFrameInfo(OpenCvUtils.convertMatToByteArray(imgResult), imgResult.type(), imgResult.cols(),
				imgResult.rows(), detectionResult, OBJECT_TYPE.HUMAN, locations2, execTime);
	}

	public static ObjectDetectFrameInfo findHuman(Bitmap sceneBitmap, Scalar rectColor, Scalar fontColor) {
		logger.debug("findHuman(), sceneBitmap=" + sceneBitmap + ", rectColor=" + rectColor + ", fontColor=" + fontColor);

		if (sceneBitmap == null || GraphicUtils.getBitmapByteCount(sceneBitmap) == 0) {
			logger.error("scene bitmap is null or empty");
			return null;
		}

		return findHuman(OpenCvUtils.convertBitmapToMat(sceneBitmap), rectColor, fontColor);
	}

	public static ObjectDetectFrameInfo findHuman(File sceneFile, Scalar rectColor, Scalar fontColor) {
		logger.debug("findHuman(), sceneFile=" + sceneFile + ", rectColor=" + rectColor + ", fontColor=" + fontColor);

		if (!FileHelper.isFileCorrect(sceneFile) || !FileHelper.isPicture(FileHelper.getFileExtension(sceneFile.getName()))) {
			logger.error("incorrect scene image file: " + sceneFile);
			return null;
		}

		return findHuman(Highgui.imread(sceneFile.getAbsolutePath()), rectColor, fontColor);
	}
}

package ru.maxsmr.opencv.commondetector.object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import ru.maxsmr.opencv.commondetector.model.object.info.ObjectDetectFrameInfo;
import ru.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;

import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.opencv.commondetector.utils.OpenCvUtils;

public class RawObjectDetector {

	private static final Logger logger = LoggerFactory.getLogger(RawObjectDetector.class);

	private final Context mContext;

	public RawObjectDetector(Context ctx) {
		mContext = ctx;
	}

	private FeatureDetector featureDetector;
	private DescriptorExtractor descriptorExtractor;
	private DescriptorMatcher descriptorMatcher;

	private void initDetector() {
		logger.debug("initDetector()");

		featureDetector = FeatureDetector.create(FeatureDetector.ORB);
		descriptorExtractor = DescriptorExtractor.create(FeatureDetector.ORB);
		descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		logger.debug("current feature detector config: " + getDetectorCurrentConf());
	}

	private String getDetectorCurrentConf() {
		logger.debug("getDetectorCurrentConf()");

		if (featureDetector == null) {
			logger.error("detector was not initialized");
			return null;
		}

		File tempFile = null;
		FileReader fileReader = null;
		BufferedReader bufReader = null;

		try {
			tempFile = File.createTempFile("config", ".yml", mContext.getCacheDir());
			if (!tempFile.exists()) {
				return null;
			}
			logger.debug("temp file: " + tempFile.getAbsolutePath());

			featureDetector.write(tempFile.getAbsolutePath());

			fileReader = new FileReader(tempFile);

			bufReader = new BufferedReader(fileReader);
			StringBuilder resultString = new StringBuilder();
			String tmpStr;

			while ((tmpStr = bufReader.readLine()) != null) {
				resultString.append(tmpStr);
			}

			return resultString.toString();

		} catch (IOException e) {
		} finally {
			try {
				if (bufReader != null) {
					bufReader.close();
				}
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException e) {
				logger.error("an IOException occured during close(): " + e.getMessage());
			}
		}

		return null;
	}

	public final static int DEFAULT_MIN_HESSIAN_VALUE = 1500;

	private void applySurfConfig(int minHessian) {

		File tempFile = null;
		FileWriter writer = null;

		try {
			tempFile = File.createTempFile("surf_config", ".yml", mContext.getCacheDir());

			StringBuilder settings = new StringBuilder();
			settings.append("%YAML:1.0\n");
			settings.append("hessianThreshold: ");
			settings.append(minHessian);
			settings.append("\n");
			// settings.append(".\noctaves: 3\noctaveLayers: 4\nupright: 0\n");

			writer = new FileWriter(tempFile, false);
			writer.write(settings.toString());

		} catch (IOException e) {
			logger.error("an IOException occured: " + e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("an IOException occured during close(): " + e.getMessage());
				}
			}
		}

		logger.debug("applying surfDetector config from temp file: " + tempFile.getAbsolutePath());
		if (tempFile != null && tempFile.exists() && tempFile.length() > 0) {
			featureDetector.read(tempFile.getAbsolutePath());

		} else {
			logger.error("can't apply surfDetector config from temp file: " + tempFile.getAbsolutePath());
		}
	}

	public final static int DEFAULT_MIN_FEATURE_COUNT = 1500;

	private void applyOrbConfig(int featuresCount) {

		// ORB Default values:
		// float scaleFactor = 1.2f // Coefficient by which we divide the dimensions from one scale pyramid level to the
		// next
		// uint nLevels = 8 // The number of levels in the scale pyramid
		// uint firstLevel = 0 // The level at which the image is given
		// int edgeThreshold = 31 // How far from the boundary the points should be.
		// int patchSize = 31 // You can not change this, it is allways 31

		// int WTA_K = 2 // How many random points are used to produce each cell of the descriptor (2, 3, 4 ...)
		// scoreType = 0 // 0 for HARRIS_SCORE / 1 for FAST_SCORE
		// nFeatures = 500 // not sure if 500 is default

		if (featuresCount <= 0) {
			return;
		}

		File tempFile = null;
		FileWriter writer = null;

		try {
			tempFile = File.createTempFile("orb_config", ".yml", mContext.getCacheDir());

			StringBuilder settings = new StringBuilder();
			settings.append("%YAML:1.0\n");
			settings.append("%YAML:1.0\nscaleFactor: 1.2\nnLevels: 8\nfirstLevel: 0 \nedgeThreshold: 31\npatchSize: 31\nWTA_K: 2\nscoreType: 0\nnFeatures: ");
			settings.append(featuresCount);
			settings.append("\n");

			writer = new FileWriter(tempFile, false);
			writer.write(settings.toString());

		} catch (IOException e) {
			logger.error("an IOException occured: " + e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("an IOException occured during close(): " + e.getMessage());
				}
			}
		}

		logger.debug("applying orbDetector config from temp file: " + tempFile.getAbsolutePath());
		if (tempFile != null && tempFile.exists() && tempFile.length() > 0) {

			featureDetector.read(tempFile.getAbsolutePath());
			descriptorExtractor.read(tempFile.getAbsolutePath());

		} else {
			logger.error("can't apply orbDetector config from temp file: " + tempFile.getAbsolutePath());
		}
	}

	// FIXME
	public ObjectDetectFrameInfo findObject(File sceneImageFile, File objectImageFile, int featuresCount, boolean drawMathesOrKeypoints) {
		logger.debug("findObject(), sceneImageFile=" + sceneImageFile + ", objectImageFile=" + objectImageFile + ", featuresCount="
				+ featuresCount + ", drawMathesOrKeypoints=" + drawMathesOrKeypoints);

		if (!FileHelper.isFileCorrect(sceneImageFile) || !FileHelper.isPicture(FileHelper.getFileExtension(sceneImageFile.getName()))) {
			logger.error("incorrect scene image file: " + sceneImageFile);
			return null;
		}

		if (!FileHelper.isFileCorrect(objectImageFile) || !FileHelper.isPicture(FileHelper.getFileExtension(objectImageFile.getName()))) {
			logger.error("incorrect find object file: " + objectImageFile);
			return null;
		}

		Mat imgScene = Highgui.imread(sceneImageFile.getAbsolutePath());
		Mat imgObject = Highgui.imread(objectImageFile.getAbsolutePath());

		if (imgScene.empty()) {
			logger.error("scene image is empty");
			return null;
		}

		if (imgObject.empty()) {
			logger.error("object image is empty");
			return null;
		}

		logger.debug("imgScene: " + imgScene.toString());
		logger.debug("imgObject: " + imgObject.toString());

		final long startCalcTime = System.currentTimeMillis();

		initDetector();

		if (featuresCount <= 0) {
			logger.error("incorrect features count value");
		} else {
			// applySurfConfig(minHessian);
			applyOrbConfig(featuresCount);
		}

		// ----Extract keypoints---
		MatOfKeyPoint keyptsScene = new MatOfKeyPoint();
		MatOfKeyPoint keyptsObj = new MatOfKeyPoint();

		logger.debug("extracting keypoints...");
		featureDetector.detect(imgScene, keyptsScene);
		featureDetector.detect(imgObject, keyptsObj);

		if (keyptsScene.empty()) {
			logger.error("keyptsScene is empty");
			return null;
		}

		if (keyptsObj.empty()) {
			logger.error("keyptsObj is empty");
			return null;
		}

		logger.debug("keyptsScene : " + keyptsScene.cols() + " x " + keyptsScene.rows());
		logger.debug("keyptsObj : " + keyptsObj.cols() + " x " + keyptsObj.rows());

		// ----Calculate descriptors (feature vectors)----
		Mat descriptorsScene = new Mat();
		Mat descriptorsObj = new Mat();

		logger.debug("calculating descriptors (feature vectors)...");
		descriptorExtractor.compute(imgScene, keyptsScene, descriptorsScene);
		descriptorExtractor.compute(imgObject, keyptsObj, descriptorsObj);

		if (descriptorsScene.empty()) {
			logger.error("descriptorsScene is empty");
			return null;
		}

		if (descriptorsObj.empty()) {
			logger.error("descriptorsObj is empty");
			return null;
		}

		logger.debug("descriptorsScene : " + descriptorsScene.cols() + " x " + descriptorsScene.rows());
		logger.debug("descriptorsObj : " + descriptorsObj.cols() + " x " + descriptorsObj.rows());

		// ----Matching descriptors using FLANN matcher----
		// The struct DMatch tells for a descriptor (feature) which descriptor (feature) from the train set is more
		// similar. So there are a queryIndex, a TrainIndex (which features decide the matcher that is more similar) and
		// a distance. The distance represents how far is one feature from other (in some metric NORM_L1, NORM_HAMMING
		// etc).

		MatOfDMatch matches12 = new MatOfDMatch();
		MatOfDMatch matches21 = new MatOfDMatch();

		logger.debug("matching scene and train descriptors...");
		descriptorMatcher.match(descriptorsScene, descriptorsObj, matches12);
		descriptorMatcher.match(descriptorsObj, descriptorsScene, matches21);

		List<DMatch> matches12List = matches12.toList();
		logger.debug("matches12: " + matches12.cols() + " x " + matches12.rows());
		logger.debug("matches12List size: " + matches12List.size());

		List<DMatch> matches21List = matches21.toList();
		logger.debug("matches21: " + matches21.cols() + " x " + matches21.rows());
		logger.debug("matches21List size: " + matches21List.size());

		if (matches12List.size() == 0 || matches21List.size() == 0) {
			logger.error("matchesList is empty");
			return null;
		}

		double maxDist = 0;
		double minDist = 1000;
		double dist;

		// Quick calculation of min and max distances between feature vectors
		for (int i = 0; i < matches12List.size(); i++) { // descriptorsObj.rows()
			dist = matches12List.get(i).distance;
			if (dist < minDist)
				minDist = dist;
			if (dist > maxDist)
				maxDist = dist;
		}

		logger.info("nearest distance between features: " + minDist);
		logger.info("most far distance between features: " + maxDist);

		List<DMatch> goodMatchesList = new ArrayList<DMatch>((int) matches12.size().area());

		/* final double upperBound = 3; for (int i = 0; i < matchesList.size(); i++) { // descriptorsObj.rows()
		 * logger.debug(" > dist_" + i + "=" + matchesList.get(i).distance); if (matchesList.get(i).distance <
		 * upperBound * minDist) goodMatchesList.add(matchesList.get(i)); } */

		for (int i = 0; i < matches12List.size(); i++) {
			DMatch forward = matches12List.get(i);
			DMatch backward = matches21List.get(forward.trainIdx);
			if (backward.trainIdx == forward.queryIdx)
				goodMatchesList.add(forward);
		}

		logger.info("good matches found: " + goodMatchesList.size());

		MatOfDMatch goodMatches = new MatOfDMatch();
		goodMatches.fromList(goodMatchesList);

		Mat imgResult = imgScene.clone(); // new Mat(); // will contain result image with watches

		if (drawMathesOrKeypoints) {
			logger.debug("drawing matches...");
			Features2d.drawMatches(imgScene, keyptsScene, imgObject, keyptsObj, goodMatches, imgResult, new Scalar(255, 0, 0), new Scalar(
					0, 255, 0), new MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS);
		} else {
			// featuredImg will be the output of first image
			Features2d.drawKeypoints(imgScene, keyptsScene, imgResult, new Scalar(255, 159, 10), 0);
			Features2d.drawKeypoints(imgObject, keyptsObj, imgResult, new Scalar(255, 159, 10), 0);
		}

		// List<Point> objList = new ArrayList<Point>(goodMatchesList.size()); List<Point> sceneList = new
		// ArrayList<Point>(goodMatchesList.size());
		//
		// List<KeyPoint> keyptsSceneList = keyptsScene.toList(); // = new ArrayList<KeyPoint>((int) //
		// sceneKeypoints.size().area()); List<KeyPoint> keyptsObjList = keyptsObj.toList(); // = new
		// ArrayList<KeyPoint>((int) // objKeypoints.size().area());
		//
		// for (int i = 0; i < goodMatchesList.size(); i++) { // --Get the keypoints from the good matches--
		//
		// logger.debug("trainIdx=" + goodMatchesList.get(i).trainIdx); logger.debug("queryIdx=" +
		// goodMatchesList.get(i).queryIdx);
		//
		// sceneList.add(keyptsSceneList.get(goodMatchesList.get(i).trainIdx).pt);
		// objList.add(keyptsObjList.get(goodMatchesList.get(i).queryIdx).pt); }
		//
		// MatOfPoint2f scene = new MatOfPoint2f(); scene.fromList(sceneList);
		//
		// MatOfPoint2f obj = new MatOfPoint2f(); obj.fromList(objList);
		//
		// logger.debug("finding a perspective transformation between object and scene..."); Mat hMat =
		// Calib3d.findHomography(obj, scene);
		//
		// // -- Get the corners from the image with object to be "detected" Mat objCorners = new Mat(4, 1,
		// CvType.CV_32FC2); Mat sceneCorners = new Mat(4, 1, CvType.CV_32FC2);
		//
		// // List<Point> objCornersList = new ArrayList<Point>(); //MatOfPoint obj_corners = new MatOfPoint();
		// obj_corners.fromList(cornerList); MatOfPoint scene_corners = new MatOfPoint();
		//
		// objCorners.put(0, 0, new double[] { 0, 0 }); objCorners.put(1, 0, new double[] { imgObject.cols(), 0 });
		// objCorners.put(2, 0, new double[] { imgObject.cols(), imgObject.rows() }); objCorners.put(3, 0, new double[]
		// { 0, imgObject.rows() });
		//
		// Core.perspectiveTransform(objCorners, sceneCorners, hMat);
		//
		// // -- Draw lines between the corners (the mapped object in the scene - image_2 )
		//
		// Core.line(resultImgMatches, new Point(sceneCorners.get(0, 0)), new Point(sceneCorners.get(1, 0)), new
		// Scalar(0, 255, 0), 4); Core.line(resultImgMatches, new Point(sceneCorners.get(1, 0)), new
		// Point(sceneCorners.get(2, 0)), new Scalar(0, 255, 0), 4); Core.line(resultImgMatches, new
		// Point(sceneCorners.get(2, 0)), new Point(sceneCorners.get(3, 0)), new Scalar(0, 255, 0), 4);
		// Core.line(resultImgMatches, new Point(sceneCorners.get(3, 0)), new Point(sceneCorners.get(0, 0)), new
		// Scalar(0, 255, 0), 4);

		imgScene.release();
		imgObject.release();

		keyptsScene.release();
		keyptsObj.release();

		descriptorsScene.release();
		descriptorsObj.release();

		matches12.release();
		matches21.release();
		goodMatches.release();

		// imgResult.release();

		logger.info("Algorithm duration: " + String.valueOf(System.currentTimeMillis() - startCalcTime) + " ms");

		if (goodMatchesList.size() >= 8 && goodMatchesList.size() <= 30) {

			// GraphicUtils.getBitmapData(OpenCvUtils.convertMatToBitmap(imgResult, false))
			return new ObjectDetectFrameInfo(OpenCvUtils.convertMatToByteArray(imgResult), imgResult.type(), imgResult.cols(),
					imgResult.rows(), true, OBJECT_TYPE.UNKNOWN, null, 0);
		} else {

			// GraphicUtils.getBitmapData(OpenCvUtils.convertMatToBitmap(imgResult, false))
			return new ObjectDetectFrameInfo(OpenCvUtils.convertMatToByteArray(imgResult), imgResult.type(), imgResult.cols(),
					imgResult.rows(), false, OBJECT_TYPE.UNKNOWN, null, 0);
		}
	}
}

package net.maxsmr.opencv.commondetector.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectorHelper {

	private static final Logger logger = LoggerFactory.getLogger(DetectorHelper.class);

	public static void logContours(List<MatOfPoint> contours, int imageWidth, int imageHeight) {

		if (contours == null || contours.isEmpty()) {
			logger.error("contours is null or empty");
			return;
		}

		if (imageWidth <= 0 || imageHeight <= 0) {
			logger.error("incorrect image size: " + imageWidth + "x" + imageHeight);
			return;
		}

		logger.debug("=== total contours count: " + contours.size() + " ===");

		int i = 0;
		double totalArea = 0;

		double minArea = Imgproc.contourArea(contours.get(0), false), maxArea = Imgproc.contourArea(contours.get(0), false);
		int minIndex = i, maxIndex = i;

		for (MatOfPoint c : contours) {

			if (c == null) {
				continue;
			}

			StringBuilder contourPoints = new StringBuilder();
			for (Point p : c.toArray())
				contourPoints.append(p + ", ");

			// logger.debug(" > contour " + i + ": " + contourPoints.toString());

			double area = Imgproc.contourArea(c, false);
			logger.debug(" > contour " + i + " area: " + area + " / " + ((area / (imageWidth * imageHeight)) * 100) + "%");

			if (area < minArea) {
				minArea = area;
				minIndex = i;
			}

			if (area > maxArea) {
				maxArea = area;
				maxIndex = i;
			}

			totalArea += area;

			i++;
		}

		logger.debug("max contour area: " + maxArea + " (contour " + maxIndex + "/" + contours.size() + ") / "
				+ ((maxArea / (imageWidth * imageHeight)) * 100) + "%");
		logger.debug("min contour area: " + minArea + " (contour " + minIndex + "/" + contours.size() + ") / "
				+ ((minArea / (imageWidth * imageHeight)) * 100) + "%");
		logger.debug("total contours area: " + totalArea + " / " + ((totalArea / (imageWidth * imageHeight)) * 100) + "%");
	}

	public static void logRects(List<Rect> rects) {

		if (rects == null || rects.isEmpty()) {
			logger.error("rects is null or empty");
			return;
		}

		logger.debug("=== total rects count: " + rects.size() + " ===");

		int i = 0;

		for (Rect r : rects) {

			if (r == null) {
				continue;
			}

			logger.debug(" > rect " + i + ": " + r);
			i++;
		}
	}

	public static double findMinContourAreaRatio(List<MatOfPoint> contours, int imageWidth, int imageHeight) {

		if (contours == null || contours.isEmpty()) {
			logger.error("contours is null or empty");
			return -1;
		}

		if (imageWidth <= 0 || imageHeight <= 0) {
			logger.error("incorrect image size: " + imageWidth + "x" + imageHeight);
			return -1;
		}

		int i = 0;

		double minArea = Imgproc.contourArea(contours.get(0), false);
		int minIndex = i;

		for (MatOfPoint c : contours) {

			if (c == null) {
				continue;
			}

			double area = Imgproc.contourArea(c, false);

			if (area < minArea) {
				minArea = area;
				minIndex = i;
			}

			i++;
		}

		double minAreaRatio = minArea / (imageWidth * imageHeight);
		logger.debug("min contour area: " + minArea + " (contour " + minIndex + "/" + contours.size() + ") / " + minAreaRatio);
		return minAreaRatio;
	}

	public static double findMaxContourAreaRatio(List<MatOfPoint> contours, int imageWidth, int imageHeight) {

		if (contours == null || contours.isEmpty()) {
			logger.error("contours is null or empty");
			return -1;
		}

		if (imageWidth <= 0 || imageHeight <= 0) {
			logger.error("incorrect image size: " + imageWidth + "x" + imageHeight);
			return -1;
		}

		int i = 0;

		double maxArea = Imgproc.contourArea(contours.get(0), false);
		int maxIndex = i;

		for (MatOfPoint c : contours) {

			if (c == null) {
				continue;
			}

			double area = Imgproc.contourArea(c, false);

			if (area > maxArea) {
				maxArea = area;
				maxIndex = i;
			}

			i++;
		}

		double maxAreaRatio = maxArea / (imageWidth * imageHeight);
		logger.debug("max contour area: " + maxArea + " (contour " + maxIndex + "/" + contours.size() + ") / " + maxAreaRatio);
		return maxAreaRatio;
	}

	public static double findTotalContourAreaRatio(List<MatOfPoint> contours, int imageWidth, int imageHeight) {

		if (contours == null || contours.isEmpty()) {
			logger.error("contours is null or empty");
			return -1;
		}

		if (imageWidth <= 0 || imageHeight <= 0) {
			logger.error("incorrect image size: " + imageWidth + "x" + imageHeight);
			return -1;
		}

		double totalArea = 0;

		for (MatOfPoint c : contours) {

			if (c == null) {
				continue;
			}

			double area = Imgproc.contourArea(c, false);
			totalArea += area;
		}

		double totalAreaRatio = totalArea / (imageWidth * imageHeight);
		logger.debug("total contour area: " + totalArea + " / " + totalAreaRatio + " (count: " + contours.size() + ")");
		return totalAreaRatio;
	}

	public static boolean isPointInPolygon(List<Point> polygon, Point point) {

		if (polygon == null || polygon.isEmpty()) {
			logger.error("polygon is null or empty");
			return false;
		}

		if (point == null) {
			logger.error("point is null");
			return false;
		}

		double x1, y1, x2, y2, dX, dY, p;

		final int verticesCount = polygon.size();

		int counter = 0, next;

		for (int i = 0; i < verticesCount; i++) {
			next = (i + 1) % verticesCount;
			x1 = polygon.get(next).x;
			y1 = polygon.get(next).y;
			x2 = polygon.get(i).x;
			y2 = polygon.get(i).y;

			if (y1 < point.y && y2 < point.y)
				continue;
			if (x1 == point.x) {
				if (y1 == point.y)
					return true;
			} else {
				if (x2 == point.x) {
					if (y1 <= point.y || y2 <= point.y)
						return true;
					else
						continue;
				}
			}

			if ((x2 <= point.x && x1 <= point.x) || (x2 > point.x && x1 > point.x))
				continue;
			if (y1 > point.y && y2 > point.y)
				counter++;
			else {
				dX = x2 - x1;
				dY = y2 - y1;
				p = y1 + (point.x - x1) / dX * dY;
				if (p > point.y)
					counter++;
				else {
					if (p == point.y)
						return true;
				}
			}
		}

		return counter % 2 != 0;
	}

	public static boolean isContourInPolygon(MatOfPoint contour, List<Point> polygon) {

		if (contour == null || contour.empty())
			return false;

		if (polygon == null || polygon.isEmpty())
			return false;

		boolean containsContour = true;

		for (Point p : contour.toArray()) {
			if (!isPointInPolygon(polygon, p))
				containsContour = false;
		}

		return containsContour;
	}

	public static List<MatOfPoint> filterContours(List<MatOfPoint> contours, List<Point> region) {

		if (contours == null)
			return null;

		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();

		if (region == null || region.isEmpty())

			filteredContours.addAll(contours);

		else {

			if (!contours.isEmpty()) {

				for (MatOfPoint contour : contours) {

					if (contour == null)
						continue;

					if (isContourInPolygon(contour, region))
						filteredContours.add(contour);
				}

			}
		}

		return filteredContours;
	}

	public static boolean isRectInPolygon(Rect rect, List<Point> polygon) {

		if (rect == null)
			return false;

		if (polygon == null || polygon.isEmpty())
			return false;

		return (isPointInPolygon(polygon, new Point(rect.x, rect.y)) && isPointInPolygon(polygon, new Point(rect.x + rect.width, rect.y))
				&& isPointInPolygon(polygon, new Point(rect.x, rect.y + rect.height)) && isPointInPolygon(polygon, new Point(rect.x
				+ rect.width, rect.y + rect.height)));
	}

	public static List<Rect> filterRects(List<Rect> rects, List<Point> region) {

		if (rects == null)
			return null;

		List<Rect> filteredRects = new ArrayList<Rect>();

		if (region == null || region.isEmpty())

			filteredRects.addAll(rects);

		else {

			if (!rects.isEmpty()) {

				for (Rect rect : rects) {

					if (rect == null)
						continue;

					if (isRectInPolygon(rect, region))
						filteredRects.add(rect);
				}
			}
		}

		return filteredRects;
	}

}

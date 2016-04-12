package ru.maxsmr.opencv.commondetector.motion;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

public final class MhiDetector extends BaseDetector implements IDetector {

	// various tracking parameters (in seconds)
	public static final double MHI_DURATION = 1;
	public static final double MAX_TIME_DELTA = 0.5;
	public static final double MIN_TIME_DELTA = 0.05;

	// number of cyclic frame buffer used for motion detection
	// (should, probably, depend on FPS)
	public static final int N = 4;

	// ring image buffer
	private Mat[] buf = null;
	private int last = 0;

	// temporary images

	// MHI
	private Mat mhi = null;

	// orientation
	private Mat orient = null;

	// valid orientation mask
	private Mat mask = null;

	// motion segmentation map
	private Mat segmask = null;

	private final int threshold;

	public MhiDetector(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public synchronized Mat detect(Mat source, List<Point> region) {
		// get current time in seconds
		double timestamp = System.currentTimeMillis() / 1000.0;
		Size size = source.size(); // get current frame size
		int i, idx1 = last, idx2;
		Mat silh;
		Rect compRect;
		double count;
		double angle;
		Point center;
		double magnitude;
		Scalar color;

		// allocate images at the beginning or
		// reallocate them if the frame size is changed
		if (mhi == null || mhi.width() != size.width || mhi.height() != size.height) {
			if (buf == null) {
				buf = new Mat[N];
			}

			for (i = 0; i < N; i++) {
				buf[i] = new Mat(size, CvType.CV_8UC1);
				buf[i] = Mat.zeros(size, CvType.CV_8UC1);
			}
			mhi = new Mat(size, CvType.CV_32FC1);
			orient = new Mat(size, CvType.CV_32FC1);
			segmask = new Mat(size, CvType.CV_32FC1);
			mask = new Mat(size, CvType.CV_8UC1);
		}
		// convert frame to gray scale
		Imgproc.cvtColor(source, buf[last], Imgproc.COLOR_BGR2GRAY);

		// index of (last - (N-1))th frame
		idx2 = (last + 1) % N;
		last = idx2;

		silh = buf[idx2];

		// get difference between frames
		Core.absdiff(buf[idx1], buf[idx2], silh);

		// and threshold it
		double computedThreshold = Imgproc.threshold(silh, silh, threshold, 255, Imgproc.THRESH_BINARY);

		// Log.i(TAG, "Computed threshold - " + computedThreshold);

		// update MHI
		mhi = Mat.zeros(size, CvType.CV_32FC1);
		Video.updateMotionHistory(silh, mhi, timestamp, MHI_DURATION);

		// convert MHI to blue 8u image
		Core.convertScaleAbs(mhi, mask, (255. / MHI_DURATION), (MHI_DURATION / -timestamp) * 255. / MHI_DURATION);

		// Merge them
		// dst = Mat.zeros(size, CvType.CV_8UC4);
		// List<Mat> sources = new ArrayList<Mat>(1);
		// sources.add(mask);
		// Core.merge(sources, dst);

		// calculate motion gradient orientation and valid orientation mask
		Video.calcMotionGradient(mhi, mask, orient, MAX_TIME_DELTA, MIN_TIME_DELTA, 3);

		// segment motion: get sequence of motion components
		// segmask is marked motion components map. It is not used further
		MatOfRect boundingRects = new MatOfRect();
		Video.segmentMotion(mhi, segmask, boundingRects, timestamp, MAX_TIME_DELTA);

		List<Rect> rects = boundingRects.toList();

		if (boundingRects.total() == 0) {
			targetDetected = false;
		} else {
			// iterate through the motion components,
			// One more iteration (i == -1) corresponds to the whole image
			// (global
			// motion)
			for (i = 0; i < boundingRects.total(); i++) {

				targetDetected = true;
				if (i < 0) {
					// case of the whole image
					compRect = new Rect(new Point(0, 0), size);
					color = new Scalar(255, 255, 255);
					magnitude = 100;
				} else {
					// i-th motion component
					compRect = rects.get(i);
					// reject very small component
					if (compRect.width + compRect.height < 100)
						continue;
					color = this.contourColor;
					magnitude = 30;
				}

				// select component ROI
				Mat silhRoi = silh.submat(compRect);
				Mat mhiRoi = mhi.submat(compRect);
				Mat orientRoi = orient.submat(compRect);
				Mat maskRoi = mask.submat(compRect);

				// calculate orientation
				angle = Video.calcGlobalOrientation(orientRoi, maskRoi, mhiRoi, timestamp, MHI_DURATION);

				// adjust for images with top-left origin
				angle = 360.0 - angle;

				// calculate number of points within silhouette ROI
				count = Core.norm(silhRoi, Core.NORM_L1);

				// check for the case of little motion
				if (count < (compRect.width * compRect.height * 0.05))
					continue;

				// draw a clock with arrow indicating the direction
				center = new Point((compRect.x + compRect.width / 2), (compRect.y + compRect.height / 2));

				Core.circle(source, center, (int) Math.round(magnitude * 1.2), color, contourThickness, Core.LINE_AA, 0);
				Core.line(
						source,
						center,
						new Point(Math.round(center.x + magnitude * Math.cos(angle * Math.PI / 180)), Math.round(center.y - magnitude
								* Math.sin(angle * Math.PI / 180))), color, contourThickness, Core.LINE_AA, 0);

			}
		}
		return source;
	}

}

package net.maxsmr.opencv.commondetector.motion;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import net.maxsmr.opencv.commondetector.utils.DetectorHelper;

public final class BasicDetector extends BaseDetector implements IDetector {

	// private static final Logger logger = LoggerFactory.getLogger(BasicDetector.class);

	// number of cyclic frame buffer used for motion detection
	// (should, probably, depend on FPS)
	public static final int N = 4;

	// ring image buffer
	private Mat[] buf = null;
	private int last = 0;

	private final int threshold;

	public BasicDetector(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public Mat detect(Mat source, List<Point> region) {
		Size size = source.size(); // get current frame size
		int i, idx1 = last, idx2;
		Mat silh;

		// allocate images at the beginning or
		// reallocate them if the frame size is changed
		if (buf == null || buf[0].width() != size.width || buf[0].height() != size.height) {
			if (buf == null) {
				buf = new Mat[N];
			}

			for (i = 0; i < N; i++) {
				buf[i] = new Mat(size, CvType.CV_8UC1);
				buf[i] = Mat.zeros(size, CvType.CV_8UC1);
			}
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
		Imgproc.threshold(silh, silh, threshold, 255, Imgproc.THRESH_BINARY);
		// logger.info("Computed threshold: " + computedThreshold);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Imgproc.findContours(silh, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		// logContours(contours, source.cols(), source.rows());

		List<MatOfPoint> filteredContours = DetectorHelper.filterContours(contours, region);
		// logContours(filteredContours, source.cols(), source.rows());

		if (filteredContours != null && filteredContours.size() > 0) {

			Imgproc.drawContours(source, filteredContours, -1, contourColor, contourThickness);
			targetDetected = true;

		} else {

			targetDetected = false;
		}
		return source;
	}

}

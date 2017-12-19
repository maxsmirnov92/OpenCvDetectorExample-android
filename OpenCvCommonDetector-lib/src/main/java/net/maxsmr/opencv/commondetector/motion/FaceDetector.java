package net.maxsmr.opencv.commondetector.motion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import net.maxsmr.opencv.commondetector.R;


public final class FaceDetector extends BaseDetector implements IDetector {

	private static final Logger logger = LoggerFactory.getLogger(MhiDetector.class);

	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

	private File cascadeFile;
	private CascadeClassifier faceDetector;
	private float relativeFaceSize = 0.2f;
	private int absoluteFaceSize = 0;

	public FaceDetector(float faceSize, Context ctx) {
		try {
			setMinFaceSize(faceSize);

			// load cascade file from application resources
			InputStream is = ctx.getResources().openRawResource(R.raw.lbpcascade_frontalface);
			File cascadeDir = ctx.getDir("cascade", Context.MODE_PRIVATE);
			cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
			FileOutputStream os = new FileOutputStream(cascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();

			faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
			if (faceDetector.empty()) {
				logger.error("Failed to load cascade classifier");
				faceDetector = null;
			} else
				logger.info("Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
			cascadeDir.delete();
		} catch (IOException e) {
			logger.error("Failed to load cascade. Exception thrown: " + e, e);
		}
	}

	public void setMinFaceSize(float faceSize) {
		relativeFaceSize = faceSize;
		absoluteFaceSize = 0;
	}

	@Override
	public synchronized Mat detect(CvCameraViewFrame frame) {
		Mat cameraRgbaFrame = frame.rgba();
		if (faceDetector != null) {
			Mat cameraGrayFrame = frame.gray();
			if (absoluteFaceSize == 0) {
				int height = cameraGrayFrame.rows();
				if (Math.round(height * relativeFaceSize) > 0) {
					absoluteFaceSize = Math.round(height * relativeFaceSize);
				}
			}
			MatOfRect faces = new MatOfRect();
			faceDetector.detectMultiScale(cameraGrayFrame, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());

			Rect[] facesArray = faces.toArray();
			for (int i = 0; i < facesArray.length; i++) {

				// Highlight face
				// Core.rectangle(cameraRgbaFrame, facesArray[i].tl(),
				// facesArray[i].br(), FACE_RECT_COLOR, 3);

				Point center = new Point(facesArray[i].x + facesArray[i].width * 0.5, facesArray[i].y + facesArray[i].height * 0.5);
				Core.ellipse(cameraRgbaFrame, center, new Size(facesArray[i].width * 0.5, facesArray[i].height * 0.5), 0, 0, 360,
						FACE_RECT_COLOR, 4, 8, 0);

			}
		}
		return cameraRgbaFrame;
	}

	@Override
	public synchronized Mat detect(Mat source, List<Point> region) {
		return source;
	}

}

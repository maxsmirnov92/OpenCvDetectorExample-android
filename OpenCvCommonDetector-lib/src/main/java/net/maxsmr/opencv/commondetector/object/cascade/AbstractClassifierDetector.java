package net.maxsmr.opencv.commondetector.object.cascade;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import net.maxsmr.commonutils.data.FileHelper;
import net.maxsmr.opencv.commondetector.utils.DetectorHelper;
import net.maxsmr.opencv.commondetector.model.graphic.Rect;
import net.maxsmr.opencv.commondetector.model.object.info.ObjectDetectFrameInfo;
import net.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;

import net.maxsmr.opencv.commondetector.utils.OpenCvUtils;

public abstract class AbstractClassifierDetector {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClassifierDetector.class);

    public static final int CONTOUR_THICKNESS_DEFAULT = 1;
    private int contourThickness = CONTOUR_THICKNESS_DEFAULT;

    public int getContourThickness() {
        return contourThickness;
    }

    public void setContourThickness(int thickness) {
        if (thickness >= 1)
            this.contourThickness = thickness;
    }

    public static final Scalar CONTOUR_COLOR_DEFAULT = new Scalar(0, 0, 255);
    private Scalar contourColor = CONTOUR_COLOR_DEFAULT;

    public Scalar getContourColor() {
        return contourColor;
    }

    public void setContourColor(Scalar color) {
        if (color != null)
            this.contourColor = color;
    }

    public static final boolean DEFAULT_GRAYSCALE = true;
    private boolean grayscale = DEFAULT_GRAYSCALE;

    public boolean grayscale() {
        return grayscale;
    }

    public void setGrayscale(boolean toggle) {
        this.grayscale = toggle;
    }

    protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss.SSS", Locale.getDefault());

    protected static final String SOURCE_FRAMES_DIR = "source";
    protected static final String PRE_PROCESSED_FRAMES_DIR = "pre-processed";
    protected static final String DETECTED_FRAMES_DIR = "detected";

    private File savedFramesDir;

    public File getSavedFramesDir() {
        return savedFramesDir;
    }

    public boolean setSavedFramesDir(File dir) {

        if (dir == null) {
            return false;
        }

        if (!FileHelper.isDirExists(dir.getAbsolutePath())) {
            if (FileHelper.createNewDir(dir.getAbsolutePath()) == null) {
                return false;
            }
        }

        this.savedFramesDir = dir;
        return true;
    }

    protected static boolean isClassifierLoaded(CascadeClassifier c) {
        return c != null && !c.empty();
    }

    protected static CascadeClassifier loadClassifier(File file) {

        CascadeClassifier classifier;

        if (FileHelper.isFileCorrect(file)) {
            classifier = new CascadeClassifier();
            if (classifier.load(file.getAbsolutePath())) {
                logger.debug("succesfully loading classifier from file: " + file);
            } else {
                logger.error("error loading classifier from file: " + file);
                classifier = null;
            }
        } else {
            logger.error("incorrect file: " + file);
            classifier = null;
        }

        return classifier.empty() ? null : classifier;
    }

    protected static CascadeClassifier loadClassifiers(List<File> files) {

        if (files == null || files.isEmpty()) {
            logger.error("classifier files is null or empty");
            return null;
        }

        CascadeClassifier classifier = new CascadeClassifier();

        for (File f : files) {
            if (FileHelper.isFileCorrect(f)) {
                if (classifier.load(f.getAbsolutePath())) {
                    logger.debug("succesfully loading classifier from file: " + f);
                } else {
                    logger.error("error loading classifier from file: " + f);
                }
            } else {
                logger.error("incorrect file: " + f);
            }
        }

        return classifier.empty() ? null : classifier;
    }

    /**
     * used for custom object detection by one or more classifiers
     */
    public abstract ObjectDetectFrameInfo detect(Mat frame, Size scaleSize, List<org.opencv.core.Point> region);

    /**
     * base object detection by given cascade classifier, can be wrapped by implemented detect(); pre-processing
     * included
     */
    protected static ObjectDetectFrameInfo detect(CascadeClassifier classifier, OBJECT_TYPE objectType, Mat frame, Size scaleSize,
                                                  List<org.opencv.core.Point> cvRegion, Scalar contourColor, boolean grayscale, File savedFramesDir) {
        logger.debug("detect(), classifier=" + classifier + ", objectType=" + objectType + ", frame=" + frame + ", scaleSize=" + scaleSize
                + ", cvRegion=" + cvRegion + ", contourColor=" + contourColor + ", grayscale=" + grayscale + ", savedFramesDir="
                + savedFramesDir);

        if (!isClassifierLoaded(classifier))
            throw new RuntimeException("classifier is not loaded");

        if (frame == null)
            throw new NullPointerException("frame mat is null");

        if (frame.empty())
            throw new IllegalArgumentException("frame mat is empty");

        if (!(frame.channels() == 1 || frame.channels() == 3 || frame.channels() == 4))
            throw new IllegalArgumentException("incorrect frame mat channels number: " + frame.channels());

        final long startTime = System.currentTimeMillis();

        if (savedFramesDir != null) {
            logger.debug("saving source frame " + frame.width() + "x" + frame.height() + "...");

            long time = new Date().getTime();
            FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", savedFramesDir.getAbsolutePath() + File.separator
                    + SOURCE_FRAMES_DIR);
            Highgui.imwrite(savedFramesDir.getAbsolutePath() + File.separator + SOURCE_FRAMES_DIR + File.separator + "frame_"
                    + dateFormatter.format(time) + ".png", frame);
        }

        if (scaleSize != null && (frame.size().width > scaleSize.width || frame.size().height > scaleSize.height))
            Imgproc.resize(frame, frame, scaleSize);

        if (grayscale) {
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

        if (grayscale && savedFramesDir != null) {
            logger.debug("saving pre-processed frame " + frame.width() + "x" + frame.height() + "...");

            long time = new Date().getTime();
            FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", savedFramesDir.getAbsolutePath() + File.separator
                    + PRE_PROCESSED_FRAMES_DIR);
            Highgui.imwrite(savedFramesDir.getAbsolutePath() + File.separator + PRE_PROCESSED_FRAMES_DIR + File.separator + "frame_"
                    + dateFormatter.format(time) + ".png", frame);
        }

        MatOfRect objects = new MatOfRect();

        classifier.detectMultiScale(frame, objects); // frameGray

        // frameGray.release();

        // DetectorHelper.logRects(objects.toList());

        // final List<org.opencv.core.Point> cvRegion;

        // if (region != null) {
        // cvRegion = new ArrayList<org.opencv.core.Point>();
        // for (Point p : region) {
        // if (p == null) {
        // continue;
        // }
        // cvRegion.add(new org.opencv.core.Point(p.x, p.y));
        // }
        // } else
        // cvRegion = null;

        List<org.opencv.core.Rect> cvFilteredObjects = DetectorHelper.filterRects(objects.toList(), cvRegion);
        List<Rect> filteredObjects = null;

        // DetectorHelper.logRects(cvFilteredObjects);

        if (cvFilteredObjects != null && !cvFilteredObjects.isEmpty()) {

            filteredObjects = new ArrayList<Rect>();

            for (org.opencv.core.Rect rect : cvFilteredObjects) {

                if (rect == null)
                    continue;

                Core.rectangle(frame, new org.opencv.core.Point(rect.x, rect.y), new org.opencv.core.Point(rect.x + rect.width, rect.y
                        + rect.height), contourColor);
                filteredObjects.add(new Rect(rect.x, rect.y, rect.width, rect.height));
            }
        }

        if (filteredObjects != null && !filteredObjects.isEmpty()) {
            if (savedFramesDir != null) {
                logger.debug("saving detected frame " + frame.width() + "x" + frame.height() + "...");

                long time = new Date().getTime();
                FileHelper.createNewFile("frame_" + dateFormatter.format(time) + ".png", savedFramesDir.getAbsolutePath() + File.separator
                        + DETECTED_FRAMES_DIR);
                Highgui.imwrite(savedFramesDir.getAbsolutePath() + File.separator + DETECTED_FRAMES_DIR + File.separator + "frame_"
                        + dateFormatter.format(time) + ".png", frame);
            }
        }

        // GraphicUtils.getBitmapData(OpenCvUtils.convertMatToBitmap(frame, false))

        return new ObjectDetectFrameInfo(OpenCvUtils.convertMatToByteArray(frame), frame.type(), frame.cols(), frame.rows(),
                filteredObjects != null && !filteredObjects.isEmpty(), objectType, filteredObjects, System.currentTimeMillis() - startTime);
    }
}

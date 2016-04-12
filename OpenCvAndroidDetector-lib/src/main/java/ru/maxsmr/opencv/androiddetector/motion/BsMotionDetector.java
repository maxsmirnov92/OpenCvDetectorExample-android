package ru.maxsmr.opencv.androiddetector.motion;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;

import ru.maxsmr.opencv.androiddetector.OpenCvInit;
import ru.maxsmr.opencv.commondetector.model.DETECTOR_SENSITIVITY;
import ru.maxsmr.opencv.commondetector.model.graphic.Point;
import ru.maxsmr.opencv.commondetector.motion.BackgroundSubtractorDetector;
import ru.maxsmr.opencv.commondetector.motion.BaseDetector;

import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.commonutils.graphic.GraphicUtils;
import ru.maxsmr.opencv.commondetector.utils.OpenCvUtils;


public class BsMotionDetector extends AbstractMotionDetector {

    private static final Logger logger = LoggerFactory.getLogger(BsMotionDetector.class);

    private BackgroundSubtractorDetector bsDetector;

    private synchronized void initBackgroundSubtractorDetector() {
        bsDetector = new BackgroundSubtractorDetector(mixtures, history, backgroundRatio, noiseSigma, learningRate, minContourAreaRatio);
        bsDetector.setContourThickness(getContourThickness());
        bsDetector.setContourColor(getContourColor());
        bsDetector.setGrayscale(grayscale);
        bsDetector.setMorphKernelSize(morphKernelSize);
        bsDetector.setSavedFramesDir(savedFramesDir);
    }

    private synchronized void releaseBackgroundSubtractorDetector() {
        bsDetector = null;
    }

    private boolean grayscale = BaseDetector.DEFAULT_GRAYSCALE;

    public boolean grayscale() {
        return grayscale;
    }

    public void setGrayscale(boolean toggle) {
        this.grayscale = toggle;
    }

    private int morphKernelSize = BaseDetector.DEFAULT_MORPH_KERNEL_SIZE;

    public int getMorphKernelSize() {
        return morphKernelSize;
    }

    public void setMorphKernelSize(int morphKernelSize) {
        if (morphKernelSize >= 0)
            this.morphKernelSize = morphKernelSize;
        else
            throw new IllegalArgumentException("incorrect morphKernelSize parameter: " + morphKernelSize);
    }

    private final int mixtures;
    private final int history;
    private final double backgroundRatio;
    private final double noiseSigma;
    private final double learningRate;
    private final double minContourAreaRatio;

    private File savedFramesDir;

    public boolean setSavedFramesDir(File savedFramesDir) {

        if (savedFramesDir == null) {
            return false;
        }

        if (!FileHelper.isDirExists(savedFramesDir.getAbsolutePath())) {
            logger.warn("directory " + savedFramesDir + "not exists, creating...");
            if (FileHelper.createNewDir(savedFramesDir.getAbsolutePath()) == null) {
                logger.error("can't create dir");
                return false;
            }
        }

        this.savedFramesDir = savedFramesDir;
        return true;
    }

    public BsMotionDetector(File savedFramesDir) {
        super();
        logger.debug("BsMotionDetector(), savedFramesDir=" + savedFramesDir);

        this.grayscale = BaseDetector.DEFAULT_GRAYSCALE;
        this.morphKernelSize = BaseDetector.DEFAULT_MORPH_KERNEL_SIZE;

        this.mixtures = BackgroundSubtractorDetector.DEFAULT_MIXTURES;
        this.history = BackgroundSubtractorDetector.DEFAULT_HISTORY;
        this.backgroundRatio = BackgroundSubtractorDetector.DEFAULT_BACKGROUND_RATIO;
        this.noiseSigma = BackgroundSubtractorDetector.DEFAULT_NOISE_SIGMA;
        this.learningRate = BackgroundSubtractorDetector.DEFAULT_LEARNING_RATE;
        this.minContourAreaRatio = BackgroundSubtractorDetector.DEFAULT_MIN_CONTOUR_AREA_RATIO;

        setSavedFramesDir(savedFramesDir);
    }

    public BsMotionDetector(boolean grayscale, int morphKernelSize, int mixtures, int history, double backgroundRatio, double noiseSigma,
                            double learningRate, double minContourAreaRatio, File savedFramesDir) {
        super();
        logger.debug("BsMotionDetector(), grayscale=" + grayscale + ", morphKernelSize=" + morphKernelSize + ", mixtures=" + mixtures
                + ", history=" + history + ", backgroundRatio=" + backgroundRatio + ", noiseSigma=" + noiseSigma + ", learningRate="
                + learningRate + ", minContourAreaRatio=" + minContourAreaRatio + ", savedFramesDir=" + savedFramesDir);

        setGrayscale(grayscale);
        setMorphKernelSize(morphKernelSize);

        this.mixtures = mixtures;
        this.history = history;
        this.backgroundRatio = backgroundRatio;
        this.noiseSigma = noiseSigma;
        this.learningRate = learningRate;
        this.minContourAreaRatio = minContourAreaRatio;

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
    public synchronized boolean detectMotionByByteArray(byte[] data, boolean isRgb, int yuvFormat, int imageWidth, int imageHeight, DETECTOR_SENSITIVITY sensitivity, List<Point> region) {
        // logger.debug("detectMotionByByteArray(), isRgb=" + isRgb + ", yuvFormat=" + yuvFormat + ", imageWidth=" +
        // imageWidth
        // + ", imageHeight=" + imageHeight + ", sensitivity=" + sensitivity + ", region=" + region);

        if (!OpenCvInit.getInstance().isOpenCvManagerLoaded())
            throw new RuntimeException("OpenCV Manager is not loaded");

        if (data == null)
            throw new NullPointerException("image data is null");

        if (data.length == 0)
            throw new IllegalArgumentException("image data is empty");

        if (imageWidth <= 0 || imageHeight <= 0)
            throw new IllegalArgumentException("incorrect image size: " + imageWidth + "x" + imageHeight);

        if (sensitivity == null || sensitivity == DETECTOR_SENSITIVITY.NONE) {
            // logger.debug("sensitivity is null or NONE, no need to detect motion");
            return true;
        }

        Bitmap frameBitmap;

        if (!isRgb) {

            if (!(yuvFormat == ImageFormat.NV16 || yuvFormat == ImageFormat.NV21 || yuvFormat == ImageFormat.YUY2 || yuvFormat == ImageFormat.YV12))
                throw new IllegalArgumentException("image format is not YUV: " + yuvFormat);

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
        // if (morphKernelSize > 0) {
        // logger.debug("applying morphology (kernel size:" + morphKernelSize + ")...");
        // Mat frameMorph = OpenCvUtils.doMorphology(frame, grayscale, morphKernelSize);
        // frame.release();
        // frame = frameMorph;
        // }
        //
        // if (frame == null || frame.empty()) {
        // throw new RuntimeException("pre-processing failed: frame is null or empty");
        // }

        // if (grayscale && morphKernelSize > 0 && savedFramesDir != null) {
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
        } else
            cvRegion = null;

        if (bsDetector == null)
            initBackgroundSubtractorDetector();

        logger.debug("detecting motion by matrix " + imageWidth + "x" + imageHeight + "...");
        Mat resultMat = bsDetector.detect(frame, cvRegion);

        Bitmap resultBitmap = null;

        if (resultMat != null) {
            resultBitmap = OpenCvUtils.convertMatToBitmap(resultMat, false);
            resultMat.release();
        } else {
            logger.error("incorrect last frame");
            return bsDetector.isDetected();
        }

        if (updateLastFrame(resultBitmap)) {

            if (bsDetector.isDetected()) {

                // if (savedFramesDir != null) {
                // logger.debug("saving detected frame " + resultBitmap.getWidth() + "x" + resultBitmap.getHeight() +
                // "...");
                // FileHelper.writeCompressedBitmapToFile(resultBitmap, Bitmap.CompressFormat.PNG,
                // "frame_" + dateFormatter.format(new Date().getTime()), savedFramesDir.getAbsolutePath() +
                // File.separator
                // + DETECTED_FRAME_DIR);
                // }
            }

            resultBitmap.recycle();

        } else
            logger.error("incorrect last frame");

        return bsDetector.isDetected();
    }

    @Override
    protected void beforeVideoDetect(File videoFile, int framesCount, DETECTOR_SENSITIVITY sensitivity) {
        initBackgroundSubtractorDetector();
    }

    @Override
    protected void afterVideoDetect(File videoFile, int framesCount, DETECTOR_SENSITIVITY sensitivity) {
        releaseBackgroundSubtractorDetector();
    }

}

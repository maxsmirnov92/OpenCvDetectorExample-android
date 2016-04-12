package ru.maxsmr.opencv.androiddetector.motion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.util.Pair;

import ru.maxsmr.opencv.commondetector.model.DETECTOR_SENSITIVITY;
import ru.maxsmr.opencv.commondetector.model.graphic.Point;
import ru.maxsmr.opencv.commondetector.model.motion.info.MotionDetectVideoInfo;
import ru.maxsmr.opencv.commondetector.model.motion.settings.MotionDetectorSettings;

import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.commonutils.graphic.GraphicUtils;

public abstract class AbstractMotionDetector {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMotionDetector.class);

    /**
     * @return last frame containing circled motion track if motion has been detected or without otherwise
     */
    public abstract Bitmap getLastFrame();

    protected abstract boolean updateLastFrame(Bitmap frame);

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

    /**
     * @param data   must be in rgb or yuv format; if rgb - must be created by copyPixelsToBuffer method and have 3-channel
     *               RGB_565 configuration
     * @param region list of polygon points to detect motion in; null / empty - use whole frame
     */
    public abstract boolean detectMotionByByteArray(byte[] data, boolean isRgb, int yuvFormat, int imageWidth, int imageHeight,
                                                    DETECTOR_SENSITIVITY sensitivity, List<Point> region);

    protected abstract void beforeVideoDetect(File videoFile, int framesCount, DETECTOR_SENSITIVITY sensitivity);

    protected abstract void afterVideoDetect(File videoFile, int framesCount, DETECTOR_SENSITIVITY sensitivity);

    protected static final String SOURCE_FRAME_DIR = "source";
    protected static final String DETECTED_FRAME_DIR = "detected";

    public final static int FRAMES_TO_ANALYZE_COUNT_DEFAULT = 20;

    public synchronized MotionDetectVideoInfo detectMotionInVideoFile(File videoFile, int framesCount, DETECTOR_SENSITIVITY sensitivity,
                                                                      List<Point> region, File savedFramesDir) {
        logger.debug("detectMotionInVideoFile(), videoFile=" + videoFile + ", framesCount=" + framesCount + ", sensitivity=" + sensitivity
                + ", region=" + ", savedFramesDir=" + savedFramesDir);

        beforeVideoDetect(videoFile, framesCount, sensitivity);

        final long startTime = System.currentTimeMillis();

        final List<Pair<Long, Bitmap>> retrievedFrames = GraphicUtils.getVideoFrames(videoFile, framesCount <= 1 ? FRAMES_TO_ANALYZE_COUNT_DEFAULT
                : framesCount);

        if (retrievedFrames == null || retrievedFrames.isEmpty()) {
            throw new RuntimeException("retrievedFrames is null or empty");
        }

        int extractedFramesCount = 0;

        int detectedFramesCount = 0;
        List<Long> detectedFramesPositions = new ArrayList<Long>();

        for (Pair<Long, Bitmap> currentFrame : retrievedFrames) {

            Bitmap frameBitmap = currentFrame.second;

            if (frameBitmap == null) {
                logger.error("video frame at " + currentFrame.first + " ms is null");
                continue;
            }

            if (frameBitmap.getConfig() != Bitmap.Config.RGB_565) {
                Bitmap convertedBitmap = GraphicUtils.reconfigureBitmap(frameBitmap, Bitmap.Config.RGB_565);
                if (convertedBitmap != null && GraphicUtils.getBitmapByteCount(convertedBitmap) > 0) {
                    frameBitmap.recycle();
                    frameBitmap = convertedBitmap;
                } else {
                    logger.error("conversion to RGB_565 failed");
                    continue;
                }
            }

            extractedFramesCount++;

            if (savedFramesDir != null) {
                logger.debug("saving source frame (position " + currentFrame.first + " ms) to file...");

                GraphicUtils.writeCompressedBitmapToFile(new File(savedFramesDir.getAbsolutePath() + File.separator + videoFile.getName()
                        + File.separator + SOURCE_FRAME_DIR, videoFile.getName() + "_"
                        + currentFrame.first + "_ms"), frameBitmap, Bitmap.CompressFormat.PNG);

                // ArchiveHelper.getInstance().saveImageBitmapToFile(ARCHIVE_MODE.DETECTOR_VIDEO_FRAME, null,
                // Bitmap.CompressFormat.PNG,
                // frameBitmap, videoFile.getName(), currentFrame.getPositionMs());
            }

            if (detectMotionByByteArray(GraphicUtils.getBitmapData(frameBitmap), true, 0, frameBitmap.getWidth(), frameBitmap.getHeight(),
                    sensitivity, region)) {

                logger.info("motion is detected in file " + videoFile.getName() + ", position: " + currentFrame.first + " ms");

                detectedFramesPositions.add(currentFrame.first);
                detectedFramesCount++;

                if (savedFramesDir != null) {
                    logger.debug("saving detected frame (position " + currentFrame.first + " ms) to file...");

                    GraphicUtils.writeCompressedBitmapToFile(new File(savedFramesDir.getAbsolutePath() + File.separator + videoFile.getName()
                            + File.separator + DETECTED_FRAME_DIR, videoFile.getName() + "_"
                            + currentFrame.first + "_ms"), getLastFrame(), Bitmap.CompressFormat.PNG);
                }
            }

            frameBitmap.recycle();
        }

        final long detectionTime = System.currentTimeMillis() - startTime;

        logger.info("motion has been detected: " + (detectedFramesCount > 0) + " (in " + extractedFramesCount + " frame(s))");
        logger.info("detection processing time: " + detectionTime + " ms");

        afterVideoDetect(videoFile, framesCount, sensitivity);

        return new MotionDetectVideoInfo(videoFile, (detectedFramesCount > 0),
                (double) detectedFramesCount / (double) extractedFramesCount, detectedFramesPositions, detectionTime);
    }

    public static List<MotionDetectVideoInfo> testMotionDetector(AbstractMotionDetector detector, String videosPath, String detectInfoName,
                                                                 String savedFramesPath, MotionDetectorSettings detectorSettings) {
        logger.debug("testMotionDetector(), detector=" + detector + ", videosPath=" + videosPath + ", detectInfoName=" + detectInfoName
                + ", savedFramesPath=" + savedFramesPath + ", detectorSettings=" + detectorSettings);

        if (detector == null) {
            logger.error("what are going to test, huh? detector is null");
            return null;
        }

        if (detectorSettings == null) {
            logger.error("detectorSettings is null");
            return null;
        }

        if (!FileHelper.isDirExists(videosPath)) {
            logger.error("directory " + videosPath + " is not exist");
            return null;
        }

        File videosDir = new File(videosPath);
        File[] files = videosDir.listFiles();

        if (files == null || files.length == 0) {
            logger.error("no files to test");
            return null;
        }

        List<MotionDetectVideoInfo> detectVideoInfos = new ArrayList<>(files.length);

        for (File file : files) {

            if (file.isDirectory()) {
                continue;
            }

            if (!FileHelper.isFileCorrect(file) || !FileHelper.isVideo(FileHelper.getFileExtension(file.getName()))
                    || GraphicUtils.getVideoDuration(file) == 0) {
                logger.error("incorrect video file: " + file);
                continue;
            }

            logger.info("detecting motion in video file " + file + "...");
            MotionDetectVideoInfo i = detector.detectMotionInVideoFile(file, detectorSettings.getFramesToAnalyze(),
                    detectorSettings.getSensitivity(), detectorSettings.getRegion(),
                    (savedFramesPath != null && savedFramesPath.length() > 0) ? new File(savedFramesPath) : null);
            logger.info("i=" + i);

            detectVideoInfos.add(i);
        }

        if (detectInfoName != null && detectInfoName.length() > 0)
            FileHelper.writeStringToFile(detectVideoInfos.toString(), detectInfoName, videosPath, false);

        return detectVideoInfos;
    }
}

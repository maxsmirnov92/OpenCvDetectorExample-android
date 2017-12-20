package net.maxsmr.opencv.androiddetector.motion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;

import net.maxsmr.commonutils.android.media.MetadataRetriever;
import net.maxsmr.commonutils.data.FileHelper;
import net.maxsmr.commonutils.graphic.GraphicUtils;
import net.maxsmr.opencv.commondetector.model.DetectorSensivity;
import net.maxsmr.opencv.commondetector.model.graphic.Point;
import net.maxsmr.opencv.commondetector.model.motion.info.MotionDetectVideoInfo;
import net.maxsmr.opencv.commondetector.model.motion.settings.MotionDetectorSettings;


public abstract class AbstractMotionDetector {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMotionDetector.class);

    protected static final String SOURCE_FRAME_DIR = "source";
    protected static final String DETECTED_FRAME_DIR = "detected";

    public final static int FRAMES_TO_ANALYZE_COUNT_DEFAULT = 20;

    public static final Scalar CONTOUR_COLOR_DEFAULT = new Scalar(0, 0, 255);

    public static final int CONTOUR_THICKNESS_DEFAULT = 1;

    private Scalar contourColor = CONTOUR_COLOR_DEFAULT;

    private int contourThickness = CONTOUR_THICKNESS_DEFAULT;

    /**
     * @return last frame containing circled motion track if motion has been detected or without otherwise
     */
    public abstract Bitmap getLastFrame();

    protected abstract boolean updateLastFrame(Bitmap frame);

    public int getContourThickness() {
        return contourThickness;
    }

    public void setContourThickness(int thickness) {
        if (thickness >= 1)
            this.contourThickness = thickness;
    }

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
                                                    DetectorSensivity sensitivity, List<Point> region);

    protected abstract void beforeVideoDetect(File videoFile, int framesCount, DetectorSensivity sensitivity);

    protected abstract void afterVideoDetect(File videoFile, int framesCount, DetectorSensivity sensitivity);

    public synchronized MotionDetectVideoInfo detectMotionInVideoFile(File videoFile, int framesCount, DetectorSensivity sensitivity,
                                                                      List<Point> region, File savedFramesDir) {
        logger.debug("detectMotionInVideoFile(), videoFile=" + videoFile + ", framesCount=" + framesCount + ", sensitivity=" + sensitivity
                + ", region=" + ", savedFramesDir=" + savedFramesDir);

        beforeVideoDetect(videoFile, framesCount, sensitivity);

        final long startTime = System.currentTimeMillis();

        final Map<Long, Bitmap> retrievedFrames = MetadataRetriever.extractFrames(videoFile, framesCount <= 1 ? FRAMES_TO_ANALYZE_COUNT_DEFAULT
                : framesCount);

        if (retrievedFrames.isEmpty()) {
            throw new RuntimeException("retrievedFrames is null or empty");
        }

        int extractedFramesCount = 0;

        int detectedFramesCount = 0;
        List<Long> detectedFramesPositions = new ArrayList<Long>();

        for (Map.Entry<Long, Bitmap> currentFrame : retrievedFrames.entrySet()) {

            Bitmap frameBitmap = currentFrame.getValue();

            if (frameBitmap == null) {
                logger.error("video frame at " + currentFrame.getKey() + " ms is null");
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
                logger.debug("saving source frame (position " + currentFrame.getKey() + " ms) to file...");

                GraphicUtils.writeCompressedBitmapToFile(new File(savedFramesDir.getAbsolutePath() + File.separator + videoFile.getName()
                        + File.separator + SOURCE_FRAME_DIR, videoFile.getName() + "_"
                        + currentFrame.getKey() + "_ms"), frameBitmap, Bitmap.CompressFormat.PNG);

                // ArchiveHelper.getInstance().saveImageBitmapToFile(ARCHIVE_MODE.DETECTOR_VIDEO_FRAME, null,
                // Bitmap.CompressFormat.PNG,
                // frameBitmap, videoFile.getName(), currentFrame.getPositionMs());
            }

            if (detectMotionByByteArray(GraphicUtils.getBitmapData(frameBitmap), true, 0, frameBitmap.getWidth(), frameBitmap.getHeight(),
                    sensitivity, region)) {

                logger.info("motion is detected in file " + videoFile.getName() + ", position: " + currentFrame.getKey() + " ms");

                detectedFramesPositions.add(currentFrame.getKey());
                detectedFramesCount++;

                if (savedFramesDir != null) {
                    logger.debug("saving detected frame (position " + currentFrame.getKey() + " ms) to file...");

                    GraphicUtils.writeCompressedBitmapToFile(new File(savedFramesDir.getAbsolutePath() + File.separator + videoFile.getName()
                            + File.separator + DETECTED_FRAME_DIR, videoFile.getName() + "_"
                            + currentFrame.getKey() + "_ms"), getLastFrame(), Bitmap.CompressFormat.PNG);
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
            throw new NullPointerException("detector is null");
        }

        if (detectorSettings == null) {
            throw new NullPointerException("detectorSettings is null");
        }

        if (!FileHelper.isDirExists(videosPath)) {
            throw new RuntimeException("directory " + videosPath + " is not exist");
        }

        File videosDir = new File(videosPath);
        File[] files = videosDir.listFiles();

        if (files == null || files.length == 0) {
            throw new RuntimeException("no files to test");
        }

        List<MotionDetectVideoInfo> detectVideoInfos = new ArrayList<>(files.length);

        for (File file : files) {

            if (file.isDirectory()) {
                continue;
            }

            if (!FileHelper.isFileCorrect(file) || !FileHelper.isVideo(FileHelper.getFileExtension(file.getName()))
                    || MetadataRetriever.extractMediaDuration(file) == 0) {
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
            FileHelper.writeStringToFile(new File(videosPath, detectInfoName), detectVideoInfos.toString(), false);

        return detectVideoInfos;
    }
}

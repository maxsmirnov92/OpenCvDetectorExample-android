package ru.maxsmr.opencv.commondetector.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;

import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.commonutils.graphic.GraphicUtils;


public final class OpenCvUtils {

    private static final Logger logger = LoggerFactory.getLogger(OpenCvUtils.class);

    public static Mat convertByteArrayToMat(byte[] data, int width, int height, int type) {
        logger.debug("convertByteArrayToMat(), data=" + (data != null ? data.length : null) + ", width=" + width + ", height=" + height
                + ", type=" + type);

        if (data == null || data.length == 0) {
            logger.error("data is null or empty");
            return null;
        }

        if (width <= 0 || height <= 0) {
            logger.error("incorrect mat resolution: " + width + "x" + height);
            return null;
        }

        if (type < 0) {
            logger.error("incorrect mat type: " + type);
            return null;
        }

        Mat mat = new Mat(height, width, type);

        try {
            mat.put(0, 0, data);
            // mat.data().put(data);
        } catch (Exception e) {
            logger.error("an Exception occured during put()", e);
            return null;
        }

        return mat;
    }

    public static byte[] convertMatToByteArray(Mat mat) {
        logger.debug("convertMatToByteArray(), mat=" + mat);

        if (mat == null || mat.empty()) {
            logger.error("mat is null or empty");
            return null;
        }

//        if (mat.depth() != 8) {
//            logger.error("incorrect mat depth: " + mat.depth());
//            return null;
//        }

        byte[] data = new byte[(int) (mat.total() * mat.channels())];
        mat.get(0, 0, data);

        return data;
    }

    /**
     * @param b bitmap in configiration ARGB_8888 or RGB_565
     * @return matrix of type CV_8U with same number of channels
     */
    public static Mat convertBitmapToMat(Bitmap b) {

        if (b == null || GraphicUtils.getBitmapByteCount(b) == 0) {
            logger.error("bitmap is null or empty");
            return null;
        }

        Mat m = new Mat();
        Utils.bitmapToMat(b, m);

        // if (b.getConfig() == Bitmap.Config.ARGB_8888 || b.getConfig() == Bitmap.Config.ARGB_4444)
        // m.convertTo(m, CvType.CV_8UC4);
        // else
        // m.convertTo(m, CvType.CV_8UC3);
        //
        if (b.getConfig() == Bitmap.Config.RGB_565) {
            Mat rgb = new Mat();
            Imgproc.cvtColor(m, rgb, Imgproc.COLOR_RGBA2RGB);
            m.release();
            m = rgb;
        }

        return m;
    }

    /**
     * @param mat   source matrix
     * @param alpha create alpha-channel in dst bitmap
     * @return bitmap in ARGB_8888 or RGB_565 configuration
     */
    public static Bitmap convertMatToBitmap(Mat mat, boolean alpha) {

        if (mat == null || mat.empty()) {
            logger.error("mat is null or empty");
            return null;
        }

        Bitmap b = Bitmap.createBitmap(mat.cols(), mat.rows(), alpha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, b);

        return b;
    }

    public static File writeMatToTempFile(Mat mat, String name, Context ctx) {

        if (mat == null || mat.empty()) {
            logger.error("mat is null or empty");
            return null;
        }

        if (name == null || name.length() == 0) {
            logger.error("file name is null or empty");
            return null;
        }

        if (ctx == null) {
            return null;
        }

        File tmpFile = null;

        try {
            tmpFile = File.createTempFile(name, ".png", ctx.getCacheDir());
        } catch (IOException e) {
            logger.error("an IOException occured during createTempFile(): " + e.getMessage());
            return null;
        }

        if (Highgui.imwrite(tmpFile.getAbsolutePath(), mat))
            return tmpFile;
        else {
            if (tmpFile.exists())
                tmpFile.delete();
            return null;
        }
    }

    /**
     * @param img 3 or 4 channels matrix
     */
    public static Mat colorToGray(Mat img) {

        if (img == null || img.empty()) {
            logger.error("image is null or empty");
            return null;
        }

        if (!(img.channels() == 3 || img.channels() == 4)) {
            logger.error("incorrect channels number: " + img.channels());
            return null;
        }

        Mat gray = new Mat(img.size(), CvType.CV_8UC1);

        if (img.channels() == 4)
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGBA2GRAY, 1);
        else
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY, 1);

        return gray;
    }

    public static Mat cropImageByRect(Mat img, int x, int y, int width, int height) {

        if (img == null || img.empty()) {
            logger.error("image is null or empty");
            return null;
        }

        if (x < 0 || y < 0 || width <= 0 || height <= 0) {
            return null;
        }

        return new Mat(img, new Rect(x, y, width, height));
    }

    public static Bitmap cropImageByRect(Bitmap img, int x, int y, int width, int height) {

        if (img == null || GraphicUtils.getBitmapByteCount(img) == 0) {
            logger.error("image is null or empty");
            return null;
        }

        return convertMatToBitmap(cropImageByRect(convertBitmapToMat(img), x, y, width, height), false);
    }

    /**
     * @return 1-channel gray matrix or matrix with same type, depth, color space as original
     */
    public static Mat doMorphology(Mat imgOriginal, boolean grayscale, int kernelSize) {
        logger.debug("doMorphology(), imgOriginal=" + imgOriginal + ", grayscale=" + grayscale + ", kernelSize=" + kernelSize);

        if (imgOriginal == null || imgOriginal.empty()) {
            logger.error("input image is null or empty");
            return null;
        }

        if (kernelSize <= 0) {
            logger.error("incorrect kernel size: " + kernelSize);
            return null;
        }

        Mat temp = null;

        if (grayscale) {
            temp = new Mat(imgOriginal.size(), CvType.CV_8UC1);

            switch (imgOriginal.channels()) {
                case 4:
                    Imgproc.cvtColor(imgOriginal, temp, Imgproc.COLOR_RGBA2GRAY);
                    break;
                case 3:
                    Imgproc.cvtColor(imgOriginal, temp, Imgproc.COLOR_RGB2GRAY);
                    break;
                case 1:
                    temp = imgOriginal.clone();
                    break;
                default:
                    logger.error("incorrect channels number: " + imgOriginal.channels());
                    return null;
            }

        } else {
            temp = imgOriginal.clone();
        }

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(kernelSize, kernelSize));

        // morphological opening (remove small objects from the foreground)
        // Imgproc.erode(temp, temp, kernel);
        // Imgproc.dilate(temp, temp, kernel);
        Imgproc.morphologyEx(temp, temp, Imgproc.MORPH_OPEN, kernel);

        // morphological closing (fill small holes in the foreground)
        // Imgproc.dilate(temp, temp, kernel);
        // Imgproc.erode(temp, temp, kernel);
        Imgproc.morphologyEx(temp, temp, Imgproc.MORPH_CLOSE, kernel);

        return temp;
    }

    // public static Bitmap doMorphology(Bitmap imageBitmap, boolean grayscale, int kernelSize) {
    // return convertMatToBitmap(doMorphology(convertBitmapToMat(imageBitmap), grayscale, kernelSize), false);
    // }

    /**
     * @return 4-channels thresholded mat
     */
    public static Mat thresholdImage(Mat imgOriginal) {
        logger.debug("thresholdImage(), imgOriginal=" + imgOriginal);

        if (imgOriginal == null || imgOriginal.empty()) {
            logger.error("input image is null or empty");
            return null;
        }

        Mat imgGray = new Mat(imgOriginal.size(), CvType.CV_8UC1);

        // logger.debug("converting from ARGB to HSV...");
        // Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_RGB2HSV);

        logger.debug("converting from ARGB to GRAY...");
        Imgproc.cvtColor(imgOriginal, imgGray, Imgproc.COLOR_RGBA2GRAY);

        // MatOfInt fromTo = new MatOfInt();
        // fromTo.setTo(new Scalar(0, 0));
        // logger.debug("fromTo: " + fromTo.toString());

        // List<Mat> imgHSVList = new ArrayList<Mat>();
        // imgHSVList.add(imgHSV);

        // logger.debug("mixing channels...");
        // Core.mixChannels(imgHSVList, imgHSVList, fromTo);

        // logger.debug("applying gaussian blur...");
        // Imgproc.GaussianBlur(imgGray, imgGray, new Size(3, 3), 0);
        // logger.debug("applying median blur...");
        // Imgproc.medianBlur(imgGray, imgGray, 3);

        logger.debug("morphologyEx, divide, normalize...");

        Mat temp = imgGray.clone(); // new Mat();

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        // Imgproc.resize(imgGray, temp, new Size(temp.cols() / 4, temp.rows() / 4));
        Imgproc.morphologyEx(temp, temp, Imgproc.MORPH_CLOSE, kernel);
        // Imgproc.resize(temp, temp, new Size(temp.cols(), temp.rows()));

        Core.divide(imgGray, temp, temp, 1, CvType.CV_32F); // temp will now have type CV_32F
        Core.normalize(temp, imgGray, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);

        Mat imgThresholded = new Mat();

        logger.debug("thresholding image using OTSU...");
        Imgproc.threshold(imgGray, imgThresholded, -1, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // logger.debug("thresholding image using adaptive method...");
        // Imgproc.adaptiveThreshold(imgGray, imgThresholded, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
        // Imgproc.THRESH_BINARY_INV, 5, 4);

        // int iLowH = 0;
        // int iHighH = 179;

        // int iLowS = 0;
        // int iHighS = 200;

        // int iLowV = 0;
        // int iHighV = 200;

        // Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded);

        // imgThresholded = doMorphology(imgThresholded);

        Mat imgResult = new Mat(imgThresholded.size(), CvType.CV_8UC4);
        imgThresholded.copyTo(imgResult);

        // logger.debug("converting from GRAY to ARGB...");
        // Imgproc.cvtColor(imgResult, imgResult, Imgproc.COLOR_GRAY2RGBA, 4);

        return imgThresholded;
    }

    public static Bitmap thresholdImage(Bitmap imageBitmap) {
        return convertMatToBitmap(thresholdImage(convertBitmapToMat(imageBitmap)), false);
    }

    public static Bitmap thresholdImage(File imageFile) {
        logger.debug("thresholdImage(), imageFile=" + imageFile);

        if (!FileHelper.isFileCorrect(imageFile) || !FileHelper.isPicture(FileHelper.getFileExtension(imageFile.getName()))) {
            logger.error("incorrect file: " + imageFile);
            return null;
        }

        // Mat imgOriginal = Highgui.imread(imageFile.getAbsolutePath(), CvType.CV_8U);
        //
        // if (imgOriginal.empty()) {
        // logger.error("input image is empty");
        // return null;
        // }

        return thresholdImage((GraphicUtils.createBitmapFromFile(imageFile, 1)));
    }

    public final static int CANNY_THRESHOLD_MAX = 255;
    public final static int CANNY_THRESHOLD_DEFAULT = 100;
    public final static int CANNY_RATIO_DEFAULT = 2;

    /**
     * @return 4-channels matrix with edges
     */
    public static Mat detectEdges(Mat img, int threshold, int ratio) {
        logger.debug("detectEdges(), img=" + img + ", threshold=" + threshold + ", ratio=" + ratio);

        if (img == null || img.empty()) {
            logger.error("img is null or empty");
            return null;
        }

        if (threshold < 0 || threshold > CANNY_THRESHOLD_MAX) {
            logger.error("incorrect canny threshold value");
            return null;
        }

        if (ratio < 0) {
            logger.error("incorrect canny ratio value");
            return null;
        }

        Mat imgGray = new Mat(img.size(), CvType.CV_8UC1);

        // logger.debug("converting from ARGB to HSV...");
        // Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_RGB2HSV);

        logger.debug("converting from ARGB to GRAY...");
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGBA2GRAY);

        // logger.debug("applying gaussian blur...");
        // Imgproc.GaussianBlur(imgGray, imgGray, new Size(3, 3), 0);

        Mat detectedEdges = new Mat();

        logger.debug("detecting edges using canny...");
        Imgproc.Canny(imgGray, detectedEdges, threshold, threshold * ratio);

        logger.debug("using canny's output as mask...");
        Mat imgResult = new Mat(imgGray.size(), CvType.CV_8UC4);
        imgGray.copyTo(imgResult, detectedEdges);

        // logger.debug("converting from GRAY to ARGB...");
        // Imgproc.cvtColor(imgResult, imgResult, Imgproc.COLOR_GRAY2RGBA, 4);

        return imgResult;
    }

    public static Bitmap detectEdges(Bitmap imageBitmap, int threshold, int ratio) {
        return convertMatToBitmap(detectEdges(convertBitmapToMat(imageBitmap), threshold, ratio), false);
    }

    public static Bitmap detectEdges(File imageFile, int threshold, int ratio) {
        logger.debug("detectEdges(), imageFile=" + imageFile);

        if (!FileHelper.isFileCorrect(imageFile) || !FileHelper.isPicture(FileHelper.getFileExtension(imageFile.getName()))) {
            logger.error("incorrect file: " + imageFile);
            return null;
        }

        // Mat imgOriginal = Highgui.imread(imageFile.getAbsolutePath(), CvType.CV_8U);
        //
        // if (imgOriginal.empty()) {
        // logger.error("input image is empty");
        // return null;
        // }

        return detectEdges((GraphicUtils.createBitmapFromFile(imageFile, 1)), threshold, ratio);
    }

    /**
     * @param img source matrix, will contain drawing contours
     * @return
     */
    public int findContours(Mat img, boolean drawContours, Scalar contourColor) {
        logger.debug("findContours(), img=" + img + ", drawContours=" + drawContours + ", contourColor=" + contourColor);

        if (img == null || img.empty()) {
            logger.error("input image is null or empty");
            return 0;
        }

        List<MatOfPoint> contoursList = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        logger.debug("finding contours...");
        Imgproc.findContours(img, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        logger.debug("detected contours: " + contoursList.size());

        if (drawContours) {

            if (contourColor == null)
                contourColor = COLOR_WHITE;

            logger.debug("drawing contours...");
            // Mat imgResult = new Mat(img.size(), CvType.CV_8UC3);

            for (int i = 0; i < contoursList.size(); i++) {
                Imgproc.drawContours(img, contoursList, i, contourColor, 2, 8, hierarchy, 0, new Point());
            }
        }

        return contoursList.size();
    }

    public int findContours(Bitmap imageBitmap, boolean drawContours, Scalar contourColor) {
        return findContours(convertBitmapToMat(imageBitmap), drawContours, contourColor);
    }

    public final static Scalar COLOR_BLACK = new Scalar(0, 0, 0);
    public final static Scalar COLOR_WHITE = new Scalar(255, 255, 255);

    public static class FontScale {

        public final static double DEFAULT_SCALE_RATIO = 2.0;

        public final static Double[] SCALE_RATIO = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0};

        public int width = 0;
        public int height = 0;
        public Double scaleRatio = DEFAULT_SCALE_RATIO;

        public FontScale(int width, int height, double scaleRatio) {
            this.width = width;
            this.height = height;
            if (Arrays.asList(SCALE_RATIO).contains(scaleRatio)) {
                this.scaleRatio = scaleRatio;
            }
        }

        @Override
        public String toString() {
            return "FontScale [width=" + width + ", height=" + height + ", scaleRatio=" + scaleRatio + "]";
        }

    }

    public static double getFontScaleByImgSize(int width, int height, List<FontScale> fontScaleList) {

        if (fontScaleList == null || fontScaleList.size() == 0) {

            // return FontScale.DEFAULT_SCALE_RATIO;

            fontScaleList = new ArrayList<FontScale>();
            fontScaleList.add(new FontScale(320, 240, 1));
            fontScaleList.add(new FontScale(640, 480, 1.5));
            fontScaleList.add(new FontScale(1024, 768, 2));
            fontScaleList.add(new FontScale(1280, 1024, 2.5));
            fontScaleList.add(new FontScale(1600, 1200, 3));
        }

        FontScale prevScale = null;

        for (FontScale fScale : fontScaleList) {

            if (fScale.width == width && fScale.height == height) {
                return fScale.scaleRatio;
            }

            if (prevScale == null) {
                if (width < fScale.width) { // && height < fScale.heightc
                    return FontScale.SCALE_RATIO[0];
                } else {
                    prevScale = fScale;
                    continue;
                }
            }

            if (prevScale.width > fScale.width) { // && prevScale.height > fScale.height
                prevScale = fScale;
                continue;
            }

            if (width > prevScale.width && width < fScale.width) { // && height > prevScale.height && height <
                // fScale.height
                return fScale.scaleRatio;
            }

            prevScale = fScale;
        }

        return FontScale.DEFAULT_SCALE_RATIO;
    }

    /**
     * @param src            8-bit matrix with 1, 2, 3 channels, grayscale or colored
     * @param grayscale      is source image should be converted to gray color space or not
     * @param filterStrength Parameter regulating filter strength. Big h value perfectly removes noise but also removes
     *                       image details, smaller h value preserves details but also preserves some noise
     * @return result matrix
     */
    public static Mat denoiseImage(Mat src, boolean grayscale, int filterStrength) {
        logger.debug("denoiseImage(), src=" + src + ", grayscale=" + grayscale + ", filterStrength=" + filterStrength);

        if (src == null || src.empty()) {
            logger.error("source image is null or empty");
            return null;
        }

        if (filterStrength < 0) {
            logger.error("incorrect filterStrength: " + filterStrength);
            return null;
        }

        final Mat dst;

        if (grayscale) {
            switch (src.channels()) {
                case 1:
                    dst = new Mat(src.size(), CvType.CV_8UC1);
                    break;
                case 2:
                    dst = new Mat(src.size(), CvType.CV_8UC2);
                    break;
                case 3:
                    dst = new Mat(src.size(), CvType.CV_8UC3);
                    break;
                default:
                    logger.error("incorrect channels number: " + src.channels());
                    dst = new Mat();
                    src.convertTo(dst, CvType.CV_8UC3);
            }
        } else {
            dst = new Mat();
            src.convertTo(dst, CvType.CV_8UC3);
        }

        if (grayscale)
            Photo.fastNlMeansDenoising(src, dst, filterStrength, 7, 21);
        else
            Photo.fastNlMeansDenoisingColored(src, dst, filterStrength, 10, 7, 21);

        return dst;
    }

    /**
     * @param src            bitmap in configiration ARGB_8888 or RGB_565
     * @param grayscale      is source image should be converted to gray color space or not
     * @param filterStrength Parameter regulating filter strength. Big h value perfectly removes noise but also removes
     *                       image details, smaller h value preserves details but also preserves some noise
     * @return result bitmap
     */
    public static Bitmap denoiseImage(Bitmap src, boolean grayscale, int filterStrength) {
        logger.debug("denoiseImage(), src=" + src + ", grayscale=" + grayscale + ", filterStrength=" + filterStrength);

        Mat imgOriginal = convertBitmapToMat(src);

        if (imgOriginal == null)
            return null;

        Mat imgConverted = new Mat(imgOriginal.size(), CvType.CV_8UC3);

        if (!grayscale) {

            if (imgOriginal.channels() == 3) {
                imgOriginal.copyTo(imgConverted);
            } else if (imgOriginal.channels() == 4) {
                Imgproc.cvtColor(imgOriginal, imgConverted, Imgproc.COLOR_RGBA2RGB, 3);
            }

        } else {

            if (imgOriginal.channels() == 3) {
                Imgproc.cvtColor(imgOriginal, imgConverted, Imgproc.COLOR_RGB2GRAY, 1);
            } else if (imgOriginal.channels() == 4) {
                Imgproc.cvtColor(imgOriginal, imgConverted, Imgproc.COLOR_RGBA2GRAY, 1);
            }
        }

        // imgOriginal.convertTo(imgConverted, CvType.CV_8UC3);

        return !imgConverted.empty() ? convertMatToBitmap(denoiseImage(imgConverted, grayscale, filterStrength), false) : null;
    }

}

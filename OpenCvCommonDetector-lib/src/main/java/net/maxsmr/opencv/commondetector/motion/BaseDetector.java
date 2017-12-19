package net.maxsmr.opencv.commondetector.motion;

import net.maxsmr.commonutils.data.FileHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;


public abstract class BaseDetector implements IDetector {

    protected boolean targetDetected = false;

    @Override
    public synchronized Mat detect(CvCameraViewFrame frame) {
        return detect(frame.rgba(), null);
    }

    @Override
    public boolean isDetected() {
        return targetDetected;
    }

    protected int contourThickness = 1;

    @Override
    public void setContourThickness(int thickness) {
        if (thickness >= 1)
            this.contourThickness = thickness;
    }

    protected Scalar contourColor = new Scalar(255, 0, 0);

    @Override
    public void setContourColor(Scalar color) {
        if (color != null)
            this.contourColor = color;
    }

    public static final boolean DEFAULT_GRAYSCALE = true;
    private boolean grayscale = DEFAULT_GRAYSCALE;

    @Override
    public boolean grayscale() {
        return grayscale;
    }

    public void setGrayscale(boolean toggle) {
        this.grayscale = toggle;
    }

    public static final int DEFAULT_MORPH_KERNEL_SIZE = 5;
    private int morphKernelSize = DEFAULT_MORPH_KERNEL_SIZE;

    @Override
    public int getMorphKernelSize() {
        return morphKernelSize;
    }

    public void setMorphKernelSize(int morphKernelSize) {
        if (morphKernelSize >= 0)
            this.morphKernelSize = morphKernelSize;
        else
            throw new IllegalArgumentException("incorrect morphKernelSize parameter: " + morphKernelSize);
    }

    protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss.SSS", Locale.getDefault());

    protected static final String SOURCE_FRAMES_DIR = "source";
    protected static final String PRE_PROCESSED_FRAMES_DIR = "pre-processed";
    protected static final String DETECTED_FRAMES_DIR = "detected";

    private File savedFramesDir;

    @Override
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
}

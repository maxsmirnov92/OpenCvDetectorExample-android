package ru.maxsmr.opencv.commondetector.model.motion.info;

import ru.maxsmr.opencv.commondetector.model.IDetectVideoInfo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MotionDetectVideoInfo implements Serializable, IDetectVideoInfo {

    private static final long serialVersionUID = 4336083442242749614L;

    private File videoFile;

    @Override
    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    private boolean detected;

    @Override
    public boolean detected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    private double ratio;

    @Override
    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    private List<Long> positions = new ArrayList<Long>();

    @Override
    public List<Long> getPositions() {
        return positions;
    }

    public boolean addPosition(Long p) {
        return positions != null ? positions.add(p) : false;
    }

    public boolean removePosition(Long p) {
        return positions != null ? positions.remove(p) : false;
    }

    public void setPositions(List<Long> positions) {
        this.positions = positions;
    }


    private long processingTime;

    @Override
    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public MotionDetectVideoInfo() {
    }

    public MotionDetectVideoInfo(File videoFile, boolean detected, double ratio, List<Long> positions, long processingTime) {
        this.videoFile = videoFile;
        this.detected = detected;
        this.ratio = ratio;
        this.positions = positions;
        this.processingTime = processingTime >= 0 ? processingTime : 0;
    }

    @Override
    public String toString() {
        return "MotionDetectVideoInfo [videoFile=" + videoFile + ", detected=" + detected + ", ratio=" + ratio + ", positions=" + positions
                + ", processingTime=" + processingTime + "]";
    }

}

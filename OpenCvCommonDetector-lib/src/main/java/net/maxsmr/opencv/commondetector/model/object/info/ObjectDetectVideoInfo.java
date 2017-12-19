package net.maxsmr.opencv.commondetector.model.object.info;

import net.maxsmr.opencv.commondetector.model.IDetectVideoInfo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetectVideoInfo implements Serializable, IDetectVideoInfo {

    private static final long serialVersionUID = 3457325079562388143L;

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


    private List<Long> positions = new ArrayList<>();

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

    public void clearPositions() {
        if (positions != null)
            positions.clear();
    }

    public void setPositions(List<Long> positions) {
        this.positions = positions;
    }

    private List<ObjectDetectFrameInfo> frameInfos = new ArrayList<ObjectDetectFrameInfo>();

    public List<ObjectDetectFrameInfo> getFrameInfos() {
        return frameInfos;
    }

    public void setFrameInfos(List<ObjectDetectFrameInfo> frameInfos) {
        this.frameInfos = frameInfos;
    }

    public boolean addObjectDetectFrameInfo(ObjectDetectFrameInfo info) {
        return frameInfos != null ? frameInfos.add(info) : false;
    }

    public boolean removeObjectDetectFrameInfo(ObjectDetectFrameInfo info) {
        return frameInfos != null ? frameInfos.remove(info) : false;
    }

    public void clearObjectDetectFrameInfo() {
        if (frameInfos != null)
            frameInfos.clear();
    }


    private long processingTime;

    @Override
    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public ObjectDetectVideoInfo() {
    }

    public ObjectDetectVideoInfo(File videoFile, boolean detected, double ratio, List<Long> positions,
                                 List<ObjectDetectFrameInfo> frameInfos, long processingTime) {
        this.videoFile = videoFile;
        this.detected = detected;
        this.ratio = ratio;
        this.positions = positions;
        this.frameInfos = frameInfos;
        this.processingTime = processingTime >= 0 ? processingTime : 0;
    }

    @Override
    public String toString() {
        return "ObjectDetectVideoInfo [videoFile=" + videoFile + ", detected=" + detected + ", ratio=" + ratio + ", positions=" + positions
                + ", frameInfos=" + frameInfos + ", processingTime=" + processingTime + "]";
    }

}

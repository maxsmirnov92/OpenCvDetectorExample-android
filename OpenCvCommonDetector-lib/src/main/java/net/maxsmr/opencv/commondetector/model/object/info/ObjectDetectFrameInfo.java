package net.maxsmr.opencv.commondetector.model.object.info;

import net.maxsmr.opencv.commondetector.model.graphic.Rect;
import net.maxsmr.opencv.commondetector.model.object.settings.ObjectType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetectFrameInfo implements Serializable {

	private static final long serialVersionUID = 4413628575426854940L;

	private byte[] sceneImage;

	/** @return Mat data with given size and type */
	public byte[] getSceneImage() {
		return sceneImage;
	}

	public void setSceneImage(byte[] sceneImage) {
		this.sceneImage = sceneImage;
	}

	private int type;

	/** @return type used for restoring Mat by byte array */
	public int getType() {
		return type;
	}

	public void setType(int type) {
		if (type > 0)
			this.type = type;
	}

	
	private int width = 0;

	public int getWidth() {
		return width;
	}

	
	private int height = 0;

	public int getHeight() {
		return height;
	}

	public void setSize(int width, int height) {
		if (width >= 0 && height >= 0) {
			this.width = width;
			this.height = height;
		}
	}

	
	private boolean detected;

	public boolean detected() {
		return detected;
	}

	public void setDetected(boolean detected) {
		this.detected = detected;
	}

	
	private ObjectType objectType = ObjectType.UNKNOWN;

	public ObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(ObjectType objectType) {
		if (objectType != null)
			this.objectType = objectType;
	}

	
	private List<Rect> objects = new ArrayList<Rect>();

	public List<Rect> getObjects() {
		return objects;
	}

	public boolean addObject(Rect r) {
		return objects != null ? objects.add(r) : false;
	}

	public boolean removeObject(Rect r) {
		return objects != null ? objects.remove(r) : false;
	}

	public void clearObjects() {
		if (objects != null)
			objects.clear();
	}

	public void setObjects(List<Rect> objects) {
		this.objects = objects;
	}

	
	private long processingTime;

	public long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}

	public ObjectDetectFrameInfo() {
	}

	public ObjectDetectFrameInfo(byte[] sceneImage, int type, int width, int height, boolean detected, ObjectType objectType,
			List<Rect> objects, long processingTime) {
		setSceneImage(sceneImage);
		setType(type);
		setSize(width, height);
		setDetected(detected);
		setObjectType(objectType);
		setObjects(objects);
		setProcessingTime(processingTime);
	}

	@Override
	public String toString() {
		return "ObjectDetectFrameInfo [sceneImage (length)=" + (sceneImage != null ? sceneImage.length : 0) + ", type=" + type + ", width="
				+ width + ", height=" + height + ", detected=" + detected + ", objectType=" + objectType + ", objects=" + objects
				+ ", processingTime=" + processingTime + "]";
	}

}

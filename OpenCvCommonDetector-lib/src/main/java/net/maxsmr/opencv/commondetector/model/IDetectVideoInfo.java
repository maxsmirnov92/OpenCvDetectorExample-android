package net.maxsmr.opencv.commondetector.model;

import java.io.File;
import java.util.List;

public interface IDetectVideoInfo {

	File getVideoFile();

	boolean detected();

	/** @return ratio detected frames count to total analyzed frames count */
	double getRatio();

	/** @return positions in ms of detected frames */
	List<Long> getPositions();

	long getProcessingTime();

}

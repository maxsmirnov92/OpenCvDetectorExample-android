package ru.maxsmr.opencv.commondetector.model;

public enum DETECTOR_SENSITIVITY {

	NONE(-1),

	HIGH(2),

	MEDIUM(1),

	LOW(0);

	private final int value;

	DETECTOR_SENSITIVITY(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static DETECTOR_SENSITIVITY fromNativeValue(int value) throws IllegalArgumentException {

		switch (value) {
		case -1:
			return NONE;
		case 2:
			return HIGH;
		case 1:
			return MEDIUM;
		case 0:
			return LOW;
		default:
			throw new IllegalArgumentException("Incorrect native value for enum type " + DETECTOR_SENSITIVITY.class.getName() + ": "
					+ value);
		}
	}

}

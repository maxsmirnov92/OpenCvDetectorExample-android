package net.maxsmr.opencv.commondetector.model;

public enum DetectorSensivity {

	NONE(-1),

	HIGH(2),

	MEDIUM(1),

	LOW(0);

	private final int value;

	DetectorSensivity(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static DetectorSensivity fromNativeValue(int value) throws IllegalArgumentException {

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
			throw new IllegalArgumentException("Incorrect native value for enum type " + DetectorSensivity.class.getName() + ": "
					+ value);
		}
	}

}

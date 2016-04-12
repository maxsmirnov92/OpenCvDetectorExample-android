package ru.maxsmr.opencv.androiddetector.object.cascade.classifiers;

import java.io.File;

import android.content.Context;

public enum CAR_CLASSIFIER {

	CAR_CHECK("lbpcascade_car_check.xml"), CAR1("lbpcascade_car_1.xml"), CAR2("lbpcascade_car_2.xml"), CAR3("lbpcascade_car_3.xml"), CAR4(
			"lbpcascade_car_4.xml");

	private final String name;

	CAR_CLASSIFIER(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public File getDestFile(Context ctx) {
		return new File(ctx.getFilesDir(), this.getName());
	}
	public static CAR_CLASSIFIER fromName(String name) {
		for (CAR_CLASSIFIER e : CAR_CLASSIFIER.values()) {
			if (e.getName().equals(name))
				return e;
		}
		throw new IllegalArgumentException("Incorrect name " + name + " for enum type" + CAR_CLASSIFIER.class.getName());
	}
}

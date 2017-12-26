package net.maxsmr.opencv.commondetector.model.object.settings;

public enum ObjectType {

    UNKNOWN(-1), CAR(0), HUMAN(1), FACE(2);

    private final int id;

    public int getId() {
        return id;
    }

    private ObjectType(int id) {
        this.id = id;
    }

    public static ObjectType fromNativeValue(int value) throws IllegalArgumentException {

        for (ObjectType objectType : ObjectType.values()) {
            if (objectType.getId() == value) {
                return objectType;
            }
        }

        throw new IllegalArgumentException("Incorrect native value for enum type " + ObjectType.class.getName() + ": " + value);
    }

}

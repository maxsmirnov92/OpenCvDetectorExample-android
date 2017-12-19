package net.maxsmr.opencv.commondetector.model.object.settings;

public enum OBJECT_TYPE {

    UNKNOWN(-1), CAR(0), HUMAN(1), FACE(2);

    private final int id;

    public int getId() {
        return id;
    }

    private OBJECT_TYPE(int id) {
        this.id = id;
    }

    public static OBJECT_TYPE fromNativeValue(int value) throws IllegalArgumentException {

        for (OBJECT_TYPE objectType : OBJECT_TYPE.values()) {
            if (objectType.getId() == value) {
                return objectType;
            }
        }

        throw new IllegalArgumentException("Incorrect native value for enum type " + OBJECT_TYPE.class.getName() + ": " + value);
    }

}

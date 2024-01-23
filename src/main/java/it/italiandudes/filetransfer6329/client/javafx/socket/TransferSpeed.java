package it.italiandudes.filetransfer6329.client.javafx.socket;

import org.jetbrains.annotations.NotNull;

public final class TransferSpeed {

    // Attributes
    private final int speed;
    @NotNull final SpeedOrderMagnitude magnitude;

    // Constructors
    public TransferSpeed(final int speed, @NotNull final SpeedOrderMagnitude magnitude) {
        this.speed = speed;
        this.magnitude = magnitude;
    }

    // Methods
    public int getSpeed() {
        return speed;
    }
    @NotNull
    public SpeedOrderMagnitude getMagnitude() {
        return magnitude;
    }
    public int getTransferSpeedBytes() {
        return speed * magnitude.getBytes();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransferSpeed)) return false;

        TransferSpeed that = (TransferSpeed) o;

        if (getSpeed() != that.getSpeed()) return false;
        return getMagnitude() == that.getMagnitude();
    }
    @Override
    public int hashCode() {
        int result = getSpeed();
        result = 31 * result + getMagnitude().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TransferSpeed{" +
                "speed=" + speed +
                ", magnitude=" + magnitude +
                '}';
    }
}

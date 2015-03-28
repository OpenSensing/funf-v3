package edu.mit.media.funf.probe.builtin;

import android.hardware.Sensor;
import edu.mit.media.funf.probe.BatchedSensorProbe;

public class BatchedAccelerometerSensorProbe extends BatchedSensorProbe implements ProbeKeys.AccelerometerSensorKeys {
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    public String[] getRequiredFeatures() {
        return new String[]{
                "android.hardware.sensor.accelerometer"
        };
    }


    public String[] getValueNames() {
        return new String[] {
                X, Y, Z
        };
    }
}

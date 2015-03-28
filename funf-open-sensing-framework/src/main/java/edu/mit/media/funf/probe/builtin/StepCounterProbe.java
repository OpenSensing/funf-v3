package edu.mit.media.funf.probe.builtin;

import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import edu.mit.media.funf.probe.SensorProbe;

public class StepCounterProbe extends SensorProbe {
    @Override
    public int getSensorType() {
        return Sensor.TYPE_STEP_COUNTER;
    }

    @Override
    public String[] getValueNames() {
        return new String[] {
                "STEP_COUNT"
        };
    }

    @Override
    public String[] getRequiredFeatures() {
        return new String[]{
                "android.hardware.sensor.step_counter"
        };
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        getSensorManager().registerListener(sensorListener, sensor, getSensorDelay(null));

    }

    @Override
    public void onRun(Bundle params) {
        sendProbeData();
    }

    @Override
    public void onStop() {
        if (!recentEvents.isEmpty()) {
            sendProbeData();
        }
    }

    @Override
    protected void onDisable() {
        getSensorManager().unregisterListener(sensorListener);
    }
}

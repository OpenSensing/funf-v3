package edu.mit.media.funf.probe;

import android.os.Bundle;
import android.util.Log;
import edu.mit.media.funf.probe.SensorProbe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BatchedSensorProbe extends SensorProbe {

    public static final int BATCH_PERIOD = 1000000 * 60 * 5;

    @Override
    protected void onEnable() {
        super.onEnable();
        boolean batchMode = getSensorManager().registerListener(sensorListener, sensor, getSensorDelay(null), BATCH_PERIOD);
        if(batchMode) {
            Log.i(TAG, "Started sensor listener in batched mode");
        } else {
            Log.i(TAG, "Started sensor listener in normal mode");
        }

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

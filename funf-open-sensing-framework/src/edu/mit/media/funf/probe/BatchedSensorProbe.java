package edu.mit.media.funf.probe;

import android.os.Bundle;
import android.util.Log;
import edu.mit.media.funf.probe.SensorProbe;

public abstract class BatchedSensorProbe extends SensorProbe {

    @Override
    protected void onEnable() {
        super.onEnable();
        boolean batchMode = getSensorManager().registerListener(sensorListener, sensor, getSensorDelay(null), 1000000 * 60 * 5);
        Log.i("Bached sensor probe", "Batch mode: " + Boolean.toString(batchMode));
    }

    @Override
    public void onRun(Bundle params) {
        Log.i("Bached sensor probehf", "RecentEvents on run:" + recentEvents.size());
        sendProbeData();
    }

    @Override
    public void onStop() {
        Log.i("Bached sensor probe", "RecentEvents on stop:" + recentEvents.size());
        if (!recentEvents.isEmpty()) {
            sendProbeData();
        }
    }

    @Override
    protected void onDisable() {
        getSensorManager().unregisterListener(sensorListener);
    }
}

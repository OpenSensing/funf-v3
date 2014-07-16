package edu.mit.media.funf.probe.builtin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import edu.mit.media.funf.Utils;
import edu.mit.media.funf.configured.ConfiguredPipeline;
import edu.mit.media.funf.configured.FunfConfig;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.ProbeScheduler;
import edu.mit.media.funf.probe.SensorProbe;

/**
 * Created by arks on 15/07/14.
 */
public class EpidemicProbe extends Probe implements ProbeKeys.EpidemicsKeys {



    private static final String DELEGATE_PROBE_NAME = BluetoothProbe.class.getName();
    private static final String OWN_NAME = "edu.mit.media.funf.probe.builtin.EpidemicProbe";
    private String tempType;
    private Handler handler;
    private Epidemic epidemic = null;

    private String EPI_TAG = "EPI_TAG";
    private long startTime;




    @Override
    protected void onDisable() {
        // Nothing
    }

    @Override
    public void sendProbeData() {
        Bundle data = new Bundle();
        data.putString(TYPE, tempType);
        sendProbeData(startTime, data); // Timestamp already in seconds
    }

    @Override
    public void onRun(Bundle params) {
        // Nothing
    }

    @Override
    public String[] getRequiredPermissions() {
        //TODO maybe?
        return new String[]{};

    }

    @Override
    protected void onEnable() {
        Log.d(EPI_TAG, "HERE! 1iii");
        // Nothing
    }

    @Override
    public void onStop() {
        Log.d(EPI_TAG, "HERE! 1ppap");
        // Nothing
    }

    @Override
    public String[] getRequiredFeatures() {
        //TODO
        return new String[]{};
    }


    @Override
    public Parameter[] getAvailableParameters() {
        return new Parameter[] {
        };
    }

    @Override
    protected ProbeScheduler getScheduler() {
        return new DelegateProbeScheduler(BluetoothProbe.class);
    }

    @Override
    protected void onHandleCustomIntent(Intent intent) {

        if (Probe.ACTION_DATA.equals(intent.getAction()) && DELEGATE_PROBE_NAME.equals(intent.getStringExtra(PROBE))) {
            Log.d(EPI_TAG, "HERE! 1");
            if (handler == null) {
                handler = new Handler(); // Make sure handler is created on message thread
            }
            if (epidemic == null) {
                epidemic= new Epidemic();
            }
            epidemic.handleBluetoothData(intent.getExtras());

        }
    }

    private class Epidemic {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        private String INFECTED_TAG = "ex";


        public void handleBluetoothData(Bundle data) {

            long timestamp = data.getLong(TIMESTAMP, 0L);
            HashMap<String, String> devices = bundleToHash(data);
            Log.d(EPI_TAG, devices.toString());
            //Log.d(EPI_TAG, PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getAll().toString());

            JSONObject prefs = new JSONObject();
            JSONArray probeConfig = new JSONArray();

            try  {
                prefs = new JSONObject(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("raw_json_main_config_for_experience_sampling", null));
                JSONObject dataRequests = prefs.getJSONObject("dataRequests");
                Log.d(EPI_TAG, dataRequests.toString());
                Log.d(EPI_TAG, OWN_NAME);
                probeConfig = dataRequests.getJSONArray(OWN_NAME);
            }
            catch (JSONException e) {
                Log.d(EPI_TAG, "CONFIG ERRROR!");
            }

            Log.d(EPI_TAG, "----> "+probeConfig.toString());


            //FunfConfig fc = FunfConfig.getInstance(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
            //Log.d(EPI_TAG, fc.getName());



        }

        private HashMap<String, String> bundleToHash(Bundle data){
            HashMap<String, String> devices = new HashMap<String, String>();

            for (Parcelable device: data.getParcelableArrayList("DEVICES") ) {
                String address = ((BluetoothDevice)((Bundle) device).get(BluetoothDevice.EXTRA_DEVICE)).getAddress();
                String name = ((BluetoothDevice)((Bundle) device).get(BluetoothDevice.EXTRA_DEVICE)).getName();
                devices.put(address, name);
            }

            return devices;
        }


        private void setInfectedName() {
            String currentName = mBluetoothAdapter.getName();
            if (currentName.endsWith(INFECTED_TAG)) return;
            mBluetoothAdapter.setName(currentName+INFECTED_TAG);

        }




    }

}

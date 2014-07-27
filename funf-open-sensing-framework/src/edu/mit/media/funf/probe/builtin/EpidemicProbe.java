package edu.mit.media.funf.probe.builtin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.mit.media.funf.R;
import edu.mit.media.funf.Utils;
import edu.mit.media.funf.configured.ConfiguredPipeline;
import edu.mit.media.funf.configured.FunfConfig;
import edu.mit.media.funf.probe.CursorCell;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.ProbeScheduler;
import edu.mit.media.funf.probe.SensorProbe;

import edu.mit.media.funf.probe.CursorCell;
import edu.mit.media.funf.probe.DatedContentProviderProbe;
import edu.mit.media.funf.probe.edu.mit.media.funf.activity.EpiDescriptionActivity;
import edu.mit.media.funf.probe.edu.mit.media.funf.activity.EpiStateActivity;

/**
 * Created by arks on 15/07/14.
 */
public class EpidemicProbe extends Probe implements ProbeKeys.EpidemicsKeys {



    private static final String DELEGATE_PROBE_NAME = BluetoothProbe.class.getName();
    public static final String OWN_NAME = "edu.mit.media.funf.probe.builtin.EpidemicProbe";
    private Handler handler;
    private Epidemic epidemic = null;

    public static final String EPI_TAG = "EPI_TAG";
    public static final String EPI_DIALOG_PREF_PREFIX = "epi_dialog_";

    private enum SelfState { S, E, I, R, V};

    private boolean firstRun = true;
    private Bundle runData = null;



    @Override
    protected void onDisable() {
        // Nothing
    }

    @Override
    protected String getDisplayName() {
        return "Epidemic Probe";
    }

    @Override
    public void sendProbeData() {

        Log.d(EPI_TAG, "sending data");
        runData.putString(TYPE, OWN_NAME);
        Log.d(EPI_TAG, runData.toString());
        sendProbeData(System.currentTimeMillis() / 1000, runData);
    }

    @Override
    public void onRun(Bundle params) {
        Log.d(EPI_TAG, "onRun "+params.toString());
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
                new Parameter(Parameter.Builtin.PERIOD, 300L),
                new Parameter(Parameter.Builtin.START, 0L),
                new Parameter(Parameter.Builtin.END, 0L)
        };
    }

    @Override
    protected ProbeScheduler getScheduler() {
        return new DelegateProbeScheduler(BluetoothProbe.class);
    }

    @Override
    protected void onHandleCustomIntent(Intent intent) {

        if (Probe.ACTION_DATA.equals(intent.getAction()) && DELEGATE_PROBE_NAME.equals(intent.getStringExtra(PROBE))) {
            Log.d(EPI_TAG, "HERE! 1 "+intent.toString());
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
        private String INFECTED_TAG = "00xbad1dea";
        private Float INFECTION_PROBABILITY = 0.0f;
        private Long EXPOSED_DURATION = 30 * 60 * 1000l;
        private Long RECOVERED_DURATION = 60 * 60 * 1000l;
        private String WAVE = "";

        private HashMap<Long, String> WAVES = null;


        private long SCAN_DELTA = 250 * 1000;
        private long TIME_LIMIT = 12 * 60 * 60 * 1000;
        private SelfState selfState = SelfState.S;


        private boolean SILENT_NIGHT = true;
        private float POPUP_PROBABILITY = 0.0f;
        private float VIBRATE_PROBABILITY = 0.0f;

        private SelfState STATE_AFTER_INFECTED = SelfState.R;
        private boolean HIDDEN_MODE = true; //don't show any dialogs
        private boolean SHOW_WELCOME_DIALOG = false;


        public void handleBluetoothData(Bundle data) {

            //TODO add global try catch here, just in case


            showDescription();


            runData = new Bundle();


            firstRun = checkFirstRun();
            if (firstRun) saveDefaultName();
            parseConfig();
            getCurrentState();
            setWaveFromConfig();

            HashMap<String, String> scanResults = bundleToHash(data);

            Log.d(EPI_TAG, selfState.toString() + " " + scanResults.toString());


            if (selfState.equals(SelfState.E)) {
                setSusceptibleName();
                infect();
            } else if (selfState.equals(SelfState.I)) {
                setInfectedName(); //just in case
                recover();
            } else if (selfState.equals(SelfState.V)) {
                setSusceptibleName(); //just in case
            } else if (selfState.equals(SelfState.S)) {


                if (deltaSufficient(scanResults.size())) {


                    for (String device_id : scanResults.keySet()) {
                        if (scanResults.get(device_id) == null) continue;
                        if (isInfection(device_id, scanResults.get(device_id))) {
                            setExposed(device_id, scanResults.get(device_id));
                            break;
                        }

                    }
                }

                if (selfState.equals(SelfState.S)) {
                    setSusceptibleName(); //just in case
                }

            }

            runData.putString("name", mBluetoothAdapter.getName());
            runData.putString("self_state", selfState.toString());

            showSymptoms();

            sendProbeData();

            setBluetoothDiscoverable();

        }

        private void infect() {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            Long toInfectTime = settings.getLong("to_infect_time", 0L);
            String infectingDevice = settings.getString("infecting_device", "");
            String infectingName = settings.getString("infecting_name", "");
            if (toInfectTime == 0) return;

            if (System.currentTimeMillis() >= toInfectTime) {
                setInfected(infectingDevice, infectingName);
            }

        }


        private Long calculateToInfectTime() {
            Long toInfectTime = System.currentTimeMillis() + EXPOSED_DURATION;
            return toInfectTime;
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


        private void recover() {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            Long toRecoverTime = settings.getLong("to_recover_time", 0L);
            Log.d(EPI_TAG, "Trying to recover! "+System.currentTimeMillis() + " "+toRecoverTime+" "+STATE_AFTER_INFECTED);
            if (toRecoverTime == 0) return;
            if (System.currentTimeMillis() >= toRecoverTime) {
               setRecovered();
            }

        }

        private Long calculateToRecoverTime() {
            Long toRecoverTime = System.currentTimeMillis() + RECOVERED_DURATION;
            return  toRecoverTime;
        }


        private void getCurrentState() {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            String state = settings.getString("self_state", "S");
            if (state.equals("S")) selfState = SelfState.S;
            if (state.equals("I")) selfState = SelfState.I;
            if (state.equals("V")) selfState = SelfState.V;
            if (state.equals("E")) selfState = SelfState.E;
            if (state.equals("R")) selfState = SelfState.R;

        }

        private void setCurrentState(SelfState state) {
            selfState = state;
            if (state.equals(SelfState.S)) saveLocalSharedPreference("self_state", "S");
            if (state.equals(SelfState.I)) saveLocalSharedPreference("self_state", "I");
            if (state.equals(SelfState.V)) saveLocalSharedPreference("self_state", "V");
            if (state.equals(SelfState.E)) saveLocalSharedPreference("self_state", "E");
            if (state.equals(SelfState.R)) saveLocalSharedPreference("self_state", "R");

        }


        private boolean deltaSufficient(int scanResultSize) {
            if (System.currentTimeMillis() - getLastScan() <= SCAN_DELTA) return false;
            setLastScan(scanResultSize);
            return true;
        }

        private void setLastScan(int scanResultSize) {
            if (scanResultSize > 0 && (System.currentTimeMillis() - getLastScan()) > SCAN_DELTA) {
                saveLocalSharedPreference("last_scan", System.currentTimeMillis());
            }
        }

        private long getLastScan() {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            return settings.getLong("last_scan", 0);
        }




        private boolean isInfection(String device_id, String device_name) {
            if (! device_name.endsWith(INFECTED_TAG)) return false;
            if (Math.random() > INFECTION_PROBABILITY) return false;
            return true;
        }

        private void setInfected(String device_id, String device_name) {
            if (selfState.equals(SelfState.I)) return;
            Log.i(EPI_TAG, "infected! " + device_id + " " + device_name);
            runData.putString("state", "infected_" + device_id + "_" + device_name);
            saveLocalSharedPreference("to_infect_time", 0l);
            saveLocalSharedPreference("to_recover_time", calculateToRecoverTime());
            saveLocalSharedPreference("infecting_device", "");
            saveLocalSharedPreference("infecting_name", "");
            setCurrentState(SelfState.I);
            setInfectedName();
        }

        private void setSusceptible() {
            if (selfState.equals(SelfState.S)) return;
            Log.i(EPI_TAG, "susceptible!");
            runData.putString("state", "susceptible");
            saveLocalSharedPreference("to_infect_time", 0l);
            saveLocalSharedPreference("to_recover_time", 0l);
            saveLocalSharedPreference("infecting_device", "");
            saveLocalSharedPreference("infecting_name", "");
            setCurrentState(SelfState.S);
            setSusceptibleName();
        }

        private void setVaccinated() {
            if (selfState.equals(SelfState.V)) return;
            Log.i(EPI_TAG, "vaccinated!");
            runData.putString("state", "vaccinated");
            saveLocalSharedPreference("to_infect_time", 0l);
            saveLocalSharedPreference("to_recover_time", 0l);
            saveLocalSharedPreference("infecting_device", "");
            saveLocalSharedPreference("infecting_name", "");
            setCurrentState(SelfState.V);
            setSusceptibleName();
        }

        private void setExposed(String device_id, String device_name) {
            if (selfState.equals(SelfState.E)) return;
            Log.i(EPI_TAG, "exposed! "+device_id+" "+device_name);
            runData.putString("state", "exposed_" + device_id + "_" + device_name);
            saveLocalSharedPreference("to_infect_time", calculateToInfectTime());
            saveLocalSharedPreference("to_recover_time", 0l);
            saveLocalSharedPreference("infecting_device", device_id);
            saveLocalSharedPreference("infecting_name", device_name);
            setCurrentState(SelfState.E);
            setInfectedName();
        }

        private void setRecovered() {
            if (selfState.equals(SelfState.R)) return;
            Log.i(EPI_TAG, "recovered!");
            runData.putString("state", "recovered");
            saveLocalSharedPreference("to_infect_time", 0l);
            saveLocalSharedPreference("to_recover_time", 0l);
            saveLocalSharedPreference("infecting_device", "");
            saveLocalSharedPreference("infecting_name", "");
            setCurrentState(STATE_AFTER_INFECTED);
            setSusceptibleName();
        }

        private void setInfectedName() {
            String currentName = mBluetoothAdapter.getName();
            if (currentName.endsWith(INFECTED_TAG)) return;
            mBluetoothAdapter.setName(getDefaultName()+INFECTED_TAG);
            runData.putString("name", mBluetoothAdapter.getName());

        }

        private void saveDefaultName() {
            String defaultName = mBluetoothAdapter.getName();
            saveLocalSharedPreference("default_name", defaultName);
        }

        private String getDefaultName() {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            return settings.getString("default_name", "");
        }



        private void setSusceptibleName() {
            mBluetoothAdapter.setName(getDefaultName());
            runData.putString("name", mBluetoothAdapter.getName());
        }


        private void parseConfig() {

            JSONObject prefs = new JSONObject();
            JSONObject probeConfig = new JSONObject();

            try  {
                //TODO ugly way to get the config, because the probe needs to know about the name of the config; how to do this better?
                prefs = new JSONObject(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("raw_json_main_config_for_experience_sampling", null));
                JSONObject dataRequests = prefs.getJSONObject("dataRequests");
                Log.d(EPI_TAG, dataRequests.toString());
                Log.d(EPI_TAG, OWN_NAME);
                probeConfig = dataRequests.getJSONArray(OWN_NAME).getJSONObject(0);
            }
            catch (JSONException e) {
                Log.d(EPI_TAG, "CONFIG ERRROR!");
                return;
            }

            Log.d(EPI_TAG, "----> "+probeConfig.toString());

            try {

                WAVES = new HashMap<Long, String>();

                String[] temp_tags = probeConfig.getString("WAVES").split(";");
                for (String state: temp_tags) {
                    try {
                        Long timestamp = Long.parseLong(state.split("!")[0]);
                        Long epoch = 1405794046 * 1000L;
                        String s = state.split("!")[1];
                        if (timestamp < epoch) {
                            timestamp = timestamp * 1000; //timestamp is in seconds, convert to mili
                        }

                        WAVES.put(timestamp, s);
                    }
                    catch (NumberFormatException e) {}
                }

            }
            catch (JSONException e) {}

            try {
                SCAN_DELTA = probeConfig.getInt("SCAN_DELTA") * 1000;
            }
            catch (JSONException e) {}

            try {
                TIME_LIMIT = probeConfig.getInt("TIME_LIMIT") * 60 * 1000;
            }
            catch (JSONException e) {}

            try {
                VIBRATE_PROBABILITY = (float)probeConfig.getDouble("VIBRATE_PROBABILITY");
            }
            catch (JSONException e) {}

            try {
                POPUP_PROBABILITY = (float)probeConfig.getDouble("POPUP_PROBABILITY");
            }
            catch (JSONException e) {}

            try {
                SILENT_NIGHT = probeConfig.getBoolean("SILENT_NIGHT");
            }
            catch (JSONException e) {}

            try {
                HIDDEN_MODE = probeConfig.getBoolean("HIDDEN_MODE");
            }
            catch (JSONException e) {}

            try {
                SHOW_WELCOME_DIALOG = probeConfig.getBoolean("SHOW_WELCOME_DIALOG");
            }
            catch (JSONException e) {}

        }


        private boolean checkFirstRun() {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            boolean firstRun = settings.getBoolean("first_run", true);
            saveLocalSharedPreference("first_run", false);
            runData.putBoolean("first_run", firstRun);
            runData.putString("default_name", getDefaultName());
            return firstRun;


        }


        private void saveLocalSharedPreference(String key, String value) {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(key, value);
            editor.commit();
        }

        private void saveLocalSharedPreference(String key, float value) {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat(key, value);
            editor.commit();
        }


        private void saveLocalSharedPreference(String key, long value) {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(key, value);
            editor.commit();
        }

        private void saveLocalSharedPreference(String key, boolean value) {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(key, value);
            editor.commit();
        }

        private ArrayList<Long> getConsumed(String tag) {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            String[] tempString = settings.getString(tag, "").split(";");
            ArrayList<Long> values = new ArrayList<Long>();
            for (String t: tempString) {
                try {
                    values.add(Long.parseLong(t));
                }
                catch (NumberFormatException e) {}
            }
            return values;

        }


        private void setConsumed(String tag, ArrayList<Long> timestamps) {
            saveLocalSharedPreference(tag, TextUtils.join(";", timestamps));
        }



        private void setWaveFromConfig() {
            Long[] times = WAVES.keySet().toArray(new Long[WAVES.size()]);
            Arrays.sort(times, Collections.reverseOrder());

            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            WAVE = settings.getString("wave", "");
            INFECTED_TAG = settings.getString("infected_tag", "00xbad1dea");
            INFECTION_PROBABILITY = settings.getFloat("infection_probability", 0.0f);
            EXPOSED_DURATION = settings.getLong("exposed_duration", 6*60*60*1000l);
            RECOVERED_DURATION = settings.getLong("recovered_duration", 18*60*60*1000l);
            String temp_string = settings.getString("state_after_infected", "R");
            if (temp_string.equals("S")) STATE_AFTER_INFECTED = SelfState.S;
            else if (temp_string.equals("R")) STATE_AFTER_INFECTED = SelfState.R;


            for (Long timestamp: times) {
                if (consumeWave(timestamp, WAVES.get(timestamp).toString())) break;
            }

            runData.putString("wave", WAVE);
            runData.putString("infected_tag", INFECTED_TAG);
            runData.putFloat("infection_probability", INFECTION_PROBABILITY);
            runData.putLong("exposed_duration", EXPOSED_DURATION);
            runData.putLong("recovered_duration", RECOVERED_DURATION);
            runData.putString("state_after_infected", STATE_AFTER_INFECTED.toString());
            saveLocalSharedPreference("wave", WAVE);
            saveLocalSharedPreference("infected_tag", INFECTED_TAG);
            saveLocalSharedPreference("infection_probability", INFECTION_PROBABILITY);
            saveLocalSharedPreference("exposed_duration", EXPOSED_DURATION);
            saveLocalSharedPreference("recovered_duration", RECOVERED_DURATION);
            saveLocalSharedPreference("state_after_infected", STATE_AFTER_INFECTED.toString());


            Log.d(EPI_TAG, "..... "+WAVE+" "+INFECTED_TAG+" "+INFECTION_PROBABILITY+ " " + selfState.toString()+" "+EXPOSED_DURATION+" "+RECOVERED_DURATION+ " "+STATE_AFTER_INFECTED);

        }


        private boolean consumeWave(Long timestamp, String wave) {
            Log.d(EPI_TAG, "&&&&& consuming wave "+timestamp + " "+wave);
            if (timestamp > System.currentTimeMillis()) return false; //wave start in the future
            if (timestamp < (System.currentTimeMillis() - TIME_LIMIT)) return false; //too late, you are not participating
            if (isWaveConsumed(timestamp, wave)) return true; //we only take the newest value, so we don't iterate more

            String tag = wave.split(",")[0];
            Float infection_probability = Float.parseFloat(wave.split(",")[1]);
            String starting_state = wave.split(",")[2];
            Long exposed_duration = Long.parseLong(wave.split(",")[3]) * 60 * 1000l;
            Long recovered_duration = Long.parseLong(wave.split(",")[4]) * 60 * 1000l;
            String temp_string = wave.split(",")[5];
            if (temp_string.equals("S")) STATE_AFTER_INFECTED = SelfState.S;
            else if (temp_string.equals("R")) STATE_AFTER_INFECTED = SelfState.R;

            INFECTED_TAG = tag;
            INFECTION_PROBABILITY = infection_probability;
            WAVE = ""+timestamp+"!"+wave;
            handleState(starting_state);
            EXPOSED_DURATION = exposed_duration;
            RECOVERED_DURATION = recovered_duration;

            ArrayList<Long> consumedWaves = getConsumed("consumed_waves");
            consumedWaves.add(timestamp);
            setConsumed("consumed_waves", consumedWaves);

            Log.d(EPI_TAG, "&&&&& consuming wave 2 "+timestamp + " "+wave);


            return true;
        }


        private boolean isWaveConsumed(Long timestamp, String wave) {
            ArrayList<Long> consumedWaves = getConsumed("consumed_waves");
            return (consumedWaves.contains(timestamp));
        }

        private void handleState(String type) {

            if (type.equals("S")) setSusceptible();
            if (type.equals("I")) setInfected("000000", "00_server_00");
            if (type.equals("V")) setVaccinated();
            if (type.contains("RA_")) {
                double probability = Double.parseDouble(type.split("_")[1]);
                if (Math.random() < probability) {
                    setInfected("000000", "00_server_00");
                }

            }


        }

        private void vibrate() {
          Vibrator vibrator = (Vibrator)getSystemService(getBaseContext().VIBRATOR_SERVICE);
          vibrator.vibrate(2000);
        }

        private void showPopup() {
            Context context = getApplicationContext();
            CharSequence text = "Sensible DTU: You are infected!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }

        private void showSymptoms() {
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (HIDDEN_MODE) return;
            if (! selfState.equals(SelfState.I)) return;
            if (SILENT_NIGHT && currentHour < 8) return;
            if (Math.random() < VIBRATE_PROBABILITY) vibrate();
            if (Math.random() < POPUP_PROBABILITY) showPopup();
        }

        void showDescription() {
           if (HIDDEN_MODE) return;
           if (!SHOW_WELCOME_DIALOG) return;
           SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
           if (settings.getBoolean(EPI_DIALOG_PREF_PREFIX+"understood", false)) return;

            Intent dialogIntent = new Intent(getBaseContext(), EpiDescriptionActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
        }

        void showState() {
            if (HIDDEN_MODE) return;
            Intent dialogIntent = new Intent(getBaseContext(), EpiStateActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);

        }


    }





    void setBluetoothDiscoverable() {

        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (!bt.isEnabled()) {
            bt.enable();
        }

        if (bt.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(discoverableIntent);
        }



    }





}



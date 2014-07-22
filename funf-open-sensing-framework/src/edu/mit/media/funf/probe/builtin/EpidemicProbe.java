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

/**
 * Created by arks on 15/07/14.
 */
public class EpidemicProbe extends Probe implements ProbeKeys.EpidemicsKeys {



    private static final String DELEGATE_PROBE_NAME = BluetoothProbe.class.getName();
    private static final String OWN_NAME = "edu.mit.media.funf.probe.builtin.EpidemicProbe";
    private Handler handler;
    private Epidemic epidemic = null;

    private String EPI_TAG = "EPI_TAG";

    private enum SelfState { S, I, V};

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
        private HashMap<Long, String> STATES = null;
        private HashMap<Long, String> INFECTED_TAGS = null;
        private HashMap<Long, Float> INFECTION_PROBABILITIES = null;
        private long SCAN_DELTA = 250 * 1000;
        private long TIME_LIMIT = 12 * 60 * 60 * 1000;
        private SelfState selfState = SelfState.S;
        private boolean SILENT_NIGHT = true;


        private float POPUP_PROBABILITY = 0.0f;
        private float VIBRATE_PROBABILITY = 0.0f;


        public void handleBluetoothData(Bundle data) {




                runData = new Bundle();


                firstRun = checkFirstRun();
                if (firstRun) saveDefaultName();
                parseConfig();
                getCurrentState();
                setStateFromConfig();
                setTagFromConfig();
                setInfectionProbabilityFromConfig();

                HashMap<String, String> scanResults = bundleToHash(data);

                Log.d(EPI_TAG, selfState.toString() + " " + scanResults.toString());


                if (selfState.equals(SelfState.I)) {
                    setInfectedName(); //just in case
                } else if (selfState.equals(SelfState.V)) {
                    setSusceptibleName(); //just in case
                } else if (selfState.equals(SelfState.S)) {


                    if (deltaSufficient(scanResults.size())) {


                        for (String device_id : scanResults.keySet()) {
                            if (scanResults.get(device_id) == null) continue;
                            if (isInfection(device_id, scanResults.get(device_id))) {
                                setInfected(device_id, scanResults.get(device_id));
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

        private HashMap<String, String> bundleToHash(Bundle data){
            HashMap<String, String> devices = new HashMap<String, String>();

            for (Parcelable device: data.getParcelableArrayList("DEVICES") ) {
                String address = ((BluetoothDevice)((Bundle) device).get(BluetoothDevice.EXTRA_DEVICE)).getAddress();
                String name = ((BluetoothDevice)((Bundle) device).get(BluetoothDevice.EXTRA_DEVICE)).getName();
                devices.put(address, name);
            }

            return devices;
        }


        private void getCurrentState() {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            String state = settings.getString("self_state", "S");
            if (state.equals("S")) selfState = SelfState.S;
            if (state.equals("I")) selfState = SelfState.I;
            if (state.equals("V")) selfState = SelfState.V;
        }

        private void setCurrentState(SelfState state) {
            selfState = state;
            if (state.equals(SelfState.S)) saveLocalSharedPreference("self_state", "S");
            if (state.equals(SelfState.I)) saveLocalSharedPreference("self_state", "I");
            if (state.equals(SelfState.V)) saveLocalSharedPreference("self_state", "V");


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
            Log.i(EPI_TAG, "infected! "+device_id+" "+device_name);
            runData.putString("state", "infection_" + device_id + "_" + device_name);
            setCurrentState(SelfState.I);
            setInfectedName();
        }

        private void setSusceptible() {
            if (selfState.equals(SelfState.S)) return;
            Log.i(EPI_TAG, "susceptible!");
            runData.putString("state", "susceptible");
            setCurrentState(SelfState.S);
            setSusceptibleName();
        }

        private void setVaccinated() {
            if (selfState.equals(SelfState.V)) return;
            Log.i(EPI_TAG, "vaccinated!");
            runData.putString("state", "vaccinated");
            setCurrentState(SelfState.V);
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

                INFECTED_TAGS = new HashMap<Long, String>();

                String[] temp_tags = probeConfig.getString("INFECTED_TAGS").split(";");
                for (String state: temp_tags) {
                    try {
                        Long timestamp = Long.parseLong(state.split(",")[0]);
                        Long epoch = 1405794046 * 1000L;
                        String s = state.split(",")[1];
                        if (timestamp < epoch) {
                            timestamp = timestamp * 1000; //timestamp is in seconds, convert to mili
                        }

                        INFECTED_TAGS.put(timestamp, s);
                    }
                    catch (NumberFormatException e) {}
                }

            }
            catch (JSONException e) {}


            try {

               INFECTION_PROBABILITIES = new HashMap<Long, Float>();

                String[] temp_probs = probeConfig.getString("INFECTION_PROBABILITIES").split(";");
                for (String state: temp_probs) {
                    try {
                        Long timestamp = Long.parseLong(state.split(",")[0]);
                        Long epoch = 1405794046 * 1000L;
                        Float s = Float.parseFloat(state.split(",")[1]);
                        if (timestamp < epoch) {
                            timestamp = timestamp * 1000; //timestamp is in seconds, convert to mili
                        }

                        INFECTION_PROBABILITIES.put(timestamp, s);
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


               STATES = new HashMap<Long, String>();


                String[] temp_states = probeConfig.getString("STATES").split(";");

                for (String state: temp_states) {
                    try {
                        Long timestamp = Long.parseLong(state.split(",")[0]);
                        Long epoch = 1405794046 * 1000L;

                        String s = state.split(",")[1];

                        if (timestamp < epoch) {
                            timestamp = timestamp * 1000; //timestamp is in seconds, convert to mili
                        }


                        Log.d(EPI_TAG, "!!! " + state.toString() + " " + timestamp + " " + s);


                        STATES.put(timestamp, s);
                    }
                    catch (NumberFormatException e) {}

                }

            }
            catch (JSONException e) {}

            Log.d(EPI_TAG, "*** "+INFECTION_PROBABILITY+" "+STATES+" "+INFECTED_TAGS);


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

        private void setInfectionProbabilityFromConfig() {
            Long[] times = INFECTION_PROBABILITIES.keySet().toArray(new Long[INFECTION_PROBABILITIES.size()]);
            Arrays.sort(times, Collections.reverseOrder());
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            INFECTION_PROBABILITY = settings.getFloat("infection_probability", 0.0f);

            for (Long timestamp: times) {
                if (consumeInfectionProbability(timestamp, INFECTION_PROBABILITIES.get(timestamp))) break;
            }

            runData.putFloat("infection_probability", INFECTION_PROBABILITY);
            saveLocalSharedPreference("infection_probability", INFECTION_PROBABILITY);

        }


        private boolean consumeInfectionProbability(Long timestamp, Float probability) {
            if (timestamp > System.currentTimeMillis()) return false;
            if (timestamp != 0 && timestamp < (System.currentTimeMillis() - TIME_LIMIT)) return false;
            if (isInfectionProbabilityConsumed(timestamp, probability)) return true; //we only take the newest value, so we don't iterate more
            INFECTION_PROBABILITY = probability;

            if (timestamp == 0) {
                setConsumed0Value("consumed_infections", probability);
            }
            else {

                ArrayList<Long> consumedInfectionProbabilities = getConsumed("consumed_infections");
                consumedInfectionProbabilities.add(timestamp);
                setConsumed("consumed_infections", consumedInfectionProbabilities);
                setConsumed0Value("consumed_infections", -1.0f);
            }
            return true;
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

        private String getConsumed0Value(String tag) {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            return settings.getString(tag+"_0value", "");
        }

        private Float getConsumed0Value(String tag, float v) {
            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            return settings.getFloat(tag + "_0value", -1.0f);
        }

        private void setConsumed(String tag, ArrayList<Long> timestamps) {
            saveLocalSharedPreference(tag, TextUtils.join(";", timestamps));
        }

        private void setConsumed0Value(String tag, String value) {
            saveLocalSharedPreference(tag+"_0value", value);
        }

        private void setConsumed0Value(String tag, Float value) {
            saveLocalSharedPreference(tag+"_0value", value);
        }


        private void setTagFromConfig() {
            Long[] times = INFECTED_TAGS.keySet().toArray(new Long[INFECTED_TAGS.size()]);
            Arrays.sort(times, Collections.reverseOrder());

            SharedPreferences settings = getSharedPreferences(OWN_NAME, 0);
            INFECTED_TAG = settings.getString("infected_tag", "00xbad1dea");

            for (Long timestamp: times) {
                   if (consumeTag(timestamp, INFECTED_TAGS.get(timestamp).toString())) break;
            }

            runData.putString("infected_tag", INFECTED_TAG);
            saveLocalSharedPreference("infected_tag", INFECTED_TAG);
        }

        private boolean consumeTag(Long timestamp, String tag) {
            if (timestamp > System.currentTimeMillis()) return false;
            if (timestamp != 0 && timestamp < (System.currentTimeMillis() - TIME_LIMIT)) return false;
            if (isTagConsumed(timestamp, tag)) return true; //we only take the newest value, so we don't iterate more

            if (! tag.equals(INFECTED_TAG)) setSusceptible();

            INFECTED_TAG = tag;

            if (timestamp == 0) {
                setConsumed0Value("consumed_tags", tag);
            }
            else {
                ArrayList<Long> consumedTags = getConsumed("consumed_tags");
                consumedTags.add(timestamp);
                setConsumed("consumed_tags", consumedTags);
                setConsumed0Value("consumed_tags", "");
            }
            return true;
        }

        private boolean isTagConsumed(Long timestamp, String tag) {
            if (timestamp == 0 && tag.equals(getConsumed0Value("consumed_tags"))) return true;
            if (timestamp == 0 && ! tag.equals(getConsumed0Value("consumed_tags"))) return false;

            ArrayList<Long> consumedTags = getConsumed("consumed_tags");
            return (consumedTags.contains(timestamp));
        }



        private void setStateFromConfig() {
            Long[] times = STATES.keySet().toArray(new Long[STATES.keySet().size()]);
            Arrays.sort(times, Collections.reverseOrder());

            for (Long timestamp: times) {
                Log.d(EPI_TAG, "xxx "+timestamp + " "+ STATES.get(timestamp).toString());
                if (consumeState(timestamp, STATES.get(timestamp).toString())) break;
            }

        }


        private void handleState(Long timestamp, String type) {
            Log.d(EPI_TAG, "handling "+timestamp + " "+ type);

            if (type.equals("S")) setSusceptible();
            if (type.equals("I")) setInfected("000000", "00_server_00");
            if (type.equals("V")) setVaccinated();
            if (type.contains("R")) {
                double probability = Double.parseDouble(type.split("_")[1]);
                if (Math.random() < probability) {
                    setInfected("000000", "00_server_00");
                }

            }


        }

        private boolean consumeState(Long timestamp, String type) {

            Log.d(EPI_TAG, "... "+timestamp+" "+type+" "+getConsumed0Value("consumed_states"));

            if (timestamp > System.currentTimeMillis()) return false;
            if (timestamp != 0 && timestamp < (System.currentTimeMillis() - TIME_LIMIT)) return false;
            if (isStateConsumed(timestamp, type)) return true; //we only take the newest value, so we don't iterate more

            handleState(timestamp, type);

            if (timestamp == 0) {
                setConsumed0Value("consumed_states", type);
            }
            else {
                ArrayList<Long> consumedStates = getConsumed("consumed_states");
                consumedStates.add(timestamp);
                setConsumed("consumed_states", consumedStates);
                setConsumed0Value("consumed_states", "");
            }


            return true;
        }

        private boolean isStateConsumed(Long timestamp, String type) {
            if (timestamp == 0 && type.equals(getConsumed0Value("consumed_states"))) return true;
            if (timestamp == 0 && ! type.equals(getConsumed0Value("consumed_states"))) return false;

            ArrayList<Long> consumedStates = getConsumed("consumed_states");
            return (consumedStates.contains(timestamp));
        }

        private boolean isInfectionProbabilityConsumed(Long timestamp, Float probability) {
            if (timestamp == 0 && probability.equals(getConsumed0Value("consumed_infections", 0.0f))) return true;
            if (timestamp == 0 && ! probability.equals(getConsumed0Value("consumed_infections", 0.0f))) return false;

            ArrayList<Long> consumedStates = getConsumed("consumed_infections");
            return (consumedStates.contains(timestamp));
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
            if (! selfState.equals(SelfState.I)) return;
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            Log.d(EPI_TAG, "showing symptoms! "+currentHour+" "+SILENT_NIGHT+" "+VIBRATE_PROBABILITY+" "+POPUP_PROBABILITY);
            if (SILENT_NIGHT && currentHour < 8) return;
            if (Math.random() > VIBRATE_PROBABILITY) vibrate();
            if (Math.random() > POPUP_PROBABILITY) showPopup();
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


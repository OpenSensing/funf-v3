package edu.mit.media.funf.probe.edu.mit.media.funf.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import edu.mit.media.funf.R;
import edu.mit.media.funf.probe.builtin.EpidemicProbe;

/**
 * Created by arks on 23/07/14.
 */
public class EpiDescriptionActivity extends Activity {



    public void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.epi_description);
    }


    public void understand(View view) {
        Log.d(EpidemicProbe.EPI_TAG, "understood!");
        saveLocalSharedPreference(EpidemicProbe.EPI_DIALOG_PREF_PREFIX+"understood", true);
        finish();
    }

    private void saveLocalSharedPreference(String key, boolean value) {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


}
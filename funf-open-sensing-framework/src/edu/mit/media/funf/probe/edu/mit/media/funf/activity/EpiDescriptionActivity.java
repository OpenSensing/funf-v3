package edu.mit.media.funf.probe.edu.mit.media.funf.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;

import java.util.Arrays;
import java.util.Date;

import edu.mit.media.funf.R;
import edu.mit.media.funf.probe.builtin.EpidemicProbe;



/**
 * Created by arks on 23/07/14.
 */





public class EpiDescriptionActivity extends Activity {


    private int screenNo = 1;
    private int lastScreenNo = 5;
    private TextView welcomeText = null;
    private Button proceedButton = null;
    private Button backButton = null;

    private boolean showVaccinationScreenFirst = false;


    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.epi_description);
        welcomeText = (TextView)findViewById(R.id.welcomeTextView);
        proceedButton = (Button)findViewById(R.id.proceedButton);
        backButton = (Button)findViewById(R.id.backButton);
        welcomeText.setMovementMethod(new ScrollingMovementMethod());

        showVaccinationScreenFirst = false;
        if (Math.random() > 0.5) showVaccinationScreenFirst = true;

        saveLocalSharedPreference("show_vaccination_screen_first", showVaccinationScreenFirst);
    }

    protected void onResume() {
        super.onResume();
        screenNo = 1;
        updateUI();
    }

    void showScreen1() {
        welcomeText.setText(R.string.welcome_text_1);
    }

    void showScreen2() {
        welcomeText.setText(R.string.welcome_text_2);
    }

    void showScreen3() {
        welcomeText.setText(R.string.welcome_text_3);
    }

    void showScreen4() {
        welcomeText.setText(R.string.welcome_text_4);
        welcomeText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.epi_icon_a, 0, 0, 0);
    }

    void showScreen5() {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        int waveNo = settings.getInt("wave_no", 1);
        String finalString = getString(R.string.welcome_text_5);
        if (waveNo > 4 && waveNo <= 8) finalString += " " + getString(R.string.welcome_text_extra_after_wave_4);
        if (waveNo > 8) finalString += " " + getString(R.string.welcome_text_extra_after_wave_8);

        welcomeText.setText(finalString);
    }

    void updateUI() {
        welcomeText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        if (screenNo == 1) showScreen1();
        if (screenNo == 2) showScreen2();
        if (screenNo == 3 && !showVaccinationScreenFirst) showScreen3();
        if (screenNo == 3 && showVaccinationScreenFirst) showScreen5();
        if (screenNo == 4 && !showVaccinationScreenFirst) showScreen4();
        if (screenNo == 4 && showVaccinationScreenFirst) showScreen3();
        if (screenNo == 5 && !showVaccinationScreenFirst) showScreen5();
        if (screenNo == 5 && showVaccinationScreenFirst) showScreen4();

        if (screenNo == 1) backButton.setVisibility(View.INVISIBLE);
        else backButton.setVisibility(View.VISIBLE);

        if (screenNo == lastScreenNo) {
            proceedButton.setText("Got it!");
        }
        else {
            proceedButton.setText("Proceed >>");
        }

    }

    public void proceed(View view) {

        if (screenNo == lastScreenNo) {
            understand();
        }

        screenNo += 1;
        updateUI();
    }

    public void back(View view) {
        screenNo -= 1;
        updateUI();
    }

    private void understand() {
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

    private void saveLocalSharedPreference(String key, Integer value) {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

}
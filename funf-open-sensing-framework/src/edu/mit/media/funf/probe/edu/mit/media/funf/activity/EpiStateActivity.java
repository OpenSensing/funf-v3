package edu.mit.media.funf.probe.edu.mit.media.funf.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.mit.media.funf.R;
import edu.mit.media.funf.probe.builtin.EpidemicProbe;

/**
 * Created by arks on 25/07/14.
 */
public class EpiStateActivity extends FragmentActivity{

    private static final String PERMISSION = "publish_actions";
    private final String PENDING_ACTION_BUNDLE_KEY = "edu.mit.media.funf.probe.edu.mit.media.funf.activity:PendingAction";


    private Button postStatusUpdateButton;
    private LoginButton loginButton;
    private PendingAction pendingAction = PendingAction.NONE;
    private ViewGroup controlsContainer;
    private GraphUser user;
    private boolean canPresentShareDialog;
    private boolean notYetPosted = true;

    private enum PendingAction {
        NONE,
        POST_STATUS_UPDATE
    }

    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {}

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        setContentView(R.layout.epi_state);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                EpiStateActivity.this.user = user;
                updateUI();
                // It's possible that we were waiting for this.user to be populated in order to post a
                // status update.
                handlePendingAction();
            }
        });


        postStatusUpdateButton = (Button) findViewById(R.id.postStatusUpdateButton);
        postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickPostStatusUpdate();
            }
        });



        controlsContainer = (ViewGroup) findViewById(R.id.main_ui_container);

        final FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            // If we're being re-created and have a fragment, we need to a) hide the main UI controls and
            // b) hook up its listeners again.
            controlsContainer.setVisibility(View.GONE);
        }

        // Listen for changes in the back stack so we know if a fragment got popped off because the user
        // clicked the back button.
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (fm.getBackStackEntryCount() == 0) {
                    // We need to re-show our UI.
                    controlsContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        // Can we present the share dialog for regular links?
        canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        notYetPosted = true;

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(1338);

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                        exception instanceof FacebookAuthorizationException)) {
            new AlertDialog.Builder(EpiStateActivity.this)
                    .setTitle(R.string.cancelled)
                    .setMessage(R.string.permission_not_granted)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
        updateUI();
    }

    private void showFacebook() {
        hideDigest();
        hideFinalDialog();
        findViewById(R.id.shareOnFacebookLayout).setVisibility(View.VISIBLE);

        Session session = Session.getActiveSession();
        boolean enableButtons = (session != null && session.isOpened());
        postStatusUpdateButton.setEnabled((enableButtons || canPresentShareDialog) && notYetPosted);

        float alpha = 0.4f;
        if ((enableButtons || canPresentShareDialog) && notYetPosted) alpha = 1.0f;

        postStatusUpdateButton.setAlpha(alpha);
    }

    private void hideFacebook() {
        findViewById(R.id.shareOnFacebookLayout).setVisibility(View.GONE);
    }

    private void showDigest(int waveNo) {

        if (waveNo <= 4) {
            showFacebook();
            return;
        }

        hideFacebook();
        hideFinalDialog();
        TextView digestTextView = (TextView)findViewById(R.id.digestTextView);

        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        String dailyDigestString = settings.getString("daily_digest", "");
        JSONObject dailyDigest = b64StringToJson(dailyDigestString);

        if (dailyDigest == null) {
            showFacebook();
            return;
        }

        try {
            if (waveNo > 4 && waveNo <= 8) digestTextView.setText(proccessDigest4(dailyDigest.getJSONObject(getDateYesterday())));
            if (waveNo > 8) digestTextView.setText(proccessDigest8(dailyDigest.getJSONObject(getDateYesterday())));

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(EpidemicProbe.EPI_TAG, "HERE *_*");
            showFacebook();
        }

    }

    private void hideDigest() {
        findViewById(R.id.digestLayout).setVisibility(View.GONE);

    }

    private void showFinalDialog() {
        hideFacebook();
        hideDigest();
        findViewById(R.id.finalDialogLayout).setVisibility(View.VISIBLE);

        ((TextView)findViewById(R.id.finalDialogTextView)).setText(R.string.final_dialog);

    }

    private void hideFinalDialog() {
        findViewById(R.id.finalDialogLayout).setVisibility(View.GONE);
    }

    private JSONObject b64StringToJson(String b64String) {
        String text = new String(Base64.decode(b64String, Base64.DEFAULT));
        JSONObject job = null;


        try {
            job = new JSONObject(text);
        } catch (Exception e) {e.printStackTrace();}

        return  job;

    }

    private String getDateNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        return sdf.format(cal.getTime());
    }

    private String getDateYesterday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return sdf.format(cal.getTime());
    }

    private String proccessDigest4(JSONObject values) {
        //TODO
        return "4: "+values.toString();
    }

    private String proccessDigest8(JSONObject values) {
        //TODO
        return "8: "+values.toString();
    }

    public void dailyDigestAccepted(View view) {
        saveLocalSharedPreference("daily_digest_accepted", getDateNow());

        updateUI();
    }

    private void updateUI() {

        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        String self_state = settings.getString("self_state", "");
        boolean wave_description_accepted = settings.getBoolean("wave_description_accepted", false);

        if (wave_description_accepted) {

            (findViewById(R.id.stateLayout)).setVisibility(View.VISIBLE);
            (findViewById(R.id.waveDescriptionLayout)).setVisibility(View.GONE);




            if (self_state.equals("S")) self_state = "susceptible";
            if (self_state.equals("I")) self_state = "infected";
            if (self_state.equals("E")) self_state = "susceptible";
            if (self_state.equals("V")) self_state = "vaccinated";
            if (self_state.equals("A")) self_state = "waiting for vaccination to become effective";
            if (self_state.equals("R")) self_state = "recovered";


            ((TextView) findViewById(R.id.postTextView)).setText(getString(R.string.status_update, self_state));

            String youAreText = "You are " + self_state + ". ";
            if (self_state.equals("waiting for vaccination to become effective")) {
                Long vaccinationEffective = settings.getLong("to_vaccinated_time", 0L);
                String untilVaccination = String.format("%.0f", (vaccinationEffective - System.currentTimeMillis())/1000.0/60.0);
                youAreText += untilVaccination + " minutes until vaccination is effective.";
            }
            ((TextView) findViewById(R.id.statusTextView)).setText(youAreText);



            if (System.currentTimeMillis() >= settings.getLong("show_final_dialog", 9999999999L*1000L)) {
                showFinalDialog();
            } else{
                if (settings.getString("daily_digest_accepted", "").equals(getDateNow()))
                    showFacebook();
                else
                    showDigest(settings.getInt("wave_no", 0));
            }








            boolean vaccination_decision_made = settings.getBoolean("vaccination_decision_made", false);

            if (self_state.equals("susceptible"))
                (findViewById(R.id.vaccinationLayout)).setVisibility(View.VISIBLE);
            else (findViewById(R.id.vaccinationLayout)).setVisibility(View.GONE);

            if (vaccination_decision_made) {
                (findViewById(R.id.vaccinationLayout)).setAlpha(0.4f);
                ((Button) findViewById(R.id.vaccinateButton)).setEnabled(false);
                ((Button) findViewById(R.id.vaccinateNotNowButton)).setEnabled(false);

                if (settings.getLong("vaccination_clicked", 0) != 0)
                    ((TextView) findViewById(R.id.vaccinationExplanationTextView)).setText(R.string.vaccination_text_waiting);
                else
                    ((TextView) findViewById(R.id.vaccinationExplanationTextView)).setText(R.string.vaccination_text_disabled);

                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(1338);


            } else {
                (findViewById(R.id.vaccinationLayout)).setAlpha(1.0f);
                ((TextView) findViewById(R.id.vaccinationExplanationTextView)).setText(R.string.vaccination_text);
                ((Button) findViewById(R.id.vaccinateButton)).setEnabled(true);
                ((Button) findViewById(R.id.vaccinateNotNowButton)).setEnabled(true);
            }
        }
        else {
            (findViewById(R.id.stateLayout)).setVisibility(View.GONE);
            (findViewById(R.id.waveDescriptionLayout)).setVisibility(View.VISIBLE);

            String final_state = settings.getString("self_state", "");
            String finalStateDisplayText = "SUSCEPTIBLE";
            if(final_state.equals("I")) {
                finalStateDisplayText = "INFECTED";
            } else if (final_state.equals("V")) {
                finalStateDisplayText = "VACCINATED";
            } else if (final_state.equals("R")) {
                finalStateDisplayText = "RECOVERED";
            }
            ((TextView)findViewById(R.id.previousWaveState)).setText(finalStateDisplayText);

            ((TextView)findViewById(R.id.vaccinationCosts)).setText(settings.getInt("vaccination_lost_points", 0));
            ((TextView)findViewById(R.id.infectionCosts)).setText(settings.getInt("infected_lost_points", 0));
            ((TextView)findViewById(R.id.sideEffectsCosts)).setText(settings.getInt("side_effects_lost_points", 0));
            ((TextView)findViewById(R.id.totalCosts)).setText(Math.min(0, 100 - settings.getInt("points", 0)));

            String defaultWaveDescription = getString(R.string.wave_description_1);
            int waveId = settings.getInt("wave_no", -1);
            int specificWaveDescriptionResourceId = getResources().getIdentifier("wave_description_" + Integer.toString(waveId), "string", EpiStateActivity.this.getPackageName());
            if (specificWaveDescriptionResourceId != 0) {
                String specificWaveDescription = getString(specificWaveDescriptionResourceId);
                ((TextView)findViewById(R.id.waveDescriptiontextView)).setText(specificWaveDescription);
            } else {
                ((TextView)findViewById(R.id.waveDescriptiontextView)).setText(defaultWaveDescription);
            }
        }

    }

    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_STATUS_UPDATE:
                postStatusUpdate();
                break;
        }
    }

    private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

    private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String alertMessage = null;
        if (error == null) {
            alertMessage = getString(R.string.successfully_posted_post);
            notYetPosted = false;
            saveLocalSharedPreference("last_facebook_update", ""+System.currentTimeMillis()+"_"+message);
        } else {
            alertMessage = error.getErrorMessage();
        }

        Toast.makeText(getApplicationContext(), alertMessage, Toast.LENGTH_LONG).show();
        updateUI();
    }

    private void onClickPostStatusUpdate() {
        performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
    }

    private FacebookDialog.ShareDialogBuilder createShareDialogBuilderForLink() {
        return new FacebookDialog.ShareDialogBuilder(this)
                .setName("SensibleDTU")
                .setDescription("You can post your state in the SensibleDTU Epidemics game on FB")
                .setLink("https://www.sensible.dtu.dk/");
    }

    private void postStatusUpdate() {
        if (canPresentShareDialog) {
            FacebookDialog shareDialog = createShareDialogBuilderForLink().build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else if (user != null && hasPublishPermission()) {
            SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
            String self_state = settings.getString("self_state", "");

            if (self_state.equals("S")) self_state = "susceptible";
            if (self_state.equals("I")) self_state = "infected";
            if (self_state.equals("E")) self_state = "susceptible";
            if (self_state.equals("V")) self_state = "vaccinated";
            if (self_state.equals("A")) self_state = "waiting for vaccination to become effective";
            if (self_state.equals("R")) self_state = "recovered";

            final String message = getString(R.string.status_update, self_state);
            Request request = Request
                    .newStatusUpdateRequest(Session.getActiveSession(), message, new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            showPublishResult(message, response.getGraphObject(), response.getError());
                        }
                    });
            request.executeAsync();
        } else {
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }



    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains(PERMISSION);
    }

    private void performPublish(PendingAction action, boolean allowNoSession) {
        Session session = Session.getActiveSession();
        if (session != null) {
            pendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSION));
                return;
            }
        }

        if (allowNoSession) {
            pendingAction = action;
            handlePendingAction();
        }
    }


    public void decisionVaccinate(View view) {

        saveLocalSharedPreference("vaccination_clicked", System.currentTimeMillis());
        saveLocalSharedPreference("vaccination_decision_made", true);
        saveLocalSharedPreference("vaccination_refused", 0L);


        updateUI();

    }

    public void decisionNotNow(View view) {
        saveLocalSharedPreference("vaccination_refused", System.currentTimeMillis());
        saveLocalSharedPreference("vaccination_decision_made", true);
        saveLocalSharedPreference("vaccination_clicked", 0L);


        updateUI();

    }

    public void waveDescriptionConfimed(View view) {

        saveLocalSharedPreference("wave_description_accepted", true);
        saveLocalSharedPreference("wave_description_accepted_t", System.currentTimeMillis());

        int currentDay =  Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        saveLocalSharedPreference("last_day_showed_state", currentDay, 0);

        updateUI();
    }

    private void saveLocalSharedPreference(String key, Long value) {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private void saveLocalSharedPreference(String key, boolean value) {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private void saveLocalSharedPreference(String key, int value, int dummy) {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private void saveLocalSharedPreference(String key, String value) {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void showDescriptionActivity(View view) {
        Intent dialogIntent = new Intent(getBaseContext(), EpiDescriptionActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    public void openFinalDigest(View view) {
        SharedPreferences settings = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(settings.getString("final_dialog_url", "http://www.sensible.dtu.dk")));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(browserIntent);
    }

}

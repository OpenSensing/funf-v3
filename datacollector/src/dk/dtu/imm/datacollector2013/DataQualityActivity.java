package dk.dtu.imm.datacollector2013;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import edu.mit.media.funf.probe.builtin.EpidemicProbe;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DataQualityActivity extends Activity {
    private static final String BASE_URL = "https://www.sensible.dtu.dk/sensible-dtu/connectors/connector_answer/v1";
    private static final String DATA_QUALITY_QUESTION_ENDPOINT = "/data_quality_question/get_data_stats_for_period/";
    public static final int WEEK_IN_MILLIS = 3600 * 24 * 7 * 1000;
    public static final String TAG = "DataQualityActivity";
    private boolean contestPeriod = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_quality);

        SharedPreferences sharedPreferences = getSharedPreferences(EpidemicProbe.OWN_NAME, 0);

        Long qualityStartDateTimestamp = System.currentTimeMillis() - WEEK_IN_MILLIS;

        String currentWaveConfig = sharedPreferences.getString("wave", "");
        Log.i("dataQuality", currentWaveConfig);
        if(currentWaveConfig.length() > 1) {

            contestPeriod = true;
            String[] configParts = currentWaveConfig.split("!");
            int waveNumber = Integer.parseInt(configParts[1].split(",")[7]);
            qualityStartDateTimestamp = Long.parseLong(configParts[0]);
            if (waveNumber % 2 == 0) {
                qualityStartDateTimestamp -= WEEK_IN_MILLIS;
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String qualityStartDate = dateFormat.format(new Date(qualityStartDateTimestamp));
        final String now = dateFormat.format(new Date(System.currentTimeMillis()));
        final String qualityEndDate = dateFormat.format(qualityStartDateTimestamp + 2 * WEEK_IN_MILLIS);

        ((TextView)findViewById(R.id.qualityPeriod)).setText("Your data quality since \n " + qualityStartDate + " is: ");
        ((TextView)findViewById(R.id.qualityMessage)).setText(getString(R.string.server_pending));

        Runnable getQualityFromServerRunnable = new Runnable() {
            @Override
            public void run() {

                double quality = 0.0;
                String qualityMessage = "";
                try {
                    quality = getCurrentQuality(qualityStartDate, now);

                    if (quality >= 0.8) {
                        qualityMessage = getString(R.string.positive_quality);
                    } else {
                        qualityMessage = getString(R.string.negative_quality);
                    }
                    String prizeMessage = getString(R.string.prize_message) + qualityEndDate;
                    if (contestPeriod) {
                        qualityMessage += prizeMessage;
                    }

                } catch (IOException e) {
                    qualityMessage = "Server connection error. Check your internet connection";
                    Log.e(TAG, e.getMessage());
                } catch (JSONException e) {
                    qualityMessage = "Error parsing server message. Please contact Sensible support";
                    Log.e(TAG, e.getMessage());
                } catch (Exception e) {
                    qualityMessage = "Error: " + e.getMessage();
                    Log.e(TAG, e.getMessage());
                } finally
                {
                    DisplayQualityRunnable displayQualityRunnable = new DisplayQualityRunnable(quality, qualityMessage);
                    runOnUiThread(displayQualityRunnable);
                }

            }
        };
        Thread httpRequestThread = new Thread(getQualityFromServerRunnable);
        httpRequestThread.start();
    }

    private class DisplayQualityRunnable implements Runnable {

        private double quality = 0.0;
        private String message = "";

        public DisplayQualityRunnable(double quality, String message) {
            this.quality = quality;
            this.message = message;
        }

        @Override
        public void run() {
            displayQuality(quality, message);
        }
    }

    private void displayQuality(double quality, String message) {
        TextView qualityTextView = (TextView) findViewById(R.id.qualityValueText);
        qualityTextView.setText("" + (int)(quality * 100) + "%");
        ((TextView)findViewById(R.id.qualityMessage)).setText(message);
    }

    private double getCurrentQuality(String startDate, String endDate) throws Exception {

        String bearerToken = getSharedPreferences("sensible_auth", Context.MODE_PRIVATE).getString("sensible_token", "");
        HttpClient client = new DefaultHttpClient();
        String url = BASE_URL + DATA_QUALITY_QUESTION_ENDPOINT + "?bearer_token=" + bearerToken + "&start_date=" + startDate + "&end_date=" + endDate + "&data_type=bluetooth";
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = client.execute(httpGet);
        String qualityJsonString = EntityUtils.toString(response.getEntity());
        Log.i(TAG, qualityJsonString);
        JSONArray results = new JSONArray(qualityJsonString);
        if (results.length() < 1) {
            throw new Exception("No data quality associated with your user. Please contact support@sensible.dtu.dk");
        }
        JSONObject qualityJson = results.getJSONObject(0);
        return qualityJson.getDouble("quality");
    }
}
package dk.dtu.imm.datacollector;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created with IntelliJ IDEA.
 * User: piotr
 * Date: 7/18/13
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */


public class AuthActivity extends Activity {

    private static final String TAG = "AUTH_AuthActivity";

    Context context;

    WebView wv;


    private static final String DOMAIN_URL = "http://54.229.13.160/";
    //private static final String DOMAIN_URL = "https://www.sensible.dtu.dk/";
    private static final String BASE_URL = DOMAIN_URL + "sensible-dtu/authorization_manager/connector_funf/auth/grant/?scope=connector_funf.submit_data";


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.auth_layout);

        uisetup();

        //wv.loadUrl("https://www.sensible.dtu.dk/");
        context = getApplicationContext();
        String regId = getSharedPreferences(AuthActivity.class.getSimpleName(), Context.MODE_PRIVATE).
                getString(RegistrationHandler.PROPERTY_REG_ID, "");
        registerWithServer(context, regId);
    }



    private void uisetup() {
        wv = (WebView)this.findViewById(R.id.webView);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.requestFocus(View.FOCUS_DOWN);
        wv.loadData("<html><body><h1>Registering with GCM</h1><p>Please wait...</p></body></html>",
                "text/html", "UTF-8");
        wv.setWebViewClient(new WebViewClient() {

            private boolean serviceStarted = false;

            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                Log.e(TAG, "SSL Error");
                handler.proceed() ;
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url){
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!serviceStarted) {
                    String code = null;
                    if (url != null) {
                        if (url.contains("&code=")) {
                            String[] parts = url.split("&code=");
                            if (parts[1].contains("&")) {
                                String [] pparts = parts[1].split("&");
                                code = pparts[0];
                            } else {
                                code = parts[1];
                            }
                            SharedPreferences.Editor editor = MainPipeline.getSystemPrefs(context).edit();
                            editor.putString(RegistrationHandler.PROPERTY_SENSIBLE_CODE, code);
                            editor.commit();
                            Log.d(TAG, "code = " + code);
                            Log.d(TAG, "got code, starting service");
                            LauncherReceiver.startService(context, RegistrationHandler.class);
                            serviceStarted = true;
                            finish();
                        }
                    }
                    Log.d(TAG, "URL: " + url);
                }
            }
        });
    }

    private void registerWithServer(Context context, String regId) {
        //get IMEI
        Log.d(TAG, "Will start the webview registration now");
        TelephonyManager tm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        wv.loadUrl(BASE_URL + "&client_id=" + RegistrationHandler.CLIENT_ID + "&gcm_id=" + regId + "&device_id=" + imei);
    }

}
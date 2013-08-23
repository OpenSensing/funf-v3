package dk.dtu.imm.datacollector2013;

import android.app.Activity;
import android.content.*;
import android.net.http.SslError;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.*;
import android.widget.Toast;


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
    String regId;

    AuthActivity activity;

    /**
     * IP of Milosz, for testing
     */
    //private static final String DOMAIN_URL = "https://54.229.13.160/";

    /**
     * Production address
     */
    private static final String DOMAIN_URL = "https://www.sensible.dtu.dk/";

    private static final String GRANT_ENDPOINT_URL = "sensible-dtu/authorization_manager/connector_funf/auth/grant/";
    private static final String SUCCESS_URL = "/sensible-data/?status=success";
    private static final String BASE_URL = DOMAIN_URL + GRANT_ENDPOINT_URL + "?scope=connector_funf.submit_data";

    private static final String CODE_URL_PREFIX = "sensible-dtu/authorization_manager/connector_funf/auth/granted/";

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        activity = this;
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        setContentView(R.layout.auth_layout);
        uisetup();

        context = getApplicationContext();
        regId = getSharedPreferences(AuthActivity.class.getSimpleName(), Context.MODE_PRIVATE).
                getString(RegistrationHandler.PROPERTY_REG_ID, "");
        registerWithServer(context, regId);
    }



    private void uisetup() {
        wv = (WebView)this.findViewById(R.id.webView);
        // clear cache
        wv.clearCache(true);
        // Don't store passwords or form data
        WebSettings mWebSettings = wv.getSettings();
        mWebSettings.setSavePassword(false);
        mWebSettings.setSaveFormData(false);
        // don't store cookies:
        CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.setAcceptCookie(false);
        cookieManager.removeAllCookie();

        wv.getSettings().setJavaScriptEnabled(true);
        wv.requestFocus(View.FOCUS_DOWN);
        wv.loadData("<html><body><h1>Registering with GCM</h1><p>Please wait...</p></body></html>",
                "text/html", "UTF-8");
        wv.setWebViewClient(new WebViewClient() {


            private boolean serviceStarted = false;

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.e(TAG, "SSL Error");
                handler.proceed();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!serviceStarted) {
                    String code = null;
                    if (url != null) {
                        if (url.contains(CODE_URL_PREFIX)) {
                            if (url.contains("&code=")) {
                                String[] parts = url.split("&code=");
                                if (parts[1].contains("&")) {
                                    String[] pparts = parts[1].split("&");
                                    code = pparts[0];
                                } else {
                                    code = parts[1];
                                }
                                SharedPreferences.Editor editor = RegistrationHandler.getAuthPreferences(context).edit();
                                editor.putString(RegistrationHandler.PROPERTY_SENSIBLE_CODE, code);
                                editor.commit();
                                Log.d(TAG, "code = " + code);
                                Log.d(TAG, "got code, starting service");
                                LauncherReceiver.startService(context, RegistrationHandler.class);
                                serviceStarted = true;
                                finish();
                            } else if(url.contains("error=access_denied")) {
                                Toast.makeText(context,
                                        "Error: You have to accept the permissions to use the application",
                                        Toast.LENGTH_LONG).show();
                                registerWithServer(context, regId);
                            }
                        } /*else if (url.contains(GRANT_ENDPOINT_URL)) {
                            if (!url.contains("client_id")
                                    || !url.contains("gcm_id")
                                    || !url.contains("device_id")) {
                                registerWithServer(context, regId);
                            }
                        } else if (url.contains(SUCCESS_URL) && url.contains("enrolled")) {
                            registerWithServer(context, regId);
                        } */

                    }
                    Log.d(TAG, "URL: " + url);
                }
            }
        });
        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                Log.d(TAG, "progress: " + progress);
                activity.setProgress(progress * 100);
            }
        });


    }

    private void registerWithServer(Context context, String regId) {
        //get IMEI
        Log.d(TAG, "Will start the webview registration now");
        TelephonyManager tm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        String link = BASE_URL + "&client_id=" + RegistrationHandler.CLIENT_ID + "&gcm_id=" + regId + "&device_id=" + imei;
        Log.d(TAG, link);
        wv.getSettings().setUserAgentString("dk.dtu.imm.datacollector2013");
        wv.loadUrl(link);

    }

}
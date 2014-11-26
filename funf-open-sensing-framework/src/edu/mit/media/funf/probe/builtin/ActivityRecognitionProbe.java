/**
 * Funf: Open Sensing Framework
 * Copyright (C) 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland. 
 * Acknowledgments: Alan Gardner
 * Contact: nadav@media.mit.edu
 * 
 * This file is part of Funf.
 * 
 * Funf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version. 
 * 
 * Funf is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with Funf. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.mit.media.funf.probe.builtin;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import edu.mit.media.funf.Utils;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.builtin.ProbeKeys.ActivityRecognitionKeys;
import edu.mit.media.funf.ActivityRecognitionIntentService;
import edu.mit.media.funf.ActivityUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;

public class ActivityRecognitionProbe extends Probe implements ActivityRecognitionKeys, ConnectionCallbacks, OnConnectionFailedListener {

    // Constants used to establish the activity update interval
    public static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int DETECTION_INTERVAL_SECONDS = 120;

    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
	private static final String ACTIVITY_UPDATE = "ACTIVITY_UPDATE";
	private BroadcastReceiver receiver;
	private Bundle mostRecentData;
	private long mostRecentTimestamp;
	private ActivityRecognitionClient mActivityRecognitionClient;
	private String clientAction;
	
	@Override
	public Parameter[] getAvailableParameters() {
		return new Parameter[] {
			new Parameter(Parameter.Builtin.PERIOD, 300L),
			new Parameter(Parameter.Builtin.START, 0L),
			new Parameter(Parameter.Builtin.END, 0L)
		};
	}

	@Override
	public String[] getRequiredFeatures() {
		return null;
	}

	@Override
	public String[] getRequiredPermissions() {
		return null;
	}
	
	@Override
	protected String getDisplayName() {
		return "Activity Recognition probe";
	}

	@Override
	protected void onEnable() {
		// Set action to ADD
		clientAction = "ADD";
		
		// Connect new client to server
		connectActivityRecognitionClient();
		
		// Create and register receiver
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				mostRecentTimestamp = Utils.getTimestamp();
				mostRecentData = intent.getExtras();
				sendProbeData();
			}
		};
		registerReceiver(receiver, new IntentFilter(ACTIVITY_UPDATE));
	}
	
	@Override
	protected void onDisable() {
		unregisterReceiver(receiver);
		
		//Set action to REMOVE
		clientAction = "REMOVE";
		
		// Connect new client to server
		connectActivityRecognitionClient();
	}

	private void connectActivityRecognitionClient() {
		if (servicesConnected()){
		    mActivityRecognitionClient = new ActivityRecognitionClient(getBaseContext(), this, this);
			mActivityRecognitionClient.connect();
		} else
			Log.d("ActivityRecognitionProbe", "Google services not available");
	}
	
	@Override
	protected void onRun(Bundle params) {
		sendProbeData();
		stop();
	}

	@Override
	protected void onStop() {
		// Nothing to stop, passive only
	}

	@Override
	public void sendProbeData() {
		if (mostRecentData != null) {
			sendProbeData(mostRecentTimestamp, mostRecentData);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            try {
                connectionResult.startResolutionForResult((Activity) getBaseContext(),
                    ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (SendIntentException e) {
               // display an error or log it here.
            }

        /*
         * If no resolution is available, display Google
         * Play service error dialog. This may direct the
         * user to Google Play Store if Google Play services
         * is out of date.
         */
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionResult.getErrorCode(),
                            (Activity) getBaseContext(),
                            ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (dialog != null) {
                dialog.show();
            }
        }
	}

	@Override
	public void onConnected(Bundle arg0) {
		if (clientAction.equals("ADD")) {
			// Request continouous activity updates
			mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, createRequestPendingIntent());
		} else if (clientAction.equals("REMOVE")){
			PendingIntent pendingIntent = createRequestPendingIntent();
	        // Remove the updates
	        mActivityRecognitionClient.removeActivityUpdates(pendingIntent);
	        
	        // Cancel the intent
	        pendingIntent.cancel();
		}
		// Disconnect client
		mActivityRecognitionClient.disconnect();
	}

	@Override
	public void onDisconnected() {
        // Destroy the current activity recognition client
        mActivityRecognitionClient = null;
	}
	
	/**
	 * Create PendingIntent from ActivityRecognition service
	 * @return
	 */
	private PendingIntent createRequestPendingIntent() {
		Intent intent = new Intent(getBaseContext(), ActivityRecognitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        return pendingIntent;
	}
	
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d("ActivityRecognitionProbe", "Google Play Services available");

            // Continue
            return true;

        // Google Play services was not available for some reason
        } else {
            return false;
        }
    }

}

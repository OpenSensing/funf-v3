/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mit.media.funf;

import java.util.ArrayList;

import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {

    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("Activity Recognition intent service", "Intent: " +  intent.getDataString());
        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            Log.i("Activity Recognition intent service", "has result");
            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            /*// Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();
            // Get the type of activity
            String activityType = getNameFromType(mostProbableActivity.getType());
            
            Intent broadcastIntent = new Intent().setAction("ACTIVITY_UPDATE");
            broadcastIntent.putExtra("ACTIVITY", activityType);
            broadcastIntent.putExtra("CONFIDENCE", confidence);
            sendBroadcast(broadcastIntent);*/

            /**
             * All activities and their probabilities
             */
            ArrayList<Integer> activityType = new ArrayList<Integer>();
            ArrayList<Integer> confidence = new ArrayList<Integer>();
            for (DetectedActivity detectedActivity : result.getProbableActivities()) {

                // Get the activity type, confidence level, and human-readable name
                activityType.add(detectedActivity.getType());
                confidence.add(detectedActivity.getConfidence());
            }
            Intent broadcastIntent = new Intent().setAction("ACTIVITY_UPDATE");
            broadcastIntent.putExtra("ACTIVITY", activityType);
            broadcastIntent.putExtra("CONFIDENCE", confidence);
            sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.WALKING:
                return "walking";
        }
        return "unknown";
    }
}
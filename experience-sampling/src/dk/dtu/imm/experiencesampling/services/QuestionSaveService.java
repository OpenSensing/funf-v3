package dk.dtu.imm.experiencesampling.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import dk.dtu.imm.experiencesampling.ConfigUtils;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.LocationStatus;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.models.answers.*;

import java.util.Timer;
import java.util.TimerTask;

public class QuestionSaveService extends Service {

    private static final String TAG = "QuestionSaveService";

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Answer answer;
    private boolean questionSaved;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent != null) {
                answer = (Answer) intent.getSerializableExtra("answer");
                if (answer != null) {
                    if (QuestionType.LOCATION_CURRENT.equals(answer.getQuestionType())) {
                        saveOrUpdatePlaceLabel(((CurrentLocation) answer).getPlaceLabel());
                        if (answer.getAnswerType().equals(AnswerType.ANSWERED_SUBMIT)) {
                            startLocationSearch();
                        } else {
                            saveAnswer(answer);
                        }
                    } else if (QuestionType.LOCATION_PREVIOUS.equals(answer.getQuestionType())) {
                        saveAnswer(answer);
                        saveOrUpdatePlaceLabel(((PreviousLocation) answer).getPlaceLabel());
                        stopSelf();
                    } else {
                        saveAnswer(answer);
                        updateFriendConnection(answer);
                        updateFriend(answer);
                        stopSelf();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during save answer");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void saveAnswer(Answer answer) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertAnswer(answer);
        dbHelper.closeDatabase();
        questionSaved = true;
    }

    private void saveOrUpdatePlaceLabel(String place) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertPlaceLabel(place);
        dbHelper.closeDatabase();
    }

    private void updateFriendConnection(Answer answer) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        if (QuestionType.SOCIAL_CLOSER_FRIEND.equals(answer.getQuestionType()) && AnswerType.ANSWERED_SUBMIT.equals(answer.getAnswerType())) {
            dbHelper.updateFriendConnectionAnswered(((CloserFriend) answer).getFriendOneId(), ((CloserFriend) answer).getFriendTwoId(), answer.getQuestionType());
        } else if (QuestionType.SOCIAL_RATE_TWO_FRIENDS.equals(answer.getQuestionType()) && AnswerType.ANSWERED_SUBMIT.equals(answer.getAnswerType())) {
            dbHelper.updateFriendConnectionAnswered(((RateTwoFriends) answer).getFriendOneId(), ((RateTwoFriends) answer).getFriendTwoId(), answer.getQuestionType());
        }
        dbHelper.closeDatabase();
    }

    private void updateFriend(Answer answer) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        if (QuestionType.SOCIAL_RATE_ONE_FRIEND.equals(answer.getQuestionType()) && AnswerType.ANSWERED_SUBMIT.equals(answer.getAnswerType())) {
            dbHelper.updateFriendAnswered(((RateOneFriend) answer).getFriendId(), answer.getQuestionType());
        }
        dbHelper.closeDatabase();
    }

    private void startLocationSearch() {
        startLocationTimer();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!questionSaved) {
                    try {
                        ((CurrentLocation) answer).setLatitude(location.getLatitude());
                        ((CurrentLocation) answer).setLongitude(location.getLongitude());
                        ((CurrentLocation) answer).setAccuracy(location.getAccuracy());
                        ((CurrentLocation) answer).setLocationStatus(LocationStatus.OK);
                        saveAnswer(answer);
                        stopLocationSearch();
                        stopSelf();
                    } catch (ClassCastException e) {
                        Log.e(TAG, "Another answer has been stored while searching for GPS coordinates - this should not happen in practice: " + e.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "Unknown exception while storing current location answer: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                if (!questionSaved) {
                    ((CurrentLocation) answer).setLocationStatus(LocationStatus.GPS_DISABLED);
                    saveAnswer(answer);
                }
            }
        };
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void startLocationTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!questionSaved) {
                    ((CurrentLocation) answer).setLocationStatus(LocationStatus.GPS_TIMEOUT);
                    saveAnswer(answer);
                }
                stopLocationSearch();
                stopSelf();
            }
        };
        timer.schedule(task, ConfigUtils.getConfigFromPrefs(getApplicationContext()).getGpsTimeout());
    }

    private void stopLocationSearch() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

}

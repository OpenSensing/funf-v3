package dk.dtu.imm.experiencesampling.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import dk.dtu.imm.experiencesampling.Config;
import dk.dtu.imm.experiencesampling.ConfigUtils;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;
import dk.dtu.imm.experiencesampling.exceptions.FacebookException;
import dk.dtu.imm.experiencesampling.exceptions.SensibleDtuException;
import dk.dtu.imm.experiencesampling.external.FacebookService;
import dk.dtu.imm.experiencesampling.external.SensibleDtuService;
import dk.dtu.imm.experiencesampling.external.dto.FriendsResponseDto;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.FriendConnection;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExperienceSamplingSetupService extends IntentService {

    private static final String TAG = "ExperienceSamplingSetupService";
    private static final String PREF_DOWNLOAD_TIMESTAMP_KEY = "pref_download_timestamp_key";

    private SharedPreferences sharedPrefs;

    public ExperienceSamplingSetupService() {
        super("ExperienceSamplingSetupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Setting up experience sampling");
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        Config config = ConfigUtils.getConfigFromPrefs(this);
        setupScheduleQuestionAlarm(this, config.getQuestionScheduleInterval());

        if (isFriendsDownloadTime()) {
            try {
                downloadFriendsInfo(ConfigUtils.getSensibleAccessToken(this));
                saveDownloadTimestamp(new Date().getTime());
            } catch (SensibleDtuException e) {
                Log.e(TAG, "Error while downloading friends info: " + e.getMessage());
            }
        }
    }

    private static void setupScheduleQuestionAlarm(Context context, long interval) {
        Log.d(TAG, "Setup question schedule with interval: " + interval);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent receiverIntent = new Intent(ConfigUtils.QUESTION_INTENT);
        PendingIntent pendingReceiverIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, pendingReceiverIntent);
    }

    public void downloadFriendsInfo(String bearerToken) throws SensibleDtuException {
        SensibleDtuService sensibleDtuService = new SensibleDtuService(this);

        FriendsResponseDto friendsResponseDto = sensibleDtuService.requestFriendsInfo(bearerToken);
        Set<Friend> friends = new HashSet<Friend>();
        Set<FriendConnection> connections = new HashSet<FriendConnection>();

        FacebookService facebookService = new FacebookService();
        for (String uid : friendsResponseDto.getFriends()) {
            try {
                friends.add(facebookService.getFriend(uid));
            } catch (FacebookException e) {
                Log.d(TAG, "Error while getting name of friend: " + uid);
            }
        }

        for (List<String> uids : friendsResponseDto.getConnections()) {
            if (uids.size() > 1) {
                FriendConnection friendConnection = new FriendConnection();
                friendConnection.setUid1(uids.get(0));
                friendConnection.setUid2(uids.get(1));
                connections.add(friendConnection);
            }
        }

        if (friends.size() != friendsResponseDto.getFriends().size() && connections.size() != friendsResponseDto.getConnections().size()) {
            Log.w(TAG, String.format("Not all friends are fetched correctly [friends: %s, %s], [connections: %s, %s]", friends.size(), friendsResponseDto.getFriends().size(), connections.size(), friendsResponseDto.getConnections().size()));
        }

        // Save friends and connections in db
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertFriends(friends.toArray(new Friend[]{}));
        dbHelper.insertFriendConnections(connections.toArray(new FriendConnection[]{}));
        dbHelper.closeDatabase();
    }

    private boolean isFriendsDownloadTime() {
        long now = new Date().getTime();
        long lastDownload = loadDownloadTimestamp();

        // todo: use update interval from config
        // download every 48h
        if ((now - lastDownload) > 48 * 60 * 60 * 1000) {
            Log.i(TAG, "download time");
            return true;
        } else {
            Log.i(TAG, "not download time");
            return false;
        }
    }

    private void saveDownloadTimestamp(long timestamp) {
        sharedPrefs.edit().putLong(PREF_DOWNLOAD_TIMESTAMP_KEY, timestamp).commit();
    }

    private long loadDownloadTimestamp() {
        return sharedPrefs.getLong(PREF_DOWNLOAD_TIMESTAMP_KEY, 0);
    }

}

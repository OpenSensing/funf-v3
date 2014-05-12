package dk.dtu.imm.experiencesampling;

public class Config {

    private static final long SECONDS = 1000;
    private static final long MINUTE = 60 * 1000;
    private static final long HOUR = 60 * 60 * 1000;

    private static final int DEFAULT_QUESTION_PER_DAY_LIMIT = 0; // It won't ask questions per default
    private static final long DEFAULT_QUESTION_SCHEDULE_INTERVAL = 10 * MINUTE;
    private static final long DEFAULT_GPS_TIMEOUT = 30 * SECONDS;
    private static final String DEFAULT_AUTH_PREF_KEY = "sensible_auth";
    private static final String DEFAULT_TOKEN_PREF_KEY = "sensible_token";
    private static final long DEFAULT_FRIENDS_UPDATE_INTERVAL = 48 * HOUR;

    private String authPrefKey = "sensible_auth";
    private String tokenPrefKey = "sensible_token";
    private int dailyQuestionLimit = DEFAULT_QUESTION_PER_DAY_LIMIT;
    private long questionScheduleInterval = DEFAULT_QUESTION_SCHEDULE_INTERVAL;
    private long gpsTimeout = DEFAULT_GPS_TIMEOUT;
    private long friendsUpdateInterval = DEFAULT_FRIENDS_UPDATE_INTERVAL;
    private String tokenSubsetLetters;

    public Config() {
    }

    public Config(String authPrefKey, String tokenPrefKey, Integer dailyQuestionLimit, Long questionScheduleInterval, Long gpsTimeout, Long friendsUpdateInterval, String tokenSubsetLetters) {
        this.authPrefKey = (authPrefKey != null) ? authPrefKey : DEFAULT_AUTH_PREF_KEY;
        this.tokenPrefKey = (tokenPrefKey != null) ? tokenPrefKey : DEFAULT_TOKEN_PREF_KEY;
        this.dailyQuestionLimit = (dailyQuestionLimit != null) ? dailyQuestionLimit : DEFAULT_QUESTION_PER_DAY_LIMIT;
        this.questionScheduleInterval = (questionScheduleInterval != null) ? questionScheduleInterval : DEFAULT_QUESTION_SCHEDULE_INTERVAL;
        this.gpsTimeout = (gpsTimeout != null) ? gpsTimeout : DEFAULT_GPS_TIMEOUT;
        this.friendsUpdateInterval = (friendsUpdateInterval != null) ? friendsUpdateInterval : DEFAULT_FRIENDS_UPDATE_INTERVAL;
        this.tokenSubsetLetters = tokenSubsetLetters;
    }

    public String getAuthPrefKey() {
        return authPrefKey;
    }

    public void setAuthPrefKey(String authPrefKey) {
        this.authPrefKey = authPrefKey;
    }

    public String getTokenPrefKey() {
        return tokenPrefKey;
    }

    public void setTokenPrefKey(String tokenPrefKey) {
        this.tokenPrefKey = tokenPrefKey;
    }

    public int getDailyQuestionLimit() {
        return dailyQuestionLimit;
    }

    public void setDailyQuestionLimit(int dailyQuestionLimit) {
        this.dailyQuestionLimit = dailyQuestionLimit;
    }

    public long getQuestionScheduleInterval() {
        return questionScheduleInterval;
    }

    public void setQuestionScheduleInterval(long questionScheduleInterval) {
        this.questionScheduleInterval = questionScheduleInterval;
    }

    public long getGpsTimeout() {
        return gpsTimeout;
    }

    public void setGpsTimeout(long gpsTimeout) {
        this.gpsTimeout = gpsTimeout;
    }

    public long getFriendsUpdateInterval() {
        return friendsUpdateInterval;
    }

    public void setFriendsUpdateInterval(long friendsUpdateInterval) {
        this.friendsUpdateInterval = friendsUpdateInterval;
    }

    public String getTokenSubsetLetters() {
        return tokenSubsetLetters;
    }

    public void setTokenSubsetLetters(String tokenSubsetLetters) {
        this.tokenSubsetLetters = tokenSubsetLetters;
    }
}

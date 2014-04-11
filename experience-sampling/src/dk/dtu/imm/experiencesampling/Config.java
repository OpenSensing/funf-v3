package dk.dtu.imm.experiencesampling;

public class Config {

    private static final long SECONDS = 1000;
    private static final long MINUTE = 60 * 1000;

    private static final int DEFAULT_QUESTION_PER_DAY_LIMIT = 1;
    private static final long DEFAULT_QUESTION_SCHEDULE_INTERVAL = 1 * MINUTE;
    private static final long DEFAULT_GPS_TIMEOUT = 30 * SECONDS;

    private String authPrefKey = "sensible_auth";
    private String tokenPrefKey = "sensible_token";
    private int questionPerDayLimit = DEFAULT_QUESTION_PER_DAY_LIMIT;
    private long questionScheduleInterval = DEFAULT_QUESTION_SCHEDULE_INTERVAL;
    private long gpsTimeout = DEFAULT_GPS_TIMEOUT;

    public Config() {
    }

    public Config(String authPrefKey, String tokenPrefKey) {
        this.authPrefKey = authPrefKey;
        this.tokenPrefKey = tokenPrefKey;
    }

    public Config(String authPrefKey, String tokenPrefKey, int questionPerDayLimit, long questionScheduleInterval, long gpsTimeout) {
        this.authPrefKey = authPrefKey;
        this.tokenPrefKey = tokenPrefKey;
        this.questionPerDayLimit = questionPerDayLimit;
        this.questionScheduleInterval = questionScheduleInterval;
        this.gpsTimeout = gpsTimeout;
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

    public int getQuestionPerDayLimit() {
        return questionPerDayLimit;
    }

    public void setQuestionPerDayLimit(int questionPerDayLimit) {
        this.questionPerDayLimit = questionPerDayLimit;
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
}

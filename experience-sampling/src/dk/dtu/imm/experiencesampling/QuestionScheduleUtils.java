package dk.dtu.imm.experiencesampling;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class QuestionScheduleUtils {

    private static final String TAG = "QuestionScheduleUtils";
    public static final String PREF_QUESTION_SCHEDULE_KEY = "question_schedule";

    public static boolean isQuestionTime(Context context) {
        boolean questionTime = false;

        ArrayList<Long> todaysQuestionSchedule = loadTodaysQuestionSchedule(context);

        if (todaysQuestionSchedule != null) {

            Iterator<Long> it = todaysQuestionSchedule.iterator();
            while(it.hasNext()) {
                Long time = it.next();
                if (time != null && time < System.currentTimeMillis()) {
                    questionTime = true;
                    // remove the time from the list
                    it.remove();
                }
            }

            // Just to print when it's question time
            Collections.sort(todaysQuestionSchedule);
            for (long time : todaysQuestionSchedule) {
                Log.i(TAG, "Today's scheduled questions: " + new Date(time));
            }

            // Only save the list if question times have been removed - this only happens if it is question time.
            if (questionTime) {
                saveQuestionSchedule(context, Calendar.getInstance(), todaysQuestionSchedule);
            }
        }

        return questionTime;
    }

    public static void scheduleNextDayQuestions(Context context) {
        int dailyLimit = ConfigUtils.getConfigFromPrefs(context).getDailyQuestionLimit();

        // Add the schedule of today if no exist
        Calendar calendar = Calendar.getInstance();
        if (!isDayScheduled(context, calendar) && dailyLimit > 0) {
            saveQuestionSchedule(context, calendar, getDayQuestionTimeSchedule(calendar, dailyLimit));
        }

        // Add the schedule of tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        if (!isDayScheduled(context, calendar) && dailyLimit > 0) {
            saveQuestionSchedule(context, calendar, getDayQuestionTimeSchedule(calendar, dailyLimit));
        }
    }

    public static void addASAPQuestionTime(Context context) {
        ArrayList<Long> todaysQuestionSchedule = loadTodaysQuestionSchedule(context);
        if (todaysQuestionSchedule != null) {
            todaysQuestionSchedule.add(System.currentTimeMillis()); // ask question when possible
        }
        saveQuestionSchedule(context, Calendar.getInstance(), todaysQuestionSchedule);
        Log.i(TAG, "ASAP question added");
    }

    private static ArrayList<Long> getDayQuestionTimeSchedule(Calendar calendar, int dailyLimit) {
        int dayInterval = (int)(getEndOfDay(calendar) - getStartOfDay(calendar));

        ArrayList<Long> nextDayTimeSchedule = new ArrayList<Long>();
        for (int i = 0; i < dailyLimit; i++) {
            int randomTime = new Random().nextInt(dayInterval);
            long questionTime = getStartOfDay(calendar) + randomTime;
            nextDayTimeSchedule.add(questionTime);
        }

        return nextDayTimeSchedule;
    }

    private static long getStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long getEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    private static boolean isDayScheduled(Context context, Calendar calendar) {
        HashMap<String, ArrayList<Long>> questionSchedule = loadQuestionSchedule(context);
        if (questionSchedule != null) {
            return questionSchedule.containsKey(getDateKey(calendar));
        }
        return false;
    }

    private static ArrayList<Long> loadTodaysQuestionSchedule(Context context) {
        Calendar calendar = Calendar.getInstance();
        String currentDateKey = getDateKey(calendar);

        HashMap<String, ArrayList<Long>> questionSchedule = loadQuestionSchedule(context);
        if (questionSchedule != null && questionSchedule.containsKey(currentDateKey)) {
            return questionSchedule.get(currentDateKey);
        }
        return null;
    }

    private static HashMap<String, ArrayList<Long>> loadQuestionSchedule(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dayScheduleJson = sharedPrefs.getString(PREF_QUESTION_SCHEDULE_KEY, null);
        try {
            if (dayScheduleJson != null) {
                HashMap<String, ArrayList<Long>> questionSchedule = new ObjectMapper().readValue(dayScheduleJson, HashMap.class);
                return questionSchedule;
            }
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error in stored question schedule a new schedule will be created");
        }
        return null;
    }

    private static void saveQuestionSchedule(Context context, Calendar calendar, ArrayList<Long> questionTimes) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Create key from calender date
        String dateKey = getDateKey(calendar);

        // Check if existing schedule is in pref
        HashMap<String, ArrayList<Long>> questionSchedule = loadQuestionSchedule(context);

        // If not - create it:
        if (questionSchedule == null) {
            questionSchedule = new HashMap<String, ArrayList<Long>>();
        }

        // Clean schedules except today and tomorrows
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_MONTH, 1);

        String todaysDateKey = getDateKey(Calendar.getInstance());
        String tomorrowDateKey = getDateKey(nextDay);

        Iterator<String> it = questionSchedule.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            if (!key.equals(todaysDateKey) && !key.equals(tomorrowDateKey)) {
                it.remove();
            }
        }

        // Put new daily schedule
        questionSchedule.put(dateKey, questionTimes);

        // Save modified schedule
        try {
            sharedPrefs.edit().putString(PREF_QUESTION_SCHEDULE_KEY, new ObjectMapper().writeValueAsString(questionSchedule)).apply();
            Log.i(TAG, "question schedule saved for date: " + dateKey);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Error while saving question schedule");
        }
    }

    private static String getDateKey(Calendar calendar) {
        return String.format("%s-%s-%s", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

}

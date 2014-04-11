package dk.dtu.imm.experiencesampling.handlers;

import android.util.Log;

// todo: not necessary when used within the sensible dtu data-collector
public class UncaughtException implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "UncaughtException";

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "There was an uncaught exception: " + ex.getMessage());
        ex.printStackTrace();
        // ignore - the app is scheduled to start again later
    }
}

package dk.dtu.imm.experiencesampling.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;

import java.util.Arrays;
import java.util.HashSet;

public class QuestionsProvider extends ContentProvider {

    // database
    private DatabaseHelper database;

    // used for the UriMatcher
    private static final int QUESTIONS = 10;

    private static final String AUTHORITY = "dk.dtu.imm.experiencesampling.answers.contentprovider";

    private static final String BASE_PATH = "answers";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/answers";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, BASE_PATH, QUESTIONS);
    }

    @Override
    public synchronized boolean onCreate() {
        database = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Checks if the caller has requested a column which does not exists
        checkColumns(projection);

        // Sets the table
        queryBuilder.setTables(DatabaseHelper.TABLE_QUESTION_ANSWER);

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case QUESTIONS:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDbInstance();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Makes sure that potential listeners are getting notified
        if (cursor != null && getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public synchronized String getType(Uri uri) {
        return null;
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Only the query operation is provided");
    }

    @Override
    public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Only the query operation is provided");
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Only the query operation is provided");
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                DatabaseHelper.QUESTION_TYPE,
                DatabaseHelper.QUESTION_ANSWER_TYPE,
                DatabaseHelper.QUESTION_ANSWER,
                DatabaseHelper.QUESTION_TIMESTAMP,
                DatabaseHelper._ID };

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}

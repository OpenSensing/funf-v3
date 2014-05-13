package dk.dtu.imm.experiencesampling.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.exceptions.NotEnoughFriendsException;
import dk.dtu.imm.experiencesampling.exceptions.NotEnoughFriendConnectionsException;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.FriendConnection;
import dk.dtu.imm.experiencesampling.models.Place;
import dk.dtu.imm.experiencesampling.models.answers.*;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestion;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestionOneFriend;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestionTwoFriends;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";

    // database configuration
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sensible_questions.db";

    // tables
    public static final String TABLE_QUESTION_ANSWER = "answers";
    private static final String TABLE_PENDING_QUESTION = "pending_questions";
    private static final String TABLE_PLACE = "places";
    private static final String TABLE_FRIEND = "friends";
    private static final String TABLE_FRIEND_CONNECTION = "friend_connections";

    // general columns
    public static final String _ID = "_id"; // _id is required for the cursor

    // question table columns
    public static final String QUESTION_TYPE = "question_type";
    public static final String QUESTION_ANSWER_TYPE = "answer_type";
    public static final String QUESTION_ANSWER = "answer";
    public static final String QUESTION_TIMESTAMP = "question_timestamp";

    // pending questions table columns
    private static final String PENDING_QUESTION_TYPE = "pending_question_type";
    private static final String PENDING_QUESTION_DATA = "pending_question_data";

    // location/place labels columns
    private static final String PLACE_LABEL = "place_label";
    private static final String PLACE_COUNT = "place_count";

    // friend list columns
    private static final String FRIEND_FB_UID = "friend_fb_uid";
    private static final String FRIEND_NAME = "friend_name";
    private static final String FRIEND_RATE_ONE_FRIEND_ANSWERED = "rate_two_friends_answered";

    // friend connection list columns
    private static final String CONNECTION_FB_UID1 = "friend_fb_uid1";
    private static final String CONNECTION_FB_UID2 = "friend_fb_uid2";
    private static final String CONNECTION_CLOSER_FRIEND_ANSWERED = "closer_friend_answered";
    private static final String CONNECTION_RATE_TWO_FRIENDS_ANSWERED = "rate_two_friends_answered";

    private DatabaseOpenHelper openHelper;
    private SQLiteDatabase database;

    // this is a wrapper class. that means, from outside world, anyone will communicate with DatabaseHelper,
    // but under the hood actually DatabaseOpenHelper class will perform database CRUD operations
    public DatabaseHelper(Context aContext) {
        if (openHelper == null) {
            openHelper = new DatabaseOpenHelper(aContext);
            database = openHelper.getWritableDatabase();
        }
    }

    public void closeDatabase() {
        if (database != null && openHelper != null) {
            database.close();
            openHelper.close();
            openHelper = null;
        }
    }

    public SQLiteDatabase getWritableDbInstance() {
        return database;
    }

    /*
        Reset friend/friends asked boolean.
        Question with the same friend/friends are only asked once, but if this reset is done, then question regarding all the friends will be asked again at some point.
        Note: The stored answers won't be deleted.
     */
    public void resetFriendAskedBooleans() {
        String buildSQL = String.format("UPDATE %s SET %s = 0, %s = 0", TABLE_FRIEND_CONNECTION, CONNECTION_CLOSER_FRIEND_ANSWERED, CONNECTION_RATE_TWO_FRIENDS_ANSWERED);
        database.execSQL(buildSQL);

        buildSQL = String.format("UPDATE %s SET %s = 0", TABLE_FRIEND, FRIEND_RATE_ONE_FRIEND_ANSWERED);
        database.execSQL(buildSQL);
    }

    /*
        Friends
     */
    public Friend getRandomFriend(QuestionType questionType) throws NotEnoughFriendsException {
        String buildSQL = "SELECT * FROM " + TABLE_FRIEND + " ORDER BY RANDOM() LIMIT 1";
        if (QuestionType.SOCIAL_RATE_ONE_FRIEND.equals(questionType)) {
            buildSQL = "SELECT * FROM " + TABLE_FRIEND + " WHERE " + FRIEND_RATE_ONE_FRIEND_ANSWERED + " = 0 ORDER BY RANDOM() LIMIT 1";
        }

        Cursor cursor = database.rawQuery(buildSQL, null);
        try {
            // There should at least be 1 friend in order to ask rate one friend questions.
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursorToFriend(cursor);
            }
        } finally {
            cursor.close();
        }
        throw new NotEnoughFriendsException("There is not enough friends: " + cursor.getCount());
    }

    private Friend getFriend(String uid) throws NotEnoughFriendsException {
        String buildSQL = "SELECT * FROM " + TABLE_FRIEND + " WHERE " + FRIEND_FB_UID + "='" + uid + "'";
        Cursor cursor = database.rawQuery(buildSQL, null);
        try {
            // There should at least be 1 friend in order to ask rate one friend questions.
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursorToFriend(cursor);
            }
        } finally {
            cursor.close();
        }
        throw new NotEnoughFriendsException("There is not enough friends: " + cursor.getCount());
    }

    public List<Friend> getTwoConnectedFriends(QuestionType questionType) throws NotEnoughFriendConnectionsException, NotEnoughFriendsException {
        List<Friend> friends = new ArrayList<Friend>();
        FriendConnection friendConn = getRandomFriendConnection(questionType);

        friends.add(getFriend(friendConn.getUid1()));
        friends.add(getFriend(friendConn.getUid2()));
        return friends;
    }

    public void updateFriendAnswered(String uid, QuestionType questionType) {
        String buildSQL = null;
        if (QuestionType.SOCIAL_RATE_ONE_FRIEND.equals(questionType)) {
            buildSQL = String.format("UPDATE %s SET %s = 1 WHERE %s = '%s'", TABLE_FRIEND, FRIEND_RATE_ONE_FRIEND_ANSWERED, FRIEND_FB_UID, uid);
        }
        if (buildSQL != null) {
            database.execSQL(buildSQL);
        }
    }

    public void insertFriends(Friend[] friends) {
        for (Friend friend : friends) {
            insertFriend(friend);
        }
    }

    public void insertFriend(Friend friend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FRIEND_FB_UID, friend.getUserId());
        contentValues.put(FRIEND_NAME, friend.getName());
        database.insertWithOnConflict(TABLE_FRIEND, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private Friend cursorToFriend(Cursor c) {
        Friend friend = new Friend();
        friend.setUserId(c.getString(c.getColumnIndex(FRIEND_FB_UID)));
        friend.setName(c.getString(c.getColumnIndex(FRIEND_NAME)));
        return friend;
    }

    /*
        Friend connections
     */
    public FriendConnection getRandomFriendConnection(QuestionType questionType) throws NotEnoughFriendConnectionsException {
        String buildSQL = "SELECT * FROM " + TABLE_FRIEND_CONNECTION + " ORDER BY RANDOM() LIMIT 1";

        if (QuestionType.SOCIAL_CLOSER_FRIEND.equals(questionType)) {
            buildSQL = "SELECT * FROM " + TABLE_FRIEND_CONNECTION + " WHERE " + CONNECTION_CLOSER_FRIEND_ANSWERED + " = 0 ORDER BY RANDOM() LIMIT 1";
        } else if (QuestionType.SOCIAL_RATE_TWO_FRIENDS.equals(questionType)) {
            buildSQL = "SELECT * FROM " + TABLE_FRIEND_CONNECTION + " WHERE " + CONNECTION_RATE_TWO_FRIENDS_ANSWERED + " = 0 ORDER BY RANDOM() LIMIT 1";
        }

        Cursor cursor = database.rawQuery(buildSQL, null);
        try {
            // There should at least be 1 connection in order to ask rate two friends or closer friend questions.
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursorToConnection(cursor);
            }
        } finally {
            cursor.close();
        }
        throw new NotEnoughFriendConnectionsException("There is not enough friend connections: " + cursor.getCount());
    }

    public void insertFriendConnections(FriendConnection[] friendConns) {
        for (FriendConnection friendConn : friendConns) {
            insertFriendConnection(friendConn);
        }
    }

    public void insertFriendConnection(FriendConnection friendConn) {
        // Sort the uids so that it does not contain two connections in the example: 1111 - 2222 and 2222 - 1111. Now there will always be one as 1111 - 2222
        List<String> orderedUids = new ArrayList<String>();
        orderedUids.add(friendConn.getUid1());
        orderedUids.add(friendConn.getUid2());
        Collections.sort(orderedUids);

        ContentValues contentValues = new ContentValues();
        contentValues.put(CONNECTION_FB_UID1, orderedUids.get(0));
        contentValues.put(CONNECTION_FB_UID2, orderedUids.get(1));

        database.insertWithOnConflict(TABLE_FRIEND_CONNECTION, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void updateFriendConnectionAnswered(String uid1, String uid2, QuestionType questionType) {
        List<String> orderedUids = new ArrayList<String>();
        orderedUids.add(uid1);
        orderedUids.add(uid2);
        Collections.sort(orderedUids);

        String buildSQL = null;
        if (QuestionType.SOCIAL_CLOSER_FRIEND.equals(questionType)) {
            buildSQL = String.format("UPDATE %s SET %s = 1 WHERE %s = '%s' AND %s ='%s'", TABLE_FRIEND_CONNECTION, CONNECTION_CLOSER_FRIEND_ANSWERED, CONNECTION_FB_UID1, orderedUids.get(0), CONNECTION_FB_UID2, orderedUids.get(1));
        } else if (QuestionType.SOCIAL_RATE_TWO_FRIENDS.equals(questionType)) {
            buildSQL = String.format("UPDATE %s SET %s = 1 WHERE %s = '%s' AND %s ='%s'", TABLE_FRIEND_CONNECTION, CONNECTION_RATE_TWO_FRIENDS_ANSWERED, CONNECTION_FB_UID1, orderedUids.get(0), CONNECTION_FB_UID2, orderedUids.get(1));
        }

        if (buildSQL != null) {
            database.execSQL(buildSQL);
        }
    }

    private FriendConnection cursorToConnection(Cursor c) {
        FriendConnection friendConn = new FriendConnection();
        friendConn.setUid1(c.getString(c.getColumnIndex(CONNECTION_FB_UID1)));
        friendConn.setUid2(c.getString(c.getColumnIndex(CONNECTION_FB_UID2)));
        return friendConn;
    }

    /*
        Place labels operations
     */
    public void insertPlaceLabel(String placeLabel) {
        if (placeLabel != null && !placeLabel.isEmpty()) {
            Place place = readPlace(placeLabel);
            if (place == null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PLACE_LABEL, placeLabel);
                contentValues.put(PLACE_COUNT, 1);
                database.insert(TABLE_PLACE, null, contentValues);
                Log.d(TAG, "Place added: " + placeLabel);
            } else {
                updatePlace(place);
            }
        }
    }

    private void updatePlace(Place place) {
        int count = place.getCount() + 1;

        ContentValues values = new ContentValues();
        values.put(PLACE_LABEL, place.getPlace());
        values.put(PLACE_COUNT, count);

        String whereClause = PLACE_LABEL + " = ?";
        String[] whereArgs = {place.getPlace()};
        database.update(TABLE_PLACE, values, whereClause, whereArgs);
        Log.d(TAG, "Place updated: " + place.getPlace() + ", count: " + count);
    }

    private Place readPlace(String placeLabel) {
        Place place = null;
        String buildSQL = "SELECT * FROM " + TABLE_PLACE + " WHERE " + PLACE_LABEL + " = '" + placeLabel + "'";
        List<Place> places = readPlaces(buildSQL);
        if (places.size() > 0) {
            place = places.get(0);
        }
        return place;
    }

    public Place cursorToPlace(Cursor c) {
        Place place = new Place();
        place.setPlace(c.getString(c.getColumnIndex(PLACE_LABEL)));
        place.setCount(c.getInt(c.getColumnIndex(PLACE_COUNT)));
        return place;
    }


    /*
    public void deletePlaceLabel(String placeLabel) {
        String buildSQL = "DELETE FROM " + TABLE_PLACE + " WHERE " + PLACE_LABEL + " = '" + placeLabel + "'";
        database.execSQL(buildSQL, null);
    }
    */


    public List<Place> readTopPlaces(int top) {
        String buildSQL = "SELECT * FROM " + TABLE_PLACE + " ORDER BY " + PLACE_COUNT + " DESC, " + PLACE_LABEL + " LIMIT " + top;
        return readPlaces(buildSQL);
    }

    public List<Place> readAllPlaces() {
        String buildSQL = "SELECT * FROM " + TABLE_PLACE + " ORDER BY " + PLACE_LABEL;
        return readPlaces(buildSQL);
    }

    private List<Place> readPlaces(String sql) {
        List<Place> places = new ArrayList<Place>();
        Cursor cursor = database.rawQuery(sql, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    places.add(cursorToPlace(cursor));
                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }
        return places;
    }

    /*
        Pending questions operations
     */
    public int getPendingQuestionsCount() {
        String buildSQL = "SELECT * FROM " + TABLE_PENDING_QUESTION;
        Cursor cursor = database.rawQuery(buildSQL, null);
        try {
            return cursor.getCount();
        } finally {
            cursor.close();
        }
    }

    public void insertPendingQuestions(Set<PendingQuestion> pendingQuestions) {
        for (PendingQuestion pendingQuestion : pendingQuestions) {
            insertPendingQuestion(pendingQuestion);
        }
    }

    public void insertPendingQuestion(PendingQuestion pendingQuestion) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonData = mapper.writeValueAsString(pendingQuestion);
            Log.d(TAG, "Saving pending question: " + jsonData);

            ContentValues contentValues = new ContentValues();
            contentValues.put(PENDING_QUESTION_TYPE, pendingQuestion.getQuestionType().name());
            contentValues.put(PENDING_QUESTION_DATA, jsonData);
            database.insert(TABLE_PENDING_QUESTION, null, contentValues);
        } catch (JsonProcessingException e) {
            Log.d(TAG, "Parse error during db insert: " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Error during db insert: " + e.getMessage());
        }
    }

    public PendingQuestion popNextPendingQuestion() {
        PendingQuestion pendingQuestion = null;
        String sql = "SELECT * FROM " + TABLE_PENDING_QUESTION + " ORDER BY " + _ID + " LIMIT 1";
        Cursor c = database.rawQuery(sql, null);
        Integer id = null;
        try {
            if (c.getCount() > 0) {
                c.moveToFirst();
                try {
                    pendingQuestion = cursorToPendingQuestion(c);
                } catch (IOException e) {
                    Log.e(TAG, "Error while parsing pending question json");
                    e.printStackTrace();
                }

                id = c.getInt(c.getColumnIndex(_ID));
            }
        } finally {
            c.close();
        }

        if (id != null) {
            deletePendingQuestion(id);
        }
        return pendingQuestion;
    }

    private void deletePendingQuestion(int id) {
        database.delete(TABLE_PENDING_QUESTION, _ID + "=?", new String[] {Integer.toString(id)});
    }

    private PendingQuestion cursorToPendingQuestion(Cursor c) throws IOException {
        PendingQuestion p;
        ObjectMapper mapper = new ObjectMapper();

        String jsonData = c.getString(c.getColumnIndex(PENDING_QUESTION_DATA));
        QuestionType type = QuestionType.getQuestionType(c.getString(c.getColumnIndex(PENDING_QUESTION_TYPE)));
        switch (type) {
            case SOCIAL_RATE_ONE_FRIEND:
                p = mapper.readValue(jsonData, PendingQuestionOneFriend.class);
                break;
            case SOCIAL_CLOSER_FRIEND:
            case SOCIAL_RATE_TWO_FRIENDS:
                p = mapper.readValue(jsonData, PendingQuestionTwoFriends.class);
                break;
            case LOCATION_CURRENT:
            case LOCATION_PREVIOUS:
                p = mapper.readValue(jsonData, PendingQuestion.class);
                break;
            default:
                throw new IOException("No valid question type");
        }
        return p;
    }

    /*
        Question answers operations
     */
    public void insertAnswer(Answer answer) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonData = mapper.writeValueAsString(answer);
            Log.d(TAG, "Saving: " + jsonData);

            ContentValues contentValues = new ContentValues();
            contentValues.put(QUESTION_TYPE, answer.getQuestionType().name());
            contentValues.put(QUESTION_ANSWER_TYPE, answer.getAnswerType().name());
            contentValues.put(QUESTION_ANSWER, jsonData);
            contentValues.put(QUESTION_TIMESTAMP, answer.getEndTimestamp() / 1000);
            database.insert(TABLE_QUESTION_ANSWER, null, contentValues);
        } catch (JsonProcessingException e) {
            Log.d(TAG, "Parse error during db insert: " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Error during db insert: " + e.getMessage());
        }
    }

    public List<Answer> readAnswers(QuestionType type) {
        String buildSQL = "SELECT * FROM " + TABLE_QUESTION_ANSWER + " WHERE " + QUESTION_TYPE + " = '" + type + "'";
        return readAnswers(buildSQL);
    }

    private List<Answer> readAnswers(String sql) {
        List<Answer> answers = new ArrayList<Answer>();
        Cursor cursor = database.rawQuery(sql, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    try {
                        Answer q = cursorToAnswer(cursor);
                        answers.add(q);
                    } catch (IOException e) {
                        Log.d(TAG, "Error during cursorToAnswer: " + e.getMessage());
                    }
                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }
        return answers;
    }

    private Answer cursorToAnswer(Cursor c) throws IOException {
        Answer a;
        ObjectMapper mapper = new ObjectMapper();

        String jsonData = c.getString(c.getColumnIndex(QUESTION_ANSWER));
        QuestionType type = QuestionType.getQuestionType(c.getString(c.getColumnIndex(QUESTION_TYPE)));
        switch (type) {
            case SOCIAL_CLOSER_FRIEND:
                a = mapper.readValue(jsonData, CloserFriend.class);
                break;
            case SOCIAL_RATE_ONE_FRIEND:
                a = mapper.readValue(jsonData, RateOneFriend.class);
                break;
            case SOCIAL_RATE_TWO_FRIENDS:
                a = mapper.readValue(jsonData, RateTwoFriends.class);
                break;
            case LOCATION_CURRENT:
                a = mapper.readValue(jsonData, CurrentLocation.class);
                break;
            case LOCATION_PREVIOUS:
                a = mapper.readValue(jsonData, PreviousLocation.class);
                break;
            default:
                throw new IOException("No valid question type");
        }
        return a;
    }

    /**
     * this DatabaseOpenHelper class will actually be used to perform database related operation
     */
    private class DatabaseOpenHelper extends SQLiteOpenHelper {

        public DatabaseOpenHelper(Context aContext) {
            super(aContext, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            // Create question table
            String buildSQL = "CREATE TABLE " + TABLE_QUESTION_ANSWER + "( " +
                    _ID + " INTEGER PRIMARY KEY, " +
                    QUESTION_TYPE + " TEXT, "  +
                    QUESTION_ANSWER_TYPE + " TEXT, "  +
                    QUESTION_ANSWER + " TEXT, "  +
                    QUESTION_TIMESTAMP + " INTEGER )";
            Log.d(TAG, "onCreate SQL question table: " + buildSQL);
            sqLiteDatabase.execSQL(buildSQL);

            // Create pending question table
            buildSQL = "CREATE TABLE " + TABLE_PENDING_QUESTION + "( " +
                    _ID + " INTEGER PRIMARY KEY, " +
                    PENDING_QUESTION_TYPE + " TEXT, "  +
                    PENDING_QUESTION_DATA + " TEXT )";
            Log.d(TAG, "onCreate SQL pending question table: " + buildSQL);
            sqLiteDatabase.execSQL(buildSQL);

            // Create place label table
            buildSQL = "CREATE TABLE " + TABLE_PLACE + "( " +
                    _ID + " INTEGER PRIMARY KEY, " +
                    PLACE_LABEL + " TEXT, " +
                    PLACE_COUNT + " INTEGER )";
            Log.d(TAG, "onCreate SQL place label table: " + buildSQL);
            sqLiteDatabase.execSQL(buildSQL);

            // Create friend table
            buildSQL = "CREATE TABLE " + TABLE_FRIEND + "( " +
                    _ID + " INTEGER PRIMARY KEY, " +
                    FRIEND_FB_UID + " TEXT NOT NULL UNIQUE, "  +
                    FRIEND_NAME + " TEXT, " +
                    FRIEND_RATE_ONE_FRIEND_ANSWERED + " INTEGER DEFAULT 0 )";
            Log.d(TAG, "onCreate SQL friend table: " + buildSQL);
            sqLiteDatabase.execSQL(buildSQL);

            // Create friend connection table
            buildSQL = "CREATE TABLE " + TABLE_FRIEND_CONNECTION + "( " +
                    _ID + " INTEGER PRIMARY KEY, " +
                    CONNECTION_FB_UID1 + " TEXT NOT NULL, "  +
                    CONNECTION_FB_UID2 + " TEXT NOT NULL, " +
                    CONNECTION_CLOSER_FRIEND_ANSWERED + " INTEGER DEFAULT 0, " +
                    CONNECTION_RATE_TWO_FRIENDS_ANSWERED + " INTEGER DEFAULT 0, " +
                    "UNIQUE(" + CONNECTION_FB_UID1 + ", " + CONNECTION_FB_UID2 + ") ON CONFLICT IGNORE )";
            Log.d(TAG, "onCreate SQL friend connection table: " + buildSQL);
            sqLiteDatabase.execSQL(buildSQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // drop previous question answer table
            dropTable(sqLiteDatabase, TABLE_QUESTION_ANSWER);

            // drop previous pending question table
            dropTable(sqLiteDatabase, TABLE_PENDING_QUESTION);

            // drop previous place table
            dropTable(sqLiteDatabase, TABLE_PLACE);

            // drop previous friend table
            dropTable(sqLiteDatabase, TABLE_FRIEND);

            // drop previous friend connection table
            dropTable(sqLiteDatabase, TABLE_FRIEND_CONNECTION);

            // create the tables from the beginning
            onCreate(sqLiteDatabase);
        }

        private void dropTable(SQLiteDatabase sqLiteDatabase, String table) {
            String buildSQL = "DROP TABLE IF EXISTS " + table;
            Log.d(TAG, "onUpgrade SQL: " + buildSQL);
            sqLiteDatabase.execSQL(buildSQL);
        }

    }
}

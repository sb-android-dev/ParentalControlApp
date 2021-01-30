package com.schoolmanager.utilities;

import android.provider.BaseColumns;

public class DBEntry implements BaseColumns {

    public static final String KEY_SCAN_ID = "scan_id";
    public static final String KEY_STUDENT_ID = "student_id";
    public static final String KEY_TRACK_STATUS = "track_status";
    public static final String KEY_TRACK_TIME = "track_time";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_TYPE = "user_type";

    public static final String TABLE_TRACK = "track_table";

    static final String CREATE_TABLE_TRACK = "CREATE TABLE " + TABLE_TRACK + "("
            + KEY_SCAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_USER_ID + " TEXT, "
            + KEY_USER_TOKEN + " TEXT, " + KEY_USER_TYPE + " TEXT, "
            + KEY_STUDENT_ID + " TEXT, " + KEY_TRACK_STATUS + " TEXT, "
            + KEY_TRACK_TIME + " TEXT)";


    static final String SELECT_SCAN_ITEM = "SELECT * FROM " + TABLE_TRACK;
}

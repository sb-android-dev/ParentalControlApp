package com.schoolmanager.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.schoolmanager.model.ScanItem;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pca.db";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBEntry.CREATE_TABLE_TRACK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long addScanItem(ScanItem scanItem) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBEntry.KEY_USER_ID, scanItem.getUserId());
        cv.put(DBEntry.KEY_USER_TOKEN, scanItem.getUserToken());
        cv.put(DBEntry.KEY_USER_TYPE, scanItem.getUserType());
        cv.put(DBEntry.KEY_STUDENT_ID, scanItem.getStudentId());
        cv.put(DBEntry.KEY_TRACK_STATUS, scanItem.getTrackStatus());
        cv.put(DBEntry.KEY_TRACK_TIME, scanItem.getTrackTime());

        long i = db.insert(DBEntry.TABLE_TRACK, null, cv);
        db.close();
        return i;
    }

    public ScanItem getScanItem() {
        ScanItem scanItem = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(DBEntry.SELECT_SCAN_ITEM, null);
        if (cursor.moveToFirst()) {
            scanItem = new ScanItem();
            scanItem.setScanId(cursor.getLong(0));
            scanItem.setUserId(cursor.getString(1));
            scanItem.setUserToken(cursor.getString(2));
            scanItem.setUserType(cursor.getString(3));
            scanItem.setStudentId(cursor.getString(4));
            scanItem.setTrackStatus(cursor.getString(5));
            scanItem.setTrackTime(cursor.getString(6));

            cursor.close();
        }
        db.close();

        return scanItem;
    }

//    public int updateVideoStatus(long videoId, int status) {
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(DBEntry.KEY_VIDEO_STATUS, status);
//        int i = db.update(DBEntry.TABLE_VIDEO, cv, DBEntry.KEY_VIDEO_ID + "=?",
//                new String[]{String.valueOf(videoId)});
//        db.close();
//        return i;
//    }

    public int deleteScan(long scanId) {
        SQLiteDatabase db = getWritableDatabase();
        int i = db.delete(DBEntry.TABLE_TRACK, DBEntry.KEY_SCAN_ID + "=?",
                new String[]{String.valueOf(scanId)});
        db.close();
        return i;
    }

}

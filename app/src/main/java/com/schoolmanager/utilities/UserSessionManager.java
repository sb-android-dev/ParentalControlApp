package com.schoolmanager.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;


public class UserSessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    // Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;

    private static final String PREFER_NAME = "UserSession";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_TYPE = "user_type";
    public static final String KEY_USER_PHONE = "user_phone";
    public static final String KEY_USER_IMAGE = "user_image";
    public static final String KEY_STUDENT_ID = "student_id";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_FCM_TOKEN = "fcm_token";

    public static final String KEY_DRIVER_ID = "driver_id";
    public static final String KEY_DRIVER_NAME = "driver_name";
    public static final String KEY_DRIVER_PHONE = "driver_phone";

    public static final String KEY_IS_BUS_NOTIFICATION_RECEIVED = "is_bus_notification_received";
    public static final String KEY_TODAY_S_DAY = "today_s_day";
    public static final String KEY_IS_COMPLAINT_REGISTERED = "is_complaint_registered";

    public static final String KEY_IS_LAST_SEEN_ENABLED = "is_last_seen_enabled";
    public static final String KEY_IS_READ_UNREAD_MESSAGES_ENABLED = "is_read_unread_message_enabled";
    public static final String KEY_RECEIVE_CALL = "receive_call";

    public static final String KEY_LANGUAGE_CODE = "lang_code";
    public static final String KEY_THEME_VALUE = "theme_value";

    public UserSessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
        //editor1=pref.edit();
    }

    public void upsertDeviceId(String deviceId) {
        editor.putString(KEY_DEVICE_ID, deviceId);
        editor.commit();
    }

    public void upsertFcmToken(String fcmToken) {
        editor.putString(KEY_FCM_TOKEN, fcmToken);
        editor.commit();
    }


    public void setLanguage(String code){
        editor.putString(KEY_LANGUAGE_CODE, code);
        editor.commit();
    }

    public void setTheme(int value){
        editor.putInt(KEY_THEME_VALUE, value);
        editor.commit();
    }

    public void createUserLoginSession(int userId, String userToken, String userName, int userType,
                                       String userPhone, String userImage) {
        Log.e("createUserLoginSession", "createUserLoginSession");
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TOKEN, userToken);
        editor.putString(KEY_USER_NAME, userName);
        editor.putInt(KEY_USER_TYPE, userType);
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.putString(KEY_USER_IMAGE, userImage);
        editor.commit();
    }

    public void createUserLoginSession(int userId, String userToken, String userName, int userType,
                                       String userPhone, String userImage, int studentId) {
        Log.e("createUserLoginSession", "createUserLoginSession");
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TOKEN, userToken);
        editor.putString(KEY_USER_NAME, userName);
        editor.putInt(KEY_USER_TYPE, userType);
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.putString(KEY_USER_IMAGE, userImage);
        editor.putInt(KEY_STUDENT_ID, studentId);
        editor.commit();
    }

    public void upsertDriver(int driverId, String driverName, String driverPhone) {
        editor.putInt(KEY_DRIVER_ID, driverId);
        editor.putString(KEY_DRIVER_NAME, driverName);
        editor.putString(KEY_DRIVER_PHONE, driverPhone);
        editor.commit();
    }

    public void updateLastSeenFlag(boolean isLastSeenEnabled) {
        editor.putBoolean(KEY_IS_LAST_SEEN_ENABLED, isLastSeenEnabled);
        editor.commit();
    }

    public void updateReadUnreadMessagesFlag(boolean isReadUnreadMessagesEnabled) {
        editor.putBoolean(KEY_IS_READ_UNREAD_MESSAGES_ENABLED, isReadUnreadMessagesEnabled);
        editor.commit();
    }

//    public void updateUserLoginSession(String fname, String mname, String lname, String dob, String address1,
//                                       String address2, String city, String state, String country, int zipcode) {
//        Log.e("updateUserLoginSession","updateUserLoginSession");
//        editor.putString(KEY_FNAME, fname);
//        editor.putString(KEY_MNAME, mname);
//        editor.putString(KEY_LNAME, lname);
//        editor.putString(KEY_DOB, dob);
//        editor.putString(KEY_ADDRESS_1, address1);
//        editor.putString(KEY_ADDRESS_2, address2);
//        editor.putString(KEY_CITY, city);
//        editor.putString(KEY_STATE, state);
//        editor.putString(KEY_COUNTRY, country);
//        editor.putString(KEY_ZIPCODE, String.valueOf(zipcode));
//        editor.commit();
//    }

//    public void addLTime(String lTime){
//        Log.e("NotificationTime","add Last Check Time");
//        editor.putString(KEY_lTime, lTime);
//        editor.commit();
//    }

    public void updateImageUrl(String imageUrl) {
        editor.putString(KEY_USER_IMAGE, imageUrl);
        editor.commit();
    }

    public void updateNotificationStatus(boolean isReceived) {
        editor.putBoolean(KEY_IS_BUS_NOTIFICATION_RECEIVED, isReceived);
        editor.commit();
    }

    public void updateTodaySDay(int day) {
        editor.putInt(KEY_TODAY_S_DAY, day);
        editor.commit();
    }

    public void registerComplaint(boolean registerComplaint) {
        editor.putBoolean(KEY_IS_COMPLAINT_REGISTERED, registerComplaint);
        editor.commit();
    }

    public void UserLoginSession(String accId) {
        Log.e("UserLoginSession", "UserLoginSession");
        editor.putString(KEY_USER_ID, accId);

        editor.commit();
    }

    public HashMap<String, String> getUserDetails() {
        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<>();
        // user name
        user.put(KEY_USER_ID, String.valueOf(pref.getInt(KEY_USER_ID, 0)));
        user.put(KEY_USER_TOKEN, pref.getString(KEY_USER_TOKEN, ""));
        user.put(KEY_USER_NAME, pref.getString(KEY_USER_NAME, ""));
        user.put(KEY_USER_TYPE, String.valueOf(pref.getInt(KEY_USER_TYPE, 0)));
        user.put(KEY_USER_PHONE, pref.getString(KEY_USER_PHONE, ""));
        user.put(KEY_USER_IMAGE, pref.getString(KEY_USER_IMAGE, ""));
        user.put(KEY_DEVICE_ID, pref.getString(KEY_DEVICE_ID, ""));
        user.put(KEY_STUDENT_ID, String.valueOf(pref.getInt(KEY_STUDENT_ID, 0)));
        user.put(KEY_DRIVER_ID, String.valueOf(pref.getInt(KEY_DRIVER_ID, 0)));
        return user;
    }

    public HashMap<String, String> getEssentials() {
        HashMap<String, String> id = new HashMap<>();
        id.put(KEY_USER_ID, String.valueOf(pref.getInt(KEY_USER_ID, 0)));
        id.put(KEY_USER_TOKEN, pref.getString(KEY_USER_TOKEN, ""));
        id.put(KEY_USER_TYPE, String.valueOf(pref.getInt(KEY_USER_TYPE, 0)));
        id.put(KEY_DEVICE_ID, pref.getString(KEY_DEVICE_ID, ""));
        return id;
    }

    public int getUserType() {
        return pref.getInt(KEY_USER_TYPE, 0);
    }

    public HashMap<String, String> getDriverDetails() {
        HashMap<String, String> driver = new HashMap<>();
        driver.put(KEY_DRIVER_ID, String.valueOf(pref.getInt(KEY_DRIVER_ID, 0)));
        driver.put(KEY_DRIVER_NAME, pref.getString(KEY_DRIVER_NAME, ""));
        driver.put(KEY_DRIVER_PHONE, pref.getString(KEY_DRIVER_PHONE, ""));
        return driver;
    }

    public String getStudentId() {
        return String.valueOf(pref.getInt(KEY_STUDENT_ID, 0));
    }

    public int getDriverId() {
        return pref.getInt(KEY_DRIVER_ID, 0);
    }

    public String getUserImage() {
        return pref.getString(KEY_USER_IMAGE, null);
    }

    public String getFcmToken() {
        return pref.getString(KEY_FCM_TOKEN, null);
    }

    public boolean getNotificationStatus() {
        return pref.getBoolean(KEY_IS_BUS_NOTIFICATION_RECEIVED, false);
    }

    public int getTodaySDay() {
        return pref.getInt(KEY_TODAY_S_DAY, 0);
    }

    public boolean getIsComplaintRegistered() {
        return pref.getBoolean(KEY_IS_COMPLAINT_REGISTERED, false);
    }

    public boolean getLastSeenFlag() {
        return pref.getBoolean(KEY_IS_LAST_SEEN_ENABLED, true);
    }

    public boolean getReadUnreadMessagesFlag() {
        return pref.getBoolean(KEY_IS_READ_UNREAD_MESSAGES_ENABLED, false);
    }

    public String getLanguage(){
        return  pref.getString(KEY_LANGUAGE_CODE, "en");
    }

    public int getTheme(){
        return pref.getInt(KEY_THEME_VALUE, -1);
    }

//    public HashMap<String, Integer> getLTime(){
//        HashMap<String, Integer> lTime = new HashMap<>();
//        if(pref.getString(KEY_lTime,"").equals("")){
//            lTime.put(KEY_lTime, 0);
//        }else{
//            lTime.put(KEY_lTime, Integer.parseInt(pref.getString(KEY_lTime,"")));
//        }
//        return lTime;
//    }

    public void logoutUser() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_TOKEN);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_TYPE);
        editor.remove(KEY_USER_PHONE);
        editor.remove(KEY_USER_IMAGE);
        editor.remove(KEY_FCM_TOKEN);
        editor.remove(KEY_STUDENT_ID);
        editor.remove(KEY_DRIVER_ID);
        editor.remove(KEY_DRIVER_NAME);
        editor.remove(KEY_DRIVER_PHONE);
        editor.remove(KEY_IS_BUS_NOTIFICATION_RECEIVED);
        editor.remove(KEY_IS_COMPLAINT_REGISTERED);
        editor.remove(KEY_IS_LAST_SEEN_ENABLED);
        editor.remove(KEY_IS_READ_UNREAD_MESSAGES_ENABLED);
        editor.commit();
    }

//    public void clearNotification(){
//        Log.e("NotificationTime","Clear Time");
//        editor.putString(KEY_lTime, "");;
//        editor.commit();
//    }

//    public void logoutUser() {
//        editor.clear();
//        editor.commit();
//        Intent i = new Intent(_context, Login.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////
////        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        _context.startActivity(i);
//    }

    public boolean isUserLoggedIn() {
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

    public boolean canReceiveCall() {
        return pref.getBoolean(KEY_RECEIVE_CALL, true);
    }

    public void updateReceiveCall(boolean value) {
        editor.putBoolean(KEY_RECEIVE_CALL, value);
        editor.commit();
    }

}

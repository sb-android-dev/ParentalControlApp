package com.schoolmanager.common;

public class Common {

//    public static final String BASE_URL = "http://aksharcomposite.in/parent_app/api/";
//    public static final String BASE_URL = "http://139.59.58.40/api/";
    public static final String BASE_URL = "http://139.59.58.40/dev/api/";
    public static final String APP_CODE = "758368";


    public static final int LOCATION_REQUEST = 1000;
    public static final int GPS_REQUEST = 1001;

    public static final int PAGE_START = 1;

    public static final String ACTION_START_SERVICE = "start_service";
    public static final String ACTION_PAUSE_SERVICE = "pause_service";
    public static final String ACTION_STOP_SERVICE = "stop_service";
    public static final String ACTION_OPEN_DASHBOARD = "open_dashboard";
    public static final String ACTION_OPEN_TRACKING = "open_tracking";

    public static final long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
    public static final long FASTEST_INTERVAL = 2 * 1000; /* 2 secs */

    public static final String TRACKING_NOTIFICATION_CHANNEL_ID = "tracking_channel";
    public static final String TRACKING_NOTIFICATION_CHANNEL_NAME = "Tracking User";
    public static final int TRACKING_NOTIFICATION_ID = 1;

    public static final String ALERT_NOTIFICATION_CHANNEL_ID = "alert_channel";
    public static final String ALERT_NOTIFICATION_CHANNEL_NAME = "Alert";
    public static final int ALERT_NOTIFICATION_ID = 2;

    public static final String CHAT_NOTIFICATION_CHANNEL_ID = "chat_channel";
    public static final String CHAT_NOTIFICATION_CHANNEL_NAME = "Messages";
    public static final int CHAT_NOTIFICATION_ID = 3;

    public static final String TRACK_NOTIFICATION_CHANNEL_ID = "track_channel";
    public static final String TRACK_NOTIFICATION_CHANNEL_NAME = "Track History";
    public static final int TRACK_NOTIFICATION_ID = 3;

    public static final int REQUEST_IMAGE_PICKER = 1002;

    public static final int HOME_TO_SCHOOL = 1;
    public static final int AT_SCHOOL = 2;
    public static final int SCHOOL_TO_HOME = 3;
    public static final int DRIVER_DROPPED = 4;
}

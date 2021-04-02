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
    public static final String ACTION_OPEN_LOCATE_ON_MAP = "open_locate_on_map";

    public static final long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
    public static final long FASTEST_INTERVAL = 2 * 1000; /* 2 secs */

    public static final String TRACKING_NOTIFICATION_CHANNEL_ID = "tracking_channel";
    public static final String DRIVER_HOME_TO_SCHOOL_CHANNEL_ID = "d_home_school_channel";
    public static final String DRIVER_SCHOOL_TO_HOME_CHANNEL_ID = "d_school_home_channel";
    public static final String SUB_ADMIN_SCHOOL_TO_HOME_CHANNEL_ID = "s_school_home_channel";
    public static final String SUB_ADMIN_HOME_TO_SCHOOL_CHANNEL_ID = "s_home_school_channel";
    public static final String TRACKING_NOTIFICATION_CHANNEL_NAME = "Tracking User";
    public static final int TRACKING_NOTIFICATION_ID = 1;

    public static final String DRIVER_ENABLE_LOCATION_CHANNEL_ID = "driver_enable_location_channel";
    public static final String DRIVER_DISABLE_LOCATION_CHANNEL_ID = "driver_disable_location_channel";
    public static final String DRIVER_LOCATION_CHANNEL_NAME = "Driver Location";

    public static final String ALERT_NOTIFICATION_CHANNEL_ID = "alert_channel";
    public static final String ALERT_NOTIFICATION_CHANNEL_NEW_ID = "alert_channel_new";
    public static final String ALERT_NOTIFICATION_CHANNEL_NAME = "Alert";
    public static final int ALERT_NOTIFICATION_ID = 2;

    public static final String CHAT_NOTIFICATION_CHANNEL_ID = "chat_channel";
    public static final String CHAT_NOTIFICATION_CHANNEL_NAME = "Messages";
    public static final int CHAT_NOTIFICATION_ID = 3;

    public static final String TRACK_NOTIFICATION_CHANNEL_ID = "track_channel";
    public static final String TRACK_NOTIFICATION_CHANNEL_NAME = "Track History";
    public static final int TRACK_NOTIFICATION_ID = 3;

    public static final String CALL_NOTIFICATION_CHANNEL_ID = "call_channel";
    public static final String CALL_NOTIFICATION_CHANNEL_NAME = "Call";
    public static final int CALL_NOTIFICATION_ID = 5;

    public static final String BROADCAST_NOTIFICATION_CHANNEL_ID = "broadcast_channel";
    public static final String BROADCAST_NOTIFICATION_CHANNEL_NAME = "Broadcast Message";
    public static final int BROADCAST_NOTIFICATION_ID = 6;

    public static final String VOICE_CALL_NOTIFICATION_CHANNEL_ID = "voice_call_noti_channel";
    public static final String VOICE_CALL_NOTIFICATION_CHANNEL_NAME = "Voice call notification";
    public static final int VOICE_CALL_NOTIFICATION_NOTIFICATION_ID = 7;

    public static final String COMPLAINT_NOTIFICATION_CHANNEL_ID = "complaint_channel";
    public static final String COMPLAINT_NOTIFICATION_CHANNEL_NAME = "Complaint";
    public static final int COMPLAINT_NOTIFICATION_NOTIFICATION_ID = 8;

    public static final String GENERAL_NOTIFICATION_CHANNEL_ID = "general_channel";
    public static final String GENERAL_NOTIFICATION_CHANNEL_NAME = "General";
    public static final int GENERAL_NOTIFICATION_NOTIFICATION_ID = 8;

    public static final int REQUEST_IMAGE_PICKER = 1002;

    public static final int HOME_TO_SCHOOL = 1;
    public static final int AT_SCHOOL = 2;
    public static final int SCHOOL_TO_HOME = 3;
    public static final int DRIVER_DROPPED = 4;

    public static final int LOG_OUT_SUCCESS = 201;
    public static final int LOG_OUT_FAILED = 202;
}

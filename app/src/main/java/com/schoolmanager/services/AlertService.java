package com.schoolmanager.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.schoolmanager.ChatBoardActivity;
import com.schoolmanager.ComplainList;
import com.schoolmanager.Dashboard;
import com.schoolmanager.LocateOnMap;
import com.schoolmanager.LogIn;
import com.schoolmanager.R;
import com.schoolmanager.VoiceCall;
import com.schoolmanager.common.Common;
import com.schoolmanager.events.EventCallEnd;
import com.schoolmanager.events.EventCallReceive;
import com.schoolmanager.events.EventCallRinging;
import com.schoolmanager.events.EventDeleteMessage;
import com.schoolmanager.events.EventLocationStatusChanged;
import com.schoolmanager.events.EventNewMessageArrives;
import com.schoolmanager.events.EventReadMessage;
import com.schoolmanager.model.ComplaintItem;
import com.schoolmanager.model.NotificationItem;
import com.schoolmanager.utilities.UserSessionManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.schoolmanager.MyApplication.mp;

public class AlertService extends FirebaseMessagingService {

    public static final String TAG = "notification_service";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.e(TAG, "onMessageReceived: data ->" + remoteMessage.getData().toString());

        UserSessionManager sessionManager = new UserSessionManager(this);
        int userId = sessionManager.getUserId();
        int userType = sessionManager.getUserType();

        if (userId > 0) {

            switch (remoteMessage.getData().get("notification_type")) {
                case "message":
                    performChatNotification(remoteMessage.getData());

                    break;

                case "message_read":
                    EventBus.getDefault().post(new EventReadMessage(
                            remoteMessage.getData().get("notification_read_message_id")
                    ));

                    break;
                case "call_init":
                    /*
                     * If driver and teacher have set the flag receive call or not
                     * then it will manage notification accordingly
                     * default value is true so other type of user can receive call
                     */

                    if (userType != 0) {
                        boolean isReceiveCall = sessionManager.canReceiveCall();
                        if (isReceiveCall) {
                            performCallNotification(remoteMessage.getData());
                        }
                    }

                    break;
                case "call_ringing":
                    EventBus.getDefault().post(new EventCallRinging(
                            remoteMessage.getData().get("notification_call_id"),
                            remoteMessage.getData().get("notification_call_token")
                    ));

                    break;
                case "call_end":
                    int call_notification_id = sessionManager.getInitiatedCallId();
                    cancelNotification(this, call_notification_id);

                    EventBus.getDefault().post(new EventCallEnd(
                            remoteMessage.getData().get("notification_call_id"),
                            remoteMessage.getData().get("notification_call_token")
                    ));

                    break;
                case "call_receive":
                    EventBus.getDefault().post(new EventCallReceive(
                            remoteMessage.getData().get("notification_call_id"),
                            remoteMessage.getData().get("notification_call_token")
                    ));

                    break;
                case "broadcast_alert":
                    performBroadcastNotification(remoteMessage.getData());

                    break;
                case "message_delete":
                    EventBus.getDefault().post(new EventDeleteMessage(
                            remoteMessage.getData().get("notification_delete_message_id")
                    ));

                    break;
                case "track_status":
                    if (userType == 1)
                        performTrackNotification(remoteMessage.getData());

                    break;
                case "complaint":
                    if (userType == 3)
                        performComplaintNotification(remoteMessage.getData());

                    break;
                case "location":
                    if (userType == 1 && !sessionManager.isAlertNotifying())
                        performAlertNotification(sessionManager, remoteMessage.getData());

//                    if (sessionManager.getTodaySDay() != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
//                        sessionManager.updateTodaySDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
//                        sessionManager.updateNotificationStatus(false);
//                    }
//                    if (!sessionManager.getNotificationStatus())
                    break;
                case "location_status":
                    if (userType == 1) {
                        performDriverLocationNotification(remoteMessage.getData());
                    }
                    break;
                default:
                    performGeneralNotification(remoteMessage.getData());
            }
        }
    }

    /**
     * @param data for this app
     *             <p>
     *             {
     *             notification_body=HelloGoodmorning,
     *             notification_type=message,
     *             notification_title=Message,
     *             notification_id=0,
     *             notification_message_receiver_type=1,
     *             notification_message_file_url=,
     *             notification_message_sender_type=2,
     *             notification_message_text=HelloGoodmorning,
     *             notification_message_time=HelloGoodmorning,
     *             notification_message_type=1,
     *             notification_message_sender_id=5,
     *             notification_message_receiver_id=5
     *             }
     */
//    Show notifications
    private void perfromChatReadNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

        if (checkForCruntActivityInStack(ChatBoardActivity.class.getName())
                || checkForCruntActivityInStack(ComplainList.class.getName())) {

            /**
             * Fire event to update the chat
             * list and chat message board for
             * new message arrivles
             */

            if (notifyType.equals("message")) {
                try {
                    JSONObject jsonObjNotificationItem = new JSONObject(data);
                    NotificationItem notificationItem = new Gson().fromJson(jsonObjNotificationItem.toString(), NotificationItem.class);
                    EventBus.getDefault().post(new EventNewMessageArrives(notificationItem, true));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void performChatNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChatNotificationChannel(notificationManager);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.CHAT_NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)

                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_give_complaint)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(data, notifyType));

        if (checkForCruntActivityInStack(ChatBoardActivity.class.getName())
                || checkForCruntActivityInStack(ComplainList.class.getName())) {

            /**
             * Fire event to update the chat
             * list and chat message board for
             * new message arrivles
             */

            if (notifyType.equals("message")) {
                try {
                    JSONObject jsonObjNotificationItem = new JSONObject(data);
                    NotificationItem notificationItem = new Gson().fromJson(jsonObjNotificationItem.toString(), NotificationItem.class);
                    EventBus.getDefault().post(new EventNewMessageArrives(notificationItem, true));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            notificationManager.notify((int) notificationId, notificationBuilder.build());
        }
    }

    private void performComplaintNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createComplaintChannel(notificationManager);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.COMPLAINT_NOTIFICATION_CHANNEL_ID);

        Log.e(TAG, "performComplaintNotification: complaint notification");

        notificationBuilder.setAutoCancel(true)

                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_complaint)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(data, notifyType));

        notificationManager.notify((int) notificationId, notificationBuilder.build());

    }

    private void performTrackNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");
        String notifyTrackStatus = data.get("notification_track_status");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(Common.TRACK_NOTIFICATION_CHANNEL_ID);
            if (notificationChannel != null)
                notificationManager.deleteNotificationChannel(Common.TRACK_NOTIFICATION_CHANNEL_ID);

            switch (notifyTrackStatus) {
                case "1":
                    createDriverHomeToSchoolChannel(notificationManager);
                    break;
                case "2":
                    createSubAdminHomeToSchoolChannel(notificationManager);
                    break;
                case "3":
                    createSubAdminSchoolToHomeChannel(notificationManager);
                    break;
                case "4":
                    createDriverSchoolToHomeChannel(notificationManager);
                    break;
            }

        }

        Uri notificationSound;
        String notificationChannelId;
        switch (notifyTrackStatus) {
            case "1":
                notificationChannelId = Common.DRIVER_HOME_TO_SCHOOL_CHANNEL_ID;
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_home_to_school);
                break;
            case "2":
                notificationChannelId = Common.SUB_ADMIN_HOME_TO_SCHOOL_CHANNEL_ID;
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_home_to_school);
                break;
            case "3":
                notificationChannelId = Common.SUB_ADMIN_SCHOOL_TO_HOME_CHANNEL_ID;
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_school_to_home);
                break;
            case "4":
                notificationChannelId = Common.DRIVER_SCHOOL_TO_HOME_CHANNEL_ID;
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_school_to_home);
                break;
            default:
                notificationSound = null;
                notificationChannelId = "";
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, notificationChannelId);

        Log.e(TAG, "performTrackNotification: track notification");

        notificationBuilder.setAutoCancel(true)

                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_tracking)
                .setSound(notificationSound)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(data, notifyType));

        notificationManager.notify((int) notificationId, notificationBuilder.build());

    }

    private void performDriverLocationNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");
        String driverId = data.get("notification_driver_id");
        String locationStatus = data.get("notification_location_status");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            switch (locationStatus) {
                case "0":
                    createDriverDisabledLocationChannel(notificationManager);
                    break;
                case "1":
                    createDriverEnabledLocationChannel(notificationManager);
                    break;
            }

        }

        if (checkForCruntActivityInStack(Dashboard.class.getName())
                || checkForCruntActivityInStack(LocateOnMap.class.getName())) {

            EventBus.getDefault().post(new EventLocationStatusChanged(driverId));

        } else {
            Uri notificationSound;
            String notificationChannelId;
            switch (locationStatus) {
                case "0":
                    notificationChannelId = Common.DRIVER_DISABLE_LOCATION_CHANNEL_ID;
                    notificationSound = Uri.parse("android.resource://"
                            + getApplicationContext().getPackageName() + "/" + R.raw.driver_location_off);
                    break;
                case "1":
                    notificationChannelId = Common.DRIVER_ENABLE_LOCATION_CHANNEL_ID;
                    notificationSound = Uri.parse("android.resource://"
                            + getApplicationContext().getPackageName() + "/" + R.raw.driver_location_on);
                    break;
                default:
                    notificationSound = null;
                    notificationChannelId = "";
            }

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, notificationChannelId);

            Log.e(TAG, "performTrackNotification: track notification");

            notificationBuilder.setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_school_bus)
                    .setSound(notificationSound)
                    .setContentTitle(notifyTitle)
                    .setContentText(notifyBody)
                    .setContentIntent(getRespectiveActivityPendingIntent(driverId));

            notificationManager.notify((int) notificationId, notificationBuilder.build());
        }

    }

    private void performBroadcastNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createBroadcastNotificationChannel(notificationManager);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.BROADCAST_NOTIFICATION_CHANNEL_ID);

        Log.e(TAG, "performBroadcastNotification: broadcast notification");

        notificationBuilder.setAutoCancel(true)

                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_management)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(data, notifyType));

        notificationManager.notify((int) notificationId, notificationBuilder.build());

    }

    private void performCallNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");
        String notifyId = data.get("notification_id");


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCallNotificationChannel(notificationManager);
        }

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.alert_notification);

      /*  if (mpCall != null && mpCall.isPlaying()) {
            mpCall.stop();
            mpCall.release();
            mpCall = null;
        }

        mpCall = MediaPlayer.create(getApplicationContext(), notificationSound);
        mpCall.setLooping(true);
        mpCall.start();*/

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.CALL_NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setSound(notificationSound)
                .setContentIntent(getRespectiveActivityPendingIntent(data, notifyType));

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_INSISTENT;
        notificationManager.notify((int) notificationId, notification);

        UserSessionManager userSessionManager = new UserSessionManager(this);
        userSessionManager.setInitiatedCallId(notificationId);
    }

    private void performAlertNotification(UserSessionManager sessionManager, Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

        Log.e(TAG, "performAlertNotification: notifying");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = Common.ALERT_NOTIFICATION_ID;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(Common.ALERT_NOTIFICATION_CHANNEL_ID);
            if (notificationChannel != null)
                notificationManager.deleteNotificationChannel(Common.ALERT_NOTIFICATION_CHANNEL_ID);

            createAlertNotificationChannel(notificationManager);
        }

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.alert_notification);

//        mp = MediaPlayer.create(getApplicationContext(), notificationSound);
        if (mp != null && !mp.isPlaying()) {
            mp.stop();
            mp.release();
            mp = null;
            Log.e(TAG, "performAlertNotification: stopping ring");
        }

        mp = MediaPlayer.create(getApplicationContext(), notificationSound);
        mp.setLooping(true);
        mp.start();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.ALERT_NOTIFICATION_CHANNEL_NEW_ID);

        notificationBuilder.setAutoCancel(true)

                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_school_bus)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
//                .setSound(notificationSound)
                .setContentIntent(getRespectiveActivityPendingIntent(data, notifyType));

        Notification notification = notificationBuilder.build();
//        notification.flags |= Notification.FLAG_INSISTENT;

        notificationManager.notify(notificationId, notification);

        sessionManager.notifyForAlert(true);

//        if (checkForCruntActivityInStack(Dashboard.class.getName())) {
//            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.setAction(Common.ACTION_OPEN_DASHBOARD);
//            startActivity(intent);
//        }

//        new UserSessionManager(this).updateNotificationStatus(true);
    }

    private void performGeneralNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createGeneralChannel(notificationManager);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.GENERAL_NOTIFICATION_CHANNEL_ID);

        Log.e(TAG, "performGeneralNotification: general notification");

        notificationBuilder.setAutoCancel(true)

                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_check)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(data, notifyType));

        notificationManager.notify((int) notificationId, notificationBuilder.build());

    }

    private PendingIntent getRespectiveActivityPendingIntent(Map<String, String> data, String notifyType) {
        Intent intent;
        if (new UserSessionManager(getApplicationContext()).getEssentials()
                .get(UserSessionManager.KEY_USER_ID).equals("0")) {
            intent = new Intent(getApplicationContext(), LogIn.class);
        } else {
            if (notifyType.equals("track_status")) {
                intent = new Intent(getApplicationContext(), Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Common.ACTION_OPEN_TRACKING);
            } else if (notifyType.equals("broadcast_alert")) {
                intent = new Intent(getApplicationContext(), Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Common.ACTION_OPEN_DASHBOARD);
                intent.putExtra("redirect_to_broadcast", "");
            } else if (notifyType.equals("call_init")) {

                intent = new Intent(getApplicationContext(), VoiceCall.class);
                intent.putExtra("channel_name", data.get("notification_call_token"))
                        .putExtra("from_user_id", "")
                        .putExtra("to_user_type", "")
                        .putExtra("to_user_id", "")
                        .putExtra("to_user_id", "")
                        .putExtra("call_id", data.get("notification_call_id"))
                        .putExtra("type", "receive")
                        .putExtra("name", data.get("notification_sender_name"))
                        .putExtra("image", data.get("notification_sender_image"))
                ;

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Common.ACTION_OPEN_DASHBOARD);

                apiCallRinging(data.get("notification_call_token"), data.get("notification_call_id"));
            } else {
                intent = new Intent(getApplicationContext(), Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Common.ACTION_OPEN_DASHBOARD);

                if (notifyType.equals("message")) {
                    ComplaintItem complaintItem = new ComplaintItem(
                            "",
                            data.get("notification_message_text"),
                            "",
                            0,
                            data.get("notification_message_sender_image"),
                            data.get("notification_message_sender_name"),
                            Integer.parseInt(data.get("notification_message_sender_id")),
                            Integer.parseInt(data.get("notification_message_sender_type")),
                            Integer.parseInt(data.get("message_read_permission")),
                            Integer.parseInt(data.get("last_seen_permission")),
                            Integer.parseInt(data.get("user_last_seen"))
                    );

                    intent.putExtra("redirect_to_chat", new Gson().toJson(complaintItem));
                } else if (notifyType.equals("location")) {
                    intent.putExtra("notification_for_arrive", true);
                }
            }


            Log.e(TAG, "showNotification: is logged in");
        }

        return PendingIntent.getActivity(this,
                1, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private PendingIntent getRespectiveActivityPendingIntent(String driverId) {
        Intent intent;
        if (new UserSessionManager(getApplicationContext()).getEssentials()
                .get(UserSessionManager.KEY_USER_ID).equals("0")) {
            intent = new Intent(getApplicationContext(), LogIn.class);
        } else {
            intent = new Intent(getApplicationContext(), Dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Common.ACTION_OPEN_LOCATE_ON_MAP);
            intent.putExtra("driver_id", driverId);

            Log.e(TAG, "showNotification: is logged in");
        }

        return PendingIntent.getActivity(this,
                1, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createAlertNotificationChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.ALERT_NOTIFICATION_CHANNEL_NEW_ID,
                Common.ALERT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("Alert Notification Channel");
        notificationChannel.setLightColor(Color.YELLOW);
        notificationChannel.setSound(null, null);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createCallNotificationChannel(NotificationManager notificationManager) {

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.alert_notification);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.CALL_NOTIFICATION_CHANNEL_ID,
                Common.CALL_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("Call Notification Channel");
        notificationChannel.setLightColor(Color.YELLOW);
        notificationChannel.enableVibration(true);
        notificationChannel.setSound(notificationSound, audioAttributes);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChatNotificationChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.CHAT_NOTIFICATION_CHANNEL_ID,
                Common.CHAT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("Chat Notification Channel");
        notificationChannel.setLightColor(Color.GREEN);

        notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createDriverSchoolToHomeChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.DRIVER_SCHOOL_TO_HOME_CHANNEL_ID,
                Common.TRACK_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.driver_school_to_home);

        notificationChannel.setDescription("Track Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(notificationSound, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createDriverHomeToSchoolChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.DRIVER_HOME_TO_SCHOOL_CHANNEL_ID,
                Common.TRACK_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.driver_home_to_school);

        notificationChannel.setDescription("Track Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(notificationSound, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createSubAdminSchoolToHomeChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.SUB_ADMIN_SCHOOL_TO_HOME_CHANNEL_ID,
                Common.TRACK_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_school_to_home);

        notificationChannel.setDescription("Track Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(notificationSound, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createSubAdminHomeToSchoolChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.SUB_ADMIN_HOME_TO_SCHOOL_CHANNEL_ID,
                Common.TRACK_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_home_to_school);

        notificationChannel.setDescription("Track Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(notificationSound, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createDriverEnabledLocationChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.DRIVER_ENABLE_LOCATION_CHANNEL_ID,
                Common.DRIVER_LOCATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.driver_location_on);

        notificationChannel.setDescription("Driver Location Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(notificationSound, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createDriverDisabledLocationChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.DRIVER_DISABLE_LOCATION_CHANNEL_ID,
                Common.DRIVER_LOCATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.driver_location_off);

        notificationChannel.setDescription("Driver Location Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(notificationSound, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createTrackNotificationChannel(NotificationManager notificationManager, String notifyTrackStatus) {
//        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
//                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.TRACK_NOTIFICATION_CHANNEL_ID,
                Common.TRACK_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

//        Uri notificationSound;
//        switch (notifyTrackStatus) {
//            case "1":
//                notificationSound = Uri.parse("android.resource://"
//                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_home_to_school);
//                break;
//            case "2":
//                notificationSound = Uri.parse("android.resource://"
//                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_home_to_school);
//                break;
//            case "3":
//                notificationSound = Uri.parse("android.resource://"
//                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_school_to_home);
//                break;
//            case "4":
//                notificationSound = Uri.parse("android.resource://"
//                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_school_to_home);
//                break;
//            default:
//                notificationSound = null;
//        }

        notificationChannel.setDescription("Track Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

//        notificationChannel.setSound(notificationSound, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createBroadcastNotificationChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.BROADCAST_NOTIFICATION_CHANNEL_ID,
                Common.BROADCAST_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("Track Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createComplaintChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.COMPLAINT_NOTIFICATION_CHANNEL_ID,
                Common.COMPLAINT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("Complaint Notification Channel");
        notificationChannel.setLightColor(Color.RED);

        notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createGeneralChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_DELAYED)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.GENERAL_NOTIFICATION_CHANNEL_ID,
                Common.GENERAL_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("Complaint Notification Channel");
        notificationChannel.setLightColor(Color.GREEN);

        notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.e(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token);
        storeToken(token);
    }

    private void storeToken(String token) {
        new UserSessionManager(getApplicationContext()).upsertFcmToken(token);
        Log.e(TAG, "storeToken: " + token);
    }

    private boolean checkForCruntActivityInStack(String activity_name) {
        boolean isDerisedActivity = false;
        ActivityManager mngr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);
        if (taskList.get(0).topActivity.getClassName().equals(activity_name)) {
            isDerisedActivity = true;
        }

        return isDerisedActivity;
    }

    private void apiCallRinging(String callToken, String callId) {

        UserSessionManager sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        String userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        String userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        String userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        String deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        String fcmToken = sessionManager.getFcmToken();

        AndroidNetworking.post(Common.BASE_URL + "app-call-ringing")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("call_token", callToken)
                .addBodyParameter("call_id", callId)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            Log.e("CALL_RINGING==>", message);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }

    public void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);

        UserSessionManager userSessionManager = new UserSessionManager(this);
        userSessionManager.setInitiatedCallId(0);
    }
}

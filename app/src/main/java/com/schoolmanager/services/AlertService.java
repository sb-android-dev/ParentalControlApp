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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.schoolmanager.ChatBoardActivity;
import com.schoolmanager.ComplainList;
import com.schoolmanager.Dashboard;
import com.schoolmanager.LogIn;
import com.schoolmanager.R;
import com.schoolmanager.common.Common;
import com.schoolmanager.events.EventNewMessageArrives;
import com.schoolmanager.model.NotificationItem;
import com.schoolmanager.utilities.UserSessionManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.schoolmanager.MyApplication.mp;

public class AlertService extends FirebaseMessagingService {

    public static final String TAG = "notification_service";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

//        Log.e(TAG, "onMessageReceived: " + remoteMessage.getMessageType());
        Log.e(TAG, "onMessageReceived: data ->" + remoteMessage.getData().toString());

        if (remoteMessage.getData().get("notification_type").equals("message")){
            // Notification to handle message
            performChatNotification(remoteMessage.getData());
        } else if(remoteMessage.getData().get("notification_type").equals("message_read")) {
            performChatReadNotification(remoteMessage.getData());
        } else if(remoteMessage.getData().get("notification_type").equals("track_status")) {
            if(new UserSessionManager(this).getUserType() == 1)
                performTrackNotification(remoteMessage.getData());
        } else {

            UserSessionManager sessionManager = new UserSessionManager(this);

            if (sessionManager.getTodaySDay() != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                sessionManager.updateTodaySDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                sessionManager.updateNotificationStatus(false);
            }
            if(!sessionManager.getNotificationStatus())
                performAlertNotification(remoteMessage.getData());
        }
//        showNotification(remoteMessage.getData());
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
//    Show notification
//    private void showNotification(Map<String, String> data) {
//        String notifyTitle = data.get("notification_title");
//        String notifyBody = data.get("notification_body");
//        String notifyType = data.get("notification_type");
//        String notifyId = data.get("notification_id");
//
//        Log.e(TAG, "showNotification: notifyType ->" + notifyType);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if(notifyType.equals("message")){
//            performChatNotification(data);
//        }else{
//            performAlertNotification(data);
//        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createAlertNotificationChannel(notificationManager);
//        }
//
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, Common.ALERT_NOTIFICATION_CHANNEL_ID);
//
//        notificationBuilder.setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.drawable.ic_school_bus)
//                .setContentTitle(notifyTitle)
//                .setContentText(notifyBody)
//                .setContentIntent(getRespectiveActivityPendingIntent(notifyType));

//        if (checkForCruntActivityInStack(ChatBoardActivity.class.getName())
//                || checkForCruntActivityInStack(ComplainList.class.getName())) {
//
//            /**
//             * Fire event to update the chat
//             * list and chat message board for
//             * new message arrivles
//             */
//
//            if (notifyType.equals("message")) {
//                try {
//                    JSONObject jsonObjNotificationItem = new JSONObject(data);
//                    NotificationItem notificationItem = new Gson().fromJson(jsonObjNotificationItem.toString(), NotificationItem.class);
//                    EventBus.getDefault().post(new EventNewMessageArrives(notificationItem, true));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            notificationManager.notify((int) notificationId, notificationBuilder.build());
//        }
//    }

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
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(notifyType));

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

    private void performChatReadNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        long notificationId = System.currentTimeMillis();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createChatNotificationChannel(notificationManager);
//        }
//
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, Common.CHAT_NOTIFICATION_CHANNEL_ID);
//
//        notificationBuilder.setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.drawable.logo)
//                .setContentTitle(notifyTitle)
//                .setContentText(notifyBody)
//                .setContentIntent(getRespectiveActivityPendingIntent(notifyType));

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
//        else {
//            notificationManager.notify((int) notificationId, notificationBuilder.build());
//        }
    }

    private void performTrackNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");
        String notifyTrackStatus = data.get("notification_track_status");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createTrackNotificationChannel(notificationManager, notifyTrackStatus);
        }

        Uri notificationSound;
        switch (notifyTrackStatus){
            case "1":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_home_to_school);
                break;
            case "2":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_home_to_school);
                break;
            case "3":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_school_to_home);
                break;
            case "4":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_school_to_home);
                break;
            default:
                notificationSound = null;
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.TRACK_NOTIFICATION_CHANNEL_ID);

        Log.e(TAG, "performTrackNotification: track notification");

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_tracking)
                .setSound(notificationSound, AudioManager.STREAM_NOTIFICATION)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(notifyType));

        notificationManager.notify((int) notificationId, notificationBuilder.build());

    }

    private void performAlertNotification(Map<String, String> data) {
        String notifyTitle = data.get("notification_title");
        String notifyBody = data.get("notification_body");
        String notifyType = data.get("notification_type");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificationId = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createAlertNotificationChannel(notificationManager);
        }

        Uri notificationSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.alert_notification);

        if(mp != null && mp.isPlaying()){
            mp.stop();
            mp.release();
        }

        mp = MediaPlayer.create(getApplicationContext(), notificationSound);
        mp.setLooping(true);
        mp.start();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.ALERT_NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_school_bus)
//                .setSound(notificationSound, AudioManager.STREAM_NOTIFICATION)
                .setContentTitle(notifyTitle)
                .setContentText(notifyBody)
                .setContentIntent(getRespectiveActivityPendingIntent(notifyType));

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_INSISTENT;

        notificationManager.notify((int) notificationId, notification);

        new UserSessionManager(this).updateNotificationStatus(true);
    }

    private PendingIntent getRespectiveActivityPendingIntent(String notifyType) {
        Intent intent;
        if (new UserSessionManager(getApplicationContext()).getEssentials()
                .get(UserSessionManager.KEY_USER_ID).equals("0")) {
            intent = new Intent(getApplicationContext(), LogIn.class);
        } else {
            if(notifyType.equals("track_status")){
                intent = new Intent(getApplicationContext(), Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Common.ACTION_OPEN_TRACKING);
            }else{
                intent = new Intent(getApplicationContext(), Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Common.ACTION_OPEN_DASHBOARD);
            }

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

        NotificationChannel notificationChannel = new NotificationChannel(Common.ALERT_NOTIFICATION_CHANNEL_ID,
                Common.ALERT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("Alert Notification Channel");
        notificationChannel.setLightColor(Color.YELLOW);
//        notificationChannel.setSound(Settings.System.DEFAULT_RINGTONE_URI, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChatNotificationChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.CHAT_NOTIFICATION_CHANNEL_ID,
                Common.CHAT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.setDescription("Chat Notification Channel");
        notificationChannel.setLightColor(Color.GREEN);

        notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
//            notificationChannel.setVibrationPattern(new long[]{0, 200});
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createTrackNotificationChannel(NotificationManager notificationManager, String notifyTrackStatus) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.TRACK_NOTIFICATION_CHANNEL_ID,
                Common.TRACK_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

        Uri notificationSound;
        switch (notifyTrackStatus){
            case "1":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_home_to_school);
                break;
            case "2":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_home_to_school);
                break;
            case "3":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.sub_admin_school_to_home);
                break;
            case "4":
                notificationSound = Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.driver_school_to_home);
                break;
            default:
                notificationSound = null;
        }


        notificationChannel.setDescription("Track Notification Channel");
        notificationChannel.setLightColor(Color.BLUE);

        notificationChannel.setSound(notificationSound, audioAttributes);
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
}

package com.schoolmanager.events;

import com.schoolmanager.model.NotificationItem;

public class EventNewMessageArrives {
    public NotificationItem notificationItem;
    private boolean reload_list = false;

    public EventNewMessageArrives(NotificationItem notificationItem, boolean reload_list) {
        this.notificationItem = notificationItem;
        this.reload_list = reload_list;
    }

    public EventNewMessageArrives(NotificationItem notificationItem) {
        this.notificationItem = notificationItem;
    }

    public NotificationItem getNotificationItem() {
        return notificationItem;
    }

    public boolean getReload_list() {
        return reload_list;
    }
}

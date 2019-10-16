package System;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private List<Notification> notificationList;

    public NotificationManager(){
        notificationList=new ArrayList<>();
    }

    public void addNotification(Notification note){
        notificationList.add(note);
    }

    public List<Notification> getNotificationList() {
        return notificationList;
    }
}

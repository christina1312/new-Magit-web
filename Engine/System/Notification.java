package System;

import java.sql.Time;
import java.util.Date;

public class Notification {
    private String message;
    private Date time;

    public Notification(String message, Date time) {
        this.message = message;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

package com.offbeat.apps.tasks.Data;

import java.io.Serializable;
import java.util.Date;

public class Todo implements Serializable {

    public String id, title, description;
    public boolean is_complete;
    public Date due_date, timestamp;

    public Todo(String id, String title, String description, boolean is_complete, Date due_date, Date timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.is_complete = is_complete;
        this.due_date = due_date;
        this.timestamp = timestamp;
    }

    public Todo() {
    }
}

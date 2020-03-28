package com.offbeat.apps.tasks.Data;

import com.offbeat.apps.tasks.R;

import java.io.Serializable;
import java.util.Date;

public class TaskList implements Serializable {
    public String id, title, start_color, end_color;
    public int completed_tasks, incomplete_tasks,icon;
    public Date timestamp;


    public TaskList(String id, String title, String start_color, String end_color, int completed_tasks, int incomplete_tasks, int icon, Date timestamp) {
        this.id = id;
        this.title = title;
        this.start_color = start_color;
        this.end_color = end_color;
        this.completed_tasks = completed_tasks;
        this.incomplete_tasks = incomplete_tasks;
        this.icon = icon;
        this.timestamp = timestamp;
    }

    public TaskList() {
    }


}

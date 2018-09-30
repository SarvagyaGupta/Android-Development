package edu.stanford.cse193a.todolist;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Item implements Comparable<Item> {
    private String description;
    private String priority;
    private Date dueDate;

    Item(String description, String priority, String dueDate) {
        this.description = description.substring(0, 1).toUpperCase() + description.substring(1);
        this.priority = priority;
        this.dueDate = formatDate(dueDate);
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public int compareTo(@NonNull Item other) {
        if (this.priority.equals(other.priority)) {
            return this.dueDate.compareTo(other.dueDate);
        } else if (this.priority.equals("High")
                || (this.priority.equals("Medium") && other.priority.equals("Low"))) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        switch (priority) {
            case "High": builder.append(getEmoji(0x1F631));
                break;
            case "Medium": builder.append(getEmoji(0x1F628));
                break;
            case "Low": builder.append(getEmoji(0x1F61A));
                break;
        }

        DateFormat format = new SimpleDateFormat("dd-MM-YYYY", Locale.ENGLISH);
        return builder.append("  ").append(format.format(dueDate)).append(":  ")
                .append(description).toString();
    }

    private String getEmoji(int unicode) {
        return new String(Character.toChars(unicode));
    }

    private Date formatDate(String date) {
        DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy",
                Locale.ENGLISH);
        Date result = null;
        try {
            result = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
}

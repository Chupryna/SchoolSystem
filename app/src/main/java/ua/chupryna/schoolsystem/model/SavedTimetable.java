package ua.chupryna.schoolsystem.model;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SavedTimetable
{
    public static final int SCHOOL_CLASS = 0;
    public static final int TEACHER = 1;

    private long id;
    private int categoryTimetable;
    private String nameTimetable;
    private long currentTimeMillis;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCategoryTimetable() {
        return categoryTimetable;
    }

    public void setCategoryTimetable(int categoryTimetable) {
        this.categoryTimetable = categoryTimetable;
    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public String getSavedDate() {
        return new SimpleDateFormat("Збережено: dd.MM.yyyy HH:mm", Locale.getDefault()).format(currentTimeMillis);
    }

    public String getNameTimetable() {
        return nameTimetable;
    }

    public void setNameTimetable(String nameTimetable) {
        this.nameTimetable = nameTimetable;
    }


}
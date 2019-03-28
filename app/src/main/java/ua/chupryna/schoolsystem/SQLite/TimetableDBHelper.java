package ua.chupryna.schoolsystem.SQLite;


import java.util.ArrayList;

import ua.chupryna.schoolsystem.model.SavedTimetable;

public interface TimetableDBHelper {
    long addTimetable(long date, int category);
    boolean deleteTimetable(long id);
    ArrayList<SavedTimetable> getAllTimetable();
}

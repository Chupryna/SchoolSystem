package ua.chupryna.schoolsystem.SQLite;

import java.util.ArrayList;

import ua.chupryna.schoolsystem.model.Lesson;

public interface LessonDBHelper {
    boolean addLesson(Lesson lesson, long timetableID, long schoolClassID, long teacherID, long dayID);
    boolean deleteLesson(long id);
    ArrayList<Lesson> getLessonsWithTeacherByTimetableID(long timetableID);
    ArrayList<Lesson> getLessonsWithClassByTimetableID(long timetableID);
}

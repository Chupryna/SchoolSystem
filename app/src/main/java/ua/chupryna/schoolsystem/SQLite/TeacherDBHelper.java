package ua.chupryna.schoolsystem.SQLite;

public interface TeacherDBHelper {
    long addTeacher(String lastName, String firstName, String surName);
    boolean deleteTeacher(long id);
    long getTeacherId(String lastName, String firstName, String surName);
    String getTeacherByID(long id);
}

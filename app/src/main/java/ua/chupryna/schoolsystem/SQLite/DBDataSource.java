package ua.chupryna.schoolsystem.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ua.chupryna.schoolsystem.model.Lesson;
import ua.chupryna.schoolsystem.model.SavedTimetable;
import ua.chupryna.schoolsystem.model.SchoolClass;

import static ua.chupryna.schoolsystem.SQLite.DBHelper.TABLE_CLASS;
import static ua.chupryna.schoolsystem.SQLite.DBHelper.TABLE_LESSON;
import static ua.chupryna.schoolsystem.SQLite.DBHelper.TABLE_TEACHER;
import static ua.chupryna.schoolsystem.SQLite.DBHelper.TABLE_TIMETABLE;

public class DBDataSource implements TeacherDBHelper, TimetableDBHelper, ClassDBHelper, LessonDBHelper {
    private SQLiteDatabase sqLiteDatabase;
    private DBHelper dbHelper;

    public DBDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        sqLiteDatabase = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
        sqLiteDatabase = null;
    }

    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }

    //LessonDBHelper
    public boolean addLesson(Lesson lesson, long timetableID, long schoolClassID, long teacherID, long dayID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("number", lesson.getNumber());
        contentValues.put("beginTime", lesson.getBeginTime());
        contentValues.put("endTime", lesson.getEndTime());
        contentValues.put("subject", lesson.getSubject());
        contentValues.put("teacher_id", teacherID);
        contentValues.put("class_id", schoolClassID);
        contentValues.put("day_id", dayID);
        contentValues.put("timetable_id", timetableID);
        long id = sqLiteDatabase.insert(TABLE_LESSON, null, contentValues);

        return id > 0;
    }

    @Override
    public boolean deleteLesson(long id) {
        int result = sqLiteDatabase.delete(TABLE_LESSON, "_id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    @Override
    public ArrayList<Lesson> getLessonsWithTeacherByTimetableID(long timetableID) {
        ArrayList<Lesson> listLessons = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_LESSON + " INNER JOIN " + TABLE_TEACHER + " ON " +
                TABLE_LESSON + ".teacher_id = " + TABLE_TEACHER + "._id WHERE timetable_id = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{String.valueOf(timetableID)});

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            do {
                Lesson lesson = new Lesson();
                lesson.setNumber(cursor.getInt(cursor.getColumnIndex("number")));
                lesson.setBeginTime(cursor.getString(cursor.getColumnIndex("beginTime")));
                lesson.setEndTime(cursor.getString(cursor.getColumnIndex("endTime")));
                lesson.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
                lesson.setDayID(cursor.getInt(cursor.getColumnIndex("day_id")));
                lesson.setTeacherLastName(cursor.getString(cursor.getColumnIndex("lastName")));
                lesson.setTeacherName(cursor.getString(cursor.getColumnIndex("firstName")));
                lesson.setTeacherSurName(cursor.getString(cursor.getColumnIndex("surName")));
                listLessons.add(lesson);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listLessons;
    }

    @Override
    public ArrayList<Lesson> getLessonsWithClassByTimetableID(long timetableID) {
        ArrayList<Lesson> listLessons = new ArrayList<>();
        String sql = "SELECT lesson.number, lesson.beginTime, lesson.endTime, lesson.subject, lesson.day_id, class.number as classNumber, class.letter " +
                "FROM " + TABLE_LESSON + " INNER JOIN " + TABLE_CLASS + " ON " +
                TABLE_LESSON + ".class_id = " + TABLE_CLASS + "._id WHERE timetable_id = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{String.valueOf(timetableID)});

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            do {
                Lesson lesson = new Lesson();
                lesson.setNumber(cursor.getInt(cursor.getColumnIndex("number")));
                lesson.setBeginTime(cursor.getString(cursor.getColumnIndex("beginTime")));
                lesson.setEndTime(cursor.getString(cursor.getColumnIndex("endTime")));
                lesson.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
                lesson.setDayID(cursor.getInt(cursor.getColumnIndex("day_id")));

                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setNumber(cursor.getInt(cursor.getColumnIndex("classNumber")));
                schoolClass.setLetter(cursor.getString(cursor.getColumnIndex("letter")));
                lesson.setSchoolClass(schoolClass);
                listLessons.add(lesson);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listLessons;
    }

    //TimetableDBHelper
    public long addTimetable(long date, int category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("savedDate", date);
        contentValues.put("category", category);

        return sqLiteDatabase.insert(TABLE_TIMETABLE, null, contentValues);
    }

    @Override
    public boolean deleteTimetable(long id) {
        int result = -1;
        sqLiteDatabase.beginTransaction();
        try {
            String[] columns = new String[] {"_id", "class_id", "teacher_id"};
            Cursor cursor = sqLiteDatabase.query(TABLE_LESSON, columns, "timetable_id = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && !cursor.isAfterLast()) {
                cursor.moveToFirst();
                do {
                    if (!deleteLesson(cursor.getLong(0)))
                        throw new Exception("lesson not delete");
                    deleteClass(cursor.getLong(1));
                    deleteTeacher(cursor.getLong(2));
                } while (cursor.moveToNext());
                cursor.close();
            }
            result = sqLiteDatabase.delete(TABLE_TIMETABLE, "_id = ?", new String[]{String.valueOf(id)});
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }

        return result > 0;
    }

    @Override
    public ArrayList<SavedTimetable> getAllTimetable() {
        ArrayList<SavedTimetable> listSavedTimetable = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(TABLE_TIMETABLE, new String[] {"*"}, null, null, null, null, "savedDate " + "DESC");
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            do {
                SavedTimetable savedTimetable = new SavedTimetable();
                savedTimetable.setId(cursor.getLong(0));
                savedTimetable.setCurrentTimeMillis(cursor.getLong(1));
                savedTimetable.setCategoryTimetable(cursor.getInt(2));
                if (savedTimetable.getCategoryTimetable() == SavedTimetable.SCHOOL_CLASS) {
                    String timetableID = String.valueOf(cursor.getInt(0));
                    Cursor cursor1 = sqLiteDatabase.query(TABLE_LESSON, new String[] {"class_id"}, "timetable_id = ?",
                            new String[] {timetableID}, null, null, null, "1");
                    if (cursor1 != null && !cursor1.isAfterLast()) {
                        cursor1.moveToFirst();
                        long classID = cursor1.getLong(0);
                        savedTimetable.setNameTimetable(getClassByID(classID).toString());
                        cursor1.close();
                    }
                } else {
                    String timetableID = String.valueOf(cursor.getInt(0));
                    Cursor cursor1 = sqLiteDatabase.query(TABLE_LESSON, new String[] {"teacher_id"}, "timetable_id = ?",
                            new String[] {timetableID}, null, null, null, "1");
                    if (cursor1 != null && !cursor1.isAfterLast()) {
                        cursor1.moveToFirst();
                        long teacherID = cursor1.getLong(0);
                        savedTimetable.setNameTimetable(getTeacherByID(teacherID));
                        cursor1.close();
                    }
                }
                listSavedTimetable.add(savedTimetable);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listSavedTimetable;
    }


    //ClassDBHelper
    public long addClass(SchoolClass schoolClass) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("number", schoolClass.getNumber());
        contentValues.put("letter", schoolClass.getLetter());

        return sqLiteDatabase.insert(TABLE_CLASS, null, contentValues);
    }

    @Override
    public boolean deleteClass(long id) {
        int result = sqLiteDatabase.delete(TABLE_CLASS, "_id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    @Override
    public long getClassID(SchoolClass schoolClass) {
        long id = -1;
        String selection = "number = ? AND letter = ?";
        String[] selectionArgs = new String[] {String.valueOf(schoolClass.getNumber()), schoolClass.getLetter()};
        Cursor cursor = sqLiteDatabase.query(TABLE_CLASS, new String[]{"_id"}, selection, selectionArgs, null, null, null);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            id = cursor.getLong(0);
            cursor.close();
        }
        return id;
    }

    @Override
    public SchoolClass getClassByID(long id) {
        SchoolClass schoolClass = new SchoolClass();
        Cursor cursor = sqLiteDatabase.query(TABLE_CLASS, new String[] {"number", "letter"}, "_id = ?",
                new String[] {String.valueOf(id)}, null, null, null);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            schoolClass.setId((int) id);
            schoolClass.setNumber(cursor.getInt(0));
            schoolClass.setLetter(cursor.getString(1));
            cursor.close();
        }
        return schoolClass;
    }


    //TeacherDBHelper
    @Override
    public long addTeacher(String lastName, String firstName, String surName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("lastName", lastName);
        contentValues.put("firstName", firstName);
        contentValues.put("surName", surName);

        return sqLiteDatabase.insert(TABLE_TEACHER, null, contentValues);
    }

    @Override
    public boolean deleteTeacher(long id) {
        int result = sqLiteDatabase.delete(TABLE_TEACHER, "_id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    @Override
    public long getTeacherId(String lastName, String firstName, String surName) {
        long id = -1;

        String selection = "lastName = ? AND firstName = ? AND surName = ?";
        String[] selectionArgs = new String[] {lastName, firstName, surName};
        Cursor cursor = sqLiteDatabase.query(TABLE_TEACHER, new String[] {"_id"}, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst())
            id = cursor.getInt(0);
        cursor.close();

        return id;
    }

    @Override
    public String getTeacherByID(long id) {
        String teacher = "";
        Cursor cursor = sqLiteDatabase.query(TABLE_TEACHER, new String[]{"lastName", "firstName", "surName"}, "_id = ?",
                new String[]{String.valueOf(id),}, null, null, null);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            teacher = String.format("%s %s %s", cursor.getString(0), cursor.getString(1), cursor.getString(2));
            cursor.close();
        }
        return teacher;
    }
}
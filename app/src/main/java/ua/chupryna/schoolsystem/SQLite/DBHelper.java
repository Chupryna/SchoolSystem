package ua.chupryna.schoolsystem.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ua.chupryna.schoolsystem.R;

public class DBHelper extends SQLiteOpenHelper  {
    private static final String DB_NAME = "LessonsDB";
    private static final int DB_VERSION = 1;
    static final String TABLE_TIMETABLE = "timetable";
    public static final String TABLE_LESSON = "lesson";
    public static final String TABLE_TEACHER = "teacher";
    public static final String TABLE_CLASS = "class";
    private static final String TABLE_DAY = "day";

    private Context context;

    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        ContentValues contentValues = new ContentValues();
        String[] days = context.getResources().getStringArray(R.array.days);
        //String[] days = new String[] {"Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця", "Субота", "Неділя"};

        //Транзакція створення і поперднього заповнення БД
        sqLiteDatabase.beginTransaction();
        try {
            //Таблиця Day
            sqLiteDatabase.execSQL ("create table " + TABLE_DAY + "( " +
                    "_id INTEGER PRIMARY KEY, " +
                    "day TEXT" + ");");

            for (int i = 0; i < days.length; i++) {
                contentValues.clear();
                contentValues.put("_id", i + 1);
                contentValues.put("day", days[i]);
                sqLiteDatabase.insert(TABLE_DAY, null, contentValues);
            }

            //Таблиця Class
            sqLiteDatabase.execSQL ("create table " + TABLE_CLASS + "(" +
                    "_id INTEGER PRIMARY KEY, " +
                    "number INTEGER, " +
                    "letter TEXT" +");");

            //Таблиця Teacher
            sqLiteDatabase.execSQL("create table " + TABLE_TEACHER + "(" +
                    "_id INTEGER PRIMARY KEY, " +
                    "lastName TEXT, " +
                    "firstName TEXT, " +
                    "surName TEXT" + ");");

            //Таблиця Timetable
            sqLiteDatabase.execSQL("create table " + TABLE_TIMETABLE + "(" +
                    "_id INTEGER PRIMARY KEY, " +
                    "savedDate INTEGER, " +
                    "category INTEGER" + ");");

            //Таблиця Lesson
            sqLiteDatabase.execSQL("create table " + TABLE_LESSON + "(" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "number INTEGER, " +
                    "beginTime TEXT, " +
                    "endTime TEXT, " +
                    "subject TEXT, " +
                    "teacher_id INTEGER NOT NULL," +
                    "class_id INTEGER NOT NULL," +
                    "day_id INTEGER NOT NULL, " +
                    "timetable_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (teacher_id) REFERENCES " + TABLE_TEACHER + "(_id)," +
                    "FOREIGN KEY (class_id) REFERENCES " + TABLE_CLASS + "(_id)," +
                    "FOREIGN KEY (day_id) REFERENCES " + TABLE_DAY + "(_id)," +
                    "FOREIGN KEY (timetable_id) REFERENCES " + TABLE_TIMETABLE + "(_id)" + ");");

            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("drop table " + TABLE_LESSON);
        sqLiteDatabase.execSQL("drop table " + TABLE_TEACHER);
        sqLiteDatabase.execSQL("drop table " + TABLE_DAY);
        sqLiteDatabase.execSQL("drop table " + TABLE_CLASS);
        onCreate(sqLiteDatabase);
    }
}
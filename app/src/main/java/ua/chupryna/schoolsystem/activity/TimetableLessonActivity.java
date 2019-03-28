package ua.chupryna.schoolsystem.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ua.chupryna.schoolsystem.adapter.RVAdapterLessons;
import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.SQLite.DBDataSource;
import ua.chupryna.schoolsystem.model.Lesson;
import ua.chupryna.schoolsystem.model.SavedTimetable;
import ua.chupryna.schoolsystem.model.SchoolClass;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;
import ua.chupryna.schoolsystem.SQLite.DBHelper;

public class TimetableLessonActivity extends AppCompatActivity
{
    private ActionBar actionBar;
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private ProgressBar progressBarView;

    private static SparseArray<ArrayList<Lesson>> sparseArrayLessons;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ClassTimetableTask classTimetableTask;
    private TeacherTimetableTask teacherTimetableTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_lesson);

        initActionBar();
        initView();

        //Створення списку з уроками
        sparseArrayLessons = new SparseArray<>();

        //Створення адаптера, який буде вертати фрагмент для кожного табу
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        fillingTimetable();
    }

    private void initView() {
        progressBarView = findViewById(R.id.lesson_progress);
        mViewPager = findViewById(R.id.container);
        tabLayout = findViewById(R.id.tabs);
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.timetable_lessons);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetable_lesson, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save_timetable_lessons:
                if (saveTimetable()) {
                    setResult(RESULT_OK);
                    Toast.makeText(getApplicationContext(), R.string.save_timetable_successfully, Toast.LENGTH_SHORT).show();
                }
                break;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillingTimetable() {
        boolean isSavedTimetable = getIntent().getBooleanExtra("isSavedTimetable", false);
        int selectedCriterion = getIntent().getIntExtra("selectedCriterion", -1);

        if (isSavedTimetable) {
            long timetableID = getIntent().getLongExtra("timetableID", -1);
            DBDataSource dbDataSource = new DBDataSource(getApplicationContext());
            dbDataSource.open();

            switch (selectedCriterion) {
                case SavedTimetable.SCHOOL_CLASS:
                    fillingTimetableForClassFromDB(timetableID, dbDataSource);
                    break;

                case SavedTimetable.TEACHER:
                    fillingTimetableForTeacherFromDB(timetableID, dbDataSource);
                    break;
            }
            dbDataSource.close();
        } else {
            switch (selectedCriterion) {
                case SavedTimetable.SCHOOL_CLASS:
                    if (checkNetwork(classTimetableTask)) {
                        classTimetableTask = new ClassTimetableTask();
                        classTimetableTask.execute(getIntent().getIntExtra("schoolID", -1));
                    }
                    break;

                case SavedTimetable.TEACHER:
                    if (checkNetwork(teacherTimetableTask)) {
                        User teacher = getIntent().getParcelableExtra("teacher");
                        teacherTimetableTask = new TeacherTimetableTask();
                        teacherTimetableTask.execute(teacher.getId());
                    }
                    break;
            }
        }
    }

    private void fillingTimetableForTeacherFromDB(long timetableID, DBDataSource dbDataSource) {
        String teacher = "";
        String sqlQuery = "SELECT teacher.lastName, teacher.firstName, teacher.surName FROM " + DBHelper.TABLE_LESSON +
                " INNER JOIN " + DBHelper.TABLE_TEACHER + " ON " + DBHelper.TABLE_LESSON + ".teacher_id=" +
                DBHelper.TABLE_TEACHER + "._id WHERE " + DBHelper.TABLE_LESSON + ".timetable_id=?";
        String[] selectionArgs1 = new String[] {String.valueOf(timetableID)};
        SQLiteDatabase database = dbDataSource.getSqLiteDatabase();
        Cursor cursor1 = database.rawQuery(sqlQuery, selectionArgs1);
        if (cursor1 != null && !cursor1.isAfterLast()) {
            cursor1.moveToFirst();
            teacher = String.format("%s %s %s", cursor1.getString(0), cursor1.getString(1), cursor1.getString(2));
            cursor1.close();
        }

        ArrayList<Lesson> listLessons1 = dbDataSource.getLessonsWithClassByTimetableID(timetableID);
        for (Lesson lesson : listLessons1) {
            int dayID = lesson.getDayID();
            ArrayList<Lesson> lessonsInDay = sparseArrayLessons.get(dayID);
            if (lessonsInDay == null) {
                lessonsInDay = new ArrayList<>();
                lessonsInDay.add(lesson);
                sparseArrayLessons.put(dayID, lessonsInDay);
            }
            else
                sparseArrayLessons.get(dayID).add(lesson);
        }

        setLessonsOfTeacherInTabLayout(teacher);
    }

    private void fillingTimetableForClassFromDB(long timetableID, DBDataSource dbDataSource) {
        SchoolClass schoolClass = new SchoolClass();

        String sql = "SELECT class.number, class.letter FROM " + DBHelper.TABLE_LESSON +
                " INNER JOIN " + DBHelper.TABLE_CLASS + " ON " + DBHelper.TABLE_LESSON + ".class_id=" +
                DBHelper.TABLE_CLASS + "._id WHERE " + DBHelper.TABLE_LESSON + ".timetable_id=?";
        String[] selectionArgs = new String[] {String.valueOf(timetableID)};
        SQLiteDatabase sqLiteDatabase = dbDataSource.getSqLiteDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(sql, selectionArgs);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            schoolClass.setNumber(cursor.getInt(0));
            schoolClass.setLetter(cursor.getString(1));
            cursor.close();
        }

        ArrayList<Lesson> listLessons = dbDataSource.getLessonsWithTeacherByTimetableID(timetableID);
        for (Lesson lesson : listLessons) {
            int dayID = lesson.getDayID();
            ArrayList<Lesson> lessonsInDay = sparseArrayLessons.get(dayID);
            if (lessonsInDay == null) {
                lessonsInDay = new ArrayList<>();
                lessonsInDay.add(lesson);
                sparseArrayLessons.put(dayID, lessonsInDay);
            }
            else
                sparseArrayLessons.get(dayID).add(lesson);
        }
        setLessonsOfClassInTabLayout(schoolClass);
    }

    private boolean saveTimetable() {
        boolean result = false;
        int category = getIntent().getIntExtra("selectedCriterion", -1);

        DBDataSource dbDataSource = new DBDataSource(getApplicationContext());
        dbDataSource.open();
        SQLiteDatabase sqLiteDatabase = dbDataSource.getSqLiteDatabase();
        sqLiteDatabase.beginTransaction();
        try{
            long timetableID = dbDataSource.addTimetable(System.currentTimeMillis(), category);

            if (category == SavedTimetable.SCHOOL_CLASS) {
                SchoolClass schoolClass = getIntent().getParcelableExtra("schoolClass");
                long schoolClassID = dbDataSource.addClass(schoolClass);

                for (int i = 0; i < sparseArrayLessons.size(); i++) {
                    ArrayList<Lesson> lessonsList = sparseArrayLessons.valueAt(i);
                    for (Lesson lesson : lessonsList) {
                        long teacherID = dbDataSource.getTeacherId(lesson.getTeacherLastName(), lesson.getTeacherName(), lesson.getTeacherSurName());
                        if (teacherID == -1)
                            teacherID = dbDataSource.addTeacher(lesson.getTeacherLastName(), lesson.getTeacherName(), lesson.getTeacherSurName());
                        long dayID = sparseArrayLessons.keyAt(i);
                        if (!dbDataSource.addLesson(lesson, timetableID, schoolClassID, teacherID, dayID))
                            throw new Exception("timetable not saved");
                    }
                }
            } else {
                User teacher = getIntent().getParcelableExtra("teacher");
                long teacherID = dbDataSource.addTeacher(teacher.getLastName(), teacher.getName(), teacher.getSurName());

                for (int i = 0; i < sparseArrayLessons.size(); i++) {
                    ArrayList<Lesson> lessonsList = sparseArrayLessons.valueAt(i);
                    for (Lesson lesson : lessonsList) {
                        long schoolClassID = dbDataSource.getClassID(lesson.getSchoolClass());
                        if (schoolClassID == -1)
                            schoolClassID = dbDataSource.addClass(lesson.getSchoolClass());
                        long dayID = sparseArrayLessons.keyAt(i);
                        if (!dbDataSource.addLesson(lesson, timetableID, schoolClassID, teacherID, dayID))
                            throw new Exception("timetable not saved");
                    }
                }
            }
            sqLiteDatabase.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        dbDataSource.close();
        return result;
    }

    private boolean checkNetwork(final Object asyncTask) {
        if (!Constants.isNetworkAvailable(this)) {
            Snackbar snackbar = Snackbar
                    .make(mViewPager, R.string.network_not_available, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkNetwork(asyncTask);
                        }
                    });
            snackbar.show();
            return false;
        }

        return asyncTask == null;
    }

    private void showProgress(final boolean show) {
        int animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        progressBarView.animate().setDuration(animTime).alpha(show ? 1 : 0).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBarView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    public void setLessonsOfClassInTabLayout(SchoolClass schoolClass) {
        actionBar.setSubtitle(schoolClass.toString());
        String[] days = getResources().getStringArray(R.array.days_abbreviated);
        for (int i = 0; i < sparseArrayLessons.size(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(days[sparseArrayLessons.keyAt(i)-1]);
            tabLayout.addTab(tab);

            mSectionsPagerAdapter.addFragment(PlaceholderFragment.newInstance(i));
        }

        //Встановлення адаптера і лістнерів зміни табів
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    public void setLessonsOfTeacherInTabLayout(String teacherName) {
        actionBar.setSubtitle(teacherName);

        String[] days = getResources().getStringArray(R.array.days_abbreviated);
        for (int i = 0; i < sparseArrayLessons.size(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(days[sparseArrayLessons.keyAt(i)-1]);
            tabLayout.addTab(tab);

            mSectionsPagerAdapter.addFragment(PlaceholderFragment.newInstance(i));
        }

        //Встановлення адаптера і лістнерів зміни табів
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            int position = args != null ? args.getInt(ARG_SECTION_NUMBER) : -1;

            View rootView = inflater.inflate(R.layout.timetable_lesson_fragment, container, false);
            RecyclerView recyclerView = rootView.findViewById(R.id.recycler_lessons);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new RVAdapterLessons(sparseArrayLessons.valueAt(position)));

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        private ArrayList<Fragment> listFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            listFragment = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return listFragment.get(position);
        }

        @Override
        public int getCount() {
            return listFragment.size();
        }

        void addFragment(Fragment fragment) {
            listFragment.add(fragment);
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class ClassTimetableTask extends AsyncTask<Integer, Void, Boolean>
    {
        private SchoolClass schoolClass;
        //private SchoolClassTask schoolClassTask;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
            //schoolClassTask = new SchoolClassTask();
            //schoolClassTask.execute(getIntent().getIntExtra("pupilID", -1));
        }

        @Override
        protected Boolean doInBackground(Integer... integers)
        {
            HttpURLConnection connection = null;

            int schoolID = integers[0];
            if (schoolID == -1)
                return false;

            schoolClass = getIntent().getParcelableExtra("schoolClass");
            if (schoolClass == null)
                return false;

            try {
                //schoolClass = schoolClassTask.get(15, TimeUnit.SECONDS);
                URL url = new URL(String.format("%s?schoolID=%s&classID=%s", Constants.URL_GET_LESSONS_BY_CLASS, schoolID, schoolClass.getId()));
                connection = (HttpURLConnection) url.openConnection();

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                parseJSON(stringBuilder.toString());
            } catch (IOException | JSONException  e) {
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return true;
        }

        private void parseJSON(String response) throws JSONException {
            JSONArray arrayLessons = new JSONArray(response);
            for (int i = 0; i < arrayLessons.length(); i++) {
                JSONObject objectLesson = arrayLessons.getJSONObject(i);

                Lesson lesson = new Lesson();
                lesson.setSubject(objectLesson.getString("subject"));
                lesson.setTeacherLastName(objectLesson.getString("lastName"));
                lesson.setTeacherName(objectLesson.getString("firstName"));
                lesson.setTeacherSurName(objectLesson.getString("surName"));
                lesson.setDay(objectLesson.getString("day"));
                lesson.setNumber(objectLesson.getInt("number"));
                lesson.setBeginTime(objectLesson.getString("beginTime").substring(0,5));
                lesson.setEndTime(objectLesson.getString("endTime").substring(0,5));

                int dayID = objectLesson.getInt("dayID");
                ArrayList<Lesson> lessonsInDay = sparseArrayLessons.get(dayID);
                if (lessonsInDay == null) {
                    lessonsInDay = new ArrayList<>();
                    lessonsInDay.add(lesson);
                    sparseArrayLessons.put(dayID, lessonsInDay);
                }
                else
                    sparseArrayLessons.get(dayID).add(lesson);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            showProgress(false);
            classTimetableTask = null;

            if (success)
                setLessonsOfClassInTabLayout(schoolClass);
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class TeacherTimetableTask extends AsyncTask<Integer, Void, Boolean>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Integer... integers)
        {
            HttpURLConnection connection = null;

            int teacherID = integers[0];
            if (teacherID == -1)
                return false;

            try {
                URL url = new URL(String.format("%s?teacherID=%s", Constants.URL_GET_LESSONS_BY_TEACHER, teacherID));
                connection = (HttpURLConnection) url.openConnection();

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                parseJSON(stringBuilder.toString());
            } catch (IOException | JSONException  e) {
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return true;
        }

        private void parseJSON(String response) throws JSONException {
            JSONArray arrayLessons = new JSONArray(response);
            for (int i = 0; i < arrayLessons.length(); i++)
            {
                JSONObject objectLesson = arrayLessons.getJSONObject(i);

                Lesson lesson = new Lesson();
                lesson.setSubject(objectLesson.getString("subject"));
                lesson.setDay(objectLesson.getString("day"));
                lesson.setNumber(objectLesson.getInt("number"));
                lesson.setBeginTime(objectLesson.getString("beginTime").substring(0,5));
                lesson.setEndTime(objectLesson.getString("endTime").substring(0,5));

                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setNumber(objectLesson.getInt("classNumber"));
                schoolClass.setLetter(objectLesson.getString("letter"));
                lesson.setSchoolClass(schoolClass);

                int dayID = objectLesson.getInt("dayID");
                ArrayList<Lesson> lessonsInDay = sparseArrayLessons.get(dayID);
                if (lessonsInDay == null) {
                    lessonsInDay = new ArrayList<>();
                    lessonsInDay.add(lesson);
                    sparseArrayLessons.put(dayID, lessonsInDay);
                }
                else
                    sparseArrayLessons.get(dayID).add(lesson);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            showProgress(false);
            teacherTimetableTask = null;

            if (success) {
                User teacher = getIntent().getParcelableExtra("teacher");
                String teacherName = String.format("%s %s %s", teacher.getLastName(), teacher.getName(), teacher.getSurName());
                setLessonsOfTeacherInTabLayout(teacherName);
            }
        }
    }
}
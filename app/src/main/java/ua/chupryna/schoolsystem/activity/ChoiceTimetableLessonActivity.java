package ua.chupryna.schoolsystem.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ua.chupryna.schoolsystem.adapter.RVAdapterSavedTimetable;
import ua.chupryna.schoolsystem.adapter.SimpleItemTouchHelperCallback;
import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.SavedTimetable;
import ua.chupryna.schoolsystem.model.SchoolClass;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;
import ua.chupryna.schoolsystem.SQLite.DBDataSource;

public class ChoiceTimetableLessonActivity extends AppCompatActivity
{
    private LinearLayout choiceTimetableLessonForm;
    private Spinner spinner;
    private ProgressBar progressBarView;
    private Button btnToViewTimetable;
    private RadioGroup radioGroup;
    private RadioButton radioButtonPupils;
    private  RecyclerView recyclerView;

    private RVAdapterSavedTimetable adapterSavedTimetable;
    private ArrayAdapter <String> arrayAdapterSpinner;
    private ArrayList<String> listForSpinner;
    private ArrayList<SchoolClass> listSchoolClasses;
    private ArrayList<User> listTeachers;

    private SchoolClassTask schoolClassTask;
    private TeachersTask teachersTask;

    private final int REQUEST_CODE_SAVED_TIMETABLE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_timetable_lesson);

        initActionBar();
        initView();
        initAdapter();
        initListeners();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapterSavedTimetable, ChoiceTimetableLessonActivity.this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.timetable_lessons);
        }
    }

    private void initView() {
        choiceTimetableLessonForm = findViewById(R.id.choice_timetable_lesson_form);
        progressBarView = findViewById(R.id.choice_timetable_lesson_progress);
        spinner = findViewById(R.id.spinner_choice_timetable_lesson);
        btnToViewTimetable = findViewById(R.id.button_to_view_timetable);
        recyclerView = findViewById(R.id.recycler_saved_timetable);
        radioGroup = findViewById(R.id.radioGroup_choice_timetable_lesson);
        radioButtonPupils = findViewById(R.id.radioButton_timetable_pupils);
    }

    private void initAdapter() {
        listForSpinner = new ArrayList<>();
        listSchoolClasses = new ArrayList<>();
        listTeachers = new ArrayList<>();

        arrayAdapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listForSpinner);
        arrayAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapterSpinner);

        //Список збережених розкладів
        ArrayList<SavedTimetable> listSavedTimetable = getListSavedTimetable();
        if (!listSavedTimetable.isEmpty()) {
            TextView textSavedTimetables = findViewById(R.id.text_saved_timetables);
            textSavedTimetables.setVisibility(View.VISIBLE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapterSavedTimetable = new RVAdapterSavedTimetable(listSavedTimetable, ChoiceTimetableLessonActivity.this);
        recyclerView.setAdapter(adapterSavedTimetable);
    }

    private void initListeners() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_timetable_pupils:
                        if (listSchoolClasses.isEmpty()) {
                            if (checkNetwork(schoolClassTask)) {
                                schoolClassTask = new SchoolClassTask();
                                schoolClassTask.execute(getIntent().getIntExtra("schoolID", -1));
                            }
                        } else {
                            arrayAdapterSpinner.clear();
                            for (SchoolClass schoolClass : listSchoolClasses)
                                listForSpinner.add(schoolClass.toString());
                            arrayAdapterSpinner.notifyDataSetChanged();
                        }
                        break;

                    case R.id.radioButton_timetable_teachers:
                        if (listTeachers.isEmpty()) {
                            arrayAdapterSpinner.clear();
                            if (checkNetwork(teachersTask)) {
                                teachersTask =  new TeachersTask();
                                teachersTask.execute(getIntent().getIntExtra("schoolID", -1));
                            }
                        } else {
                            arrayAdapterSpinner.clear();
                            for (User teacher : listTeachers) {
                                String name = String.format("%s %s %s", teacher.getLastName(), teacher.getName(), teacher.getSurName());
                                listForSpinner.add(name);
                            }
                            arrayAdapterSpinner.notifyDataSetChanged();
                        }
                        break;
                    default: break;
                }
            }
        });
        radioButtonPupils.setChecked(true);

        btnToViewTimetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TimetableLessonActivity.class);
                intent.putExtra("schoolID", getIntent().getIntExtra("schoolID", -1));
                int position = spinner.getSelectedItemPosition();

                if (radioButtonPupils.isChecked()) {
                    SchoolClass schoolClass = listSchoolClasses.get(position);
                    intent.putExtra("schoolClass", schoolClass);
                    intent.putExtra("selectedCriterion", SavedTimetable.SCHOOL_CLASS);
                } else {
                    User teacher = listTeachers.get(position);
                    intent.putExtra("teacher", teacher);
                    intent.putExtra("selectedCriterion", SavedTimetable.TEACHER);
                }
                startActivityForResult(intent, REQUEST_CODE_SAVED_TIMETABLE);
            }
        });
    }

    private ArrayList<SavedTimetable> getListSavedTimetable() {
        DBDataSource dbDataSource = new DBDataSource(getApplicationContext());
        dbDataSource.open();
        ArrayList<SavedTimetable> listSavedTimetable = dbDataSource.getAllTimetable();
        dbDataSource.close();
        return listSavedTimetable;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_SAVED_TIMETABLE && resultCode == RESULT_OK) {
            adapterSavedTimetable.setListSavedTimetable(getListSavedTimetable());
            adapterSavedTimetable.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkNetwork(final Object asyncTask) {
        if (!Constants.isNetworkAvailable(this)) {
            Snackbar snackbar = Snackbar
                    .make(choiceTimetableLessonForm, R.string.network_not_available, Snackbar.LENGTH_INDEFINITE)
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

    @SuppressLint("StaticFieldLeak")
    private class SchoolClassTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            if (progressBarView.getVisibility() == View.GONE)
                progressBarView.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... integers)
        {
            HttpURLConnection connection = null;

            try {
                int schoolID = integers[0];
                if (schoolID == -1)
                    return false;

                URL url = new URL(String.format("%s?schoolID=%s",Constants.URL_GET_CLASSES, URLEncoder.encode(String.valueOf(schoolID), Constants.UTF_8)));
                connection = (HttpURLConnection) url.openConnection();

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                parseJSON(stringBuilder.toString());
            } catch (IOException | JSONException e) {
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return true;
        }

        private void parseJSON(String response) throws JSONException
        {
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setId(jsonObject.getInt("id"));
                schoolClass.setNumber(jsonObject.getInt("number"));
                schoolClass.setLetter(jsonObject.getString("letter"));
                listSchoolClasses.add(schoolClass);
            }
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            progressBarView.setVisibility(View.GONE);
            schoolClassTask = null;

            if (success) {
                for (SchoolClass schoolClass : listSchoolClasses)
                    listForSpinner.add(schoolClass.toString());
                arrayAdapterSpinner.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class TeachersTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            if (progressBarView.getVisibility() == View.GONE)
                progressBarView.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... integers)
        {
            HttpURLConnection connection = null;

            try {
                int schoolID = integers[0];
                if (schoolID == -1)
                    return false;

                URL url = new URL(String.format("%s?schoolID=%s",Constants.URL_GET_TEACHERS, URLEncoder.encode(String.valueOf(schoolID), Constants.UTF_8)));
                connection = (HttpURLConnection) url.openConnection();

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                parseJSON(stringBuilder.toString());
            } catch (IOException | JSONException e) {
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return true;
        }

        private void parseJSON(String response) throws JSONException
        {
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);

                User user = new User();
                user.setId(object.getInt("id"));
                user.setLastName(object.getString("lastName"));
                user.setName(object.getString("firstName"));
                user.setSurName(object.getString("surName"));
                user.setEmail(object.getString("email"));
                user.setTelephone(object.getString("telephone"));
                user.setPhotoPath(object.getString("photo"));
               // user.setGroupID(object.getInt("groupID"));
                user.setSchoolID(object.getInt("schoolID"));

                listTeachers.add(user);
            }
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            progressBarView.setVisibility(View.GONE);
            schoolClassTask = null;

            if (success) {
                for (User teacher : listTeachers) {
                    String name = String.format("%s %s %s", teacher.getLastName(), teacher.getName(), teacher.getSurName());
                    listForSpinner.add(name);
                }
                arrayAdapterSpinner.notifyDataSetChanged();
            }
        }
    }
}
package ua.chupryna.schoolsystem.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Calendar;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.Subject;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;

public class ChoiceSuccessActivity extends AppCompatActivity
{
    private RelativeLayout choiceSuccessForm;
    private RadioButton radioButtonDay;
    private Spinner spinnerSubject;
    private TextView textDateView;
    private ProgressBar progressBar;
    private RadioGroup radioGroup;
    private Button btnReview;

    private ArrayList<Subject> listSubjects;
    private SubjectsTask subjectsTask;

    private User user;
    private String date;
    private DatePickerDialog datePickerDialog;
    private Snackbar snackbar;

    public static final int DATE = 0;
    public static final int SUBJECT = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_success);

        initActionBar();
        initView();
        initListeners();

        user = getIntent().getParcelableExtra("user");
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.success);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initView() {
        choiceSuccessForm = findViewById(R.id.choice_success_form);
        radioGroup = findViewById(R.id.radioGroupSuccess);
        radioButtonDay = findViewById(R.id.radioButton_success_day);
        spinnerSubject = findViewById(R.id.spinner_success_school);
        textDateView = findViewById(R.id.text_success_date);
        btnReview = findViewById(R.id.btn_success_review);
        progressBar = findViewById(R.id.choice_success_progress);

        spinnerSubject.setEnabled(false);
    }

    private void initListeners() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId) {
                    case R.id.radioButton_success_day:
                        spinnerSubject.setEnabled(false);
                        textDateView.setEnabled(true);
                        break;

                    case R.id.radioButton_success_subject:
                        if (listSubjects == null)
                            setSubjectsInSpinner();
                        spinnerSubject.setEnabled(true);
                        textDateView.setEnabled(false);
                        break;

                    default: break;
                }
            }
        });

        textDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SuccessActivity.class);
                intent.putExtra("user", user);

                if (radioButtonDay.isChecked()) {
                    if (date == null) {
                        Toast.makeText(getApplicationContext(), "Виберіть дату для перегляду успішності", Toast.LENGTH_LONG).show();
                        return;
                    }
                    intent.putExtra("selectedCriterion", DATE);
                    intent.putExtra("date", date);
                    intent.putExtra("dateForTitle", textDateView.getText().toString());
                }
                else {
                    Subject subject = (Subject) spinnerSubject.getSelectedItem();
                    if (subject == null) {
                        Toast.makeText(getApplicationContext(), "Виберіть предмет для перегляду успішності", Toast.LENGTH_LONG).show();
                        return;
                    }
                    intent.putExtra("selectedCriterion", SUBJECT);
                    intent.putExtra("subject", subject);
                }

                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker()
    {
        if (datePickerDialog != null) {
            datePickerDialog.show();
            return;
        }

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                textDateView.setText(String.format("%02d.%02d.%d", dayOfMonth, month+1, year));
                date = String.format("%s/%s/%s", year, month+1, dayOfMonth);
            }
        };

        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(ChoiceSuccessActivity.this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void setSubjectsInSpinner()
    {
        if (!Constants.isNetworkAvailable(this)) {
            snackbar = Snackbar
                    .make(choiceSuccessForm, R.string.network_not_available, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setSubjectsInSpinner();
                        }
                    });
            snackbar.show();
            return;
        }

        if (subjectsTask != null)
            return;

        listSubjects = new ArrayList<>();
        subjectsTask = new SubjectsTask();
        subjectsTask.execute(user.getSchoolID());
    }


    private void showProgress(final boolean show)
    {
        int animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        progressBar.animate().setDuration(animTime).alpha(show ? 1 : 0).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @SuppressLint("StaticFieldLeak")
    private class SubjectsTask extends AsyncTask<Integer, Void, Boolean>
    {
        @Override
        protected void onPreExecute() {
            showProgress(true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            HttpURLConnection connection = null;

            int schoolID = integers[0];
            if (schoolID == 0)
                return false;

            try {
                URL url = new URL(String.format("%s?schoolID=%s", Constants.URL_GET_SUBJECTS, schoolID));
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
            JSONObject jsonObject = new JSONObject(response);
            JSONArray array = jsonObject.getJSONArray("data");

            for (int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);

                Subject subject = new Subject();
                subject.setId(object.getInt("id"));
                subject.setName(object.getString("name"));
                //subject.setDescription(object.getString("description"));

                listSubjects.add(subject);
            }
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            subjectsTask = null;
            showProgress(false);

            if(success) {
                ArrayAdapter<Subject> adapterSpinner = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, listSubjects);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSubject.setAdapter(adapterSpinner);
                if (snackbar != null && snackbar.isShown())
                    snackbar.dismiss();
            }
        }
    }
}
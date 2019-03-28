package ua.chupryna.schoolsystem.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ua.chupryna.schoolsystem.adapter.RVAdapterSuccess;
import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.SchoolClass;
import ua.chupryna.schoolsystem.model.Subject;
import ua.chupryna.schoolsystem.model.Success;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;

public class SuccessActivity extends AppCompatActivity
{
    private User user;
    private Intent intent;
    private int selectedCriterion;

    private TextView textNoDataToDisplayView;
    private TextView textPupilClassView;

    private RVAdapterSuccess adapterSuccess;
    private ArrayList<Success> listSuccess;
    private SuccessTask successTask;
    private RecyclerView recyclerSuccessView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        intent = getIntent();
        user = intent.getParcelableExtra("user");
        selectedCriterion = intent.getIntExtra("selectedCriterion", -1);

        initActionBar();
        initView();
        initAdapter();

        setSuccessInRecyclerView();
    }

    private void initView() {
        textNoDataToDisplayView = findViewById(R.id.text_success_no_data_to_display);
        TextView textInfoTitleView = findViewById(R.id.text_success_info_title);
        TextView textPupilNameView = findViewById(R.id.text_success_pupil);
        textPupilClassView = findViewById(R.id.text_success_class);
        TextView textSelectionModeView = findViewById(R.id.text_success_selection_mode);
        recyclerSuccessView = findViewById(R.id.recycler_success);

        if (selectedCriterion == ChoiceSuccessActivity.DATE) {
            textInfoTitleView.setText(R.string.subject);
            textSelectionModeView.setText(String.format("Дата: %s", intent.getStringExtra("dateForTitle")));
        } else {
            textInfoTitleView.setText(R.string.date);
            textSelectionModeView.setText(String.format("Предмет: %s", ((Subject)intent.getParcelableExtra("subject")).getName()));
        }
        textPupilNameView.setText(String.format("%s %s %s", user.getLastName(), user.getName(), user.getSurName()));
    }

    private void initAdapter() {
        recyclerSuccessView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listSuccess = new ArrayList<>();
        adapterSuccess = new RVAdapterSuccess(listSuccess, getApplicationContext(), selectedCriterion);
        recyclerSuccessView.setAdapter(adapterSuccess);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.success);
        }
    }

    private void setSuccessInRecyclerView() {
        if (!Constants.isNetworkAvailable(this)) {
            Toast.makeText(getApplicationContext(), R.string.network_not_available, Toast.LENGTH_LONG).show();
            finish();
        }

        if (successTask != null)
            return;

        //Заповнення списку успішності
        successTask = new SuccessTask();
        if (selectedCriterion == ChoiceSuccessActivity.DATE) {
            successTask.execute(Constants.URL_GET_SUCCESS_BY_DATE, String.format("date=%s&pupilID=%s",
                    intent.getStringExtra("date"), user.getId()));
        } else {
            Subject subject = intent.getParcelableExtra("subject");
            successTask.execute(Constants.URL_GET_SUCCESS_BY_SUBJECT, String.format("subjectID=%s&pupilID=%s",
                    subject.getId(), user.getId()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    private void showProgress(final boolean show) {
        int animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        final ProgressBar progressBarView = findViewById(R.id.success_progress);
        progressBarView.animate().setDuration(animTime).alpha(show ? 1 : 0).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                       progressBarView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @SuppressLint("StaticFieldLeak")
    private class SuccessTask extends AsyncTask<String, Void, Boolean>
    {
        private  SchoolClass schoolClass;

        @Override
        protected void onPreExecute() {
            showProgress(true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;

            String host = strings[0];
            String param = strings[1];

            try {
                URL url = new URL(String.format("%s?%s", host, param));
                connection = (HttpURLConnection) url.openConnection();

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                parseJSON(stringBuilder.toString());
            } catch (IOException | JSONException | ParseException e) {
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return true;
        }

        @SuppressLint("SimpleDateFormat")
        private void parseJSON(String response) throws JSONException, ParseException {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray array = jsonObject.getJSONArray("data");

            JSONObject object = array.getJSONObject(0);
            schoolClass = new SchoolClass();
            schoolClass.setId(object.getInt("id"));
            schoolClass.setNumber(object.getInt("number"));
            schoolClass.setLetter(object.getString("letter"));

            if (array.get(1) == null)
                return;

            JSONArray arraySuccess = array.getJSONArray(1);
            for (int i = 0; i < arraySuccess.length(); i++)
            {
                object = arraySuccess.getJSONObject(i);

                Success success = new Success();
                success.setNumberLesson(object.getInt("number"));
                success.setPresenceStatus(object.getString("status"));
                success.setRating(object.getInt("rating"));
                if (selectedCriterion == ChoiceSuccessActivity.DATE)
                    success.setSubject(object.getString("subject"));
                else {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(object.getString("date"));
                    success.setDate(new SimpleDateFormat("dd.MM.yyyy").format(date));
                }
                listSuccess.add(success);
            }
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            showProgress(false);
            textPupilClassView.setText(schoolClass.toString());

            if (success)
                adapterSuccess.notifyDataSetChanged();
            else
                textNoDataToDisplayView.setVisibility(View.VISIBLE);
        }
    }
}
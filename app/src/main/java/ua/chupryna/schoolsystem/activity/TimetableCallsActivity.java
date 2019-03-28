package ua.chupryna.schoolsystem.activity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.Call;
import ua.chupryna.schoolsystem.adapter.RVAdapterCalls;
import ua.chupryna.schoolsystem.R;

public class TimetableCallsActivity extends AppCompatActivity
{
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Call> listCalls;
    private RVAdapterCalls adapterCalls;
    private CallsTask callsTask;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_calls);

        initActionBar();
        initView();
        initAdapters();

        setTimetableInRecyclerView();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.timetable_calls);
        }
    }

    private void initView() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_timetable_calls);
        recyclerView = findViewById(R.id.recycler_calls);
    }

    private void initAdapters() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setTimetableInRecyclerView();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listCalls = new ArrayList<>();
        adapterCalls = new RVAdapterCalls(listCalls);
        recyclerView.setAdapter(adapterCalls);
    }

    private void setTimetableInRecyclerView() {
        if (!Constants.isNetworkAvailable(this)) {
            Snackbar snackbar = Snackbar
                    .make(swipeRefreshLayout, R.string.network_not_available, Snackbar.LENGTH_LONG)
                    .setAction(R.string.try_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setTimetableInRecyclerView();
                        }
                    });
            snackbar.show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (callsTask != null)
            return;

        listCalls.clear();
        callsTask = new CallsTask();
        callsTask.execute(getIntent().getIntExtra("schoolID", -1));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("StaticFieldLeak")
    private class CallsTask extends AsyncTask<Integer, Void, Boolean>
    {
        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... integers)
        {
            HttpURLConnection connection = null;

            int schoolID = integers[0];
            if (schoolID == 0)
                return false;

            try {
                URL url = new URL(String.format("%s?schoolID=%s", Constants.URL_GET_CALLS, schoolID));
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
                Call call = new Call();
                call.setNumber(object.getInt("number"));
                call.setBeginTime(object.getString("beginTime").substring(0,5));
                call.setEndTime(object.getString("endTime").substring(0,5));

                listCalls.add(call);
            }

            for (int i = 0; i < listCalls.size() - 1; i++) {
                String breakBetweenLesson = diffTime(listCalls.get(i + 1).getBeginTime(), listCalls.get(i).getEndTime());
                listCalls.get(i).setBreakBetweenLesson(breakBetweenLesson);
            }
        }

        private String diffTime(String beginTime, String endTime)
        {
            String[] begin = beginTime.split(":");
            String[] end = endTime.split(":");

            int hourBegin = Integer.parseInt(begin[0]);
            int hourEnd = Integer.parseInt(end[0]);
            int minuteBegin = Integer.parseInt(begin[1]);
            int minuteEnd = Integer.parseInt(end[1]);

            int hour = hourBegin - hourEnd;
            int minute = minuteBegin - minuteEnd;
            if (minute < 0) {
                hour--;
                minute += 60;
            }
            if (hour > 0)
                minute += hour * 60;

            return String.format("%s хв.", minute);
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            callsTask = null;
            swipeRefreshLayout.setRefreshing(false);

            if (success)
                adapterCalls.notifyDataSetChanged();
        }
    }
}
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
import android.support.v7.widget.SearchView;
import android.view.Menu;
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


import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.image.DownloadImagesTask;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;
import ua.chupryna.schoolsystem.adapter.RVAdapterTeachers;

public class TeachersActivity extends AppCompatActivity
{
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

    private ArrayList<User> listTeachers;
    private RVAdapterTeachers adapterTeachers;
    private TeachersTask teachersTask;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        initActionBar();
        initView();
        initAdapters();

        setTeachersInRecyclerView();
    }

    private void initView() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_teachers);
        recyclerView = findViewById(R.id.recycler_teachers);
    }

    private void initAdapters() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setTeachersInRecyclerView();
            }
        });

        //Список
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listTeachers = new ArrayList<>();
        adapterTeachers = new RVAdapterTeachers(listTeachers, getApplicationContext());
        recyclerView.setAdapter(adapterTeachers);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.teachers);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teachers, menu);

        MenuItem item = menu.getItem(0);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapterTeachers.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterTeachers.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!searchView.isIconified())
                searchView.onActionViewCollapsed();
            else
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTeachersInRecyclerView() {
        if (!Constants.isNetworkAvailable(this)) {
            Snackbar snackbar = Snackbar
                    .make(swipeRefreshLayout, R.string.network_not_available, Snackbar.LENGTH_LONG)
                    .setAction(R.string.try_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setTeachersInRecyclerView();
                        }
                    });
            snackbar.show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (teachersTask != null)
            return;

        listTeachers.clear();
        teachersTask = new TeachersTask();
        teachersTask.execute(getIntent().getIntExtra("schoolID", -1));
    }


    @SuppressLint("StaticFieldLeak")
    private class TeachersTask extends AsyncTask<Integer, Void, Boolean>
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
                URL url = new URL(String.format("%s?schoolID=%s", Constants.URL_GET_TEACHERS, schoolID));
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
            JSONArray array = new JSONArray(response);

            for (int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);

                User user = new User();
                user.setId(object.getInt("id"));
                user.setLastName(object.getString("lastName"));
                user.setName(object.getString("firstName"));
                user.setSurName(object.getString("surName"));
                user.setEmail(object.getString("email"));
                user.setTelephone(object.getString("telephone"));
                user.setPhotoPath(object.getString("photo"));
                //user.setGroupID(object.getInt("groupID"));
                user.setSchoolID(object.getInt("schoolID"));

                listTeachers.add(user);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            teachersTask = null;
            swipeRefreshLayout.setRefreshing(false);

            if (success)
                adapterTeachers.notifyDataSetChanged();

            //Створення масиву шляхів до фото
            String[] filePaths = new String[listTeachers.size()];
            for (int i = 0; i < filePaths.length; i++)
                filePaths[i] = listTeachers.get(i).getPhotoPath();

            DownloadImagesTask downloadImagesTask = new DownloadImagesTask(adapterTeachers, swipeRefreshLayout, listTeachers, getCacheDir());
            downloadImagesTask.execute(filePaths);
        }
    }
}
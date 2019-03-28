package ua.chupryna.schoolsystem.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.image.DownloadImagesTask;
import ua.chupryna.schoolsystem.model.News;
import ua.chupryna.schoolsystem.R;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.adapter.RVAdapterNews;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private User user;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private Spinner spinnerSchool;
    private TextView textNoDataToDisplayView;
    private ImageView photoImageView;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private View header;

    private ArrayList<News> listNews;
    private List<String> listSchools;
    private RVAdapterNews adapterNews;
    private ArrayAdapter<String> arrayAdapterSpinner;

    private ListSchoolsTask listSchoolsTask;
    private NewsTask newsTask;

    private int[] schoolIDs;
    private final int REQUEST_CODE_PROFILE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = getIntent().getParcelableExtra("user");

        initView();
        initAdapters();
        initListeners();

        setPhotoToImageView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        recyclerView = findViewById(R.id.recycler_news);
        textNoDataToDisplayView = findViewById(R.id.text_main_no_data_to_display);
        spinnerSchool = findViewById(R.id.spinner_main_school);

        setSupportActionBar(toolbar);

        //Пошук хеадера і встановлення картинки
        header = navigationView.getHeaderView(0);
        photoImageView = header.findViewById(R.id.imageView);

        //Встановлення тексту в хеадер та обробника, що відкриває Мій профіль
        TextView textNavHeaderNameView = header.findViewById(R.id.text_nav_header_name);
        TextView textNavHeaderEmailView = header.findViewById(R.id.text_nav_header_email);
        textNavHeaderNameView.setText(String.format("%s %s", user.getLastName(), user.getName()));
        textNavHeaderEmailView.setText(user.getEmail());
    }

    private void initAdapters() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listNews = new ArrayList<>();
        adapterNews = new RVAdapterNews(listNews, getApplicationContext());
        recyclerView.setAdapter(adapterNews);

        getListSchools();
        arrayAdapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,listSchools);
        arrayAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchool.setAdapter(arrayAdapterSpinner);
    }

    private void initListeners() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listNews.clear();
                setNewsInMainActivity(spinnerSchool.getSelectedItemPosition(), false);
            }
        });

        //Перемикач бічного меню
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        switch (user.getGroupID())
        {
            case Constants.PUPIL_ID:
                navigationView.inflateMenu(R.menu.main_pupil);
                break;
            case Constants.PARENT_ID:
                navigationView.inflateMenu(R.menu.main_parent);
                break;
            case Constants.TEACHER_ID:
                navigationView.inflateMenu(R.menu.main_teacher);
                break;
            case Constants.DIRECTOR_ID:

                break;
            case Constants.ADMIN_ID:

                break;
        }

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("myProfile", true);
                startActivityForResult(intent, REQUEST_CODE_PROFILE);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0){
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItemPosition > listNews.size()-2){
                        if (newsTask != null && !newsTask.isEndListNews())
                            setNewsInMainActivity(spinnerSchool.getSelectedItemPosition(), true);
                    }
                }
            }
        });

        spinnerSchool.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listNews.clear();
                setNewsInMainActivity(position, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setPhotoToImageView() {
        String fileName = Constants.getFileNameFromPath(user.getPhotoPath());
        File photoFile = new File(getCacheDir(), fileName);
        if (photoFile.exists()) {
            photoImageView.setImageURI(Uri.fromFile(photoFile));
            user.setPhotoPathOnDevice(photoFile.getAbsolutePath());
        } else {
            DownloadImageTask downloadImageTask = new DownloadImageTask();
            downloadImageTask.execute(user.getPhotoPath());
        }
    }

    private void getListSchools() {
        if (!Constants.isNetworkAvailable(this)) {
            Snackbar snackbar = Snackbar
                    .make(swipeRefreshLayout, R.string.network_not_available, Snackbar.LENGTH_LONG)
                    .setAction(R.string.try_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getListSchools();
                        }
                    });
            snackbar.show();
            swipeRefreshLayout.setRefreshing(false);
            textNoDataToDisplayView.setVisibility(View.VISIBLE);
            return;
        }

        if (listSchoolsTask != null)
            return;

        listSchools = new ArrayList<>();
        listSchools.add(getString(R.string.all_news));

        listSchoolsTask = new ListSchoolsTask();
        listSchoolsTask.execute();
    }

    private void setNewsInMainActivity(final int item, final boolean isAddingNews) {
        if (!Constants.isNetworkAvailable(this)) {
            Snackbar snackbar = Snackbar
                    .make(swipeRefreshLayout, R.string.network_not_available, Snackbar.LENGTH_LONG)
                    .setAction(R.string.try_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setNewsInMainActivity(item, isAddingNews);
                        }
                    });
            snackbar.show();
            swipeRefreshLayout.setRefreshing(false);
            if (listNews.isEmpty())
                textNoDataToDisplayView.setVisibility(View.VISIBLE);
            return;
        }

        if (newsTask != null && newsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;

        newsTask = new NewsTask();
        if (isAddingNews)
            newsTask.execute(item, listNews.get(listNews.size()-1).getId());
        else
            newsTask.execute(item);
    }

    @Override
    protected void onResume() {
        if (drawerLayout != null)
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
         else
            super.onBackPressed();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id)
        {
            case R.id.nav_news:
                listNews.clear();
                setNewsInMainActivity(spinnerSchool.getSelectedItemPosition(), false);
                break;

            case R.id.nav_timetable_lessons:
                intent = new Intent(getApplicationContext(), ChoiceTimetableLessonActivity.class);
                intent.putExtra("schoolID", user.getSchoolID());
                startActivity(intent);
                break;

            case R.id.nav_timetable_calls:
                intent = new Intent(getApplicationContext(), TimetableCallsActivity.class);
                intent.putExtra("schoolID", user.getSchoolID());
                startActivity(intent);
                break;

            case R.id.nav_teachers:
                intent = new Intent(getApplicationContext(), TeachersActivity.class);
                intent.putExtra("schoolID", user.getSchoolID());
                startActivity(intent);
                break;

            case R.id.nav_success:
                intent = new Intent(getApplicationContext(), ChoiceSuccessActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                break;

            case R.id.nav_exit:
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PROFILE) {
            if (data != null) {
                String filePath = data.getStringExtra("filePath");
                user.setPhotoPath(filePath);
                DownloadImageTask downloadImageTask = new DownloadImageTask();
                downloadImageTask.execute(filePath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadImageTask extends AsyncTask<String, Void, Boolean>
    {
        private File file;

        @Override
        protected void onPreExecute() {
            if (!swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            String path = strings[0];
            try {
                URL url = new URL(String.format("%s?path=%s", Constants.URL_DOWNLOAD_IMAGE, URLEncoder.encode(path, Constants.UTF_8)));
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);

                //Створення шляху до файлу та відкриття потоку
                file = new File(getCacheDir(), Constants.getFileNameFromPath(path));
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.close();

                int serverResponseCode = connection.getResponseCode();
                if (serverResponseCode != 200)
                    return false;
            } catch (IOException e) {
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            swipeRefreshLayout.setRefreshing(false);
            if (success) {
                photoImageView.setImageURI(Uri.fromFile(file));
                user.setPhotoPathOnDevice(file.getAbsolutePath());
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class NewsTask extends AsyncTask<Integer, Void, Boolean>
    {
        private boolean isEndListNews = false;

        boolean isEndListNews() {
            return isEndListNews;
        }

        @Override
        protected void onPreExecute() {
            if (!swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            HttpURLConnection connection = null;
            String address;
            int schoolID = schoolIDs[integers[0]];

            //Створення посилання за критеріями вибору новину: для всіх/обраної школи, завантаження спочатку або додавання у список
            if (integers.length < 2)
                if (schoolID == 0)
                    address = Constants.URL_GET_NEWS;
                else
                    address = String.format("%s?schoolID=%s", Constants.URL_GET_NEWS, schoolID);
            else
                if (schoolID == 0)
                    address = String.format("%s?newsID=%s", Constants.URL_GET_NEWS, integers[1]);
                else
                    address = String.format("%s?schoolID=%s&newsID=%s", Constants.URL_GET_NEWS, schoolID, integers[1]);

            try {
                URL url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                if (stringBuilder.toString().equals("null")) {
                    isEndListNews = true;
                    return false;
                } else
                    isEndListNews = false;

                parseJSON(stringBuilder.toString());
            } catch (IOException | JSONException | ParseException e) {
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return true;
        }

        private void parseJSON(String response) throws JSONException, ParseException {
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                News news = new News();
                news.setId(object.getInt("id"));
                news.setSchoolID(object.getInt("schoolID"));
                news.setTitle(object.getString("title"));
                news.setText(object.getString("text"));
                news.setImagePath(object.getString("imagePath"));

                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = formatter.parse(object.getString("date"));
                news.setDate(date);

                listNews.add(news);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            swipeRefreshLayout.setRefreshing(false);

            if (success) {
                adapterNews.notifyDataSetChanged();
                textNoDataToDisplayView.setVisibility(View.GONE);

                String[] filePaths = new String[listNews.size()];
                for (int i = 0; i < filePaths.length; i++)
                    filePaths[i] = listNews.get(i).getImagePath();

                DownloadImagesTask downloadImagesTask = new DownloadImagesTask(adapterNews, swipeRefreshLayout, listNews, getCacheDir());
                downloadImagesTask.execute(filePaths);
            } else if(listNews.isEmpty()){
                adapterNews.notifyDataSetChanged();
                textNoDataToDisplayView.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ListSchoolsTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute() {
            if (!swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(Constants.URL_GET_SCHOOLS);
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

        private void parseJSON(String response) throws JSONException {
            JSONArray jsonArray = new JSONArray(response);

            schoolIDs = new int[jsonArray.length()+1];
            schoolIDs[0] = 0;
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                listSchools.add(jsonObject.getString("shortName"));
                schoolIDs[i+1] = jsonObject.getInt("id");
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            swipeRefreshLayout.setRefreshing(false);
            listSchoolsTask = null;

            if (success) {
                arrayAdapterSpinner.notifyDataSetChanged();
                setNewsInMainActivity(0, false);
            }
        }
    }
}
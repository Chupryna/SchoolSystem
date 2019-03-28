package ua.chupryna.schoolsystem.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.News;
import ua.chupryna.schoolsystem.R;

public class NewsActivity extends AppCompatActivity {

    private News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        news = getIntent().getParcelableExtra("news");

        initActionBar();
        initView();
    }

    private void initView() {
        TextView titleNewsTextView = findViewById(R.id.text_news_title_full);
        TextView newsTextView = findViewById(R.id.text_news);
        TextView dateNewsTextView = findViewById(R.id.text_news_date_full);
        ImageView newsImageView = findViewById(R.id.image_news_full);

        titleNewsTextView.setText(news.getTitle());
        newsTextView.setText(news.getText());
        dateNewsTextView.setText(Constants.parseDate(news.getDate(), getApplicationContext()));

        if (news.getImagePathOnDevice() == null)
            newsImageView.setImageResource(R.drawable.school_2);
        else
            newsImageView.setImageURI(Uri.parse(news.getImagePathOnDevice()));
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.news);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}

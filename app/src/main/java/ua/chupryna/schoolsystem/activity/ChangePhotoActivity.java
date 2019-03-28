package ua.chupryna.schoolsystem.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ua.chupryna.schoolsystem.image.ImageFilePath;
import ua.chupryna.schoolsystem.R;

public class ChangePhotoActivity extends AppCompatActivity implements View.OnClickListener
{
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_photo);

        initView();
    }

    private void initView() {
        ImageView imageView = findViewById(R.id.image_change_photo);
        TextView textCancel = findViewById(R.id.text_change_photo_cancel);
        TextView textConfirm = findViewById(R.id.text_change_photo_confirm);

        selectedImageUri = getIntent().getParcelableExtra("selectedImageUri");
        imageView.setImageURI(selectedImageUri);

        textCancel.setOnClickListener(this);
        textConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.text_change_photo_cancel:
                setResult(RESULT_CANCELED);
                break;

            case R.id.text_change_photo_confirm:
                String realPath = ImageFilePath.getPath(ChangePhotoActivity.this, selectedImageUri);
                intent.putExtra("selectedImagePath", realPath);
                setResult(RESULT_OK, intent);
                break;
        }
        finish();
    }
}
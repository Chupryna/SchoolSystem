package ua.chupryna.schoolsystem.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;
import ua.chupryna.schoolsystem.image.CompressImage;
import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.School;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;

public class ProfileActivity extends AppCompatActivity
{
    private User user;
    private School school;
    private SchoolTask schoolTask;
    private ChangeTelephoneTask changeTelephoneTask;
    private FileUploadingTask fileUploadingTask;

    private final int REQUEST_CODE_CHOOSE_FILE = 1000;
    private final int REQUEST_CODE_PERMISSIONS = 2000;

    private ImageView profileImageView;
    private ProgressBar progressBarView;
    private TextView textChangePhoto;
    private TextView textTelephone;
    private TextView textChangeTelephone;
    private TextView textChangePassword;
    private EditText editTelephone;
    private View telephoneForm;
    private View emailForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = initActionBar();
        initView();
        initListeners();
        initMaskForPhone(actionBar);
    }

    private void initView() {
        user = getIntent().getParcelableExtra("user");

        progressBarView = findViewById(R.id.profile_progress);
        profileImageView = findViewById(R.id.image_profile);
        textChangePhoto = findViewById(R.id.text_change_photo);
        TextView textLastName = findViewById(R.id.text_lastName);
        TextView textFirstName = findViewById(R.id.text_firstName);
        TextView textSurName = findViewById(R.id.text_surName);
        TextView textEmail = findViewById(R.id.text_profile_email);
        textTelephone = findViewById(R.id.text_profile_telephone);
        textChangeTelephone = findViewById(R.id.text_change_telephone);
        textChangePassword = findViewById(R.id.text_change_password);
        editTelephone = findViewById(R.id.edit_telephone);
        telephoneForm = findViewById(R.id.profile_telephone_form);
        emailForm = findViewById(R.id.profile_email_form);

        //Встановлення ПІБ і Email
        textLastName.setText(user.getLastName());
        textFirstName.setText(user.getName());
        textSurName.setText(user.getSurName());
        textEmail.setText(user.getEmail());

        //Фото профіля
        File photoFile = new File(user.getPhotoPathOnDevice());
        if (photoFile.exists())
            profileImageView.setImageURI(Uri.fromFile(photoFile));
        else
            profileImageView.setImageResource(R.drawable.profile);
    }

    private void initListeners() {
        //Вибрати нове фото
        textChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoInStorage();
                } else
                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSIONS);
            }
        });

        //Зміна номеру телефону
        textChangeTelephone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = Constants.parseTelephone(editTelephone.getText().toString());
                if (phone.equals(user.getTelephone())) {
                    Toast.makeText(getApplicationContext(), R.string.enter_new_phone, Toast.LENGTH_LONG).show();
                    return;
                }
                changeTelephoneTask = new ChangeTelephoneTask();
                changeTelephoneTask.execute(String.valueOf(user.getId()), phone);
            }
        });

        setSchoolInfo();

        //Відкриття нового вікна з номером телефону для дзвінка
        telephoneForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                String telephone = Constants.parseTelephone(textTelephone.getText().toString());
                intent.setData(Uri.parse(String.format("tel:+%s", telephone)));
                startActivity(intent);
            }
        });

        emailForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = user.getEmail();
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                startActivity(Intent.createChooser(intent, "Відправити лист"));
            }
        });

        //Виклик діалогового вікна для зміни паролю
        textChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialogActivity changePasswordDialog = new ChangePasswordDialogActivity();
                changePasswordDialog.setContext(getApplicationContext());
                changePasswordDialog.setUser(user);
                changePasswordDialog.setObject(changePasswordDialog);
                changePasswordDialog.setImm((InputMethodManager) ProfileActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                changePasswordDialog.show(getSupportFragmentManager(), "changePassword");
            }
        });
    }

    @Nullable
    private ActionBar initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.profile);
        }
        return actionBar;
    }

    private void initMaskForPhone(ActionBar actionBar) {
        //Створення маски для номеру телефону
        MaskImpl mask = MaskImpl.createTerminated(Constants.UA_PHONE_NUMBER);
        FormatWatcher formatWatcher = new MaskFormatWatcher(mask);

        boolean isMyProfile = getIntent().getBooleanExtra("myProfile", true);
        if (!isMyProfile) {
            Objects.requireNonNull(actionBar).setTitle(R.string.profile_user);
            textChangePhoto.setVisibility(View.INVISIBLE);
            textChangeTelephone.setVisibility(View.INVISIBLE);
            textChangePassword.setVisibility(View.GONE);
            editTelephone.setVisibility(View.GONE);
            telephoneForm.setVisibility(View.VISIBLE);

            formatWatcher.installOn(textTelephone);
            textTelephone.setText(user.getTelephone());
        } else {
            formatWatcher.installOn(editTelephone);
            editTelephone.setText(user.getTelephone());
        }
    }

    private void setSchoolInfo()
    {
        if (!Constants.isNetworkAvailable(this)) {
            Toast.makeText(getApplicationContext(), R.string.network_not_available, Toast.LENGTH_LONG).show();
            finish();
        }

        if (schoolTask != null)
            return;

        schoolTask = new SchoolTask();
        schoolTask.execute(String.valueOf(user.getSchoolID()));
    }

    private void showProgress(final boolean show)
    {
        int animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        progressBarView.animate().setDuration(animTime).alpha(show ? 1 : 0).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBarView.setVisibility(show ? View.VISIBLE : View.GONE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhotoInStorage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        final int REQUEST_CODE_CHANGE_PHOTO = 3000;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_FILE:
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri == null)
                        return;

                    Intent intent = new Intent(getApplicationContext(), ChangePhotoActivity.class);
                    intent.putExtra("selectedImageUri", selectedImageUri);

                    startActivityForResult(intent, REQUEST_CODE_CHANGE_PHOTO);
                    break;

                case REQUEST_CODE_CHANGE_PHOTO:
                    updatePhoto(data.getStringExtra("selectedImagePath"));
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Запсук діалогового вікна для вибору фото
    private void choosePhotoInStorage()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
    }

    private void updatePhoto(String selectedImagePath)
    {
        if (!Constants.isNetworkAvailable(this)) {
            Toast.makeText(getApplicationContext(), R.string.network_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (fileUploadingTask != null)
            return;

        fileUploadingTask = new FileUploadingTask(selectedImagePath);
        fileUploadingTask.execute(String.valueOf(user.getId()));
    }


    //Отримання інформації про школу
    @SuppressLint("StaticFieldLeak")
    private class SchoolTask extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected void onPreExecute() {
            showProgress(true);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(String.format("%s?schoolID=%s", Constants.URL_GET_SCHOOL_INFO, strings[0]));
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
            JSONObject object = new JSONObject(response);
            school = new School();
            school.setId(user.getSchoolID());
            school.setFullName(object.getString("fullName"));
            school.setCity(object.getString("city"));
            school.setLocationType(object.getString("locationType"));
            school.setLocationName(object.getString("locationName"));
            school.setHouse(object.getString("house"));
        }

        @Override
        protected void onPostExecute(Boolean success) {
            showProgress(false);
            schoolTask = null;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            if (success) {
                TextView textSchoolName = findViewById(R.id.text_profile_school_name);
                TextView textSchoolLocation = findViewById(R.id.text_profile_school_location);
                View locationForm = findViewById(R.id.profile_location_form);
                textSchoolName.setText(school.getFullName());
                textSchoolLocation.setText(school.toString());

                //Відкриття карти з маркером
                locationForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(school.toString()));
                        mapIntent.setData(uri);
                        if (mapIntent.resolveActivity(getPackageManager()) != null)
                            startActivity(mapIntent);
                    }
                });
            }
        }
    }

    //Зміна номеру телефону
    @SuppressLint("StaticFieldLeak")
    private class ChangeTelephoneTask extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected void onPreExecute() {
            showProgress(true);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;

            try {
                String query = String.format("id=%s&telephone=%s", URLEncoder.encode(strings[0], Constants.UTF_8),
                        URLEncoder.encode(strings[1], Constants.UTF_8));

                URL url = new URL(Constants.URL_CHANGE_PHONE);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-length", String.valueOf(query.length()));

                OutputStream os = connection.getOutputStream();
                os.write(query.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();

                if (!response.equals("update successfully"))
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
            changeTelephoneTask = null;
            showProgress(false);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            if (success)
                Toast.makeText(getApplicationContext(), R.string.phone_successfully_changed, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(), R.string.error_change_phone, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Завантажує фото на сервер
     */
    @SuppressLint("StaticFieldLeak")
    public class FileUploadingTask extends AsyncTask<String, Void, String>
    {
        private String lineEnd = "\r\n";
        private String twoHyphens = "--";
        private String boundary =  "----WebKitFormBoundary9xFB2hiUh5zqbBQ4M";

        // Змінні для зчитування файлу в опер. пам'ять
        private int bytesRead, bytesAvailable, bufferSize;
        private byte[] buffer;
        private int maxBufferSize = 1024*1024;

        // Шлях до файлу на пристрої
        private String filePath;

        // Ключ, под которым файл передается на сервер
        private final String FORM_FILE_NAME = "photo";

        FileUploadingTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings)
        {
            String userID = strings[0];
            String result = null;

            try {
                URL uploadUrl = new URL(Constants.URL_CHANGE_PHOTO);
                HttpURLConnection httpURLConnection = (HttpURLConnection) uploadUrl.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod("POST");

                // Задание необходимых свойств запросу
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                // Створення потоку для запису у з'єднання
                DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());

                // Формування multipart контента
                // Початок контента
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"userID\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(userID + lineEnd);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                // Заголовок элемента формы
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                        FORM_FILE_NAME + "\"; filename=\"" + filePath + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: image/jpg" + lineEnd);
                // Конец заголовка
                outputStream.writeBytes(lineEnd);

                // Стиснення фото
                CompressImage compressImage = new CompressImage(getApplicationContext());
                File compressedImage = compressImage.compressBitmap(new File(filePath), 2, 50);

                // Зчитування файлу в оперативну пам'ять і запис його у з'єднання
                FileInputStream fileInputStream = new FileInputStream(compressedImage);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // Конец элемента формы
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                // Отримання відповіді від сервера
                int serverResponseCode = httpURLConnection.getResponseCode();
                if(serverResponseCode == 200) {
                    result = readStream(httpURLConnection.getInputStream());
                } else {
                    result = readStream(httpURLConnection.getErrorStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        //Отримання відповіді сервера
        String readStream(InputStream inputStream) throws IOException
        {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line);

            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            showProgress(false);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            fileUploadingTask = null;

            String[] response = result.split(":");
            if (response[0].equalsIgnoreCase("File uploaded")) {
                profileImageView.setImageURI(Uri.parse(filePath));
                Toast.makeText(getApplicationContext(), R.string.photo_successfully_changed, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK, new Intent().putExtra("filePath", response[1].trim()));
            }
        }
    }
}
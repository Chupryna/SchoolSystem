package ua.chupryna.schoolsystem.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.R;

public class NewPasswordActivity extends AppCompatActivity {

    private EditText editPasswordView;
    private EditText editPasswordConfirmView;
    private TextView textMessageSetNewPasswordView;
    private TextView textReadyView;
    private ProgressBar progressBarView;
    private View newPasswordFormView;

    private PasswordTask passwordTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        initView();
        initListeners();
    }

    private void initView() {
        editPasswordView = findViewById(R.id.edit_new_password);
        editPasswordConfirmView = findViewById(R.id.edit_new_password_confirm);
        textMessageSetNewPasswordView = findViewById(R.id.text_message_set_new_password);
        progressBarView = findViewById(R.id.new_password_progress);
        newPasswordFormView = findViewById(R.id.new_password_form);
        textReadyView = findViewById(R.id.text_ready);
    }

    private void initListeners() {
        textReadyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSetNewPassword();
            }
        });

        editPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSetNewPassword();
                    return true;
                }
                return false;
            }
        });
    }

    private void attemptSetNewPassword() {
        String password = editPasswordView.getText().toString();
        String passwordConfirm = editPasswordConfirmView.getText().toString();

        if (passwordTask != null)
            return;

        if (!password.equals(passwordConfirm)) {
            editPasswordConfirmView.setError(getString(R.string.passwords_not_match));
            editPasswordConfirmView.requestFocus();
            return;
        }

        if (!isPasswordValid(password)) {
            editPasswordConfirmView.setError(getString(R.string.more_than_5_characters));
            editPasswordConfirmView.requestFocus();
            return;
        }

        View focusView = getCurrentFocus();
        if (focusView instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }

        int userID = getIntent().getIntExtra("userID", 0);
        if (userID == 0) {
            Toast toast = Toast.makeText(NewPasswordActivity.this,
                    "userID = 0", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (Constants.isNetworkAvailable(NewPasswordActivity.this)) {
            showProgress(true);
            passwordTask = new PasswordTask();
            passwordTask.execute(String.valueOf(userID), password);
        } else {
            Toast toast = Toast.makeText(NewPasswordActivity.this,
                    getString(R.string.network_not_available), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /*******************
     *  Показ/приховування прогресу
     *******************/
    private void showProgress(final boolean show) {
        int animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        newPasswordFormView.animate().setDuration(animTime).alpha(show ? (float) 0.4 : 1).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        newPasswordFormView.setAlpha(show ? (float) 0.4 : 1);
                    }
                });

        progressBarView.animate().setDuration(animTime).alpha(show ? 1 : 0).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBarView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }


    @SuppressLint("StaticFieldLeak")
    private class PasswordTask extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... strings)
        {
            HttpURLConnection connection = null;

            try {
                String query = String.format("id=%s&password=%s", URLEncoder.encode(strings[0], Constants.UTF_8),
                        URLEncoder.encode(strings[1], Constants.UTF_8));

                URL url = new URL(Constants.URL_NEW_PASSWORD);
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
                String responce = reader.readLine();
                reader.close();

                if (!responce.equals("password update"))
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
            passwordTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("message","Пароль успішно встановлено");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            else
                textMessageSetNewPasswordView.setText("Не вдалось встановити новий пароль");
        }

        @Override
        protected void onCancelled() {
            passwordTask = null;
            showProgress(false);
        }
    }
}

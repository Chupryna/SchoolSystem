package ua.chupryna.schoolsystem.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.R;

public class PasswordRecoveryActivity extends AppCompatActivity
{
    private EditText editEmailView;                 //Поле для пошти
    private TextView textRecoveryView;              //Клікабельний текст для відправки коду
    private TextView textMessageRecoveryCodeView;   //Текст повідомлення про відправку коду
    private EditText editRecoveryCodeView;              //Поле для коду відновлення
    private TextView textMessageVerificationCodeView;   //Текст повідомлення про перевірку коду
    private TextView textConfirmCodeView;          //Клікабельний текст для перевірки коду
    private View recoveryFormView;
    private ProgressBar progressBarView;

    private RecoveryTask recoveryTask;
    private VerificationCodeTask verificationCodeTask;

    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        initActionBar();
        initView();
        initListeners();
    }

    private void initView() {
        editEmailView = findViewById(R.id.edit_email_for_recovery);
        textRecoveryView = findViewById(R.id.text_recovery);
        textMessageRecoveryCodeView = findViewById(R.id.text_message_recovery_code);
        editRecoveryCodeView = findViewById(R.id.edit_recovery_code);
        textMessageVerificationCodeView = findViewById(R.id.text_message_verification_code);
        textConfirmCodeView = findViewById(R.id.text_confirm_code);
        recoveryFormView = findViewById(R.id.recovery_form);
        progressBarView = findViewById(R.id.recovery_progress);

        editEmailView.setText(getIntent().getStringExtra("email"));
        textConfirmCodeView.setEnabled(false);
        editRecoveryCodeView.setEnabled(false);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initListeners() {
        editRecoveryCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptConfirmCode();
                    return true;
                }
                return false;
            }
        });

        textRecoveryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRecovery();
            }
        });
        textConfirmCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptConfirmCode();
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

    /*****************
     * Перевірка коректності email і запуск потоку відновлення
     ****************/
    private void attemptRecovery()
    {
        if (recoveryTask != null)
            return;

        textMessageRecoveryCodeView.setText("");

        String email = editEmailView.getText().toString();
        editEmailView.setError(null);

        if(!isEmailValid(email)) {
            editEmailView.setError(getString(R.string.error_invalid_email));
            editEmailView.requestFocus();
            return;
        }

        //Приховання клавіатури
        View focusView = getCurrentFocus();
        if (focusView instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }

        //Перервірка доступності мережі
        if (Constants.isNetworkAvailable(PasswordRecoveryActivity.this)) {
            recoveryTask = new RecoveryTask();
            recoveryTask.execute(email);
        }
        else {
            Toast toast = Toast.makeText(PasswordRecoveryActivity.this,
                    getString(R.string.network_not_available), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    /*************
     * Запуск завдання перевірки коду
     *************/
    private void attemptConfirmCode()
    {
        if (verificationCodeTask != null)
            return;

        String code = editRecoveryCodeView.getText().toString();
        if (TextUtils.isEmpty(code)) {
            editRecoveryCodeView.setError("Заповніть поле");
            editRecoveryCodeView.requestFocus();
            return;
        }

        //Приховання клавіатури
        View focusView = getCurrentFocus();
        if (focusView instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }

        //Перервірка доступності мережі
        if (Constants.isNetworkAvailable(PasswordRecoveryActivity.this)) {
            verificationCodeTask = new VerificationCodeTask();
            verificationCodeTask.execute(code);
        }
        else {
            Toast.makeText(PasswordRecoveryActivity.this,
                    getString(R.string.network_not_available), Toast.LENGTH_LONG).show();
        }
    }

    /*******************
     *  Показ/приховування прогресу
     *******************/
    private void showProgress(final boolean show)
    {
        int animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        recoveryFormView.animate().setDuration(animTime).alpha(show ? (float) 0.4 : 1).setListener(
                new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                recoveryFormView.setAlpha(show ? (float) 0.4 : 1);
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
    public class RecoveryTask extends AsyncTask<String, Void, Boolean>
    {
        private String messageRecoveryCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            HttpURLConnection connection = null;

            try {
                String query = String.format("email=%s", URLEncoder.encode(strings[0], Constants.UTF_8));

                URL url = new URL(Constants.URL_RECOVERY);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-length", String.valueOf(query.length()));

                OutputStream os = connection.getOutputStream();
                os.write(query.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();

                switch (response) {
                    case "email not found":
                        messageRecoveryCode = getString(R.string.email_not_found);
                        return false;

                        //Відправити і обробити userID
                    case "code already sent":
                        messageRecoveryCode = getString(R.string.code_already_sent);
                        return true;

                    case "code not created":
                        messageRecoveryCode = getString(R.string.error_send_code);
                        return false;

                    case "code not sent":
                        messageRecoveryCode = getString(R.string.error_send_code);
                        return false;
                }

                JSONObject jsonObject = new JSONObject(response);
                userID = jsonObject.getInt("id");
                messageRecoveryCode = getString(R.string.successfully_send_code);
            } catch (IOException | JSONException e) {
                messageRecoveryCode = getString(R.string.error_send_code);
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            recoveryTask = null;
            showProgress(false);

            textMessageRecoveryCodeView.setText(messageRecoveryCode);
            if (success) {
                editRecoveryCodeView.setEnabled(true);
                textConfirmCodeView.setEnabled(true);

                textRecoveryView.setEnabled(false);
                editEmailView.setEnabled(false);
            }
        }

        @Override
        protected void onCancelled() {
            recoveryTask = null;
            showProgress(false);
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class VerificationCodeTask extends AsyncTask<String, Void, Boolean>
    {
        private String messageVerificationCode;
        private boolean result = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            HttpURLConnection connection = null;

            try {
                String query = String.format("id=%s&code=%s", userID, strings[0]);

                URL url = new URL(Constants.URL_CODE_VERIFICATION);
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

                switch (response) {
                    case "code validity time is over":
                        messageVerificationCode = "Час дії коду вичерпано";
                        break;

                    case "number of attempts exceeded":
                        messageVerificationCode = "Перевищено кількість допустимих спроб";
                        break;

                    case "code not verified":
                        messageVerificationCode = "Код введено невірно";
                        break;

                    case "code verified":
                        messageVerificationCode = "Код підтверджено";
                        result = true;
                        break;
                }

            } catch (IOException e) {
                return result;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            verificationCodeTask = null;
            showProgress(false);
            textMessageVerificationCodeView.setText(messageVerificationCode);

            if (success) {
                Intent intent = new Intent(getApplicationContext(), NewPasswordActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
            else
                editRecoveryCodeView.setText("");
        }
    }
}
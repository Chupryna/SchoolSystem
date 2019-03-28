package ua.chupryna.schoolsystem.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.nio.charset.StandardCharsets;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.R;
import ua.chupryna.schoolsystem.model.User;

public class LoginActivity extends AppCompatActivity
{
    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private CheckBox cbShowPassword;
    private Button mEmailSignInButton;
    private TextView tvPasswordRecovery;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        if (message != null) {
            Toast toast = Toast.makeText(getApplicationContext(), message,
                    Toast.LENGTH_LONG);
            toast.show();
        }

        initView();
        initListeners();
    }

    private void initView() {
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        tvPasswordRecovery = findViewById(R.id.text_password_recovery);
        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void initListeners() {
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        tvPasswordRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PasswordRecoveryActivity.class);
                intent.putExtra("email", mEmailView.getText().toString());
                startActivity(intent);
            }
        });

        //Показ/приховування паролю
        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    mPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mPasswordView.setSelection(mPasswordView.length());
            }
        });
    }

    //Перевірка помилок у вхідних даних і запуск завдання авторизації
    private void attemptLogin() {
        if (mAuthTask != null)
            return;

        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Отримання логіну і паролю з вікна
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Перевірка коректності паролю
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password_length));
            focusView = mPasswordView;
            cancel = true;
        }

        // Перевірка коректності логіну
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //Перевірка присутності помилки
        if (cancel) {
            focusView.requestFocus();
            return;
        } else {
            focusView = getCurrentFocus();
            if (focusView instanceof EditText) {
                InputMethodManager imm = (InputMethodManager) LoginActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
        }

        //Перевірка доступності мережі
        if (Constants.isNetworkAvailable(LoginActivity.this)) {
            if (cbShowPassword.isChecked())
                cbShowPassword.setChecked(false);

            //Запуск фонового завдання для логування
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        } else
            Toast.makeText(LoginActivity.this, R.string.network_not_available, Toast.LENGTH_LONG).show();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    //Показ прогресу
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        //Приховання/показ форми логування
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? (float) 0.4 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setAlpha(show ? (float) 0.4 : 1);
            }
        });

        //Приховання/показ форми прогресу
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


    //Авторизація користувача
    @SuppressLint("StaticFieldLeak")
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {
        private final String mEmail;
        private final String mPassword;
        private User user;
        boolean incorrectAutoData;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            user = new User();
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            String result, line;
            incorrectAutoData = false;
            HttpURLConnection httpURLConnection = null;

            try {
                String query = String.format("email=%s&password=%s", URLEncoder.encode(mEmail, Constants.UTF_8),
                        URLEncoder.encode(mPassword, Constants.UTF_8));

                URL url = new URL(Constants.URL_AUTHORIZATION);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(20000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Content-length", String.valueOf(query.length()));

                //Запис у вихідний потік даних для авторизації
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(query.getBytes());
                os.flush();
                os.close();

                //Отримання відповіді
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    stringBuilder.append(line);
                result = stringBuilder.toString();

                reader.close();

                //Перевірка відповіді про коректність даних авторизації
               if (result.equals("incorrect email") || result.equals("incorrect password")) {
                    incorrectAutoData = true;
                    return false;
                }

                //Парсінг відповіді
                parseJSON(result);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (httpURLConnection != null)
                    httpURLConnection.disconnect();
            }

            return true;
        }

        private void parseJSON(String result) throws JSONException
        {
            JSONObject jsonObject = new JSONObject(result);
            user.setId(jsonObject.getInt("id"));
            user.setLastName(jsonObject.getString("lastName"));
            user.setName(jsonObject.getString("firstName"));
            user.setSurName(jsonObject.getString("surName"));
            user.setEmail(jsonObject.getString("email"));
            user.setTelephone(jsonObject.getString("telephone"));
            user.setPhotoPath(jsonObject.getString("photo"));
            user.setGroupID(jsonObject.getInt("groupID"));
            user.setSchoolID(jsonObject.getInt("schoolID"));
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            if (success) {
              Intent intent = new Intent(getApplicationContext(), MainActivity.class);
              intent.putExtra("user", user);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              startActivity(intent);
              finish();
            } else if (incorrectAutoData) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            else {
                Toast toastErrorAuto = Toast.makeText(LoginActivity.this, getString(R.string.error_authorization), Toast.LENGTH_LONG);
                toastErrorAuto.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}
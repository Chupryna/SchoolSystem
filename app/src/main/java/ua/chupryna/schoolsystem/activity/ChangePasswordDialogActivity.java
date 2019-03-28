package ua.chupryna.schoolsystem.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;

public class ChangePasswordDialogActivity extends DialogFragment
{
    private EditText currentPasswordEdit;
    private EditText newPasswordEdit;
    private EditText confirmNewPasswordEdit;
    private CheckBox cbShowPassword;

    private User user;
    private Context context;
    private InputMethodManager imm;
    private ChangePasswordTask changePasswordTask;
    private ChangePasswordDialogActivity changePasswordDialogActivity;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setObject(ChangePasswordDialogActivity object) {
        this.changePasswordDialogActivity = object;
    }

    public void setImm(InputMethodManager imm) {
        this.imm = imm;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        currentPasswordEdit = view.findViewById(R.id.edit_current_password);
        newPasswordEdit = view.findViewById(R.id.edit_new_password);
        confirmNewPasswordEdit = view.findViewById(R.id.edit_new_password_confirm);
        cbShowPassword = view.findViewById(R.id.cbShowPassword);
        cbShowPassword.setOnCheckedChangeListener(onCheckedChangeListener);

        TextView positiveTextView = view.findViewById(R.id.text_confirm_change_password);
        positiveTextView.setOnClickListener(onClickListenerPositive);
        TextView negativeTextView = view.findViewById(R.id.text_negative);
        negativeTextView.setOnClickListener(onClickListenerNegative);

        builder.setMessage(R.string.change_password);
        builder.setView(view);

        return builder.create();
    }

    //Обробник чекбоксу
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                currentPasswordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                newPasswordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmNewPasswordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                if (currentPasswordEdit.hasFocus())
                    currentPasswordEdit.setSelection(currentPasswordEdit.getText().length());
                if (newPasswordEdit.hasFocus())
                    newPasswordEdit.setSelection(newPasswordEdit.getText().length());
                if (confirmNewPasswordEdit.hasFocus())
                    confirmNewPasswordEdit.setSelection(confirmNewPasswordEdit.getText().length());
            }
            else {
                currentPasswordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                newPasswordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmNewPasswordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());

                if (currentPasswordEdit.hasFocus())
                    currentPasswordEdit.setSelection(currentPasswordEdit.getText().length());
                if (newPasswordEdit.hasFocus())
                    newPasswordEdit.setSelection(newPasswordEdit.getText().length());
                if (confirmNewPasswordEdit.hasFocus())
                    confirmNewPasswordEdit.setSelection(confirmNewPasswordEdit.getText().length());
            }
        }
    };

    //Обробник кнопки Підтвердити
    private View.OnClickListener onClickListenerPositive = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            String currentPassword = currentPasswordEdit.getText().toString();
            String newPassword = newPasswordEdit.getText().toString();
            String confirmNewPassword = confirmNewPasswordEdit.getText().toString();

            if (!isPasswordValid(currentPassword)) {
                currentPasswordEdit.setError(getString(R.string.error_invalid_password_length));
                currentPasswordEdit.requestFocus();
                return;
            }

            if (!isPasswordValid(newPassword)) {
                newPasswordEdit.setError(getString(R.string.more_than_5_characters));
                newPasswordEdit.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                confirmNewPasswordEdit.setError(getString(R.string.passwords_not_match));
                confirmNewPasswordEdit.requestFocus();
                return;
            }

            //Перевірка наявності мережі та запуск завдання зміни паролю
            if (Constants.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
                cbShowPassword.setChecked(false);
                imm.hideSoftInputFromWindow(confirmNewPasswordEdit.getWindowToken(), 0);
                changePasswordTask = new ChangePasswordTask();
                changePasswordTask.execute(String.valueOf(user.getId()), currentPassword, newPassword);
            }
            else
                Toast.makeText(context, R.string.network_not_available, Toast.LENGTH_LONG).show();
        }
    };

    //Обробник кнопки Скасувати
    private View.OnClickListener onClickListenerNegative = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            changePasswordDialogActivity.dismiss();
        }
    };


    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }



    @SuppressLint("StaticFieldLeak")
    public class ChangePasswordTask extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... strings)
        {
            HttpURLConnection connection = null;

            try {
                String query = String.format("id=%s&oldPassword=%s&newPassword=%s", URLEncoder.encode(strings[0], Constants.UTF_8),
                        URLEncoder.encode(strings[1], Constants.UTF_8), URLEncoder.encode(strings[2], Constants.UTF_8));

                URL url = new URL(Constants.URL_CHANGE_PASSWORD);
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

                if (!response.equals("password update"))
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
        protected void onPostExecute(Boolean success)
        {
            changePasswordTask = null;
            //showProgress(false);

            if (success) {
                changePasswordDialogActivity.dismiss();
                Toast.makeText(context, R.string.password_successfully_changed, Toast.LENGTH_LONG).show();
            }
            else {
                currentPasswordEdit.setError(getString(R.string.incorrect_password));
                currentPasswordEdit.requestFocus();
            }
        }
    }
}
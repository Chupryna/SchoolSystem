package ua.chupryna.schoolsystem;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ua.chupryna.schoolsystem.model.SchoolClass;

public class SchoolClassTask extends AsyncTask<Integer, Void, SchoolClass>
{
    @Override
    protected SchoolClass doInBackground(Integer... integers)
    {
        HttpURLConnection connection = null;
        SchoolClass schoolClass = null;
        try {
            URL url = new URL(String.format("%s?pupilID=%s", Constants.URL_GET_CLASS, integers[0]));
            connection = (HttpURLConnection) url.openConnection();

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            schoolClass = parseJSON(stringBuilder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return schoolClass;
    }

    private SchoolClass parseJSON(String response) throws JSONException {
        JSONObject objectClass = new JSONObject(response);
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(objectClass.getInt("id"));
        schoolClass.setNumber(objectClass.getInt("number"));
        schoolClass.setLetter(objectClass.getString("letter"));

        return schoolClass;
    }
}
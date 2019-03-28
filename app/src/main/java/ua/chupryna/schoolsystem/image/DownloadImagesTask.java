package ua.chupryna.schoolsystem.image;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.News;
import ua.chupryna.schoolsystem.model.User;

public class DownloadImagesTask extends AsyncTask<String, Void, Boolean>
{
    @SuppressLint("StaticFieldLeak")
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.Adapter adapter;
    private ArrayList list;
    private File customDir;

    public DownloadImagesTask(@NonNull RecyclerView.Adapter adapter, SwipeRefreshLayout swipeRefreshLayout,
                              @NonNull ArrayList list, @NonNull File customDir) {
        this.adapter = adapter;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.list = list;
        this.customDir = customDir;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected Boolean doInBackground(String... filePaths)
    {
        /*File customDir = new File(getCacheDir(), "teachers");
        if (!customDir.exists()) {
            boolean isMakeDir = customDir.mkdir();
            if (!isMakeDir)
                customDir = getCacheDir();
        }*/

        for (int i = 0; i < filePaths.length; i++)
        {
            String path = filePaths[i];
            if (path == null || path.equals("null"))
                continue;

            File image = new File(customDir, Constants.getFileNameFromPath(path));
            if (!image.exists()) {
                boolean isSuccessfullyDownload = downloadImage(path, image);
                if (isSuccessfullyDownload)
                    setPathOnDevice(i, image);
            } else {
                setPathOnDevice(i, image);
            }
        }

        return true;
    }

    private boolean downloadImage(String filePath, File image)
    {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(String.format("%s?path=%s",Constants.URL_DOWNLOAD_IMAGE, URLEncoder.encode(filePath, Constants.UTF_8)));
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(20000);

            InputStream is = connection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(image);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer, 0, 1024)) != -1) {
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

    private void setPathOnDevice(int i, File image)
    {
        if (list.get(i) instanceof User)
            ((User)list.get(i)).setPhotoPathOnDevice(image.getAbsolutePath());
        else if (list.get(i) instanceof News)
            ((News)list.get(i)).setImagePathOnDevice(image.getAbsolutePath());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        if (success)
            adapter.notifyDataSetChanged();
    }
}
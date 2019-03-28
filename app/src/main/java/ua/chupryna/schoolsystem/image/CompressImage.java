package ua.chupryna.schoolsystem.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CompressImage
{
    private static final int SIZE_LIMIT = 300;
    private File tempFile;

    public CompressImage(Context context) {
        tempFile = new File(context.getCacheDir(), String.format("photo_profile_%s.jpg", System.currentTimeMillis()));
    }

    public File compressBitmap(File file, int sampleSize, int quality)
    {
        if (file.length() / 1024 < SIZE_LIMIT) {
            return file;
        }

        try {
            FileInputStream inputStream = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.close();

            long lengthInKb = tempFile.length() / 1024;
            if (lengthInKb > SIZE_LIMIT) {
                compressBitmap(tempFile, sampleSize, quality);
            }
            selectedBitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }
}
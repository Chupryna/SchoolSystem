package ua.chupryna.schoolsystem.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class News implements Parcelable
{
    private int id;
    private int schoolID;
    private String title;
    private String text;
    private String imagePath;
    private String imagePathOnDevice;
    private Date date;

    public News() {
    }

    public int getSchoolID() {
        return schoolID;
    }

    public void setSchoolID(int schoolID) {
        this.schoolID = schoolID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePathOnDevice() {
        return imagePathOnDevice;
    }

    public void setImagePathOnDevice(String path) {
        imagePathOnDevice = path;
    }

    private News(Parcel in) {
        id = in.readInt();
        schoolID = in.readInt();
        title = in.readString();
        text = in.readString();
        imagePath = in.readString();
        imagePathOnDevice = in.readString();
        date = (Date) in.readValue(Date.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(schoolID);
        dest.writeString(title);
        dest.writeString(text);
        dest.writeString(imagePath);
        dest.writeString(imagePathOnDevice);
        dest.writeValue(date);
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };
}
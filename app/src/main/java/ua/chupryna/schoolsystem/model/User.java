package ua.chupryna.schoolsystem.model;

import android.os.Parcel;
import android.os.Parcelable;


public class User implements Parcelable
{
    private int id;
    private String lastName;
    private String name;
    private String surName;
    private String telephone;
    private String email;
    private String photoPath;
    private String photoPathOnDevice;
    private int groupID;
    private int schoolID;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getPhotoPathOnDevice() {
        return photoPathOnDevice;
    }

    public void setPhotoPathOnDevice(String photoPathOnDevice) {
        this.photoPathOnDevice = photoPathOnDevice;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getSchoolID() {
        return schoolID;
    }

    public void setSchoolID(int schoolID) {
        this.schoolID = schoolID;
    }

    private User(Parcel in) {
        id = in.readInt();
        lastName = in.readString();
        name = in.readString();
        surName = in.readString();
        telephone = in.readString();
        email = in.readString();
        photoPath = in.readString();
        photoPathOnDevice = in.readString();
        groupID = in.readInt();
        schoolID = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(lastName);
        dest.writeString(name);
        dest.writeString(surName);
        dest.writeString(telephone);
        dest.writeString(email);
        dest.writeString(photoPath);
        dest.writeString(photoPathOnDevice);
        dest.writeInt(groupID);
        dest.writeInt(schoolID);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
package ua.chupryna.schoolsystem.model;


import android.os.Parcel;
import android.os.Parcelable;

public class SchoolClass implements Parcelable
{
    private int id;
    private int number;
    private String letter;

    public SchoolClass() {
    }

    public SchoolClass(int id, int number, String letter) {
        this.id = id;
        this.number = number;
        this.letter = letter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", number, letter);
    }


    private SchoolClass(Parcel in) {
        id = in.readInt();
        number = in.readInt();
        letter = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(number);
        dest.writeString(letter);
    }

    public static final Creator<SchoolClass> CREATOR = new Creator<SchoolClass>() {
        @Override
        public SchoolClass createFromParcel(Parcel in) {
            return new SchoolClass(in);
        }

        @Override
        public SchoolClass[] newArray(int size) {
            return new SchoolClass[size];
        }
    };
}
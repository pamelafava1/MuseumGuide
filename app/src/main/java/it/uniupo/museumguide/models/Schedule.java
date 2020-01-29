package it.uniupo.museumguide.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Schedule implements Parcelable {

    private boolean open;
    private String openingTime;
    private String closingTime;

    public Schedule() {}

    public Schedule(boolean open, String openingTime, String closingTime) {
        this.open = open;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    private Schedule(Parcel in) {
        open = in.readByte() != 0;
        openingTime = in.readString();
        closingTime = in.readString();
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    public boolean isOpen() {
        return open;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (open ? 1 : 0));
        dest.writeString(openingTime);
        dest.writeString(closingTime);
    }
}

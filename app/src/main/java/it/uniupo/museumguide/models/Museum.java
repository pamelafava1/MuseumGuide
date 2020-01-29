package it.uniupo.museumguide.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Museum implements Parcelable {

    private String id;
    private String name;
    private String location;
    private double latitude;
    private double longitude;
    private List<Schedule> schedules;

    public Museum() {}

    public Museum(String id, String name, String location, double latitude, double longitude, List<Schedule> schedules) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.schedules = schedules;
    }

    private Museum(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        schedules = new ArrayList<>();
        in.readList(schedules, getClass().getClassLoader());
    }

    public static final Creator<Museum> CREATOR = new Creator<Museum>() {
        @Override
        public Museum createFromParcel(Parcel in) {
            return new Museum(in);
        }

        @Override
        public Museum[] newArray(int size) {
            return new Museum[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeList(schedules);
    }
}

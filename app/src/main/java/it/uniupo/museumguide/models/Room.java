package it.uniupo.museumguide.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Room implements Parcelable {

    private String id;
    private String idMuseum;
    private String name;

    public Room() {}

    public Room(String id, String idMuseum, String name) {
        this.id = id;
        this.idMuseum = idMuseum;
        this.name = name;
    }

    private Room(Parcel in) {
        id = in.readString();
        idMuseum = in.readString();
        name = in.readString();
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getIdMuseum() {
        return idMuseum;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(idMuseum);
        dest.writeString(name);
    }
}

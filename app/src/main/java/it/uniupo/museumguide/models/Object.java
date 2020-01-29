package it.uniupo.museumguide.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Object implements Parcelable {

    private String id;
    private String idRoom;
    private String name;
    private String image;
    private String description;

    public Object() {}

    public Object(String id, String idRoom, String name, String image, String description) {
        this.id = id;
        this.idRoom = idRoom;
        this.name = name;
        this.image = image;
        this.description = description;
    }

    private Object(Parcel in) {
        id = in.readString();
        idRoom = in.readString();
        name = in.readString();
        image = in.readString();
        description = in.readString();
    }

    public static final Creator<Object> CREATOR = new Creator<Object>() {
        @Override
        public Object createFromParcel(Parcel in) {
            return new Object(in);
        }

        @Override
        public Object[] newArray(int size) {
            return new Object[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getIdRoom() {
        return idRoom;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(idRoom);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(description);
    }
}

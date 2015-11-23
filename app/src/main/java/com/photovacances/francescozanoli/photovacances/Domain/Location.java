package com.photovacances.francescozanoli.photovacances.Domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by francescozanoli on 24/09/15.
 */
//Class temporanee, on ne la utilise pas dans le database et elle a été code pour une futur version de l'appli
public class Location implements Parcelable,Serializable {
    private String name;
    private int latitude, longitude;

    public Location(String name) {
        setName(name);
        setLatitude(0);
        setLongitude(0);
    }

    public Location(Parcel in) {

        name = in.readString();
        latitude = in.readInt();
        longitude=in.readInt();
    }
    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(latitude);
        dest.writeInt(longitude);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getLatitude() {
        return latitude;
    }
    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }
    public int getLongitude() {
        return longitude;
    }
    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return getName();
    }
}

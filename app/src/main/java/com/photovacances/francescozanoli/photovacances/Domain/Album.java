package com.photovacances.francescozanoli.photovacances.Domain;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.photovacances.francescozanoli.photovacances.Common.Pair;
import com.photovacances.francescozanoli.photovacances.Exception.PeriodDateException;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by francescozanoli on 24/09/15.
 */
//Tout le class dans domain sont la description du database, ils ont le meme colons/variables
public class Album implements Parcelable,Serializable{

    //Variables de l'album, tout le SerializedName sont aussi de colons dans la base de donnes
    @com.google.gson.annotations.SerializedName("id")
    private String id;
    @com.google.gson.annotations.SerializedName("name")
    private String name;
    @com.google.gson.annotations.SerializedName("start")
    private Date start;
    @com.google.gson.annotations.SerializedName("fin")
    private Date fin;
    @com.google.gson.annotations.SerializedName("idcover")
    private String idCover;
    @com.google.gson.annotations.SerializedName("location")
    private String location;

    //L'attribu transient permet de ne pas cherche le colon sur le database, no serialized
    transient private Location locationO;
    transient private Pair<Date, Date> period;
    transient private List<Comment> comments;
    transient private List<Photo> photos;
    transient private Photo cover;

    //Construtcteur
    public Album(String id){
        this.id=id;
    }
    public Album(String id,String name,Date start,Date fin,String idCover,String location){
        try{
            this.id=id;
            setName(name);
            setPeriod(new Pair<Date, Date>(start, fin));
            setIdCover(idCover);
            setLocation(location);
        }
        catch (Exception e){}
    }
    public Album(String id,String name, Pair<Date, Date> period, Location location) throws Exception {
        setName(name);
        setPeriod(period);
        setLocation(location.getName());
        this.id=id;
        photos=new ArrayList<>();
        comments=new ArrayList<>();
    }
    public Album(){}

    //Implementation de l'interface IParcable
    //Constructeur pour envoie de donne par l'intent
    protected Album(Parcel in) {
        try {
            id = in.readString();
            name = in.readString();
            setLocation(((Location) in.readParcelable(Location.class.getClassLoader())).getName());
            setCover((Photo) in.readParcelable(Photo.class.getClassLoader()));
            setPeriod(new Pair<>(new Date(in.readInt(), in.readInt(), in.readInt()), new Date(in.readInt(), in.readInt(), in.readInt())));
            comments = in.createTypedArrayList(Comment.CREATOR);
            photos = in.createTypedArrayList(Photo.CREATOR);
        }catch (Exception e){

        }

    }
    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(locationO, Parcelable.CONTENTS_FILE_DESCRIPTOR);
        dest.writeParcelable(cover, Parcelable.CONTENTS_FILE_DESCRIPTOR);
        dest.writeInt(period.getFirst().getDay());
        dest.writeInt(period.getFirst().getMonth());
        dest.writeInt(period.getFirst().getYear());
        dest.writeInt(period.getSecond().getDay());
        dest.writeInt(period.getSecond().getMonth());
        dest.writeInt(period.getSecond().getYear());
        dest.writeParcelableArray(photos.toArray(new Photo[photos.size()]), Parcelable.CONTENTS_FILE_DESCRIPTOR);
        dest.writeParcelableArray(comments.toArray(new Comment[comments.size()]), Parcelable.CONTENTS_FILE_DESCRIPTOR);
    }

    //Methode pour acceder aux variables
    public Photo getCover() {
        if (cover==null)
            return new Photo();
        return cover;
    }
    public String getId() {
        return id;
    }
    public void setCover(Photo cover) {
        if (cover!=null)this.idCover=cover.getId();
        this.cover = cover;
    }
    public Location getLocation() {
        return new Location(location);
    }
    public void setLocation(String location) {
        this.locationO = new Location(location);
        this.location=location;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Pair<Date, Date> getPeriod() {
        return new Pair<>(start,fin);
    }
    public void setPeriod(Pair<Date, Date> period) throws Exception {
        if (!period.getFirst().before(period.getSecond()))
            throw new PeriodDateException("La date de fin doit etre posterieure à la date de debuit");
        this.period = period;
        this.start=period.getFirst();
        this.fin=period.getSecond();
    }
    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    public List<Photo> getPhotos() {
        if (photos!=null)
            return photos;
        return new ArrayList<>();
    }
    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
    public void setIdCover(String idCover) {
        this.idCover = idCover;
    }
    public String getIdCover() {
        return idCover;
    }


    //Methode standart de Object, la methode equals a été re-ecrit car il nous permit de recherche dans un list avec la methode standart indexOf
    @Override
    public String toString() {
        return getName();
    }
    public boolean equals(Object other) {
        if (id==null)
            return false;
        if (other!=null && (Album)other!=null ) {
            Album o = (Album) other;
            return (this.id.equals(o.getId()));
        }else
            return false;
    }
}

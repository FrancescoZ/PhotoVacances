package com.photovacances.francescozanoli.photovacances.Domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by francescozanoli on 24/09/15.
 */
public class Comment implements Parcelable,Serializable {

    //variable et indice des colons dans le database
    @com.google.gson.annotations.SerializedName("comment")
    private String comment;
    @com.google.gson.annotations.SerializedName("id")
    private String id;
    @com.google.gson.annotations.SerializedName("idphoto")
    private String idPhoto;

    //Methode pour acceder aux variables
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setIdPhoto(String idPhoto) {
        this.idPhoto = idPhoto;
    }
    public String getIdPhoto(){
        return idPhoto;
    }

    //Interface IParceble
    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
    protected Comment(Parcel in) {
        comment = in.readString();
        id=in.readString();
        idPhoto=in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comment);
        dest.writeString(id);
        dest.writeString(idPhoto);
    }

    //Constructor
    public Comment(String comment) {
        this.comment = comment;
    }
    public Comment(String id,String comment,String idPhoto){
        this.id=id;
        this.comment=comment;
        this.idPhoto=idPhoto;
    }

    //Methode appeller pour chercher dans une list
    public boolean equals(Object other) {
        if ((Comment)other!=null ) {
            Comment o = (Comment) other;
            return (this.id==o.getId());
        }else
            return false;
    }
}

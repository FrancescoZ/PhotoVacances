package com.photovacances.francescozanoli.photovacances.Domain;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by francescozanoli on 24/09/15.
 */
public class Photo implements Parcelable,Serializable {

    //Interface IParceable
    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
    public Photo(Parcel in) {
        id = in.readString();
        name = in.readString();
        fileName = in.readString();
        idAlbum=in.readString();
        List<String> l=new ArrayList<>();

        img=(Bitmap)in.readParcelable(getClass().getClassLoader());
        comments=new ArrayList<>();
        in.readStringList(l);
        for (Iterator<String> i=l.iterator();i.hasNext();)
            comments.add(new Comment(i.next()));
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(fileName);
        dest.writeString(idAlbum);
        List<String> l=new LinkedList<>();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        // Convert Drawable to Bitmap first:
        Bitmap bitmap = img;
        // Serialize bitmap as Parcelable:
        dest.writeParcelable(bitmap, flags);
        if (comments!=null)
            for (Iterator<Comment> i=comments.iterator();i.hasNext();)
                l.add(i.next().getComment());
        dest.writeStringList(l);
    }

    @com.google.gson.annotations.SerializedName("name")
    private String name;
    @com.google.gson.annotations.SerializedName("filename")
    private String fileName=null;
    @com.google.gson.annotations.SerializedName("id")
    private String id;
    @com.google.gson.annotations.SerializedName("idalbum")
    private String idAlbum;
    @com.google.gson.annotations.SerializedName("cover")
    private boolean cover;

    transient private String path=null;
    transient private Bitmap img;
    transient private String image;
    transient private List<Comment> comments=new ArrayList<>();


    public Photo(){ }
    public Photo(String id,String name, String fileName,String idAlbum) {
        this.id=id;
        setName(name);
        setIdAlbum(idAlbum);
        setFileName(fileName);
    }
    public Photo(String name, String image) {
        setName(name);
        setImage(image);
    }

    //Methode pour acceder aux variables
    public void setFileName(String value) {
        fileName =value;
    }
    public String getFileName() {
        return fileName;
    }
    public void setCover(boolean value) {
        cover =value;
    }
    public boolean getCover() {
        return cover;
    }
    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    public Bitmap getImg() {
        return img;
    }
    public void setImg(Bitmap img) {
        this.img = img;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getPath(){
        return path;
    }
    public void setIdAlbum(String idAlbum) {
        this.idAlbum = idAlbum;
    }


    public boolean equals(Object other) {
        if ((Photo)other!=null ) {
            Photo o = (Photo) other;
            return (this.id.equals(o.getId()));
        }else
            return false;
    }
}

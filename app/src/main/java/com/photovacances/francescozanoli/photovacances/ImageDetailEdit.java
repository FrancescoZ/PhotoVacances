package com.photovacances.francescozanoli.photovacances;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.photovacances.francescozanoli.photovacances.Domain.Photo;

//Permet de dessiner sur la photo mais il permet pas de l'enregistre avec le modification
public class ImageDetailEdit extends Fragment{

    //ImageView est apres utiliser comme DrawingView
    ImageView choosenImageView;
    View v;

    //bitmap initial
    Bitmap bmp;
    //utliser pour registrer la photo
    Bitmap alteredBitmap;
    Canvas canvas;
    Paint paint;
    Matrix matrix;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;

    public ImageDetailEdit() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_image_detail_edit, container, false);
        choosenImageView=(ImageView) v.findViewById(R.id.editableImage);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //montre la photo chosi
        choosenImageView.setImageBitmap(bmp);
        choosenImageView.setOnTouchListener((View.OnTouchListener) choosenImageView);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void setUpImage(Photo photo) {
        try {
            //meme type de cas possible, void ALbumDetail
            if (photo.getPath()!=null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDither = false;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inSampleSize = 3;
                options.inPurgeable = true;
                Bitmap bitmap = BitmapFactory.decodeFile(photo.getPath(),options);
                bmp=bitmap;
            }else if (photo.getImg()!=null)
                bmp=photo.getImg();
            else
                bmp= BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        } catch (Exception e) {
            Log.v("ERROR", e.toString());
        }

    }

    //Change la couleur
    public void changePaint(String s) {
        ((DrawingView)choosenImageView).setColor(s);
    }

    //effacer de ligne dessinè
    public void getBack(){
        ((DrawingView)choosenImageView).eraseLast();
    }

    //enregistre la photo
    public Bitmap getBitmap(){
        return ((DrawingView)choosenImageView).getBitmap(bmp,v.getContext());
    }
}

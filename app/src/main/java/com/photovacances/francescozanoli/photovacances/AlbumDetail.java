package com.photovacances.francescozanoli.photovacances;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Comment;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//Fragment de droit qui montre le contenu de l'album
public class AlbumDetail extends Fragment {

    //La list des variables ici est present en touts le fragment donc elle ne sera pas explique à nouveau
    //View principal, fait reference à l'activity
    private View v;
    //Objet qui gere l'interface
    private OnAlbumListener listener;
    //Composant de l'interface visuelle
    private TextView nameTxt, periodeTxt, locationTxt;
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    ImageView coverImg;
    //Album montre
    private Album alb;

    public AlbumDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        //Je enregistre tous le variable au debut
        v = inflater.inflate(R.layout.fragment_album_detail, container, false);
        nameTxt = (TextView) v.findViewById(R.id.nameTxt);
        periodeTxt = (TextView) v.findViewById(R.id.periodTxt);
        locationTxt = (TextView) v.findViewById(R.id.locationTxt);
        coverImg=(ImageView)v.findViewById(R.id.coverImg);
        gridView = (GridView) v.findViewById(R.id.gridView);

        //Quand je click sur une photo je doit ouvrir une autre activite
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Photo item = (Photo) parent.getItemAtPosition(position);

                listener.onPhotoClickListener(item, alb);
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //C'est mon activitè principal qui s'occupe de gerer l'interface
        this.listener = (OnAlbumListener) getActivity();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void RefreshPhoto(Album alm){

        gridAdapter = new GridViewAdapter(v.getContext(), R.layout.image_gallery, alm.getPhotos());
        gridView.setAdapter(gridAdapter);
    }


    public void AddPhoto(Photo photo){
        //on ajoute la photo à l'album et on dis au gridview que les chose à afficher sont change
        alb.getPhotos().add(photo);
        ((GridViewAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }
    //Je change la visualisation de l'album
    public void AlbumSelected(Album alm) {

        if (alm!=null) {
            alb = alm;
            //Je peut avoir trois type d'image à montre:
            //une image qui vien du database, alors on l'a deja enregistre sur la tablet et on va la chercher grace a getPath
            //une image que on vien de charger sur l'appli, on a donc l'image bitmap
            //l'image manque et on montre donc une image de default
            if (alm.getCover()!=null && alm.getCover().getPath()!=null){
                //Bitmap options pour ne pas utiliser trop de memorie avec l'affichage des bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDither = false;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inSampleSize = 3;
                options.inPurgeable = true;
                Bitmap bitmap = BitmapFactory.decodeFile(alm.getCover().getPath(),options);
                coverImg.setImageBitmap(bitmap);
            }
            else if (alm.getCover().getImg() != null)
                coverImg.setImageBitmap(alb.getCover().getImg());
            else
                coverImg.setImageResource(R.mipmap.ic_launcher);
            //je montre le nom de l'album
            nameTxt.setText(alm.getName());
            //si la period est indique on l'affiche si non on me la date de chargement
            if (alm.getPeriod().getFirst()!=null && alm.getPeriod().getSecond()!=null)
                periodeTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(alm.getPeriod().getFirst()) + " - "
                    + new SimpleDateFormat("dd/MM/yyyy").format(alm.getPeriod().getSecond()));
            else
                periodeTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " - "
                        + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            //on affiche la position
            if (alm.getLocation()!=null)
                locationTxt.setText(alm.getLocation().toString());
            //on affiche la liste de photo de l'album grace à un gridViewAdapter
            if (alm.getPhotos() != null && alm.getPhotos().size() != 0) {
                gridAdapter = new GridViewAdapter(v.getContext(), R.layout.image_gallery, alb.getPhotos());
                gridView.setAdapter(gridAdapter);
            }
            else{
                gridAdapter = new GridViewAdapter(v.getContext(), R.layout.image_gallery, new ArrayList<Photo>());
                gridView.setAdapter(gridAdapter);
            }
        }
    }

    //Interface qui gere l'interface graphic
    public interface OnAlbumListener {
        void onPhotoClickListener(Photo ph, Album al);
    }

}

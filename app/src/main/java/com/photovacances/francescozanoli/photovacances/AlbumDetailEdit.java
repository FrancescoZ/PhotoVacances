package com.photovacances.francescozanoli.photovacances;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//Fragment full-frame qui permet d'effacer de photo, de change la photo de couverture et de change le nome et la periode de l'album
public class AlbumDetailEdit extends Fragment {

    //Variable deja explique dans album detail
    private OnAlbumDetailEditListener mlistener;
    View v;
    EditText nameTxt;
    EditText periodeStartTxt,periodeEndTxt;
    EditText locationTxt;
    GridView gridView;
    GridViewAdapter gridAdapter;
    Album alb;

    public AlbumDetailEdit() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment

        v = inflater.inflate(R.layout.fragment_album_detail_edit, container, false);
        nameTxt = (EditText) v.findViewById(R.id.editText);
        periodeStartTxt = (EditText) v.findViewById(R.id.dateStart);
        periodeEndTxt = (EditText) v.findViewById(R.id.dateEnd);
        locationTxt = (EditText) v.findViewById(R.id.locationEditTxt);

        gridView = (GridView) v.findViewById(R.id.gridView);

        //Quand je click sur une photo je l'efface
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Photo item = (Photo) parent.getItemAtPosition(position);

                mlistener.onPhotoEditClickListener(item);
            }
        });
        return v;
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Meme concept que AlbumDetail
        this.mlistener = (OnAlbumDetailEditListener) getActivity();
        nameTxt.setText(alb.getName());
        if (alb.getPeriod().getFirst()!=null)
            periodeStartTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(alb.getPeriod().getFirst()));
        else
            periodeStartTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        if (alb.getPeriod().getSecond()!=null)
            periodeEndTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(alb.getPeriod().getSecond()));
        else
            periodeEndTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        if (alb.getLocation()!=null)
            locationTxt.setText(alb.getLocation().toString());
        if (alb.getCover()!=null && alb.getCover().getPath()!=null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 3;
            options.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(alb.getCover().getPath(),options);
            ((ImageView) v.findViewById(R.id.coverEditImg)).setImageBitmap(bitmap);
        } else if (alb.getCover().getImg() != null)
            ((ImageView) v.findViewById(R.id.coverEditImg)).setImageBitmap(alb.getCover().getImg());
        else
            ((ImageView) v.findViewById(R.id.coverEditImg)).setImageResource(R.mipmap.ic_launcher);


        gridAdapter = new GridViewAdapter(v.getContext(), R.layout.image_gallery_edit, alb.getPhotos());
        gridView.setAdapter(gridAdapter);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mlistener = null;
    }

    //On l'appele quand on click sur une photo
    public void ErasedPhoto(Photo photo){
        alb.getPhotos().remove(alb.getPhotos().indexOf(photo));
        ((GridViewAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }

    //Cherche la photo dans la list et donne sa position, on a implemente equals donc on peut faire comme ça
    public int getPosition(Photo photo){
        return alb.getPhotos().indexOf(photo);
    }

    //Non plus utilise
    public void AddPhoto(Photo photo){
        alb.getPhotos().add(photo);
        ((GridViewAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }

    //ALbumEdit clicked
    public void onShowAlbum(Album alm){
        alb = alm;
    }

    public interface OnAlbumDetailEditListener {

        void onPhotoEditClickListener(Photo ph);
    }

}

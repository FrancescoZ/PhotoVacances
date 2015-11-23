package com.photovacances.francescozanoli.photovacances;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.photovacances.francescozanoli.photovacances.Domain.Photo;

//Fragment principal que montre full screen la photo chosi
public class ImageFragment extends Fragment {

    TextView titleTextView;
    ImageView imageView;
    private OnFragmentInteractionListener mListener;
    private View v;
    private Photo photo;


    public ImageFragment() {
        // Required empty public constructor
    }

    public static ImageFragment newInstance() {
        ImageFragment fragment = new ImageFragment();
        return fragment;
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
        v = inflater.inflate(R.layout.fragment_image, container, false);
        titleTextView = (TextView) v.findViewById(R.id.title);
        imageView = (ImageView) v.findViewById(R.id.image);
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Selectionne la photo chosi
    public void ImageSelected(Photo ph) {

        photo = ph;
        titleTextView.setText(ph.getName());
        if (ph.getPath()!=null) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(ph.getPath(),bmOptions);
            imageView.setImageBitmap(bitmap);
        }else if (photo.getImg()!=null)
            imageView.setImageBitmap(photo.getImg());
        else
            imageView.setImageResource(R.mipmap.ic_launcher);
    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

}

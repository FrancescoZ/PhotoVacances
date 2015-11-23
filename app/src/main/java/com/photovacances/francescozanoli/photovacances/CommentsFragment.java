package com.photovacances.francescozanoli.photovacances;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.photovacances.francescozanoli.photovacances.Domain.Comment;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

//Le fragment corrisponde à la partie PhotoDetailDetail, il montre les details d'un photo à partir du Caruosel
public class CommentsFragment extends Fragment {

    //Liste variables fragment (Voir AlbumDetail)
    private View v;
    private List<Comment> comments;
    private String name;
    private TextView nameTxt;
    private Bitmap image;
    private ImageView imageView;
    private ListView listView;

    private OnCommentFragmentListerner mListener;
    private String path=null;

    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Interface Setup
        this.mListener = (OnCommentFragmentListerner) getActivity();
        nameTxt.setText(name);
        if (path!=null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 3;
            options.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(path,options);
            imageView.setImageBitmap(bitmap);
        }else if (image!=null)
            imageView.setImageBitmap(image);
        else
            imageView.setImageResource(R.mipmap.ic_launcher);
        listView.setAdapter(new ListCommentAdapter(v.getContext(),R.layout.item_comments,comments));
    }


    public void onShowComment(Photo photo){
        //Je indique la photo seletione
        comments=photo.getComments();
        this.name=photo.getName();
        this.image=photo.getImg();
        this.path=photo.getPath();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_comments, container, false);
        nameTxt=(TextView)v.findViewById(R.id.nameTxt);
        imageView=(ImageView)v.findViewById(R.id.prvImg);
        listView=(ListView)v.findViewById(R.id.comments);

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnCommentFragmentListerner {

    }

    //Je peux commenter
    public void AddComment(Comment cmm){
        comments.add(cmm);
        //Je comunique a mon adpter que quelque chose a change
        ((ArrayAdapter<Comment>)listView.getAdapter()).notifyDataSetChanged();
    }

}

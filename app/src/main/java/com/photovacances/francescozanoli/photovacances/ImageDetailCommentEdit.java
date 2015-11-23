package com.photovacances.francescozanoli.photovacances;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Comment;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;

import java.text.SimpleDateFormat;
import java.util.List;

//il permet de change le donne de la photo et d'effacer les commentaires
//le functionnement est egale à tous les autres fragment donc le commentaire sont moins
public class ImageDetailCommentEdit extends Fragment {

    //Comme AlbumDetail
    private View v;
    private List<Comment> comments;
    private String name,period;
    private Bitmap image;
    private String path=null;
    private TextView nameTxt;
    private ImageView imageView;
    private ListView listView;

    private OnCommentEditFragmentListerner mListener;

    public ImageDetailCommentEdit() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mListener = (OnCommentEditFragmentListerner) getActivity();
        nameTxt.setText(name);
        listView.setAdapter(new ListCommentAdapter(v.getContext(), R.layout.item_comments_edit, comments));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                mListener.onListCommentClick((Comment) listView.getItemAtPosition(position));

            }
        });
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
    }


    public void onShowComment(Photo photo){
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
        v= inflater.inflate(R.layout.fragment_image_detail_comment_edit, container, false);
        nameTxt=(TextView)v.findViewById(R.id.nameTxt);
        listView=(ListView)v.findViewById(R.id.comments);
        imageView=(ImageView)v.findViewById(R.id.prvImg);
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnCommentEditFragmentListerner {
        public void onListCommentClick(Comment comment);
    }

    public void RemoveComment(Comment cmm){
        comments.remove(cmm);
        ((ArrayAdapter<Comment>)listView.getAdapter()).notifyDataSetChanged();
    }
}

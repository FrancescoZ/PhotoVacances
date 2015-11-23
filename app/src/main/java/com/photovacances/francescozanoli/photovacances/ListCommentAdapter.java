package com.photovacances.francescozanoli.photovacances;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by francescozanoli on 08/10/15.
 */
//List de commentaire, marche comme la listFragment pour le commentaire
public class ListCommentAdapter extends ArrayAdapter<Comment> {

    private List<Comment> commentList,orCommentList;
    private Context context;
    private Filter filter;

    public ListCommentAdapter(Context context, int resource, List<Comment> comments) {
        super(context, resource, comments);
        this.commentList = comments;
        this.context = context;
        orCommentList=commentList;
    }

    public int getCount() {
        if (commentList!=null)
            return commentList.size();
        return 0;
    }

    public Comment getItem(int position) {
        return commentList.get(position);
    }

    public long getItemId(int position) {
        return commentList.get(position).hashCode();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        CommentHolder holder = new CommentHolder();

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_comments, null);
            // Now we can fill the layout with the right values
            TextView name = (TextView) v.findViewById(R.id.nameCommentTxt);
            ImageView img = (ImageView) v.findViewById(R.id.photUserComment);

            holder.commentTxt = name;
            holder.imgeView = img;

            v.setTag(holder);
        } else
            holder = (CommentHolder) v.getTag();

        Comment abl = commentList.get(position);
        holder.commentTxt.setText(abl.getComment().toString());
        holder.imgeView.setImageResource(R.drawable.user);
        return v;
    }

    public void resetData() {
        commentList = orCommentList;
    }

    private static class CommentHolder {
        public TextView commentTxt;
        public ImageView imgeView;
    }

}
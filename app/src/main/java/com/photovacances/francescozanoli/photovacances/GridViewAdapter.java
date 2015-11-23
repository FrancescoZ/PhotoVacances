package com.photovacances.francescozanoli.photovacances;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.photovacances.francescozanoli.photovacances.Domain.Photo;

import java.util.ArrayList;
import java.util.List;
//List de photo dans ALbumDetail et AlbumDetailEdit
public class GridViewAdapter extends ArrayAdapter<Photo> {

    private Context context;
    private int layoutResourceId;
    //Liste de photo à afficher
    private List<Photo> data;

    public GridViewAdapter(Context context, int layoutResourceId, List<Photo> data) {
        super(context, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    //appeler chaque fois qu'il faut affiche la list de photo
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        Photo item = data.get(position);
        holder.imageTitle.setText(item.getName());
        //Meme type d'afficage que dans la cover de AlbumDetail
        if (item.getPath()!=null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 3;
            options.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(item.getPath(),options);

            holder.image.setImageBitmap(bitmap);
        }
        else if (item.getImg()!=null)
            holder.image.setImageBitmap(item.getImg());
        else
            holder.image.setImageResource(R.mipmap.ic_launcher);
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
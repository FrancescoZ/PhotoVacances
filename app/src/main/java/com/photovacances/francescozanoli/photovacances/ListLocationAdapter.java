package com.photovacances.francescozanoli.photovacances;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.photovacances.francescozanoli.photovacances.Domain.Album;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by francescozanoli on 24/09/15.
 */
//Le functionnement de cette classe est le meme que ListNameAdpter et ListPeriodAdapter, il change seulement l'affichage dans la methode getiView
//donc on expliquera que une fois dans ListNameAdapter
public class ListLocationAdapter extends ArrayAdapter<Album> implements Filterable {

    private List<Album> albumList;
    private Context context;
    private Filter filter;
    private List<Album> origAlbumList;

    public ListLocationAdapter(Context context, int resource, List<Album> albums) {
        super(context, resource, albums);
        this.albumList = albums;
        this.context = context;
        this.origAlbumList = albumList;
    }

    public int getCount() {
        return albumList.size();
    }

    public Album getItem(int position) {
        return albumList.get(position);
    }

    public long getItemId(int position) {
        return albumList.get(position).hashCode();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        AlbumHolder holder = new AlbumHolder();

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_list, null);
            // Now we can fill the layout with the right values
            TextView name = (TextView) v.findViewById(R.id.albumTxt);
            TextView periodtx = (TextView) v.findViewById(R.id.periodTxt);
            ImageView img = (ImageView) v.findViewById(R.id.photoImg);

            holder.albumTxt = name;
            holder.periodTxt = periodtx;
            holder.imgeView = img;

            v.setTag(holder);
        } else
            holder = (AlbumHolder) v.getTag();

        Album abl = albumList.get(position);
        holder.albumTxt.setText(abl.getLocation().toString());
        if (abl.getPeriod().getFirst()!=null && abl.getPeriod().getSecond()!=null)
            holder.periodTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(abl.getPeriod().getFirst()) + " - "
                    + new SimpleDateFormat("dd/MM/yyyy").format(abl.getPeriod().getSecond()));
        else
            holder.periodTxt.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " - "
                    + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        if (abl.getCover()!=null && abl.getCover().getPath()!=null) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(abl.getCover().getPath(),bmOptions);
            holder.imgeView.setImageBitmap(bitmap);
        }
        else if (abl.getCover()!=null && abl.getCover().getImg()!=null)
            holder.imgeView.setImageBitmap(abl.getCover().getImg());
        else
            holder.imgeView.setImageResource(R.mipmap.ic_launcher);
        return v;
    }

    public void resetData() {
        albumList = origAlbumList;
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new AlbumFilter();
        return filter;
    }

    private static class AlbumHolder {
        public TextView albumTxt;
        public TextView periodTxt;
        public ImageView imgeView;
    }

    private class AlbumFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = origAlbumList;
                results.count = origAlbumList.size();
            } else {
                // We perform filtering operation
                List<Album> nAlbumList = new ArrayList<Album>();

                for (Album p : albumList) {
                    if (p.getLocation().getName().toUpperCase().contains(constraint.toString().toUpperCase()))
                        nAlbumList.add(p);
                }

                results.values = nAlbumList;
                results.count = nAlbumList.size();

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                albumList = (List<Album>) results.values;
                notifyDataSetChanged();
            }

        }

    }
}
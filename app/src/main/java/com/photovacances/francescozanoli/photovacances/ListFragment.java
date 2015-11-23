package com.photovacances.francescozanoli.photovacances;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.photovacances.francescozanoli.photovacances.Common.Pair;
import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Comment;
import com.photovacances.francescozanoli.photovacances.Domain.Location;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//List des Albums dans l'activite principal
public class ListFragment extends Fragment implements SearchView.OnQueryTextListener {

    //listview
    private ListView lst;
    //textbox pour trie
    private SearchView scView;
    private View v;
    //album
    private List<Album> l;
    //array qui est gerre par la listview
    private ArrayAdapter<Album> arrAd;
    private OnListInteractionListener listener;

    public List<Album> getList(){return l;}

    public ListFragment() {
        // Required empty public constructor
    }

    //indique la liste a montre
    public void setList(List<Album> l) {
        this.l = l;
        arrAd = new ListNameAdapter(v.getContext(), R.layout.item_list, l);
        lst.setAdapter(arrAd);
        lst.setTextFilterEnabled(true);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                listener.onListClick((Album) lst.getItemAtPosition(position));

            }
        });
        setupSearchView();
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

        v = inflater.inflate(R.layout.fragment_list, container, false);
        lst = (ListView) v.findViewById(R.id.listAlbum);
        scView = ((SearchView) v.findViewById(R.id.searchView));


        return v;
    }

    public void Refresh(List<Album> albs){
        l=albs;
    }

    //changer un album apres l'avoir edité
    public void ChangeAlbum(Album newAbl){
        l.set(l.indexOf(newAbl),newAbl);
        ((ArrayAdapter<Album>)lst.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setList(l);
        this.listener = (OnListInteractionListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setupSearchView() {
        //On cherche le filtre et on lie la listview avec le searchBox
        scView.setIconifiedByDefault(false);
        scView.setOnQueryTextListener(this);
        scView.setSubmitButtonEnabled(false);
        scView.setQueryHint("Cherche ici");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    //on change le filtre quand change la valeur de le searchBox
    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText))
            lst.clearTextFilter();
        else
            lst.setFilterText(newText.toString());
        return true;
    }

    //Trie par Location,Name,Period
    public void filterClick(String view) {
        if (view.compareTo("Nom") == 0)
            lst.setAdapter(new ListNameAdapter(v.getContext(), R.layout.item_list, l));
        else if (view.compareTo("Periode") == 0)
            lst.setAdapter(new ListPeriodAdapter(v.getContext(), R.layout.item_list, l));
        else if (view.compareTo("Lieu") == 0)
            lst.setAdapter(new ListLocationAdapter(v.getContext(), R.layout.item_list, l));
        lst.clearTextFilter();
        lst.setTextFilterEnabled(true);
    }

    public interface OnListInteractionListener {

        void onListClick(Album alb);
    }

}

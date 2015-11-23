package com.photovacances.francescozanoli.photovacances;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.photovacances.francescozanoli.photovacances.Common.Pair;
import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;
import com.photovacances.francescozanoli.photovacances.StaticHelper.ActivityActionHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//Activitè principal avec la liste des Album
public class HomeActivity extends AppCompatActivity implements ListFragment.OnListInteractionListener, SearchView.OnQueryTextListener, AlbumDetail.OnAlbumListener,AlbumDetailEdit.OnAlbumDetailEditListener {

    //list des album
    List<Album> l;
    //fragment pour selectioner l'album à voir
    ListFragment mn;
    //stock du fragment appele avant
    Fragment oldFrgm;

    //album selectionne
    Album selectedAlbum;
    //list de photo ajouté sur l'album selectionnée
    List<Photo> addedPhoto;
    //list des photo efface dans AlbumDetailEdit
    List<Pair<Integer, Photo>> erasedPhoto;

    //intent à envoie au service
    Intent mServiceIntent;
    //Objet qui reponde à le demande du service
    private DataUpdateReceiver dataUpdateReceiver;
    //Binding avec le service
    ServiceConnection conService;
    //Service qui gere le database
    //on lance le service et on lui demande de telecharger le donnes, une fois qui cela c'est fait à partir du service on lance
    //une reponse qui est prise en charge per le DataUpdateReceiver et qui est geré dans la methode Received
    static Manager mService=null;
    //il indique si on a fait le binding avec le service
    boolean mBound;
    //il indique si j'ai change la photo de couverture dans AlbumDetailEdit
    boolean coverChanged=false;
    //Vieux photo change dans AlbumDetailEdit
    Photo oldCov;

    //Message pour attendre la connection au database
    ProgressDialog Dialog;

    public Manager getService(){return mService;}

    //string pour comprendre quelle methode a appelee action_result
    public static final int REQUEST_IMAGE_CAPTURE_EDIT = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 0;
    public static final int EDIT_GALLERY = 2;
    public static final int GET_FROM_GALLERY = 3;
    public static final int GET_FROM_GALLERY_EDIT = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dialog= new ProgressDialog(HomeActivity.this);
        erasedPhoto = new LinkedList<>();
        addedPhoto = new LinkedList<>();
        setContentView(R.layout.activity_home);
        //On lance le service
        mn = (ListFragment) getFragmentManager().findFragmentById(R.id.listFrgm);
        mServiceIntent = new Intent(this, Manager.class);
        mServiceIntent.setData(Uri.parse(ActivityActionHelper.DOWNLOAD));
        mServiceIntent.putExtra(ActivityActionHelper.RECEIVER, new ReceveirData());
        startService(mServiceIntent);
        //On fait le binding entree service e activity
        conService= new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                Manager.LocalBinder binder = (Manager.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };
        boolean b=bindService(mServiceIntent, conService, 0);
        StartLoad();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(ActivityActionHelper.REFRESH);
        registerReceiver(dataUpdateReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    //Inutile
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    //Inutile
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void filterClick(View view) {
        //Research dans la listview
        String txtBtn = ((Button) view).getText().toString();
        mn.filterClick(txtBtn);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return mn.onQueryTextSubmit(query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return mn.onQueryTextChange(newText);
    }

    //on selection l'album dans la liste de gauche
    public void onListClick(Album click) {
        AlbumDetail abl = (AlbumDetail) getFragmentManager().findFragmentById(R.id.detail);
        selectedAlbum = click;
        abl.AlbumSelected(selectedAlbum);
    }

    //Click sur la photo dans AlbumDetail
    @Override
    public void onPhotoClickListener(Photo photo, Album album) {
        //On ouvre l'activite pour montre la photo chosie
        //Create intent
        Intent intent = new Intent(this, PhotoDetailActivity.class);
        //Start details activity
        Bundle bundle = new Bundle();
        bundle.putString("idPhoto", photo.getId());
        bundle.putString("idAlbum",album.getId());
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_GALLERY);

    }

    //Click sur la photo dans AlbumDetailEdit
    @Override
    public void onPhotoEditClickListener(Photo photo) {
        erasedPhoto.add(
                new Pair(((AlbumDetailEdit) getFragmentManager().findFragmentById(R.id.detail)).getPosition(photo),
                        photo));
        ((AlbumDetailEdit) getFragmentManager().findFragmentById(R.id.detail)).ErasedPhoto(photo);
    }

    //On click sur le bouton edit dans AlbumDetail et on ouvre AlbumDetailEdit
    public void editClick(View view) {
        if (selectedAlbum == null)
            return;
        coverChanged=false;
        // Create new fragment and transaction
        oldFrgm = getFragmentManager().findFragmentById(R.id.detail);
        Fragment albFragment = new AlbumDetailEdit();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(getFragmentManager().findFragmentById(R.id.listFrgm));

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.detail, albFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        ((AlbumDetailEdit) albFragment).onShowAlbum(selectedAlbum);
    }

    //charge une photo de la gallery de AlbumDetailEdit
    public void loadClick(View view) {
        if (selectedAlbum == null)
            return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_FROM_GALLERY_EDIT);
    }

    //Charge une photo de la gallery de AlbumDetail
    public void newloadClick(View view) {
        if (selectedAlbum == null)
            return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_FROM_GALLERY);
    }

    //Prendre un photo de la camera de AlbumDetail
    public void photoClick(View view) {
        if (selectedAlbum == null)
            return;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            //si la reponse est positive de l'appli que on viens d'utiliser
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    //Album cover
                    case GET_FROM_GALLERY_EDIT:
                        Uri imageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        ((ImageView) findViewById(R.id.coverEditImg)).setImageBitmap(bitmap);
                        oldCov=selectedAlbum.getCover();
                        oldCov.setCover(false);
                        Photo cov = new Photo();
                        cov.setImg(bitmap);
                        selectedAlbum.setCover(cov);
                        cov.setCover(true);
                        coverChanged=true;
                        break;
                    //New image from gallery
                    case GET_FROM_GALLERY:
                        Uri imageUr = data.getData();
                        Bitmap bitma = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUr);
                        Photo N = new Photo( "New take Gallery", "Great");
                        N.setImg(bitma);
                        mService.addPhoto(N, selectedAlbum.getId());
                        StartLoad();
                        break;
                    //Carousel
                    case EDIT_GALLERY:
                        if (mService==null){
                            mServiceIntent = new Intent(this, Manager.class);
                            mServiceIntent.setData(Uri.parse(ActivityActionHelper.DOWNLOAD));
                            mServiceIntent.putExtra(ActivityActionHelper.RECEIVER, new ReceveirData());
                            startService(mServiceIntent);
                        }else{
                            mService.setReveiver(new ReceveirData());
                            mService.refresh();
                        }
                        onListClick(selectedAlbum);
                        break;
                    //Not used
                    case REQUEST_IMAGE_CAPTURE_EDIT:
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        Photo tak = new Photo("New take", "Great");
                        tak.setImg(imageBitmap);
                        ((AlbumDetailEdit) getFragmentManager().findFragmentById(R.id.detail)).AddPhoto(tak);
                        addedPhoto.add(tak);
                        break;
                    //new image from camera
                    case REQUEST_IMAGE_CAPTURE:
                        Bundle extra = data.getExtras();
                        Bitmap imageBitma = (Bitmap) extra.get("data");
                        Photo take = new Photo("New take", "Great");
                        take.setImg(imageBitma);
                        ((AlbumDetail) getFragmentManager().findFragmentById(R.id.detail)).AddPhoto(take);
                        mService.addPhoto(take, selectedAlbum.getId());
                        StartLoad();
                        break;
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Cancelled
            }
        } catch (Exception e) {
            int a = 0;
            //Exception
        }
    }

    //Affiche message box de attent
    private void StartLoad() {
        Dialog.setMessage("Connecting to the Db");
        Dialog.show();
    }

    //ferme message box d'attent
    private void StopLoad() {
        Dialog.dismiss();
    }


    //button X or Enregister
    public void escClick(View view) {
        //si le boutton est SaveImg on enregistre tous le changement
        if (view instanceof ImageButton && (view).getId() == R.id.SaveImg) {
            //Avec AlbumEditDetail on peut aussi ajouter un nouveau album donc on regarde si l'id est null ou pas pour comprendre si
            //on doit le sauver ou ajouter au db
            if (selectedAlbum.getId()==null){
                selectedAlbum.setName(((TextView) findViewById(R.id.editText)).getText().toString());
                selectedAlbum.setLocation(((TextView) findViewById(R.id.locationEditTxt)).getText().toString());
                if (selectedAlbum.getPeriod().getFirst()!=null && selectedAlbum.getPeriod().getSecond() != null) {
                    selectedAlbum.getPeriod().setFirst(new Date(((TextView) findViewById(R.id.dateStart)).getText().toString()));
                    selectedAlbum.getPeriod().setSecond(new Date(((TextView) findViewById(R.id.dateEnd)).getText().toString()));
                }else {
                    selectedAlbum.getPeriod().setFirst(new Date());
                    selectedAlbum.getPeriod().setSecond(new Date());
                }
                //((ListFragment) getFragmentManager().findFragmentById(R.id.listFrgm)).ChangeAlbum(selectedAlbum);
                //((AlbumDetail) oldFrgm).AlbumSelected(selectedAlbum);
                mService.addAlbum(selectedAlbum);
                StartLoad();
            }else{
                selectedAlbum.setName(((TextView) findViewById(R.id.editText)).getText().toString());
                selectedAlbum.setLocation(((TextView) findViewById(R.id.locationEditTxt)).getText().toString());
                if (selectedAlbum.getPeriod().getFirst()!=null && selectedAlbum.getPeriod().getSecond()!=null) {
                    selectedAlbum.getPeriod().setFirst(new Date(((TextView) findViewById(R.id.dateStart)).getText().toString()));
                    selectedAlbum.getPeriod().setSecond(new Date(((TextView) findViewById(R.id.dateEnd)).getText().toString()));
                }
                ((ListFragment) getFragmentManager().findFragmentById(R.id.listFrgm)).ChangeAlbum(selectedAlbum);
                //((AlbumDetail) oldFrgm).AlbumSelected(selectedAlbum);
                //si on a efface de photo on doit l'effacer de la base de donne
                for (Pair<Integer,Photo> erased:erasedPhoto
                        ) {
                    mService.deletePhoto(erased.getSecond(),selectedAlbum.getId());
                }
                if (coverChanged) {
                    //si il y a de changement dans la cover on le change aussi sur le db
                    mService.editCover(selectedAlbum.getCover(), selectedAlbum.getId(),oldCov);
                }
                //on enregistre le changement
                mService.editAlbum(selectedAlbum);
                StartLoad();
            }
        } else {
            //si je ne doit pas enregister est suffisant de faire une refresh de donne de la base de donne pour effacer le changement local
           mService.refresh();
        }
        //On retourn sur a AlbumDetail
        erasedPhoto = new LinkedList<>();
        addedPhoto = new LinkedList<>();
        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.show(getFragmentManager().findFragmentById(R.id.listFrgm));
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.detail, oldFrgm);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        oldFrgm = null;
        StartLoad();

    }

    //refresh force par l'utilisatior
    public void reloadClick(View view) {
        StartLoad();
        mService.download();
    }

    //il appele l'AlbumDetailEdit avec un album vide
    public void addAlbum(View view) {
        // Create new fragment and transaction
        oldFrgm = getFragmentManager().findFragmentById(R.id.detail);
        Fragment albFragment = new AlbumDetailEdit();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(getFragmentManager().findFragmentById(R.id.listFrgm));

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.detail, albFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        selectedAlbum=new Album();
        ((AlbumDetailEdit) albFragment).onShowAlbum(selectedAlbum);

    }

    //Inutile pour le moment
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ActivityActionHelper.REFRESH)) {
                // Do stuff - maybe update my view based on the changed DB contents
                l = (List<Album>) intent.getExtras().getSerializable("dataAlbum");
                mn.Refresh(l);
                StopLoad();
            }
        }
    }

    //il attende l'appele du service pour metre a jour le donne
    private class ReceveirData extends ResultReceiver {
            public ReceveirData() {
                super(new Handler());
            }
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                //d'abord je regarde si je doit faire une refresh
                if (resultData.getBoolean("isRefreshNeeded")){
                    //Data update
                    List<Album> items = (List<Album>) resultData.getSerializable("dataAlbums");
                    if (items != null) {
                        l=items;
                        mn.setList(l);
                    }
                }
                String dataString=resultData.getString("TypeIntent");
                switch (dataString){
                    case ActivityActionHelper.ADD_PHOTO:
                        Photo photo = (Photo) resultData.getSerializable("dataPhoto");
                        ((AlbumDetail) getFragmentManager().findFragmentById(R.id.detail)).AddPhoto(photo);
                        break;
                    //si il y a un errour on affiche un message
                    case ActivityActionHelper.SHOW_MESSAGE:
                        StopLoad();
                        Context context = getApplicationContext();
                        CharSequence text = resultData.getString(ActivityActionHelper.SHOW_MESSAGE);
                        int duration = Toast.LENGTH_LONG;
                        //moyenne pour envoie un message en andriod
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                        break;
                }
                StopLoad();

            }
        }
}

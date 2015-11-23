package com.photovacances.francescozanoli.photovacances;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.photovacances.francescozanoli.photovacances.Common.Pair;
import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Comment;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;
import com.photovacances.francescozanoli.photovacances.StaticHelper.ActivityActionHelper;

import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;


public class PhotoDetailActivity extends AppCompatActivity implements CommentsFragment.OnCommentFragmentListerner, ImageDetailCommentEdit.OnCommentEditFragmentListerner{

    //List pour le carousel
    List<Photo> phs;
    //photo a montrer
    Photo pht;
    //id album des photos et photo montrée
    String idPhoto,idAlbum;
    //different fragment a montrer
    Fragment commentFr,commentEditFr,imageFr,imageEditFr;
    List<Pair<Integer,Comment>> erasedComment;

    //meme variables que dans la class HomeActivity et meme fonctionnement
    private DataUpdateReceiver dataUpdateReceiver;
    ServiceConnection conService;
    Manager mService;
    boolean mBound;
    Intent mServiceIntent;

    ProgressDialog Dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        erasedComment=new ArrayList<>();
        Dialog= new ProgressDialog(PhotoDetailActivity.this);
        Bundle bundle = getIntent().getExtras();
        idPhoto = bundle.getString("idPhoto");
        idAlbum = bundle.getString("idAlbum");
        ImageFragment abl = (ImageFragment) getFragmentManager().findFragmentById(R.id.imageFgm);
        imageFr=abl;
        StartLoad();
        //on prende le service de l'activity que on viens de ferme, si il n'existe pas on le cree à nouveau
        mService=HomeActivity.mService;
        if (mService==null){
            mServiceIntent = new Intent(this, Manager.class);
            mServiceIntent.setData(Uri.parse(ActivityActionHelper.DOWNLOAD_PHOTO));
            mServiceIntent.putExtra("idAlbum",idAlbum);
            mServiceIntent.putExtra(ActivityActionHelper.RECEIVER, new ReceveirData());
            startService(mServiceIntent);
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
            bindService(mServiceIntent, conService, 0);
        }else{
            mService.setReveiver(new ReceveirData());
            mService.refreshPhoto(idAlbum);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_detail, menu);
        return true;
    }

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

    public void setImageToView(Photo photo){
        ((ImageFragment) imageFr).ImageSelected(photo);
    }

    //Carousel Right
    public void rightClick(View view) {
        if (view.getId()==R.id.rightArrowComment)
            getImageFragment(view);
        int pos = phs.indexOf(pht);
        if (pos > 0)
            pos--;
        if (pos!=-1){
            pht = phs.get(pos);
            idPhoto=pht.getId();
            setImageToView(pht);
        }

    }

    //Carousel left
    public void leftClick(View view) {
        if (view.getId()==R.id.leftArrowComment)
            getImageFragment(view);
        int pos = phs.indexOf(pht);
        if (pos < phs.size() - 1)
            pos++;
        if (pos!=-1){
            pht = phs.get(pos);
            idPhoto=pht.getId();
            setImageToView(pht);
        }
    }

    //open the fragment pour montre les commentaires
    public void showComment(View view) {
        // Create new fragment and transaction
        Fragment commentsFrgm = new CommentsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.imageFgm, commentsFrgm);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        ((CommentsFragment)commentsFrgm).onShowComment(pht);
        commentFr=commentsFrgm;

    }

    //on ajoute une commentaire
    public void addComment(View view) {
        ((CommentsFragment) commentFr).AddComment(
                new Comment(
                        ((TextView) findViewById(R.id.newComment)).getText().toString()));
        mService.addComment(idPhoto, idAlbum, ((TextView) findViewById(R.id.newComment)).getText().toString());
        ((TextView)findViewById(R.id.newComment)).setText("");
    }

    //on return à la photodetail principal
    public void getImageFragment(View view){

        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.imageFgm, imageFr);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

    }

    //on return à la liste d'album
    public void getBack(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        setResult(RESULT_OK, intent);
        this.finish();
    }

    //On click sur un commentaire dans IMagedetailCommentEdit donc on le remove
    @Override
    public void onListCommentClick(Comment comment) {
        ((ImageDetailCommentEdit) getFragmentManager().findFragmentById(R.id.imageFgm)).RemoveComment(comment);
    }

    //je demande une refresh
    public void escEditClick(View view) {
        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.imageFgm, imageFr);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();


    }

    //je enregistre le difference entre le deux photo
    public void saveClick(View view) {
        if (view instanceof ImageButton && (view).getId()==R.id.SaveImg){
            mService.setPhotoName(idPhoto, idAlbum, ((TextView) findViewById(R.id.nameTxt)).getText().toString());
            phs.set(phs.indexOf(pht),pht);
            ((ImageFragment) imageFr).ImageSelected(pht);
            for (Pair<Integer,Comment> toErase:erasedComment)
                mService.removeComment(idPhoto, idAlbum, toErase.getSecond());
        }
        else{
            mService.refreshPhoto(idAlbum);
        }

        erasedComment=new ArrayList<>();

        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.imageFgm, imageFr);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

    }

    //on ouvre le fragment pour modifie le data de la photo
    public void editClick(View view) {
        if (pht==null)
            return;
        // Create new fragment and transaction
        Fragment commentsFrgm = new ImageDetailCommentEdit();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.imageFgm, commentsFrgm);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        commentEditFr=commentsFrgm;
        ((ImageDetailCommentEdit)commentsFrgm).onShowComment(pht);

    }

    //EDITOR
    public void onEditImageClik(View view) {
        if (pht == null)
            return;
        // Create new fragment and transaction
        Fragment imgEFra = new ImageDetailEdit();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.imageFgm, imgEFra);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        ((ImageDetailEdit) imgEFra).setUpImage(pht);
        imageEditFr = imgEFra;

    }

    //on enregistre la photo avec le dessin
    public void onSaveImageClick(View v){
        pht.setImg(((ImageDetailEdit)imageEditFr).getBitmap());

        erasedComment=new LinkedList<>();

        // Create new fragment and transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.imageFgm, imageFr);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        ((ImageFragment) imageFr).ImageSelected(pht);

    }

    //je change la couleur
    public void ChangeColorClick(View view) {
        if (view.getId()==R.id.paintBlue)
            ((ImageDetailEdit)imageEditFr).changePaint("BLUE");
        else if (view.getId()==R.id.paintRed)
            ((ImageDetailEdit)imageEditFr).changePaint("RED");
        else if (view.getId()==R.id.paintWhite)
            ((ImageDetailEdit)imageEditFr).changePaint("WHITE");
        else if (view.getId()==R.id.paintYellow)
            ((ImageDetailEdit)imageEditFr).changePaint("YELLOW");
    }

    //effacer de ligne
    public void eraseClick(View view) {
        ((ImageDetailEdit)imageEditFr).getBack();
    }


    //Inutile pour le moment
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ActivityActionHelper.REFRESH_PHOTO)) {
                int a=0;
            }
        }
    }

    //comme pour HomeActivity
    private class ReceveirData extends ResultReceiver {
        public ReceveirData() {
            super(new Handler());
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData.getBoolean("isRefreshNeeded")){
                List<Photo> items = (List<Photo>) resultData.getSerializable("dataAlbum");
                if (items != null) {
                    phs=items;
                    for (Photo toView:phs
                         ) {
                        if (toView.getId()!=null && toView.getId().equals(idPhoto)) {
                            pht = toView;
                            if ( getFragmentManager().findFragmentById(R.id.imageFgm) instanceof ImageFragment) {
                                ImageFragment abl = (ImageFragment) getFragmentManager().findFragmentById(R.id.imageFgm);
                                abl.ImageSelected(pht);
                            }
                        }
                    }
                }
            }

            String dataString=resultData.getString("TypeIntent");
            switch (dataString){
                //si il y a un errour on affiche un message
                case ActivityActionHelper.SHOW_MESSAGE:
                    StopLoad();
                    Context context = getApplicationContext();
                    CharSequence text = resultData.getString(ActivityActionHelper.SHOW_MESSAGE);
                    int duration = Toast.LENGTH_SHORT;
                    //moyenne pour envoie un message en andriod
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    break;
                default:
                    break;
            }
            StopLoad();

        }
    }

    private void StartLoad() {
        Dialog.setMessage("Connecting to the Db");
        Dialog.show();
    }

    private void StopLoad() {
        Dialog.dismiss();
    }

}

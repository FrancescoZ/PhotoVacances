package com.photovacances.francescozanoli.photovacances;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.photovacances.francescozanoli.photovacances.Common.Pair;
import com.photovacances.francescozanoli.photovacances.Domain.Album;
import com.photovacances.francescozanoli.photovacances.Domain.Comment;
import com.photovacances.francescozanoli.photovacances.Domain.Location;
import com.photovacances.francescozanoli.photovacances.Domain.Photo;
import com.photovacances.francescozanoli.photovacances.StaticHelper.ActivityActionHelper;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Manager extends IntentService {

    // Define the connection-string with your values
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=https;" +
                    "AccountName=photovacances;" +
                    "AccountKey=khRR4H0GMouIRVeUCfR5bVumJn+aktd5zkogCGmHVN2fqvbbkwsc/orYJ4q2XP4JGRKBUUx6mjfN9So9n775ig==";

    private static final int PHOTO=2;
    private static final int ALBUM=1;

    private Bitmap bit=null;
    private ResultReceiver receiver;

    List<Album> list;


    public Manager() {
        super("Manager");
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    //Methode appele de l'activity pour faire quelque chose
    public void refresh() {
        launchRefresh(ALBUM);
    }

    //album detail
    public void addPhoto(Photo take, String idAlbum) {
        uploadPhoto(take);
        for (Album look:list
             ) {
            if(look.getId()==idAlbum){
                addPhotoDatabase(take, idAlbum);
                //look.getPhotos().add(take);
                break;
            }
        }
        launchRefresh(ALBUM);
    }
    public void deletePhoto(Photo take, String idAlbum){
        if (take.getFileName()!=null)
            new DeleteFilesTask().execute(take.getFileName());
        deletePhotoDatabase(take, idAlbum);
    }

    //edit album
    public void editCover(Photo cov, String idAlbum,Photo oldPhoto) {
        editPhotoDatabase(oldPhoto,idAlbum);
        addPhoto(cov, idAlbum);
        for (Album look:list
                ) {
            if(look.getId().equals(idAlbum)){
                look.setCover(cov);
                editAlbumDatabase(look);
                break;
            }
        }
        launchRefresh(ALBUM);
    }
    public void editAlbum(Album edit){
        editAlbumDatabase(edit);
        launchRefresh(ALBUM);
    }

    //photo detail
    public void refreshPhoto(String idAlbum) {
        launchRefresh(PHOTO, idAlbum);
    }
    public void setPhotoName(String idPhoto,String idAlbum,String name) {
        for (Album albumSelected:list)
            if (albumSelected.getId().equals(idAlbum))
                for (Photo photoSelected:albumSelected.getPhotos())
                    if (photoSelected.getId().equals(idPhoto)) {
                        photoSelected.setName(name);
                        launchRefresh(PHOTO, idAlbum);
                        editPhotoDatabase(photoSelected, idAlbum);
                        return;
                    }
    }
    public void addComment(String idPhoto,String idAlbum, String comment) {
        for (Album albumSelected:list)
            if (albumSelected.getId().equals(idAlbum))
                for (Photo photoSelected:albumSelected.getPhotos())
                    if (photoSelected.getId().equals(idPhoto)) {
                        photoSelected.getComments().add(new Comment(comment));
                        addCommentDatabase(new Comment(comment), idPhoto);
                        launchRefresh(PHOTO, idAlbum);
                        return;
                    }
    }
    public void removeComment(String idPhoto, String idAlbum, Comment comment) {
        for (Album albumSelected:list)
        if (albumSelected.getId().equals(idAlbum))
            for (Photo photoSelected:albumSelected.getPhotos())
                if (photoSelected.getId().equals(idPhoto)) {
                    photoSelected.getComments().remove(comment);
                    deleteCommentDatabase(comment, idPhoto);
                    launchRefresh(PHOTO, idAlbum);
                    return;
                }
    }
    public void addAlbum(Album toAdd){
        addAlbumDatabase(toAdd);
        list.add(toAdd);
        launchRefresh(ALBUM);
    }


    //Impose a qui on doit envoyer les donnes
    public void setReveiver(ResultReceiver receveirData) {
        receiver=receveirData;
    }

    //download de photo sur la tablet
    public void download() {
        getData(false," ");
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public Manager getService() {
            // Return this instance of LocalService so clients can call public methods
            return Manager.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //il est appele par l'activity
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // Gets data from the incoming Intent
            String dataString = intent.getDataString();
            receiver=intent.getParcelableExtra(ActivityActionHelper.RECEIVER);
            // Do work here, based on the contents of dataString
            switch (dataString){
                //Refresh request from album's list
                case ActivityActionHelper.REFRESH:
                    launchRefresh(ALBUM);
                    break;
                //refresh request from carousel
                case ActivityActionHelper.REFRESH_PHOTO:
                    launchRefresh(PHOTO,intent.getStringExtra("idAlbum"));
                    break;
                //data request from album's list
                case ActivityActionHelper.DOWNLOAD:
                    getData(false, " ");
                    break;
                //data request from carousel
                case ActivityActionHelper.DOWNLOAD_PHOTO:
                    getData(true, intent.getStringExtra("idAlbum"));
                    break;
                //NOT used right now
                case ActivityActionHelper.ADD_ALBUM:
                    break;
                case ActivityActionHelper.DELETE_ALBUM:
                    break;
                case ActivityActionHelper.EDIT_ALBUM:
                    break;
                case ActivityActionHelper.ADD_PHOTO:
                    break;
                case ActivityActionHelper.DELETE_PHOTO:
                    break;
                case ActivityActionHelper.EDIT_PHOTO:
                    break;
                case ActivityActionHelper.ADD_COMMENT:
                    break;
                case ActivityActionHelper.DELETE_COMMENT:
                    break;
            }
        }
    }

    //REFRESH les album
    private void launchRefresh(int TypeDataSend){
        Bundle bundle = new Bundle();
        bundle.putString("TypeIntent",ActivityActionHelper.REFRESH);
        switch (TypeDataSend){
            case ALBUM:
                bundle.putBoolean("isRefreshNeeded", true);
                bundle.putParcelableArrayList("dataAlbums", (ArrayList<Album>) list);
                break;
            default:
                break;
        }
        receiver.send(0,bundle);
    }
    //Refresh les photos
    private void launchRefresh(int TypeDataSend,String idAlbum ){
        Bundle bundle = new Bundle();
        bundle.putString("TypeIntent",ActivityActionHelper.REFRESH);
        switch (TypeDataSend){
            case PHOTO:
                bundle.putBoolean("isRefreshNeeded",true);
                Album toSend=null;
                if (list.indexOf(new Album(idAlbum))!=-1)
                    toSend=list.get(list.indexOf(new Album(idAlbum)));
                if (toSend==null)
                    toSend=new Album("","",new Date(),new Date(),"","");
                bundle.putParcelableArrayList("dataAlbum", (ArrayList<Photo>) toSend.getPhotos());
                break;
            case ALBUM:
                bundle.putBoolean("isRefreshNeeded", true);
                bundle.putParcelableArrayList("dataAlbums", (ArrayList<Album>) list);
                break;
        }
        receiver.send(0, bundle);
    }

    //STORAGE
    public boolean uploadPhoto(Photo add){
        new UploadFilesTask().execute(add);
        return true;
    }
    public void DownloadBitmap(Photo photo){
        try{
         new DownloadFilesTask().execute(photo).get();}
        catch (Exception e){
            bit=null;
        }
    }

    //on l'appele dans une methode Async donc il doit etre sync
    private Bitmap DowloadSync(Photo photo){
        Bitmap bitmap=null;
        try
        {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
            // Create the blob client.
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference("photoalbum");
            // Loop through each blob item in the container.
            for (ListBlobItem blobItem : container.listBlobs()) {
                // If the item is a blob, not a virtual directory.
                if (blobItem instanceof CloudBlob && ((CloudBlob)blobItem).getName().equals(photo.getFileName())) {
                    // Download the item and save it to a file with the same name.
                    CloudBlob blob = (CloudBlob) blobItem;
                    //on l'enregistrr dans la memoire temporanee, elle est nettoie chaque fois que on ferme l'appli
                    File source = File.createTempFile(photo.getFileName(),".png", getCacheDir());
                    FileOutputStream fOut = new FileOutputStream(source);
                    blob.download(fOut);
                    //on set le parcour pour montre la photo
                    photo.setPath(source.getAbsolutePath());
                }
            }
        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
            SendMessage("Unable to download the picture");
        }
        return bitmap;
    }

    //Comunique avec l'activity si il y a des erreur
    private void SendMessage(String s) {
        Bundle bundle = new Bundle();
        bundle.putString("TypeIntent",ActivityActionHelper.SHOW_MESSAGE);

        bundle.putBoolean("isRefreshNeeded", false);
        bundle.putString(ActivityActionHelper.SHOW_MESSAGE, s);

        receiver.send(0, bundle);
    }

    //On upload la photo dans le Azure storage
    private class UploadFilesTask extends AsyncTask<Photo, Integer, Long> {
        @Override
        protected Long doInBackground(Photo... params) {
            try
            {
                // Retrieve storage account from connection-string.
                CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
                // Create the blob client.
                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                // Get a reference to a container.
                // The container name must be lower case
                CloudBlobContainer container = blobClient.getContainerReference("photoalbum");
                // Create the container if it does not exist.
                container.createIfNotExists();


                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "PNG_" + timeStamp + "_";


                File source = File.createTempFile(imageFileName,null, getCacheDir());
                FileOutputStream fOut = new FileOutputStream(source);
                params[0].getImg().compress(Bitmap.CompressFormat.PNG,0, fOut);
                fOut.flush();
                fOut.close();

                // Create or overwrite the "id.jpg" blob with contents from a local file.
                CloudBlockBlob blob = container.getBlockBlobReference(source.getName()+".png");
                // Create an image file name
                blob.upload(new FileInputStream(source), source.length());

                params[0].setImg(null);
                params[0].setPath(source.getAbsolutePath());
                params[0].setFileName(source.getName()+".png");
            }
            catch (Exception e)
            {
                // Output the stack trace.
                e.printStackTrace();
            }
            return new Long(0);
        }
    }
    //On delete la photo de azure
    private class DeleteFilesTask extends AsyncTask<String, Integer, Long> {
        @Override
        protected Long doInBackground(String... params) {
            try
            {
                try
                {
                    // Retrieve storage account from connection-string.
                    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
                    // Create the blob client.
                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                    // Retrieve reference to a previously created container.
                    CloudBlobContainer container = blobClient.getContainerReference("photoalbum");
                    // Retrieve reference to a blob named "myimage.jpg".
                    CloudBlockBlob blob = container.getBlockBlobReference(params[0]);
                    // Delete the blob.
                    blob.deleteIfExists();
                }
                catch (Exception e)
                {
                    // Output the stack trace.
                    e.printStackTrace();
                }
            }
            catch (Exception e)
            {
                // Output the stack trace.
                e.printStackTrace();
            }
            return new Long(0);
        }
    }
    //on download Asinchroniusly la photo
    private class DownloadFilesTask extends AsyncTask<Photo, Integer, Bitmap> {
        @Override
        protected Bitmap doInBackground(Photo... params) {
           return DowloadSync(params[0]);
        }
    }

    //DATABASE
    private final String MOBILE_URL="https://photovacances.azure-mobile.net/",
            MOBILE_KEY="wYRRvdevsDlLwzVFolzvaovjxzbfUg36";
    private MobileServiceClient db;
    private MobileServiceTable<Photo> photoTable;
    private MobileServiceTable<Comment> commentTable;
    private MobileServiceTable<Album> albumTable;

    public void Connection(){
        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            db = new MobileServiceClient(MOBILE_URL, MOBILE_KEY, this);

            // Get the Mobile Service Table instance to use
            photoTable = db.getTable("Photo",Photo.class);
            albumTable=db.getTable("Album",Album.class);
            commentTable=db.getTable("Comment",Comment.class);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void getData(final boolean isPhoto,final String idAlbum){
        Connection();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //je prende l'array d'album
                    List<Album> l = new ArrayList<>();
                    List<Album> listALbum = albumTable.where().execute().get();
                    for (Album album : listALbum) {
                        //pour chaque album je prende sa photo de coverture qui a l'attribute coverture=true
                        if ((photoTable.where().field("idalbum").eq(album.getId())
                                .and().field("cover").eq("true").execute().get()).size()!=0){
                            album.setCover((photoTable.where().field("idalbum").eq(album.getId())
                                .and().field("cover").eq("true").execute().get()).get(0));
                            DowloadSync(album.getCover());
                        }
                        //pour chaque album je prende aussi la liste de photo
                        List<Photo> listPhoto=photoTable.where().field("idalbum").eq(album.getId()).execute().get();
                        for(Photo photo:listPhoto) {
                            //je telecharge la photo et je prend aussi les commentaires
                            photo.setComments(commentTable.where().field("idphoto").eq(photo.getId()).execute().get());
                            DowloadSync(photo);
                        }
                        album.setPhotos(listPhoto);
                        l.add(album);
                    }
                    list=listALbum;

                } catch (Exception e) {
                    e.printStackTrace();
                    SendMessage("Unable to connect to the database, verify your internet connection");
                }
                if (isPhoto)
                    refreshPhoto(idAlbum);
                else
                refresh();
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
            }
        }.execute();
    }
    //ADD
    private void addPhotoDatabase(Photo photo, final String idAlbum){
        new AsyncTask<Photo, Void, Void>() {
            @Override
            protected Void doInBackground(Photo... params) {
                try {
                    //on utilise azure qui fait la comparason tout seul entre objet et tableau
                    params[0].setIdAlbum(idAlbum);
                    photoTable.insert(params[0]).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(photo);
    }
    private void addAlbumDatabase(Album photo){
        new AsyncTask<Album, Void, Void>() {
            @Override
            protected Void doInBackground(Album... params) {
                try {
                    albumTable.insert(params[0]).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(photo);
    }
    private void addCommentDatabase(Comment photo, final String idPhoto){
        new AsyncTask<Comment, Void, Void>() {
            @Override
            protected Void doInBackground(Comment... params) {
                try {
                    params[0].setIdPhoto(idPhoto);
                    commentTable.insert(params[0]).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(photo);
    }

    //EDIT
    private void editPhotoDatabase(Photo photo, final String idAlbum){
        new AsyncTask<Photo, Void, Void>() {
            @Override
            protected Void doInBackground(Photo... params) {
                try {
                    params[0].setIdAlbum(idAlbum);
                    photoTable.update(params[0]).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(photo);
    }
    private void editAlbumDatabase(Album al){
        new AsyncTask<Album, Void, Void>() {
            @Override
            protected Void doInBackground(Album... params) {
                try {
                    albumTable.update(params[0]).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(al);
    }

    //DELETE
    private void deletePhotoDatabase(Photo photo, final String idAlbum){
        new AsyncTask<Photo, Void, Void>() {
            @Override
            protected Void doInBackground(Photo... params) {
                try {
                    params[0].setIdAlbum(idAlbum);
                    photoTable.delete(params[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(photo);
    }
    private void deleteAlbumDatabase(Album photo){
        new AsyncTask<Album, Void, Void>() {
            @Override
            protected Void doInBackground(Album... params) {
                try {
                    albumTable.delete(params[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(photo);
    }
    private void deleteCommentDatabase(Comment photo, final String idPhoto){
        new AsyncTask<Comment, Void, Void>() {
            @Override
            protected Void doInBackground(Comment... params) {
                try {
                    params[0].setIdPhoto(idPhoto);
                    commentTable.delete(params[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(photo);
    }
}

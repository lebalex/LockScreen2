package xyz.lebalex.lockscreen;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ivc_lebedevav on 13.01.2017.
 */

public class MyServiceGoole extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //private static final int REQUEST_CODE_RESOLUTION = 3;
    private static GoogleApiClient mGoogleApiClient;
    private MyServiceGoole con=this;


    public MyServiceGoole() {
        super("MyServiceGoole");
    }
    protected void onHandleIntent(Intent intent) {
        Log.i("MyServiceGoole", "Start MyServiceGoole");
        getImageFromGooglwDrive();

    }
    private void getImageFromGooglwDrive()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i("MyServiceGoole", "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            //GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            //result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (Exception e) {
            Log.e("MyServiceGoole", "Exception while starting resolution activity", e);
        }
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("MyServiceGoole", "GoogleApiClient connection suspended");
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("MyServiceGoole", "API client connected.");
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);

    }
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();
                    Log.i("MyServiceGoole", "New contents created.");
                    final String folderName = "LockScreen";
                    try {
                        Log.i("MyServiceGoole", "Query.Builder");
                        Query query = new Query.Builder().addFilter(Filters.and(
                                Filters.eq(SearchableField.TITLE, folderName),
                                Filters.eq(SearchableField.TRASHED, false))).build();
                        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                                                                                            @Override
                                                                                            public void onResult(DriveApi.MetadataBufferResult result) {
                                                                                                if (!result.getStatus().isSuccess()) {
                                                                                                    //showMessage("Problem while retrieving files");
                                                                                                    //Toast.makeText(appContext, "Problem while retrieving files", Toast.LENGTH_SHORT).show();
                                                                                                    return;
                                                                                                }
                                                                                                MetadataBuffer aaa = result.getMetadataBuffer();
                                                                                                Log.i("MyServiceGoole",aaa.getCount()+"");
                                                                                                if (aaa.getCount() > 0) {
                                                                                                    Log.i("MyServiceGoole",aaa.get(0).getTitle());
                                                                                                    DriveId sFolderId = aaa.get(0).getDriveId();
                                                                                                    DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, sFolderId);
                                                                                                    folder.listChildren(mGoogleApiClient).setResultCallback(childrenRetrievedCallback);
                                                                                                }
                                                                                            }
                                                                                        }
                                );

                            }catch(Exception edr)
                            {
                                //Toast.makeText(appContext, edr.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                }
            };

    ResultCallback<DriveApi.MetadataBufferResult> childrenRetrievedCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        //showMessage("Problem while retrieving files");
                        return;
                    }
                    MetadataBuffer aaa = result.getMetadataBuffer();
                    for(int i=0;i<aaa.getCount();i++)
                    {
                        Log.i("MyServiceGoole", aaa.get(i).getTitle());

                    }
                    Log.i("MyServiceGoole", aaa.getCount()+"");

                    if(aaa.getCount()>0) {
                        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient,
                                aaa.get(0).getDriveId());
                        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                .setResultCallback(contentsOpenedCallback);
                    }else
                        Log.i("MyServiceGoole", aaa.getCount()+" нет файлов");

                }
            };
    ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // display an error saying file can't be opened
                        return;
                    }
                    Log.i("MyServiceGoole", "DriveContents");
                    DriveContents contents = result.getDriveContents();
                    InputStream is = contents.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Log.i("MyServiceGoole", "Bitmap");
try {
    WallpaperManager wallpaperManager = WallpaperManager
            .getInstance(con);
    wallpaperManager.clear();

    wallpaperManager.setBitmap(imageForScreen(bitmap));
    Log.i("MyServiceGoole", "Set");

}catch(Exception e)
{

}


                }
            };

    private Bitmap imageForScreen(Bitmap bitmap) {
        int hh = bitmap.getHeight();
        int ww = bitmap.getWidth();
        double c = 1;
        int newH = 1;
        int newW = 1;
        if (hh <= ww) {
            c = 1920.0 / hh;
            newH = 1920;
            newW = (int) (ww * c);
        } else {
            c = 1080.0 / ww;
            newW = 1080;
            newH = (int) (hh * c);
        }

        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, newW, newH, false);
        return bitmap2;
    }
}

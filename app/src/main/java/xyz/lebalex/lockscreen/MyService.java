package xyz.lebalex.lockscreen;

import android.app.IntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by ivc_lebedevav on 12.01.2017.
 */

public class MyService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private int sn = 1;
    private static GoogleApiClient mGoogleApiClient;
    private MyService context = this;

    public MyService() {
        super("MyServiceName");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.i("MyService", "About to execute MyTask");
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            Calendar calen = Calendar.getInstance();
            int startTime = Integer.parseInt(sp.getString("update_start", "0"));
            if (calen.get(Calendar.HOUR_OF_DAY) > startTime) {
                int source_load_value = Integer.parseInt(sp.getString("source_load", "1"));
                if (source_load_value == 3) {
                    //Log.i("MyService", "GooglwDrive");
                    getImageFromGooglwDrive();
                } else {
                    Bitmap bmp=null;
                    if (source_load_value == 1) {
                        //Log.i("MyService", "FromModile");
                        bmp=imageForScreen(getFromModile(getImageName()));
                    } else {
                        //Log.i("MyService", "FromNetwork");
                        bmp=imageForScreen(loadImageFromNetwork(sn));
                    }
                    if(bmp!=null)
                    {
                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                        wallpaperManager.clear();
                        wallpaperManager.setBitmap(bmp);
                    }
                }

            }
            //Log.i("MyService", "Set Wallpaper");
        } catch (IOException e) {
            //Log.e("MyService", "Service ", e);
        }
    }

    public Bitmap loadImageFromNetwork(int sn) {
        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            String urls = "";
            switch (sn) {
                case 1:
                    urls = "http://lebalexwebapp.azurewebsites.net/lockscreen/erotic.aspx";
                    break;
                case 2:
                    urls = "http://lebalexwebapp.azurewebsites.net/lockscreen/rand500.aspx";
                    break;

            }
            Bitmap bmp = null;
            while (bmp == null) {
                URL url = new URL(urls);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                if ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String resultJson = buffer.toString();
                JSONObject dataJsonObj = null;

                try {
                    JSONArray jsonArray = new JSONArray(resultJson);
                    String urlName = jsonArray.getString(0);
                    urls = jsonArray.getString(1);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream imageStream = urlConnection.getInputStream();


                bmp = BitmapFactory.decodeStream(imageStream);
                if (bmp.getHeight() < bmp.getWidth()) bmp = null;
            }
            return bmp;

        } catch (Exception eee) {
            return null;
        }
    }

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

    private String getImageName() {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LockScreen";
        //Log.d("Files", "Path: " + file_path);
        File directory = new File(file_path);
        File[] files = directory.listFiles();
        //Log.d("Files", "Size: "+ files.length);
        /*for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }*/
        Random rnd = new Random();
        String f = file_path + "/" + files[rnd.nextInt(files.length - 1)].getName();
        //Log.d("Files",f);
        return f;

    }

    private Bitmap getFromModile(String filePath) {
        File imgFile = new File(filePath);
        if (imgFile.exists())
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        else
            return null;
    }

    private void getImageFromGooglwDrive() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        //Log.i("MyServiceGoole", "GoogleApiClient connection failed: " + result.toString());
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
        //Log.i("MyServiceGoole", "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Log.i("MyServiceGoole", "API client connected.");
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
                    //Log.i("MyServiceGoole", "New contents created.");
                    final String folderName = "LockScreen";
                    try {
                        //Log.i("MyServiceGoole", "Query.Builder");
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
                                                                                                //Log.i("MyServiceGoole",aaa.getCount()+"");
                                                                                                if (aaa.getCount() > 0) {
                                                                                                    //Log.i("MyServiceGoole",aaa.get(0).getTitle());
                                                                                                    DriveId sFolderId = aaa.get(0).getDriveId();
                                                                                                    DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, sFolderId);
                                                                                                    folder.listChildren(mGoogleApiClient).setResultCallback(childrenRetrievedCallback);
                                                                                                }
                                                                                            }
                                                                                        }
                        );

                    } catch (Exception edr) {
                        //Toast.makeText(appContext, edr.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MyServiceGoole", "Exception edr", edr);
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
                    /*for(int i=0;i<aaa.getCount();i++)
                    {
                        Log.i("MyServiceGoole", aaa.get(i).getTitle());

                    }
                    Log.i("MyServiceGoole", aaa.getCount()+"");*/

                    if (aaa.getCount() > 0) {
                        Random rnd = new Random();
                        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient,
                                aaa.get(rnd.nextInt(aaa.getCount() - 1)).getDriveId());
                        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                .setResultCallback(contentsOpenedCallback);
                    }/*else
                        Log.i("MyServiceGoole", aaa.getCount()+" нет файлов");*/

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
                    //Log.i("MyServiceGoole", "DriveContents");
                    try {
                    DriveContents contents = result.getDriveContents();
                    InputStream is = contents.getInputStream();
                    Bitmap bitmap = null;
                    bitmap = BitmapFactory.decodeStream(is);
                    //Log.i("MyServiceGoole", "Bitmap");
                        if(bitmap!=null) {
                            WallpaperManager wallpaperManager = WallpaperManager
                                    .getInstance(context);
                            wallpaperManager.clear();

                            wallpaperManager.setBitmap(imageForScreen(bitmap));
                            //Log.i("MyServiceGoole", "Set");
                        }

                    } catch (Exception e3) {
                        Log.e("MyServiceGoole", "Exception e3", e3);
                    }


                }
            };
}
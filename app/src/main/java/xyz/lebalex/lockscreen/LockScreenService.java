package xyz.lebalex.lockscreen;

import android.app.IntentService;


import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;


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

public class LockScreenService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private int sn = 1;
    private static GoogleApiClient mGoogleApiClient;
    private LockScreenService context = this;

    public LockScreenService() {
        super("LockScreenService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogWrite.Log(context, "About to execute LockScreenService");
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LockScreenService");
        try {
            wl.acquire();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            int source_load_value = Integer.parseInt(sp.getString("source_load", "1"));
            Bitmap bmp = null;
            int countLoad=0;
            while (bmp == null && countLoad < 3) {
                switch (source_load_value) {
                    case 1:
                        LogWrite.Log(context, "FromModile");
                        bmp = imageForScreen(getFromModile(getImageName()));
                        break;
                    case 2:
                        LogWrite.Log(context, "FromNetwork");
                        bmp = imageForScreen(loadImageFromNetwork(sn));
                        break;
                    case 3:
                        LogWrite.Log(context, "GooglwDrive");
                        getImageFromGooglwDrive();
                        break;
                }
                countLoad++;
                if(source_load_value<3)
                    source_load_value++;
                else
                    source_load_value=1;
            }
            if (bmp != null) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                wallpaperManager.clear();
                if (sp.getBoolean("flag_wall", true)) {
                    wallpaperManager.setBitmap(bmp, null, true, WallpaperManager.FLAG_SYSTEM);
                    wallpaperManager.setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK);
                } else
                    wallpaperManager.setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK);

                LogWrite.Log(context, "Set Wallpaper");
            } else
                LogWrite.Log(context, "bmp null");

        } catch (IOException e) {
            LogWrite.Log(context, e.getMessage());
        } finally {
            wl.release();
        }
    }

    public Bitmap loadImageFromNetwork(int sn) {
        Bitmap bmp = null;
        int countLoad = 0;

        //PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LockScreenService");
        //wl.acquire();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String[] urls = null;
        switch (sn) {
            case 1:
                urls[0] = "http://lebalex.xyz/lockscreen/erotic.php";
                urls[1] = "http://lebalexwebapp.azurewebsites.net/lockscreen/erotic.aspx";
                break;
            case 2:
                urls[0] = "http://lebalex.xyz/lockscreen/lock500p.php";
                urls[1] = "http://lebalexwebapp.azurewebsites.net/lockscreen/rand500.aspx";
                break;

        }
        int sUrl = 0;

        while (bmp == null && countLoad < 5) {
            String select_urls = urls[sUrl];
            try {
                LogWrite.Log(context, "countLoad = " + countLoad);
                LogWrite.Log(context, "select_urls = " + select_urls);
                URL url = new URL(select_urls);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    LogWrite.Log(context, "first step HTTP_OK");

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    if ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    String resultJson = buffer.toString();
                    JSONObject dataJsonObj = null;
                    String url_json = null;
                    try {
                        JSONArray jsonArray = new JSONArray(resultJson);
                        String urlName = jsonArray.getString(0);
                        url_json = jsonArray.getString(1);
                    } catch (JSONException e) {
                        url_json = null;
                        LogWrite.Log(context, "JSONException = " + e.getMessage());
                    }

                    if (url_json != null) {
                        url = new URL(url_json);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();
                        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            LogWrite.Log(context, "second step HTTP_OK");

                            InputStream imageStream = urlConnection.getInputStream();
                            bmp = BitmapFactory.decodeStream(imageStream);
                            if (bmp != null) {
                                if (bmp.getHeight() < bmp.getWidth()) {
                                    LogWrite.Log(context, "Height < Width");
                                    bmp = null;
                                }
                            }
                        } else
                            LogWrite.Log(context, "second step HTTP_BAD");
                    } else {
                        LogWrite.Log(context, "url_json=null");
                    }
                } else
                    LogWrite.Log(context, "first step HTTP_BAD");

            } catch (java.net.ConnectException e_t) {
                LogWrite.Log(context, "Network error ConnectException = " + e_t.getMessage());
                sUrl = (sUrl == 0) ? 1 : 0;
                bmp = null;
            } catch (Exception eee) {
                LogWrite.Log(context, "Network error = " + eee.getMessage());
                sUrl = (sUrl == 0) ? 1 : 0;
                bmp = null;
            }
            countLoad++;
        }
        //wl.release();
        return bmp;
    }

    private Bitmap imageForScreen(Bitmap bitmap) {
        if (bitmap != null) {
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
        } else
            return null;
    }

    private String getImageName() {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LockScreen";
        File directory = new File(file_path);
        File[] files = directory.listFiles();
        Random rnd = new Random();
        String f = file_path + "/" + files[rnd.nextInt(files.length - 1)].getName();
        return f;

    }

    @Nullable
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
        if (!result.hasResolution()) {
            return;
        }
        try {
        } catch (Exception e) {
            LogWrite.Log(context, "Exception while starting resolution activity " + e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LogWrite.Log(context, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LogWrite.Log(context, "API client connected.");
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
                    LogWrite.Log(context, "New contents created.");
                    final String folderName = "LockScreen";
                    try {
                        LogWrite.Log(context, "Query.Builder");
                        Query query = new Query.Builder().addFilter(Filters.and(
                                Filters.eq(SearchableField.TITLE, folderName),
                                Filters.eq(SearchableField.TRASHED, false))).build();
                        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                                                                                            @Override
                                                                                            public void onResult(DriveApi.MetadataBufferResult result) {
                                                                                                if (!result.getStatus().isSuccess()) {
                                                                                                    return;
                                                                                                }
                                                                                                MetadataBuffer aaa = result.getMetadataBuffer();
                                                                                                if (aaa.getCount() > 0) {
                                                                                                    DriveId sFolderId = aaa.get(0).getDriveId();
                                                                                                    DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, sFolderId);
                                                                                                    folder.listChildren(mGoogleApiClient).setResultCallback(childrenRetrievedCallback);
                                                                                                }
                                                                                            }
                                                                                        }
                        );

                    } catch (Exception edr) {
                        LogWrite.Log(context, "Exception edr " + edr.getMessage());
                    }

                }
            };

    ResultCallback<DriveApi.MetadataBufferResult> childrenRetrievedCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    MetadataBuffer aaa = result.getMetadataBuffer();

                    if (aaa.getCount() > 0) {
                        Random rnd = new Random();
                        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient,
                                aaa.get(rnd.nextInt(aaa.getCount() - 1)).getDriveId());
                        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                .setResultCallback(contentsOpenedCallback);
                    } else
                        LogWrite.Log(context, aaa.getCount() + " нет файлов");

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
                    try {
                        DriveContents contents = result.getDriveContents();
                        InputStream is = contents.getInputStream();
                        Bitmap bitmap = null;
                        bitmap = BitmapFactory.decodeStream(is);
                        if (bitmap != null) {
                            WallpaperManager wallpaperManager = WallpaperManager
                                    .getInstance(context);
                            wallpaperManager.clear();

                            bitmap = imageForScreen(bitmap);
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                            if (sp.getBoolean("flag_wall", true)) {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                            } else
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                        }

                    } catch (Exception e3) {
                        LogWrite.Log(context, e3.getMessage());
                    }


                }
            };
}

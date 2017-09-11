package xyz.lebalex.lockscreen;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.app.enterprise.license.EnterpriseLicenseManager;
import android.app.enterprise.lso.LockscreenOverlay;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.RotateAnimation;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.sec.enterprise.knox.license.KnoxEnterpriseLicenseManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,

        OnConnectionFailedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static Context appContext = null;
    private static int height = 0;
    private static int width = 0;
    private static final String TAG = "lockscreen";
    private static String urlName = "";
    private static Bitmap mBitmapToSave;
    private static ImageView mDmImageView;
    private static TextView mDmTextView;
    private static String googleLoadFileName;
    private static ImageButton mDmImageButton;
    private static String originalUrlTemp;
    private static List<String> historyUrl = new ArrayList<String>();
    private static int indexUrl = 0;
    private static boolean saveGoogleDrive = false;
    private static int moveFinger=0;

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private static final int REQUEST_WRITE_STORAGE = 112;

    private static GoogleApiClient mGoogleApiClient;

    private static SharedPreferences sp;

    private DevicePolicyManager mDPM;
    private ComponentName mCN;

    private static MetadataBuffer searchFiles = null;
    private static int idxG=0;
    private static int idxFLocal=-1;
    private static boolean samsung=false;


    public void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        //Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;
        mDmImageView.setEnabled(false);
        mDmImageView.setAlpha(0.5F);

        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>() {
                @Override
                public void onResult(DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Otherwise, we can write our data to the new contents.
                    //Log.i(TAG, "New contents created.");
                    // Get an output stream for the contents.
                    OutputStream outputStream = result.getDriveContents().getOutputStream();
                    // Write the bitmap data from it.
                    ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                    mBitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                    try {
                        outputStream.write(bitmapStream.toByteArray());
                    } catch (IOException e1) {
                        //Log.i(TAG, "Unable to write file contents.");
                    }
                    // Create the initial metadata - MIME type and title.
                    // Note that the user will be able to change the title later.

                    final String folderName = "LockScreen";


                    try {
                        //Log.i(TAG, "Query.Builder");
                        /*DriveFolder driveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
                        driveFolder.listChildren(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                                    @Override
                                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                                        //log("got root folder");
                                        MetadataBuffer buffer = metadataBufferResult.getMetadataBuffer();
                                        Log.i(TAG,"Buffer count  " + buffer.getCount());
                                        for(Metadata m : buffer){
                                            if (m.isFolder() && m.getTitle().equals(folderName))
                                                DriveId sFolderId = m.getDriveId();
                                        }
                                    }
                                });*/


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
                                                                                                if (aaa.getCount() == 0) {
                                                                                                    //create folder
                                                                                                    MetadataChangeSet changeSet2 = new MetadataChangeSet.Builder()
                                                                                                            .setTitle(folderName).build();
                                                                                                    Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient
                                                                                                            , changeSet2).setResultCallback(folderCreatedCallback);
                                                                                                    mDmTextView.setText("create folder, try again");

                                                                                                } else {

                                                                                                    DriveId sFolderId = aaa.get(0).getDriveId();
                                                                                                    DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, sFolderId);
                                                                                                    java.util.Calendar c = java.util.Calendar.getInstance();
                                                                                                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                                                                                            .setMimeType("image/jpeg").setTitle("image" + c.get(java.util.Calendar.YEAR) + (c.get(java.util.Calendar.MONTH) + 1) + c.get(java.util.Calendar.DATE) + c.get(java.util.Calendar.HOUR_OF_DAY) + c.get(java.util.Calendar.MINUTE) + c.get(java.util.Calendar.SECOND) + ".jpg").build();

                                                                                                    folder.createFile(mGoogleApiClient, metadataChangeSet, driveContents)
                                                                                                            .setResultCallback(fileCallback);
                                                                                                    mDmTextView.setText("complite upload");

                                                                                                }
                                                                                            }
                                                                                        }
                        );

                    } catch (Exception edr) {
                        Toast.makeText(appContext, edr.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    mDmImageView.setEnabled(true);
                    mDmImageView.setAlpha(1F);

                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        //showMessage("Error while trying to create the file");
                        return;
                    }
                    //showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
                }
            };
    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(DriveFolder.DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                //                showMessage("Error while trying to create the folder");
                return;
            }
            //            showMessage("Created a folder: " + result.getDriveFolder().getDriveId());
            DriveId drv = result.getDriveFolder().getDriveId();
        }
    };


    @Override
    public void onConnected(Bundle connectionHint) {
        //Log.i(TAG, "API client connected.");
        if (mBitmapToSave == null && saveGoogleDrive) {
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
            return;
        }
        if (saveGoogleDrive)
            saveFileToDrive();
        else {
            if(searchFiles==null)
                loadImageFromGoogleDrive();
            else {
                if(moveFinger==1)
                    loadImageFromGoogleDriveNext();
                else
                    loadImageFromGoogleDrivePrev();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        //Log.i(TAG, "GoogleApiClient connection suspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (Exception e) {
            //Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    public void loadImageFromGoogleDrive() {

        try {
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(driveContentsLoadCallback);

        } catch (Exception e) {

        }

    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsLoadCallback = new
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

                                                                                                if (aaa.getCount() > 0) {
                                                                                                    //Log.i("MyServiceGoole",aaa.get(0).getTitle());
                                                                                                    DriveId sFolderId = aaa.get(0).getDriveId();
                                                                                                    LogWrite.Log(appContext, "sFolderId = " + aaa.get(0).getTitle());
                                                                                                    DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, sFolderId);
                                                                                                    folder.listChildren(mGoogleApiClient).setResultCallback(childrenRetrievedCallback);
                                                                                                }
                                                                                            }
                                                                                        }
                        );

                    } catch (Exception edr) {
                        //Toast.makeText(appContext, edr.getMessage(), Toast.LENGTH_SHORT).show();
                        //Log.e("MyServiceGoole", "Exception edr", edr);
                    }

                }
            };
    ResultCallback<DriveApi.MetadataBufferResult> childrenRetrievedCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    try {
                        if (!result.getStatus().isSuccess()) {
                            return;
                        }
                        searchFiles = result.getMetadataBuffer();
                        LogWrite.Log(appContext, "searchFiles = " + searchFiles.getCount());

                        if (searchFiles.getCount() > 0) {
                            Random rnd = new Random();
                            idxG = rnd.nextInt(searchFiles.getCount() - 1);

                            googleLoadFileName = searchFiles.get(idxG).getTitle();
                            DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient,
                                    searchFiles.get(idxG).getDriveId());
                            file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                    .setResultCallback(contentsOpenedCallback);
                        }
                    }catch(Exception e) {
                        LogWrite.Log(appContext, e.getMessage());
                    }

                }
            };
    public void loadImageFromGoogleDriveNext() {

        try {
            if (searchFiles.getCount() > 0) {
                /*Random rnd = new Random();
                int idx = rnd.nextInt(searchFiles.getCount() - 1);*/
                if(idxG==searchFiles.getCount()-1) idxG=0;
                else
                    idxG++;

                googleLoadFileName = searchFiles.get(idxG).getTitle();
                DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient,
                        searchFiles.get(idxG).getDriveId());
                file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                        .setResultCallback(contentsOpenedCallback);
            }

        } catch (Exception e) {
            LogWrite.Log(appContext, "loadImageFromGoogleDriveNext,"+ idxG +", "+ e.getMessage());
        }

    }
    public void loadImageFromGoogleDrivePrev() {

        try {
            if (searchFiles.getCount() > 0) {
                /*Random rnd = new Random();
                int idx = rnd.nextInt(searchFiles.getCount() - 1);*/
                if(idxG==0) idxG=searchFiles.getCount()-1;
                else
                    idxG--;

                googleLoadFileName = searchFiles.get(idxG).getTitle();
                DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient,
                        searchFiles.get(idxG).getDriveId());
                file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                        .setResultCallback(contentsOpenedCallback);
            }

        } catch (Exception e) {
            LogWrite.Log(appContext, "loadImageFromGoogleDrivePrev,"+ idxG +", "+ e.getMessage());
        }

    }
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
                        if (bitmap != null) {
                            mDmImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            mDmImageView.setDrawingCacheEnabled(false);
                            mDmImageView.setImageBitmap(bitmap);
                            mDmImageView.setDrawingCacheEnabled(true);
                            mDmImageView.buildDrawingCache();

                            mDmImageButton.clearAnimation();
                            mDmImageButton.setEnabled(true);
                            mDmImageButton.setAlpha(1F);
                            mDmImageView.setEnabled(true);
                            mDmImageView.setAlpha(1F);
                            //Log.i("MyServiceGoole", "Set");

                            PlaceholderFragment frag = (PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
                            frag.setBitMap(bitmap);
                            frag.setTextLabel(googleLoadFileName);

                        }

                    } catch (Exception e3) {
                        //Log.e("MyServiceGoole", "Exception e3", e3);
                        LogWrite.Log(appContext,"MyServiceGoole"+ e3.getMessage());
                    }


                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContext = getApplicationContext();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        height = metrics.heightPixels;
        width = metrics.widthPixels;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        //mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        checkPermision();
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if(android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung"))
            samsung=true;


        //Toast.makeText(appContext, deviceMan, Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("samsung", samsung);
        if(!samsung) editor.putBoolean("flag_wall", true);
        editor.commit();
        LogWrite.Log(this, "samsung = "+samsung);



        if (!sp.getBoolean("start_service", false)) {
            int interval = Integer.parseInt(sp.getString("update_frequency", "60")) * 1000 * 60;
            int startTime = Integer.parseInt(sp.getString("update_start", "0"));
            LogWrite.Log(this, "start APP, interval = " + interval / 1000 / 60);
            startBackgroundService(interval, startTime);
        }


    }

    private void startBackgroundService(int interval, int startTime) {
        Intent alarmIntent = new Intent(this, LockScreenServiceReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(this, 1001, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (interval > 0) {
            Calendar curentTime = Calendar.getInstance();

            Calendar startCalen = Calendar.getInstance();
            startCalen.set(Calendar.HOUR_OF_DAY, startTime);
            startCalen.set(Calendar.MINUTE, 5);
            startCalen.set(Calendar.SECOND, 0);
            startCalen.set(Calendar.MILLISECOND, 0);

            boolean find = false;
            while (!find) {
                if (curentTime.before(startCalen))
                    find = true;
                else
                    startCalen.add(Calendar.MILLISECOND, interval);
            }


            manager.setRepeating(AlarmManager.RTC_WAKEUP, startCalen.getTimeInMillis(), interval, pendingIntent);

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("start_service", true);
            editor.commit();
            LogWrite.Log(this, "start Alarm " + startCalen.get(Calendar.YEAR) + "-" + startCalen.get(Calendar.MONTH) + "-" + startCalen.get(Calendar.DATE) + " " + startCalen.get(Calendar.HOUR_OF_DAY) + ":" + startCalen.get(Calendar.MINUTE) + ":" + startCalen.get(Calendar.SECOND));
        } else {
            LogWrite.Log(this, "stop Alarm");
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    private void checkPermision() {
        int permission = ContextCompat.checkSelfPermission(appContext,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
                builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRequest();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(!samsung) {
            MenuItem action_licItem = menu.findItem(R.id.action_lic);
            action_licItem.setVisible(false);
        }

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
            //SettingsActivity.actionTo(this);
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_lic) {
            //SettingsActivity.actionTo(this);
            Intent i = new Intent(this, AdminLicenseActivation.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_reload) {
            PlaceholderFragment frag = (PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
            frag.reloadImage(historyUrl.get(indexUrl - 1));
            return true;
        }
        if (id == R.id.action_logs) {
            //SettingsActivity.actionTo(this);
            Intent i = new Intent(this, LogActivity.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_restart_service) {
            int interval = Integer.parseInt(sp.getString("update_frequency", "60")) * 1000 * 60;
            int startTime = Integer.parseInt(sp.getString("update_start", "0"));
            LogWrite.Log(this, "--restart APP, interval = " + interval / 1000 / 60);
            startBackgroundService(interval, startTime);
            PlaceholderFragment frag = (PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
            frag.setTextLabel("restart APP");
            return true;
        }
        if (id == R.id.action_share_to) {
            PlaceholderFragment frag = (PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
            frag.sendToImage();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        View rootView;
        private String errorLoadBmp = "";
        ImageButton imageButton;
        ImageButton imageButtonBack;
        ImageButton imageButtonResize;
        ImageButton imageButtonRotate;
        ImageButton imageButtonMirror;
        ImageButton imageButtonWallSystem;
        ImageButton imageButtonPlus;
        ImageButton imageButtonCheck;
        ImageView mImageView;
        TextView textView;
        Handler handler = new Handler();
        //Bitmap mBitmapToBack;
        Bitmap bitmap;
        //private android.graphics.Matrix matrix;
        //private float scaleX, scaleY;

        private Intent mServiceIntent;
        //private PendingIntent pendingIntent;
        //private AlarmManager manager;

        private String fileNameforWall;


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void setImageToView(Bitmap bitmap) {

            mImageView.invalidate();
            mImageView.setScrollX(0);
            mImageView.setScrollY(0);
            mImageView.setDrawingCacheEnabled(false);
            mImageView.setImageBitmap(bitmap);
            //mImageView.setImageBitmap(bmOverlay);
            mImageView.setDrawingCacheEnabled(true);
            mImageView.buildDrawingCache();


        }


        private void loadDate() {

            imageButton = (ImageButton) rootView.findViewById(R.id.button);
            textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("loading ...");
            RotateAnimation rotateAnimation = new RotateAnimation(0f, 360 * 10, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnimation.setStartOffset(1);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setInterpolator(new AccelerateInterpolator());
            rotateAnimation.setDuration(10000);
            imageButton.startAnimation(rotateAnimation);
            imageButton.setEnabled(false);
            imageButton.setAlpha(0.7F);
            mImageView.setEnabled(false);
            mImageView.setAlpha(0.5F);


            new Thread(new Runnable() {
                public void run() {
                    final TextView textView = (TextView) rootView.findViewById(R.id.section_label);
                    int sn = getArguments().getInt(ARG_SECTION_NUMBER);
                    if (sn == 3) {
                        //google drive
                        mDmImageView = mImageView;
                        mDmTextView = textView;
                        mDmImageButton = imageButton;
                        if (mGoogleApiClient.isConnected())
                            mGoogleApiClient.disconnect();
                        saveGoogleDrive = false;
                        mGoogleApiClient.connect();
                    }
                    else  if (sn == 4)
                    {
                        bitmap = getFromModile(getImageName());
                        mImageView.post(new Runnable() {
                            public void run() {
                                if (bitmap != null) {
                                    setImageToView(bitmap);
                                }
                                try {
                                    imageButton.clearAnimation();
                                    imageButton.setEnabled(true);
                                    imageButton.setAlpha(1F);
                                    mImageView.setEnabled(true);
                                    mImageView.setAlpha(1F);
                                } catch (Exception ea) {
                                    ea.printStackTrace();
                                }

                            }
                            });
                    }
                    else {
                        bitmap = loadImageFromNetwork(sn);
                        mImageView.post(new Runnable() {
                            public void run() {
                                if (bitmap != null) {
                                    android.graphics.Matrix matrix = new android.graphics.Matrix();
                                    mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    setImageToView(bitmap);
                                    textView.setText(urlName);
                                    historyUrl.add(originalUrlTemp);
                                    indexUrl = historyUrl.size();
                                } else {
                                    if (errorLoadBmp.length() == 0) textView.setText("error");
                                    else
                                        textView.setText(errorLoadBmp);
                                }
                                try {
                                    imageButton.clearAnimation();
                                    imageButton.setEnabled(true);
                                    imageButton.setAlpha(1F);
                                    mImageView.setEnabled(true);
                                    mImageView.setAlpha(1F);
                                } catch (Exception ea) {
                                    ea.printStackTrace();
                                }

                            }
                        });
                    }
                }
            }).start();

        }

        public void setBitMap(Bitmap b) {
            bitmap = b;
        }

        private void setWall() {
            textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("set ...");
            mImageView.setEnabled(false);
            mImageView.setAlpha(0.5F);

            new Thread(new Runnable() {
                public void run() {

                    handler.post(new Runnable() {
                        public void run() {
                            try {

                                Bitmap bitMap = null;
                                bitMap = mImageView.getDrawingCache(true);
                                if (bitMap != null) {

                                    Bitmap bmOverlay = setAlpha(bitMap, Integer.parseInt(sp.getString("alpha_value", "150")));

                                 /*android 7*/
                                    WallpaperManager wallpaperManager = WallpaperManager
                                            .getInstance(appContext);

                if (sp.getBoolean("flag_wall", true))
                    wallpaperManager.setBitmap(bmOverlay, null, true, WallpaperManager.FLAG_SYSTEM);
                if(sp.getBoolean("samsung", false)) {
                    String bitmapPath = MediaStore.Images.Media.insertImage(appContext.getContentResolver(), bmOverlay, "title", null);
                    Uri bitmapUri = Uri.parse(bitmapPath);
                    fileNameforWall = getRealPathFromURI(appContext, bitmapUri);
                    setSamsungWall(fileNameforWall);
                }else
                    wallpaperManager.setBitmap(bmOverlay, null, true, WallpaperManager.FLAG_LOCK);



                                    /*android 6*/
                                    /*if(samsung) {
                                        String bitmapPath = MediaStore.Images.Media.insertImage(appContext.getContentResolver(), bmOverlay, "title", null);
                                        Uri bitmapUri = Uri.parse(bitmapPath);
                                        fileNameforWall = getRealPathFromURI(appContext, bitmapUri);
                                        if (sp.getBoolean("flag_wall", true)) {
                                            Intent intent = new Intent(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER);
                                            String mime = "image/*";
                                            intent.setDataAndType(bitmapUri, mime);
                                            startActivityForResult(intent, 11);
                                        }
                                        setSamsungWall(fileNameforWall);
                                    }else
                                    {
                                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(appContext);
                                        wallpaperManager.clear();
                                        wallpaperManager.setBitmap(bmOverlay);
                                    }*/


                                }
                                textView.setText("set Wallpaper");
                                mImageView.setEnabled(true);
                                mImageView.setAlpha(1F);
                            } catch (Exception e1) {
                                textView.setText("error");
                                Toast.makeText(appContext, "error: " + e1.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }
            }).start();


        }

        private void deleteFile(String path) {
            try {
                File file = new File(path);
                file.delete();
            } catch (Exception e) {
                Toast.makeText(appContext, "delete error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private Bitmap setAlpha(Bitmap bmp, int alpha) {
            Bitmap bmOverlay = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(bmp, new Matrix(), null);
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setAlpha(alpha);
            canvas.drawRect(0, 0, bmp.getWidth(), bmp.getHeight(), p);
            return bmOverlay;
        }

        private void setSamsungWall(String bitmapUri) {
            LockscreenOverlay lso = LockscreenOverlay.getInstance(appContext);

            if (lso.canConfigure()) {
                int result = lso.setWallpaper(bitmapUri);

                if (LockscreenOverlay.ERROR_NONE != result) {
                    String a = "";
                    switch (result) {
                        case LockscreenOverlay.ERROR_BAD_STATE:
                            a = "ERROR_BAD_STATE";
                            break;
                        case LockscreenOverlay.ERROR_FAILED:
                            a = "ERROR_FAILED";
                            break;
                        case LockscreenOverlay.ERROR_NOT_ALLOWED:
                            a = "ERROR_NOT_ALLOWED";
                            break;
                        case LockscreenOverlay.ERROR_NOT_READY:
                            a = "ERROR_NOT_READY";
                            break;
                        case LockscreenOverlay.ERROR_NOT_SUPPORTED:
                            a = "ERROR_NOT_SUPPORTED";
                            break;
                        case LockscreenOverlay.ERROR_PERMISSION_DENIED:
                            a = "ERROR_PERMISSION_DENIED";
                            break;
                        case LockscreenOverlay.ERROR_UNKNOWN:
                            a = "ERROR_UNKNOWN";
                            break;
                    }
                    Toast.makeText(appContext, a, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(appContext, "Administrator cannot customize lock screen", Toast.LENGTH_SHORT).show();
            }
        }

        public String getRealPathFromURI(Context context, Uri contentUri) {
            Cursor cursor = null;
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        public void setTextLabel(String text) {
            textView.setText(text);
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

        private void reloadImage(String originalUrl) {
            originalUrlTemp = originalUrl;
            if (originalUrlTemp != null) {
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                textView.setText("loading ...");
                RotateAnimation rotateAnimation = new RotateAnimation(0f, 360 * 10, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);

                rotateAnimation.setStartOffset(1);
                rotateAnimation.setRepeatCount(-1);
                rotateAnimation.setInterpolator(new AccelerateInterpolator());
                rotateAnimation.setDuration(10000);
                imageButton.startAnimation(rotateAnimation);
                imageButton.setEnabled(false);
                imageButton.setAlpha(0.7F);
                mImageView.setEnabled(false);
                mImageView.setAlpha(0.5F);
                new Thread(new Runnable() {
                    public void run() {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        bitmap = getBitMapFromUrl(originalUrlTemp, Integer.parseInt(sp.getString("timeout", "10")));
                        handler.post(new Runnable() {
                            public void run() {
                                setImageToView(bitmap);
                                imageButton.clearAnimation();
                                imageButton.setEnabled(true);
                                imageButton.setAlpha(1F);
                                mImageView.setEnabled(true);
                                mImageView.setAlpha(1F);
                                textView.setText("complite");
                            }
                        });
                    }
                }).start();
            }
        }

        private void sendToImage() {
            try {
                String bitmapPath = MediaStore.Images.Media.insertImage(appContext.getContentResolver(), bitmap, "title", null);
                Uri bitmapUri = Uri.parse(bitmapPath);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
            } catch (Exception e) {

            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 11) {
                deleteFile(fileNameforWall);
                try {
                    if (textView != null) textView.setText("set Wallpaper");
                } catch (Exception e) {
                }
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("");
            mImageView = (ImageView) rootView.findViewById(R.id.imgView);
            imageButton = (ImageButton) rootView.findViewById(R.id.button);
            imageButtonBack = (ImageButton) rootView.findViewById(R.id.button_back);
            imageButtonResize = (ImageButton) rootView.findViewById(R.id.button_resize);
            imageButtonPlus = (ImageButton) rootView.findViewById(R.id.button_plus);
            imageButtonCheck = (ImageButton) rootView.findViewById(R.id.button_check);
            imageButtonRotate = (ImageButton) rootView.findViewById(R.id.button_rotate);
            imageButtonMirror = (ImageButton) rootView.findViewById(R.id.button_mirror);
            imageButtonWallSystem = (ImageButton) rootView.findViewById(R.id.button_setwallsystem);
            //sp = PreferenceManager.getDefaultSharedPreferences(appContext);



            switch (getArguments().getInt(ARG_SECTION_NUMBER))
            {
                case 1:textView.setText("ERO");break;
                case 2:textView.setText("INET");break;
                case 3:textView.setText("GOOGLE");break;
                case 4:textView.setText("LOCAL");break;
            }

            textView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    textView.setText("uploading ...");
                    mImageView.setEnabled(false);
                    mImageView.setAlpha(0.5F);

                    mBitmapToSave = mImageView.getDrawingCache();


                    if (sp.getBoolean("save_google_switch", false)) {
                        mDmImageView = mImageView;
                        mDmTextView = textView;
                        if (mGoogleApiClient.isConnected())
                            mGoogleApiClient.disconnect();

                        saveGoogleDrive = true;
                        mGoogleApiClient.connect();

                    } else {

                        new Thread(new Runnable() {
                            public void run() {
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LockScreen";
                                            File dir = new File(file_path);
                                            if (!dir.exists())
                                                dir.mkdirs();
                                            java.util.Calendar c = java.util.Calendar.getInstance();
                                            String fileName = "image" + c.get(java.util.Calendar.YEAR) + (c.get(java.util.Calendar.MONTH) + 1) + c.get(java.util.Calendar.DATE) + c.get(java.util.Calendar.HOUR_OF_DAY) + c.get(java.util.Calendar.MINUTE) + c.get(java.util.Calendar.SECOND) + ".jpg";
                                            File file = new File(dir, fileName);
                                            FileOutputStream fOut = new FileOutputStream(file);
                                            mBitmapToSave.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                                            fOut.flush();
                                            fOut.close();
                                            textView.setText("complite");
                                            mImageView.setEnabled(true);
                                            mImageView.setAlpha(1F);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            textView.setText(e.getMessage());
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });

            final GestureDetector gesture = new GestureDetector(getActivity(),
                    new GestureDetector.SimpleOnGestureListener() {

                        @Override
                        public boolean onDown(MotionEvent e) {
                            return true;
                        }

                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {

                            if (mImageView.getScaleType() != ImageView.ScaleType.MATRIX)
                                setWall();

                            return super.onSingleTapUp(e);
                        }

                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                               float velocityY) {
                            //Log.i("fling", "onFling has been called!");
                            final int SWIPE_MIN_DISTANCE = 120;
                            try {
                                //scrolX=0;scrolY=0;
                                float ae1 = e1.getY();
                                float ae2 = e2.getY();
                                if (mImageView.getScaleType() != ImageView.ScaleType.MATRIX) {
                                    if (e1.getY() - e2.getY() < SWIPE_MIN_DISTANCE * -1) {
                                        //Log.i("fling", "down");
                                        //imageButton.callOnClick();

                                        moveFinger=-1;
                                        loadDate();
                                    } else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                                        //Log.i("fling", "up");
                                        //imageButton.callOnClick();
                                        moveFinger=1;
                                        loadDate();
                                    }
                                } else {
                                    int mx = (int) e1.getX();
                                    int my = (int) e1.getY();

                                    int curX = (int) e2.getX();
                                    int curY = (int) e2.getY();
                                    int xs = (mx - curX);
                                    int ys = (my - curY);

                                    mImageView.scrollBy((int) xs, (int) ys);


                                }
                            } catch (Exception e) {
                                // nothing
                            }
                            return super.onFling(e1, e2, velocityX, velocityY);
                        }
                    });

            mImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gesture.onTouchEvent(event);
                }
            });


            imageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    loadDate();
                }
            });
            imageButtonBack.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    //setImageToView(mBitmapToBack);
                    //bitmap = mBitmapToBack;
                    indexUrl--;
                    if (indexUrl > 0)
                        reloadImage(historyUrl.get(indexUrl - 1));

                }
            });

            imageButtonResize.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (mImageView.getScaleType() == ImageView.ScaleType.FIT_CENTER) {
                            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            setImageToView(imageForScreen(bitmap));
                            textView.setText("CENTER_CROP");
                        } else {
                            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            setImageToView(bitmap);
                            textView.setText("FIT_CENTER");
                        }
                    } catch (Exception e) {
                    }
                }
            });
            imageButtonPlus.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        mImageView.setScaleType(ImageView.ScaleType.MATRIX);

                        mImageView.setScrollX(0);
                        mImageView.setScrollY(0);
                        int nh = (int) (bitmap.getHeight() * 1.5);
                        int nw = (int) (bitmap.getWidth() * 1.5);
                        bitmap = Bitmap.createScaledBitmap(bitmap, nw, nh, true);
                        setImageToView(bitmap);
                        textView.setText("Plus");
                        //Toast.makeText(appContext, bitmap.getWidth()+"-"+bitmap.getHeight(), Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {

                    }
                }
            });
            imageButtonRotate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        setImageToView(bitmap);
                        textView.setText("Rotate");
                    } catch (Exception e) {

                    }
                }
            });
            imageButtonMirror.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        Matrix matrix = new Matrix();
                        matrix.setScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

                        setImageToView(bitmap);
                        textView.setText("Rotate");
                    } catch (Exception e) {

                    }
                }
            });
            /*imageButtonWallSystem.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    textView = (TextView) rootView.findViewById(R.id.section_label);
                    try {
                        Bitmap bitMap = mImageView.getDrawingCache(true);
                        if (bitMap != null) {

                            Bitmap bmOverlay = setAlpha(bitMap, Integer.parseInt(sp.getString("alpha_value", "150")));
                            String bitmapPath = MediaStore.Images.Media.insertImage(appContext.getContentResolver(), bmOverlay, "title", null);
                            Uri bitmapUri = Uri.parse(bitmapPath);
                            fileNameforWall = getRealPathFromURI(appContext, bitmapUri);
                            Intent intent = new Intent(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER);
                            String mime = "image/*";
                            intent.setDataAndType(bitmapUri, mime);
                            startActivityForResult(intent, 11);
                        }
                    } catch (Exception e) {
                        Toast.makeText(appContext, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });*/
            imageButtonWallSystem.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Bitmap bitMap = mImageView.getDrawingCache(true);
                    if (bitMap != null) {
                        //bitMap = imageForScreen(bitMap);
                        Bitmap bmOverlay = setAlpha(bitMap, Integer.parseInt(sp.getString("alpha_value", "150")));
                        try {
                            String filename = "bitmap.png";
                            FileOutputStream stream = appContext.openFileOutput(filename, Context.MODE_PRIVATE);
                            bmOverlay.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            stream.close();

                        Intent i = new Intent(v.getContext(), FullscreenActivity.class);
                        Bundle b = new Bundle();
                        b.putString("image", filename);
                        i.putExtras(b);


                        startActivity(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            /*imageButtonWallSystem.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    textView = (TextView) rootView.findViewById(R.id.section_label);
                    textView.setText("set ...");
                    mImageView.setEnabled(false);
                    mImageView.setAlpha(0.5F);
                    new Thread(new Runnable() {
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        Bitmap bitMap = mImageView.getDrawingCache(true);
                                        if (bitMap != null) {

                                            Bitmap bmOverlay = setAlpha(bitMap, Integer.parseInt(sp.getString("alpha_value", "150")));

                                            WallpaperManager wallpaperManager = WallpaperManager
                                                    .getInstance(appContext);
                                            //wallpaperManager.setBitmap(bmOverlay);
                                            wallpaperManager.setBitmap(bmOverlay, null, true, WallpaperManager.FLAG_SYSTEM);

                                            textView.setText("set Wallpaper");
                                            mImageView.setEnabled(true);
                                            mImageView.setAlpha(1F);
                                        }
                                    } catch (Exception e) {
                                        textView.setText("error");
                                        Toast.makeText(appContext, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }).start();
                }
            });*/


            imageButtonCheck.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        //Toast.makeText(appContext, scrolX+"-"+scrolY, Toast.LENGTH_SHORT).show();
                        Bitmap drawable = mImageView.getDrawingCache(true);

                        //Bitmap bmOverlay = Bitmap.createBitmap(drawable.getWidth(), drawable.getHeight(), drawable.getConfig());

                        bitmap = null;
                        bitmap = Bitmap.createBitmap(drawable.getWidth(), drawable.getHeight(), drawable.getConfig());
                        Canvas canvas = new Canvas(bitmap);
                        Paint paint = new Paint();
                        //paint.setColor(Color.RED);
                        //paint.setStrokeWidth(10);
                        canvas.drawBitmap(drawable, new Matrix(), null);
                        //canvas.drawCircle(mImageView.getScrollX(), mImageView.getScrollY(), 50, paint);

                        //Bitmap bmOverlay2 = Bitmap.createBitmap(bmOverlay, mImageView.getScrollX(), mImageView.getScrollY(), ww, hh, m, false);

                        setImageToView(bitmap);

                        mImageView.setScrollX(0);
                        mImageView.setScrollY(0);

                        //mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        //scrolX = 0;
                        //scrolY = 0;

                       /* Bitmap result = Bitmap.createBitmap(mImageView.getWidth(), mImageView.getHeight(), Bitmap.Config.RGB_565);
                            Canvas c = new Canvas(result);
                        mImageView.draw(c);*/
                        textView.setText("Check");


                    } catch (Exception e) {
                        textView.setText(e.getMessage());
                        Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mImageView.setScrollX(0);
                        mImageView.setScrollY(0);
                        //scrolX = 0;
                        //scrolY = 0;
                    }
                }
            });


            return rootView;
        }

        private String getImageName() {
            try {
                String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LockScreen";
                File directory = new File(file_path);
                File[] files = directory.listFiles();
                if (idxFLocal == -1) {
                    Random rnd = new Random();
                    if(files.length>1)
                        idxFLocal = rnd.nextInt(files.length - 1);
                    else
                        idxFLocal=0;
                }else {
                    switch (moveFinger) {
                        case 1:
                            if (idxFLocal == files.length - 1) idxFLocal = 0;
                            else idxFLocal++;
                            break;
                        case -1:
                            if (idxFLocal == 0) idxFLocal = files.length - 1;
                            else idxFLocal--;
                            break;
                    }
                }
                textView.setText( files[idxFLocal].getName());
                String f = file_path + "/" + files[idxFLocal].getName();
                LogWrite.Log(appContext, f);
                return f;
            }catch(Exception e)
            {
                LogWrite.Log(appContext, e.getMessage());
                return null;
            }

        }

        private Bitmap getFromModile(String filePath) {
            if(filePath!=null) {
                File imgFile = new File(filePath);
                if (imgFile.exists()) {
                    LogWrite.Log(appContext, "decodeFile " + imgFile.getAbsolutePath());
                    return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                } else {
                    LogWrite.Log(appContext, filePath + " not exists");
                    return null;
                }
            }else return null;
        }
        public Bitmap loadImageFromNetwork(int sn) {
            try {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);

                String urls = "";
                switch (sn) {
                    case 1:
                        //urls = "http://lebalexwebapp.azurewebsites.net/lockscreen/erotic.aspx";
                        if (sp.getBoolean("home_pic", true))
                            urls = "http://lebalex.xyz/lockscreen/lockhome.php";
                        else
                            urls = "http://lebalex.xyz/lockscreen/erotic.php";
                        break;
                    case 2:
                        //urls = "http://lebalexwebapp.azurewebsites.net/lockscreen/rand500.aspx";
                        urls = "http://lebalex.xyz/lockscreen/lock500p.php";
                        break;

                }
                int timeout = Integer.parseInt(sp.getString("timeout", "10"));

                /*URL url = new URL(urls);

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
                JSONObject dataJsonObj = null;*/

                BufferedReader inputStream = null;

                URL jsonUrl = new URL(urls);
                URLConnection dc = jsonUrl.openConnection();
                dc.setConnectTimeout(timeout*1000);
                dc.setReadTimeout(timeout*1000);
                inputStream = new BufferedReader(new InputStreamReader(
                        dc.getInputStream()));

                // read the JSON results into a string
                String resultJson = inputStream.readLine();



                try {
                    JSONArray jsonArray = new JSONArray(resultJson);
                    urlName = jsonArray.getString(0);
                    urls = jsonArray.getString(1);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return getBitMapFromUrl(urls, timeout);
            } catch (Exception eee) {
                errorLoadBmp = eee.getMessage();
                return null;
            }
        }

    }

    private static Bitmap getBitMapFromUrl(String urls, int timeout) {
        try {
            originalUrlTemp = urls;
            /*URL url = new URL(urls);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream imageStream = urlConnection.getInputStream();*/

            URL url = new URL(urls);
            URLConnection dc = url.openConnection();
            dc.setConnectTimeout(timeout*1000);
            dc.setReadTimeout(timeout*1000);
            InputStream imageStream = dc.getInputStream();
            return BitmapFactory.decodeStream(imageStream);
        } catch (Exception e) {
            return null;
        }
    }

    public void finish() {
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.finish();
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ERO";
                case 1:
                    return "INET";
                case 2:
                    return "GOOGLE";
                case 3:
                    return "LOCAL";
            }
            return null;
        }
    }
}

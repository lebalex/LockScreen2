package xyz.lebalex.lockscreen;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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

public class MyStartServiceReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        LogWrite.Log(context, "start MyStartServiceReceiver");
        Intent dailyUpdater = new Intent(context, MyService.class);
        //Intent dailyUpdater = new Intent(context, MyServiceGoole.class);
        context.startService(dailyUpdater);
        //Log.i("AlarmReceiver", "Called context.startService from AlarmReceiver.onReceive");


    }



}

package xyz.lebalex.lockscreen;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;


/**
 * Created by ivc_lebedevav on 12.01.2017.
 */

public class LockScreenServiceReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        LogWrite.Log(context, "start LockScreenServiceReceiver");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calen = Calendar.getInstance();
        int startTime = Integer.parseInt(sp.getString("update_start", "0"));
        if (calen.get(Calendar.HOUR_OF_DAY) >= startTime) {
            Intent dailyUpdater = new Intent(context, LockScreenService.class);
            context.startService(dailyUpdater);
            //Log.i("AlarmReceiver", "Called context.startService from AlarmReceiver.onReceive");
        } else LogWrite.Log(context, "not time Set Wallpaper "+calen.get(Calendar.HOUR_OF_DAY) +" - "+ startTime);


    }



}

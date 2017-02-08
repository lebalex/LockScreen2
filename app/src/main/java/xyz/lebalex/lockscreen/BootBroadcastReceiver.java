package xyz.lebalex.lockscreen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by ivc_lebedevav on 03.02.2017.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context pContext, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(pContext);
        int interval = Integer.parseInt(sp.getString("update_frequency", "60")) * 1000 * 60;
        if (interval > 0) {
            Intent alarmIntent = new Intent(pContext, LockScreenServiceReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(pContext, 1001, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager manager = (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
        }
        LogWrite.Log(pContext, "start by BootBroadcastReceiver with interval = "+interval);
    }

}

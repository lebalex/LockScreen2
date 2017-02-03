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
            Intent alarmIntent = new Intent(pContext, MyStartServiceReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(pContext, 1001, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager manager = (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
        }
        Log(pContext, "start with interval = "+interval);
    }
    private void Log(Context pContext, String str) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(pContext);
        if(sp.getBoolean("save_log", false)) {
            Calendar calen = Calendar.getInstance();
            int c = calen.get(Calendar.DATE);
            String logs = sp.getString("logs", "");
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("logs", logs + calen.get(Calendar.YEAR) + "-" + calen.get(Calendar.MONTH) + "-" + calen.get(Calendar.DATE) + " " + calen.get(Calendar.HOUR_OF_DAY) + ":" +
                    calen.get(Calendar.MINUTE) + ":" + calen.get(Calendar.SECOND) + " " + str + "\n");
            editor.commit();
        }
    }
}

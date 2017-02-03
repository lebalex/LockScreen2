package xyz.lebalex.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by ivc_lebedevav on 12.01.2017.
 */

public class MyStartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean("save_log", false)) {
            Calendar calen = Calendar.getInstance();
            int c = calen.get(Calendar.DATE);
            String logs = sp.getString("logs", "");
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("logs", logs + calen.get(Calendar.YEAR) + "-" + calen.get(Calendar.MONTH) + "-" + calen.get(Calendar.DATE) + " " + calen.get(Calendar.HOUR_OF_DAY) + ":" +
                    calen.get(Calendar.MINUTE) + ":" + calen.get(Calendar.SECOND) + " start MyStartServiceReceiver\n");
            editor.commit();
        }
        Intent dailyUpdater = new Intent(context, MyService.class);
        //Intent dailyUpdater = new Intent(context, MyServiceGoole.class);
        context.startService(dailyUpdater);
        //Log.i("AlarmReceiver", "Called context.startService from AlarmReceiver.onReceive");
    }
}

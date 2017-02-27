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
        int startTime = Integer.parseInt(sp.getString("update_start", "0"));
        if (interval > 0) {
            Intent alarmIntent = new Intent(pContext, LockScreenServiceReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(pContext, 1001, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager manager = (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);

            Calendar curentTime = Calendar.getInstance();

            Calendar startCalen = Calendar.getInstance();
            startCalen.set(Calendar.HOUR_OF_DAY, startTime);
            startCalen.set(Calendar.MINUTE, 5);
            startCalen.set(Calendar.SECOND, 0);
            startCalen.set(Calendar.MILLISECOND, 0);

            boolean find=false;
            while(!find)
            {
                if(curentTime.before(startCalen))
                    find=true;
                else
                    startCalen.add(Calendar.MILLISECOND, interval);
            }


            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
            LogWrite.Log(pContext, "start Boot Alarm "+startCalen.get(Calendar.YEAR)+"-"+startCalen.get(Calendar.MONTH)+"-"+startCalen.get(Calendar.DATE)+" "+startCalen.get(Calendar.HOUR_OF_DAY)+":"+startCalen.get(Calendar.MINUTE)+":"+startCalen.get(Calendar.SECOND));

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("start_service",true);
            editor.commit();
        }
        LogWrite.Log(pContext, "start by BootBroadcastReceiver with interval = "+interval);
    }

}

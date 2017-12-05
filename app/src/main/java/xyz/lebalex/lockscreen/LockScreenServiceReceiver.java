package xyz.lebalex.lockscreen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;


/**
 * Created by ivc_lebedevav on 12.01.2017.
 */

public class LockScreenServiceReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        LogWrite.Log(context, "start LockScreenServiceReceiver");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calen = Calendar.getInstance();
        int startTime = Integer.parseInt(sp.getString("update_start", "0"));
        if (calen.get(Calendar.HOUR_OF_DAY) >= startTime) {
            startBackgroundService(context, Integer.parseInt(sp.getString("update_frequency", "60")) * 1000 * 60, startTime, sp);

            Intent service = new Intent(context, LockScreenService.class);
            startWakefulService(context, service);

            //Log.i("AlarmReceiver", "Called context.startService from AlarmReceiver.onReceive");
        } else LogWrite.Log(context, "not time Set Wallpaper "+calen.get(Calendar.HOUR_OF_DAY) +" - "+ startTime);


    }
    private void startBackgroundService(Context context, int interval, int startTime, SharedPreferences sp) {
        try {
            Intent alarmIntent = new Intent(context, LockScreenServiceReceiver.class);
            alarmIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            PendingIntent pendingIntent;
            pendingIntent = PendingIntent.getBroadcast(context, 1001, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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


                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startCalen.getTimeInMillis(), pendingIntent);
            //manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, interval, pendingIntent);


                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("start_service", true);
                editor.commit();

                LogWrite.Log(context, "start next Alarm " + startCalen.get(Calendar.YEAR) + "-" + startCalen.get(Calendar.MONTH) + "-" + startCalen.get(Calendar.DATE) + " " + startCalen.get(Calendar.HOUR_OF_DAY) + ":" + startCalen.get(Calendar.MINUTE) + ":" + startCalen.get(Calendar.SECOND));
            } else {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("start_service", false);
                editor.commit();
                LogWrite.Log(context, "stop Alarm");
            }
        }catch (Exception e)
        {
            LogWrite.LogError(context, e.getMessage());
        }
    }



}

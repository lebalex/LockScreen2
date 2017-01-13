package xyz.lebalex.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ivc_lebedevav on 12.01.2017.
 */

public class MyStartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent dailyUpdater = new Intent(context, MyService.class);
        //Intent dailyUpdater = new Intent(context, MyServiceGoole.class);
        context.startService(dailyUpdater);
        //Log.i("AlarmReceiver", "Called context.startService from AlarmReceiver.onReceive");
    }
}

package jcuenod.brainrot;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DBHelper db = new DBHelper(context);
        AlarmManager alarmManager = (AlarmManager)(context.getSystemService( MainActivity.ALARM_SERVICE ));
		alarmManager.set(AlarmManager.RTC_WAKEUP, db.getSoonestDueMilliseconds(), MainActivity.getSyncPendingIntent(context));
		db.close();
    }
}
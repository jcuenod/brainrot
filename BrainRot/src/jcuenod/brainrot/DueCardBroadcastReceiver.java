package jcuenod.brainrot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class DueCardBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		PendingIntent pIntent = PendingIntent.getActivity(arg0, 0, 
    			new Intent(arg0, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(MainActivity.FROM_NOTIFICATION, true),
    			0);
    	NotificationManager notificationManager = (NotificationManager) arg0.getSystemService(MainActivity.NOTIFICATION_SERVICE);
    	NotificationCompat.Builder noti = new NotificationCompat.Builder(arg0);
    	noti.setContentTitle("Brain Rot")
    	    .setContentText("Your cards await you (click to do them)...")
    	    .setSmallIcon(R.drawable.brainrot_notification)
    	    .setContentIntent(pIntent)
    	    .build().flags |= Notification.FLAG_AUTO_CANCEL;
    	notificationManager.notify(0, noti.build());
    	
    	if (!MainActivity.isInForeground)
    	{
    		try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(arg0, notification);
                r.play();
            } catch (Exception e) {}
    	}
	}

}

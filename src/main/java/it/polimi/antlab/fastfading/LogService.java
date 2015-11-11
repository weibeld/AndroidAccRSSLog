package it.polimi.antlab.fastfading;

import android.app.Service;
import android.content.Intent;
import android.app.PendingIntent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.app.TaskStackBuilder;

public class LogService extends Service {
  public static boolean running = false;

  final int NOTIFICATION_ID = 1;
  NotificationManager nm;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    running = true;
    setupNofification();

    // Start thread reading accelerometer and RSSI data every 10 ms

    return Service.START_STICKY;
  }

  /* Setup notification in notification bar when service starts */
  private void setupNotification() {
    PendingIntent clickPendingIntent = createNotificationIndent();
    Notification n = new Notification.Builder(this)
        .setContentTitle("Fast Fading")
        .setContentText("Collecting data...")
        .setSmallIcon(R.drawable.notification_icon)
        .setColor(Color.BLUE)
        .setOngoing(true)
        .setContentIntent(clickPendingIntent)
        .build();
    nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(NOTIFICATION_ID, n);
  }

  /* Create PendingIntent to be issued when notification is clicked */
  private PendingIntent createNotificationIndent() {
    Intent clickIntent = new Intent(this, FastFadingActivity.class);
    // Use of the TaskStackBuilder makes sure that clicking the "Back" button
    // in the new activity leads to the home screen (not to another activity)
    TaskStackBuilder tsb = TaskStackBuilder.create(this);
    tsb.addParentStack(FastFadingActivity.class);
    tsb.addNextIntent(clickIntent);
    return tsb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(NOTIFICATION_ID);
    running = false;
  }
}

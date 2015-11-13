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
import java.lang.Thread;
import java.lang.Runnable;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Exception;

public class LogService extends Service {
  public static boolean isRunning = false;

  final int NOTIFICATION_ID = 1;
  private NotificationManager nm;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    isRunning = true;
    addNotification();
    //file = new File(this.getFilesDir(), "test.csv");
    // Start thread executing function logData()
    final File file = new File(intent.getData().getPath());
    Thread loggerThread = new Thread(new Runnable() {
      public void run() {
        logData(file);
      }
    });
    loggerThread.start();
    return Service.START_STICKY;
  }

  private void logData(File file) {
    // Notes on saving files (Nexus 6):
    // External storage: Environment.getExternalStorageDirectory()
    //   --> /storage/emulated/0
    // Internal storage: Context.getFilesDir()
    //   --> /data/user/0/it.polimi.antlab.fastfading/files
    // Save a file in internal storage and send it by email
    writeToFile(file, "Hello world");
  }

  private void writeToFile(File file, String text) {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      out.write(text.getBytes());
      out.close();
    }
    catch (Exception e) {
      return;
    }
  }

  /* Setup notification in notification bar when service starts */
  private void addNotification() {
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
    // Remove notification
    nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(NOTIFICATION_ID);
    isRunning = false;
  }
}

package it.polimi.antlab.fastfading;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.NotificationManager;
import android.content.Context;
import java.lang.Thread;
import java.lang.Runnable;
import android.os.Environment;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import android.telephony.TelephonyManager;
import java.util.List;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

public class LogService extends Service implements SensorEventListener {
  // For application-wide querying if service is running
  public static boolean isRunning = false;
  // Private fields
  private File                file;
  private SensorManager       sm;
  private TelephonyManager    tm;
  public static int           currentSignalStrength;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Initialise fields
    isRunning = true;
    file = new File(intent.getData().getPath());
    sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    currentSignalStrength = Util.getSignalStrength();
    // Start separate thread doing the actual work
    Thread loggerThread = new Thread(new Runnable() {
      public void run() { logData(file); }
    });
    loggerThread.start();
    return Service.START_STICKY;
  }

  private void logData(final File file) {
    // Write CSV header to data file
    Util.append(file, "ts,acc_x,acc_y,acc_z,dbm\n");

    // Set up accelerometer handler to be called every 10 milliseconds
    Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sm.registerListener(this, acc, 10000);  // Microseconds

    // Set up signal strength change listener
    // tm.listen(new PhoneStateListener() {
    //     @Override
    //     public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    //       super.onSignalStrengthsChanged(signalStrength);
    //       currentSignalStrength = Util.getSignalStrength();
    //     }
    //   }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
  }

  /* From interface SensorEventListener. Called relatively reliably in the
   * specified interval (see registerListener()). */
  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      // Read accelerometer values
      float x = event.values[0];
      float y = event.values[1];
      float z = event.values[2];
      // Convert sensor event timestamp to absolute timestamp in milliseconds
      //long ts = ((System.currentTimeMillis() * 1000000) - System.nanoTime() + event.timestamp) / 1000000;
      // Timestamp = milliseconds since phone startup
      long ts = event.timestamp / 1000000;  // event.timestamp is in nanoseconds

      // Write CSV line
      Util.append(file, ts + "," + x + "," + y + "," + z + "," + currentSignalStrength + "\n");
    }
  }

  // public class MySignalStrengthListener extends PhoneStateListener {
  //   @Override
  //   public void onSignalStrengthsChanged(SignalStrength signalStrength) {
  //     super.onSignalStrengthsChanged(signalStrength);
  //     currentSignalStrength = Util.getSignalStrength();
  //   }
  // }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Do something here if sensor accuracy changes.
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  
  @Override
  public void onDestroy() {
    isRunning = false;
    sm.unregisterListener(this);
  }
}

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
  // For application-wide querying whether service is running
  private static boolean isRunning = false;
  // Communication of signal strength to service by MySignalStrengthListener
  private static MySignalStrength currentSignalStrength;
  // Fields
  private File                file;
  private SensorManager       sm;
  private TelephonyManager    tm;
  

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Initialise variables
    isRunning = true;
    currentSignalStrength = Util.getSignalStrength();
    file = new File(intent.getData().getPath());
    sm = (SensorManager)    getSystemService(Context.SENSOR_SERVICE);
    tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

    // Write CSV header to data file
    Util.append(file, "ts,acc_x,acc_y,acc_z,dbm,type\n");

    // Set up accelerometer handler to be called every 10 milliseconds
    Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sm.registerListener(this, acc, 10000);  // Microseconds

    return Service.START_STICKY;
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
      // Milliseconds since phone startup
      long ts = event.timestamp / 1000000;  // event.timestamp is in nanoseconds

      // Write CSV line
      Util.append(file, ts + "," + x + "," + y + "," + z + "," + currentSignalStrength.getDbm() + "," + currentSignalStrength.getType() + "\n");
    }
  }

  /* From interface SensorEventListener. */
  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Not needed, but necessary to implement
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

  // Static getter and setter methods
  public static boolean isRunning() {
    return isRunning;
  }
  public static void setCurrentSignalStrength(MySignalStrength strength) {
    currentSignalStrength = strength;
  }
}

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
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.io.FileUtils;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
//import it.polimi.antlab.fastfading.MyPhoneStateListener;
import android.util.Log;
import java.util.List;

import android.telephony.*;

import android.hardware.*;

public class LogService extends Service implements SensorEventListener {
  public static boolean isRunning = false;
  private File file;

  private NotificationManager nm;
  private SensorManager sm;
  private TelephonyManager tm;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    isRunning = true;
    // Start thread executing function logData()
    file = new File(intent.getData().getPath());
    Thread loggerThread = new Thread(new Runnable() {
      public void run() { logData(file); }
    });
    loggerThread.start();
    return Service.START_STICKY;
  }


  private void logData(final File file) {
    // Notes on saving files (Nexus 6):
    // External storage: Environment.getExternalStorageDirectory()
    //   --> /storage/emulated/0
    // Internal storage: Context.getFilesDir()
    //   --> /data/user/0/it.polimi.antlab.fastfading/files
    // Save a file in internal storage and send it by email
    Util.append(file, "timestamp,timestamp_2,acc_x,acc_y,acc_z,dBm,ASU_level,level\n");
    sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    Log.i(Util.TAG, "getName(): " + acc.getName());
    Log.i(Util.TAG, "getType(): " + acc.getType());
    Log.i(Util.TAG, "getStringType(): " + acc.getStringType());
    Log.i(Util.TAG, "getReportingMode(): " + acc.getReportingMode());
    Log.i(Util.TAG, "getResolution(): " + acc.getResolution());
    Log.i(Util.TAG, "getMaximumRange(): " + acc.getMaximumRange());
    Log.i(Util.TAG, "getPower(): " + acc.getPower());
    Log.i(Util.TAG, "getVendor(): " + acc.getVendor());
    Log.i(Util.TAG, "getVersion(): " + acc.getVersion());
    Log.i(Util.TAG, "getMinDelay(): " + acc.getMinDelay());
    Log.i(Util.TAG, "getMaxDelay(): " + acc.getMaxDelay());
    Log.i(Util.TAG, "isWakeUpSensor(): " + acc.isWakeUpSensor());
    // Results (Nexus 6):
    // getName(): Invensense Accelerometer
    // getType(): 1
    // getStringType(): android.sensor.accelerometer
    // getReportingMode(): 0 (REPORTING_MODE_CONTINUOUS)
    // getResolution(): 0.07846069
    // getMaximumRange(): 39.22661
    // getPower(): 0.3
    // getVendor(): Invensense Inc.
    // getVersion(): 4
    // getMinDelay(): 4444     (min. delay between two measurements in microseconds)
    // getMaxDelay(): 1000000  (max. delay between two measurements in microseconds)
    // isWakeUpSensor(): false
    sm.registerListener(this, acc, 10000);

    tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


    // TimerTask task = new TimerTask() {
    //   @Override
    //   public void run() {
    //     Util.append(file, System.currentTimeMillis() + "," + "\n");
    //   }
    // };
    // Timer timer = new Timer();
    // timer.scheduleAtFixedRate(task, 0, 10);

    // List<CellInfo> allCells = tm.getAllCellInfo();
    // CellSignalStrength strength = null;
    // for (CellInfo cell : allCells) {
    //   if (cell.isRegistered()) {
    //     if (cell instanceof CellInfoGsm)
    //       strength = ((CellInfoLte) cell).getCellSignalStrength();
    //     else if (cell instanceof CellInfoLte)
    //       strength = ((CellInfoLte) cell).getCellSignalStrength();
    //     else if (cell instanceof CellInfoCdma)
    //       strength = ((CellInfoCdma) cell).getCellSignalStrength();
    //     else if (cell instanceof CellInfoWcdma)
    //       strength = ((CellInfoWcdma) cell).getCellSignalStrength();
    //     Log.e(Util.TAG, cell.toString());
    //     Log.e(Util.TAG, "getDbm(): " + strength.getDbm());
    //     Log.e(Util.TAG, "getLevel(): " + strength.getLevel());
    //     Log.e(Util.TAG, "getAsuLevel(): " + strength.getAsuLevel());
    //   }
    // }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Do something here if sensor accuracy changes.
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      float x = event.values[0];
      float y = event.values[1];
      float z = event.values[2];
      // Convert sensor event timestamp to absolute timestamp in milliseconds
      long ts = ((System.currentTimeMillis() * 1000000) - System.nanoTime() + event.timestamp) / 1000000;

      List<CellInfo> allCells = tm.getAllCellInfo();
      CellSignalStrength strength = null;
      int dbm = 0;
      int asu = 0;
      int lev = 0;
      long ts2 = 0;
      for (CellInfo cell : allCells) {
        if (cell.isRegistered()) {
          if (cell instanceof CellInfoGsm)
            strength = ((CellInfoLte) cell).getCellSignalStrength();
          else if (cell instanceof CellInfoLte)
            strength = ((CellInfoLte) cell).getCellSignalStrength();
          else if (cell instanceof CellInfoCdma)
            strength = ((CellInfoCdma) cell).getCellSignalStrength();
          else if (cell instanceof CellInfoWcdma)
            strength = ((CellInfoWcdma) cell).getCellSignalStrength();
          dbm = strength.getDbm();
          asu = strength.getAsuLevel();
          lev = strength.getLevel();
          ts2 = ((System.currentTimeMillis() * 1000000) - System.nanoTime() + cell.getTimeStamp()) / 1000000;
          break;
        }
      }

      Util.append(file, ts + "," + ts2 + "," + x + "," + y + "," + z + "," + dbm + "," + asu + "," + lev + "\n");

      // List<CellInfo> cellInfo = tm.getAllCellInfo();
      // for (CellInfo e : cellInfo) {
      //   Log.e(Util.TAG, e.toString());
      // }
      // Log.e(Util.TAG, "");
    }
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

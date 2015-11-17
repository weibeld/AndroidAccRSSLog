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

import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;

import android.hardware.*;

public class LogService extends Service {
  public static boolean isRunning = false;
  public static File file;

  private NotificationManager nm;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    isRunning = true;

    MyPhoneStateListener psListener = new MyPhoneStateListener();
    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    telephonyManager.listen(psListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    //file = new File(this.getFilesDir(), "test.csv");
    // Start thread executing function logData()
    file = new File(intent.getData().getPath());
    Thread loggerThread = new Thread(new Runnable() {
      public void run() {
        logData(file);
      }
    });
    loggerThread.start();
    return Service.START_STICKY;
  }

  class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            int ss = signalStrength.getGsmSignalStrength();
            String line = System.currentTimeMillis() + "," + ss + "\n";
            Util.append(file, line);
        }

    }

  private void logData(final File file) {
    // Notes on saving files (Nexus 6):
    // External storage: Environment.getExternalStorageDirectory()
    //   --> /storage/emulated/0
    // Internal storage: Context.getFilesDir()
    //   --> /data/user/0/it.polimi.antlab.fastfading/files
    // Save a file in internal storage and send it by email
    //LogService.append(file, "timestamp,acc_x,acc_y,acc_z,rssi");
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        int rssi = 0;
        try {
          final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
          for (final CellInfo info : tm.getAllCellInfo()) {
              if (info instanceof CellInfoGsm) {
                  final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                  rssi = gsm.getDbm();
              } else if (info instanceof CellInfoCdma) {
                  final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                  rssi = cdma.getDbm();
              } else if (info instanceof CellInfoLte) {
                  final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                  rssi = lte.getDbm();
              } else {
                  throw new Exception("Unknown type of cell signal!");
              }
          }
        }
        catch (Exception e) {
          return;
        }
        Util.append(file, System.currentTimeMillis() + "," + rssi + "\n");
      }
    };
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(task, 0, 10);
  }

  


  

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  
  @Override
  public void onDestroy() {
    isRunning = false;
  }
}

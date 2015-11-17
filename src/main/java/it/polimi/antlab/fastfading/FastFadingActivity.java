package it.polimi.antlab.fastfading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ToggleButton;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Context;
import android.os.Environment;
import java.io.File;
import android.media.MediaScannerConnection;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Exception;
import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import android.app.AlertDialog;
import org.apache.commons.io.FileUtils;
import android.util.Log;

public class FastFadingActivity extends Activity {

  private static Context context;

  ToggleButton toggle;
  TextView info;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Set app-widely accessible context variable
    context = this;
    // Set layout
    setContentView(R.layout.main);
    // Layout components
    toggle = (ToggleButton) findViewById(R.id.toggle);
    info   = (TextView)     findViewById(R.id.info);
    // Set up toggling actions of toggle button
    toggle.setChecked(LogService.isRunning);
    toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      File csvFile = null;  // File for saving logged data
      Intent serviceIntent = new Intent();
      public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        // Toggle button OFF -> ON
        if (isChecked) {
          // Directory where to save the generated CSV file
          File dir = getExternalCacheDir();
          // Remove all existing files in directory
          Util.cleanDir(dir);
          // Create empty CSV file
          String csvFilename = dir.getAbsolutePath() + "/FastFading_" + Util.getDate() + ".csv";
          csvFile = new File(csvFilename);
          // Start service
          serviceIntent.setClass(FastFadingActivity.getContext(), LogService.class);
          serviceIntent.setData(Uri.fromFile(csvFile));
          startService(serviceIntent);
          // Add notification in notification bar
          Util.startNotification();
        }
        // Toggle button ON -> OFF
        else {
          if (LogService.isRunning) {
            //try {
              Util.cancelNotification();
              stopService(serviceIntent);
              try { info.setText(FileUtils.readFileToString(csvFile)); }
              catch (Exception e) { Util.dispException(e); }
              File zipFile = Util.createZip(csvFile);
              Util.sendEmail("daniel.weibel@utilnifr.ch", "Fast Fading File", "Your file", zipFile);
          }
        }
      }
    });
  }

  public static Context getContext() {
    return context;
  }
}

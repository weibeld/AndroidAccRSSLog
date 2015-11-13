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

public class FastFadingActivity extends Activity {

  ToggleButton toggle;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    // Toggle Button for starting and stopping LogService
    toggle = (ToggleButton) findViewById(R.id.toggle);
    toggle.setChecked(LogService.isRunning);
    final Context context = this;
    toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      //File dataFile = new File(Environment.getExternalStorageDirectory(), "test.csv");
      File dataFile = null; 
      Intent serviceIntent = new Intent(context, LogService.class);
      public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        if (isChecked) {
          // TODO: remove all existing files in directory
          String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
          dataFile = new File(getExternalCacheDir(), "FastFading_" + date + ".csv");
          serviceIntent.setData(Uri.fromFile(dataFile));
          startService(serviceIntent);
        } 
        else {
          if (LogService.isRunning) {
            stopService(serviceIntent);
            // TODO: Zip file dataFile
            sendEmail("daniel.weibel@unifr.ch", "Fast Fading File", "Your file", dataFile);
          }
        }
      }
    });
  }

  private void sendEmail(String to, String subject, String text, File file) {
    Intent emailIntent = new Intent();
    emailIntent.setAction(Intent.ACTION_SEND);
    emailIntent.setType("text/csv");
    emailIntent.putExtra(Intent.EXTRA_EMAIL,   new String[] {to});
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
    emailIntent.putExtra(Intent.EXTRA_TEXT,    text);
    emailIntent.putExtra(Intent.EXTRA_STREAM,  Uri.fromFile(file));
    if (emailIntent.resolveActivity(getPackageManager()) != null) {
        startActivity(Intent.createChooser(emailIntent, "Send data file by e-mail..."));
    }
  }

  public boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
        return true;
    }
    return false;
  }
  
}

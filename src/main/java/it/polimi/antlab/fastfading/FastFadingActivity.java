package it.polimi.antlab.fastfading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ToggleButton;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.content.Context;
import java.io.File;
import java.lang.Exception;
import android.net.Uri;
import org.apache.commons.io.FileUtils;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.Log;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;

public class FastFadingActivity extends Activity {

  private static Context context;

  ToggleButton toggle;
  TextView info;

  /* Called when app is first started, and when user clicks on notification
   * (in which case LogService is running). */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    // Fields
    context = this;
    toggle  = (ToggleButton) findViewById(R.id.toggle);
    info    = (TextView)     findViewById(R.id.info);
    // Set up toggling actions of toggle button
    toggle.setChecked(LogService.isRunning);
    toggle.setOnCheckedChangeListener(new MyToggleListener());

    
  }

  @Override
  public void onStop() {
    super.onStop();
    info.setText("");
  }

  public static Context getContext() {
    return context;
  }

  public class MyToggleListener implements OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
      // Toggle button OFF ==> ON
      if (isChecked) {
        MyCsvFile.createNewInstance();  // File to write to
        Intent i = new Intent(FastFadingActivity.getContext(), LogService.class);
        i.setData(Uri.fromFile(MyCsvFile.getInstance()));
        //TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //tm.listen(new LogService.MySignalStrengthListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        startService(i);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Shelf.ssListener = new MySignalStrengthListener();
        tm.listen(Shelf.ssListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Util.startNotification();
      }
      // Toggle button ON ==> OFF
      else {
        if (LogService.isRunning) {
          Util.cancelNotification();
          TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
          tm.listen(Shelf.ssListener, PhoneStateListener.LISTEN_NONE);
          Intent i = new Intent(FastFadingActivity.getContext(), LogService.class);
          stopService(i);
          File csvFile = MyCsvFile.getInstance();
          info.setText(Util.readFile(csvFile));
          File zipFile = Util.createZip(csvFile);
          Util.sendEmail("daniel.weibel@unifr.ch", "Fast Fading File", "Your file", zipFile);
        }
      }
    }
  }
}

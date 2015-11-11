package it.polimi.antlab.fastfading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ToggleButton;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Context;

public class FastFadingActivity extends Activity {

  ToggleButton toggle;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set up toggle button
    toggle = (ToggleButton) findViewById(R.id.toggle);
    toggle.setChecked(LogService.running);

    final Context context = this;
    toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      Intent serviceIntent = new Intent(context, LogService.class);
      public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        if (isChecked)
          startService(serviceIntent);
        else
          stopService(serviceIntent);
      }
    });

  }
  
}

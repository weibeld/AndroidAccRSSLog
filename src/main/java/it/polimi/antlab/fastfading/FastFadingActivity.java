package it.polimi.antlab.fastfading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ToggleButton;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FastFadingActivity extends Activity {

  ToggleButton toggle;
  TextView status;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set up toggle button
    toggle = (ToggleButton) findViewById(R.id.toggle);
    status = (TextView) findViewById(R.id.status);

    toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        if (isChecked) {
          status.setText("On");
        }
        else {
          status.setText("Off");
        }
      }
    });

  }

  
}

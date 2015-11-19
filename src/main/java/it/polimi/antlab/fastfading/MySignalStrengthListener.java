package it.polimi.antlab.fastfading;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

public class MySignalStrengthListener extends PhoneStateListener {
	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
	  LogService.currentSignalStrength = Util.getSignalStrength();
	  Log.e(Util.TAG, "SignalStrengthsChanged listener");
	}
}
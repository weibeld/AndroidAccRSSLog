package it.polimi.antlab.fastfading;

import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.Exception;
import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import android.app.AlertDialog;
import org.apache.commons.io.FileUtils;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.app.TaskStackBuilder;
import android.util.Log;
import android.hardware.Sensor;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import java.util.List;
import android.telephony.TelephonyManager;


public class Util {
  // Tag for log output (used in Log.i(), Log.e(), etc.)
	public static String TAG = "FastFading";
  // ID for the notification in the notification bar
	private final static int NOTIFICATION_ID = 1;

  // Return string of current date
	public static String getDate() {
		return new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
	}

  // Zip passed file and return Zip file with same name as input file with
  // extension replaced by .zip
	public static File createZip(File inFile) {
	  // Filename strings
	  String inFilename  = inFile.getAbsolutePath();
	  String outFilename = inFilename.substring(0, inFilename.lastIndexOf(".")) + ".zip";
	  String inBasename  = inFilename.substring(inFilename.lastIndexOf("/") + 1);

		try {
		  // In and out streams
		  FileInputStream in  = new FileInputStream(inFilename); 
		  ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));

		  // Name of file in zip archive
		  out.putNextEntry(new ZipEntry(inBasename)); 

		  // Buffer with data for feeding to out stream
		  byte[] buffer = new byte[2048];
		  int count;

		  // Read chunks of input stream into buffer, and from there to out stream
		  while ((count = in.read(buffer)) > 0) {  // count: number of bytes
		      out.write(buffer, 0, count);
		  }
		  out.close();
		  in.close();
		}
		catch (Exception e) {
		  //dispException(e);
		}
		return new File(outFilename);
	}

  public static void handleException(Exception e) {
    // Handle exception
  }

  // Open an app chooser for sending an e-mail with a file in the attachment
	public static void sendEmail(String to, String subject, String text, File file) {
		Context context = FastFadingActivity.getContext();
		Intent emailIntent = new Intent();
		emailIntent.setAction(Intent.ACTION_SEND);
		emailIntent.setType("text/csv");
		emailIntent.putExtra(Intent.EXTRA_EMAIL,   new String[] {to});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT,    text);
		emailIntent.putExtra(Intent.EXTRA_STREAM,  Uri.fromFile(file));
		if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
		  context.startActivity(Intent.createChooser(emailIntent, "Send file by e-mail..."));
		}
	}

  public static MySignalStrength getSignalStrength() {
    Context context = FastFadingActivity.getContext();
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    List<CellInfo> allCells = tm.getAllCellInfo();
    MySignalStrength result = new MySignalStrength();
    for (CellInfo cell : allCells) {
      if (cell.isRegistered()) {
        if (cell instanceof CellInfoGsm) {
          result.setType("gsm");
          result.setDbm(((CellInfoGsm) cell).getCellSignalStrength().getDbm());
        }
        else if (cell instanceof CellInfoLte) {
          result.setType("lte");
          result.setDbm(((CellInfoLte) cell).getCellSignalStrength().getDbm());
        }
        else if (cell instanceof CellInfoCdma) {
          result.setType("cdma");
          result.setDbm(((CellInfoCdma) cell).getCellSignalStrength().getDbm());
        }
        else if (cell instanceof CellInfoWcdma) {
          result.setType("wcdma");
          result.setDbm(((CellInfoWcdma) cell).getCellSignalStrength().getDbm());
        }
        else {
          result.setType("unknown");
          result.setDbm(0);
        }
        break;
      }
    }
    return result;
  }

  // Append a string to an existing file
	public static void append(File file, String text) {
		// Notes on saving files (Nexus 6):
    // External storage: Environment.getExternalStorageDirectory()
    //   --> /storage/emulated/0
    // Internal storage: Context.getFilesDir()
    //   --> /data/user/0/it.polimi.antlab.fastfading/files
    try { FileUtils.writeStringToFile(file, text, true); }
    catch (Exception e) { handleException(e); }
  }

  // Read content of a file to a string
  public static String readFile(File file) {
    String text = null;
  	try { text = FileUtils.readFileToString(file);  }
    catch (Exception e) { handleException(e); }
    return text;
  }

  // Truncate string to a specified number of bytes (characters)
  public static String truncate(String string, int bytes) {
    int length = string.length();
    if (length <= bytes)
      return string;
    else
      return string.substring(0, bytes) + "\n... (" + (length - bytes) + " bytes more)";
  }

  // For debugging: write some information about a sensor to the log
  public static void logSensorInfo(Sensor s) {
  	Log.i(TAG, "getName(): " + s.getName());
    Log.i(TAG, "getType(): " + s.getType());
    Log.i(TAG, "getStringType(): " + s.getStringType());
    Log.i(TAG, "getReportingMode(): " + s.getReportingMode());
    Log.i(TAG, "getResolution(): " + s.getResolution());
    Log.i(TAG, "getMaximumRange(): " + s.getMaximumRange());
    Log.i(TAG, "getPower(): " + s.getPower());
    Log.i(TAG, "getVendor(): " + s.getVendor());
    Log.i(TAG, "getVersion(): " + s.getVersion());
    Log.i(TAG, "getMinDelay(): " + s.getMinDelay());
    Log.i(TAG, "getMaxDelay(): " + s.getMaxDelay());
    Log.i(TAG, "isWakeUpSensor(): " + s.isWakeUpSensor());
    // Results for accelerometer of Nexus 6:
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
  }

  // Setup notification signalising that data collection is running
  public static void startNotification() {
  	Context context = FastFadingActivity.getContext();
    PendingIntent clickPendingIntent = Util.createNotificationIntent();
    Notification n = new Notification.Builder(context)
        .setContentTitle("Fast Fading")
        .setContentText("Collecting data...")
        .setSmallIcon(R.drawable.notification_icon)
        .setColor(Color.BLUE)
        .setOngoing(true)
        .setContentIntent(clickPendingIntent)
        .build();
    NotificationManager nm = (NotificationManager)
    	context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(NOTIFICATION_ID, n);
  }

  // Helper function of startNotification(). Intent when notification is clicked
  private static PendingIntent createNotificationIntent() {
  	Context context = FastFadingActivity.getContext();
    Intent clickIntent = new Intent(context, FastFadingActivity.class);
    // Use of the TaskStackBuilder makes sure that clicking the "Back" button
    // in the new activity leads to the home screen (not to another activity)
    TaskStackBuilder tsb = TaskStackBuilder.create(context);
    tsb.addParentStack(FastFadingActivity.class);
    tsb.addNextIntent(clickIntent);
    return tsb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  // Remove notification from notification bar
  public static void cancelNotification() {
  	Context context = FastFadingActivity.getContext();
  	NotificationManager nm = (NotificationManager)
  		context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(NOTIFICATION_ID);
  }

  // Test if external storage is writable (should be always the case)
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    return true;
		}
		return false;
	}

}

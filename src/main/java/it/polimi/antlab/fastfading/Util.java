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
import android.app.PendingIntent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.app.TaskStackBuilder;


public class Util {

	public static String TAG = "FastFading";

	private final static int NOTIFICATION_ID = 1;

	public static String getDate() {
		return new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
	}

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

	public static void dispException(Exception e) {
		Context context = FastFadingActivity.getContext();
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("MyException Occured");
		dialog.setMessage(e.getMessage());
		dialog.setNeutralButton("Cool", null);
		dialog.create().show();
	}

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

	public static void append(File file, String text) {
    try 						    { FileUtils.writeStringToFile(file, text, true); }
    catch (Exception e) { dispException(e); }
  }

  public static void cleanDir(File dir) {
  	try   				      { FileUtils.cleanDirectory(dir);  }
    catch (Exception e) { Util.dispException(e); }
  }

  /* Setup notification in notification bar when service starts */
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
    nm.notify(Util.NOTIFICATION_ID, n);
  }

  /* Create PendingIntent to be issued when notification is clicked */
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

  public static void cancelNotification() {
  	Context context = FastFadingActivity.getContext();
  	NotificationManager nm = (NotificationManager)
  		context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(Util.NOTIFICATION_ID);
  }

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    return true;
		}
		return false;
	}

}
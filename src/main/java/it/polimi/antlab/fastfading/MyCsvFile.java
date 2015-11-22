package it.polimi.antlab.fastfading;

import java.io.File;
import android.content.Context;

/* Single instance of the CSV file that the current data recording session is
 * writing to. Used only by FastFadingActivity. */
public class MyCsvFile {
	private static File instance = null;

	private MyCsvFile() {}

	public static void createNewInstance() {
		deleteInstance();
		Context context = FastFadingActivity.getContext();
		File dir        = context.getExternalCacheDir();
		String filename = "FastFading_" + Util.getDate() + ".csv";
    instance = new File(dir, filename);
	}

	public static File getInstance() {
		return instance;
	}

	private static void deleteInstance() {
		if (instance != null) {
			instance.delete();
			instance = null;
		}
	}
}
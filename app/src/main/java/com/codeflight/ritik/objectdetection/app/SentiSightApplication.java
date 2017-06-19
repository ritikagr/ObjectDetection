package com.codeflight.ritik.objectdetection.app;

import android.app.Application;
import android.util.Log;

import com.codeflight.ritik.objectdetection.app.database.DBAdapter;

public final class SentiSightApplication extends Application {

	private static final String TAG = SentiSightApplication.class.getSimpleName();

	public static final String APP_NAME = "sentisight";

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			System.setProperty("jna.nounpack", "true");
			System.setProperty("java.io.tmpdir", getCacheDir().getAbsolutePath());
			DBAdapter.init(this);
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}
	}
}

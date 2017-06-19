package com.codeflight.ritik.objectdetection.app.licensing;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.neurotec.licensing.NLicense;
import com.neurotec.licensing.gui.LicensingPreferencesFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class LicensingManager {

	public interface LicensingStateCallback {
		void onLicensingStateChanged(LicensingState state);
	}

	private static final String TAG = LicensingManager.class.getSimpleName();

	private static LicensingManager sInstance;

	public static final String LICENSE_SENTISIGHT = "SentiSight";

	public static synchronized LicensingManager getInstance() {
		if (sInstance == null) {
			sInstance = new LicensingManager();
		}
		return sInstance;
	}

	public static boolean isActivated(String license) {
		if (license == null) throw new NullPointerException("license");
		try {
			return NLicense.isComponentActivated(license);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			return false;
		}
	}

	public static boolean isSentiSightActivated() {
		return isActivated(LICENSE_SENTISIGHT);
	}

	private List<String> mComponents;

	private LicensingManager() {
		mComponents = new ArrayList<String>();
	}

	public void obtain(Context context, LicensingStateCallback callback, List<String> components) {
		if (context == null) throw new NullPointerException("context");
		obtain(callback, components, LicensingPreferencesFragment.getServerAddress(context), LicensingPreferencesFragment.getServerPort(context));
	}

	public void obtain(final LicensingStateCallback callback, final List<String> components, final String address, final int port) {
		if (callback == null) throw new NullPointerException("callback");
		new AsyncTask<Boolean, Boolean, Boolean>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				callback.onLicensingStateChanged(LicensingState.OBTAINING);
			}
			@Override
			protected Boolean doInBackground(Boolean... params) {
				try {
					return obtain(components, address, port);
				} catch (Exception e) {
					Log.e(TAG, "Exception", e);
					return false;
				}
			}
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				callback.onLicensingStateChanged(result ? LicensingState.OBTAINED : LicensingState.NOT_OBTAINED);
			}
		}.execute();
	}

	public boolean obtain(List<String> components, String address, int port) throws IOException {
		if (components == null) throw new NullPointerException("components");
		if (components.isEmpty()) throw new IllegalArgumentException("List of components is empty");

		Log.i(TAG, String.format("Obtaining licenses from server %s:%s", address, port));

		boolean result = false;
		mComponents.addAll(components);
		for (String component : components) {
			boolean available = false;
			available = NLicense.obtainComponents(address, port, component);
			result |= available;
			Log.i(TAG, String.format("Obtaining '%s' license %s.", component, available ? "succeeded" : "failed"));
		}
		return result;
	}

	public void release(List<String> components) {
		try {
			if (components != null && !components.isEmpty()) {
				Log.i(TAG, "Releasing licenses: " + components);
				NLicense.releaseComponents(components.toString().replace("[", "").replace("]", "").replace(" ", ""));
				mComponents.removeAll(components);
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}
	}

}

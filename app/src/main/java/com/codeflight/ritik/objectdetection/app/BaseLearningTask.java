package com.codeflight.ritik.objectdetection.app;

import android.os.AsyncTask;

public abstract class BaseLearningTask extends AsyncTask<Void, Void, LearningResult> {

	public interface StatusCallback {
		void onLearningStart();
		void onLearningFinish(LearningResult result);
	}

	private StatusCallback mCallback;

	public void setCallback(StatusCallback callback) {
		this.mCallback = callback;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mCallback != null) {
			mCallback.onLearningStart();
		}
	}

	@Override
	protected void onPostExecute(LearningResult result) {
		super.onPostExecute(result);
		if (mCallback != null) {
			mCallback.onLearningFinish(result);
			mCallback = null;
		}
	}
}

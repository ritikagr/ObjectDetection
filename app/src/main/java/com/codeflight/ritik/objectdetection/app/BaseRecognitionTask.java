package com.codeflight.ritik.objectdetection.app;

import android.os.AsyncTask;
import android.util.Log;

import com.codeflight.ritik.objectdetection.app.database.ModelRecord;
import com.neurotec.sentisight.SEEngine;
import com.neurotec.sentisight.SEModel;
import com.neurotec.sentisight.SEStatus;

public abstract class BaseRecognitionTask extends AsyncTask<Void, Void, RecognitionResult> {

	private static final String TAG = BaseRecognitionTask.class.getSimpleName();

	private StatusCallback mCallback;

	public interface StatusCallback {
		void onRecognitionStart();
		void onRecognitionFinish(RecognitionResult result);
	}

	protected boolean addModelsToEngine(SEEngine engine, ModelRecord[] models) {
		SEStatus status;

		engine.getRecognition().removeAllModels();
		for (ModelRecord model : models) {
			if (isCancelled()) {
				return false;
			}
			SEModel loadedModel  = engine.createModel();
			loadedModel.load(model.getModel());
			if (loadedModel.isGeneralized()) {
				status = engine.getRecognition().addModel(loadedModel, new Long(model.getId()));

				if (status != SEStatus.SUCCEEDED) {
					Log.i(TAG, "Model adding failed. Status not succeeded");
					return false;
				}
			}
		}
		Log.i(TAG, "Models successfully added");
		return true;
	}

	public void setCallback(StatusCallback callback) {
		this.mCallback = callback;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mCallback != null) {
			mCallback.onRecognitionStart();
		}
	}

	@Override
	protected void onPostExecute(RecognitionResult result) {
		super.onPostExecute(result);
		if (mCallback != null) {
			mCallback.onRecognitionFinish(result);
			mCallback = null;
		}
	}

}

package com.codeflight.ritik.objectdetection.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.codeflight.ritik.objectdetection.app.database.DBAdapter.ModelCollection;

public final class RecognitionTaskManager {

	private static RecognitionTaskManager sInstance;
	private static final String TAG = RecognitionTaskManager.class.getSimpleName();

	public static RecognitionTaskManager getInstance() {
		synchronized (RecognitionTaskManager.class) {
			if (sInstance == null) {
				sInstance = new RecognitionTaskManager();
			}
			return sInstance;
		}
	}

	private BaseRecognitionTask mRecognitionTask;
	private BaseRecognitionTask.StatusCallback mRecognitionCallback;

	private RecognitionTaskManager() {
	}

	private void startRecognitionTask(BaseRecognitionTask recognitionTask) {
		if (mRecognitionTask != null) {
			switch (mRecognitionTask.getStatus()) {
			case PENDING:
				mRecognitionTask.cancel(true);
				mRecognitionTask = recognitionTask;
				recognitionTask.execute();
				break;
			case RUNNING:
				Log.i(TAG, "Failed to start task: current task in progress");
				break;
			case FINISHED:
				mRecognitionTask = recognitionTask;
				recognitionTask.execute();
				break;
			default:
				throw new AssertionError("Unreachable");
			}
		} else {
			mRecognitionTask = recognitionTask;
			recognitionTask.execute();
		}
	}

	public void setRecognitionCallback(BaseRecognitionTask.StatusCallback callback) {
		this.mRecognitionCallback = callback;
	}

	public void recognise(Context context, Bitmap image, ModelCollection collection) {
		EngineManager.getInstance().updateEngine(context);
		startRecognitionTask(new BitmapRecognitionTask(EngineManager.getInstance().getEngine(), image, collection, mRecognitionCallback));
	}

}

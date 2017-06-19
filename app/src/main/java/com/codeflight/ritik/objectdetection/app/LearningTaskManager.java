package com.codeflight.ritik.objectdetection.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.neurotec.io.NBuffer;
import com.neurotec.sentisight.SEShape;

public final class LearningTaskManager {

	private static LearningTaskManager sInstance;
	private static final String TAG = LearningTaskManager.class.getSimpleName();

	public static LearningTaskManager getInstance() {
		synchronized (LearningTaskManager.class) {
			if (sInstance == null) {
				sInstance = new LearningTaskManager();
			}
			return sInstance;
		}
	}

	private BaseLearningTask mLearningTask;
	private BaseLearningTask.StatusCallback mLearningCallback;

	private LearningTaskManager() {
	}

	private void startLearningTask(BaseLearningTask learningTask) {
		if (mLearningTask != null) {
			switch (mLearningTask.getStatus()) {
			case PENDING:
				mLearningTask.cancel(true);
				mLearningTask = learningTask;
				mLearningTask.execute();
				break;
			case RUNNING:
				Log.i(TAG, "Failed to start task: current task in progress");
				break;
			case FINISHED:
				mLearningTask = learningTask;
				learningTask.execute();
				break;
			default:
				throw new AssertionError("Unreachable");
			}
		} else {
			mLearningTask = learningTask;
			learningTask.execute();
		}
	}

	public void cancelLearningTask() {
		if (mLearningTask != null) {
			mLearningTask.cancel(true);
		}
	}

	public void setLearningCallback(BaseLearningTask.StatusCallback callback) {
		this.mLearningCallback = callback;
	}

//	public void learn(Context context, Uri uri, SEShape[] shapes, ByteBuffer model) {
//		startLearningTask(new UriLearningTask(context, EngineManager.getInstance().getEngine(), uri, shapes, model, mLearningCallback));
//	}
//
//	public void learn(Context context, Uri uri, SEShape[] shapes) {
//		startLearningTask(new UriLearningTask(context, uri, EngineManager.getInstance().getEngine(), shapes, mLearningCallback));
//	}

	public void learn(Context context, Bitmap image, SEShape[] shapes) {
		EngineManager.getInstance().updateEngine(context);
		startLearningTask(new BitmapLearningTask(EngineManager.getInstance().getEngine(), image, shapes, mLearningCallback));
	}

	public void learn(Context context, Bitmap image, SEShape[] shapes, NBuffer model) {
		EngineManager.getInstance().updateEngine(context);
		startLearningTask(new BitmapLearningTask(EngineManager.getInstance().getEngine(), image, shapes, model, mLearningCallback));
	}

}

package com.codeflight.ritik.objectdetection.app;

import android.content.Context;

import com.codeflight.ritik.objectdetection.app.preference.LearningPreferences;
import com.codeflight.ritik.objectdetection.app.preference.RecognitionPreferences;
import com.neurotec.sentisight.SEEngine;
import com.neurotec.sentisight.SELrnMode;

public final class EngineManager {

	private static EngineManager sInstance = null;

	public static final SELrnMode DEFAULT_LEARNING_MODE = SELrnMode.LOW_PROFILE;
	public static final boolean DEFAULT_USE_AUTO_RECOGNISE = true;

	private SEEngine mEngine = null;
	private boolean mUseAutoRecognise;

	private EngineManager() {
		mEngine = new SEEngine();
		mEngine.getLearning().setMode(DEFAULT_LEARNING_MODE);
		mUseAutoRecognise = DEFAULT_USE_AUTO_RECOGNISE;
	}

	public static EngineManager getInstance() {
		synchronized (EngineManager.class) {
			if (sInstance == null) {
				sInstance = new EngineManager();
			}
			return sInstance;
		}
	}

	public SEEngine getEngine() {
		return mEngine;
	}

	public void updateEngine(Context context) {
		LearningPreferences.updateEngine(mEngine, context);
		RecognitionPreferences.updateEngine(mEngine, context);
		mUseAutoRecognise = RecognitionPreferences.isUseAutoRecognize(context);
	}

	public boolean isUseAutoRecognise() {
		return mUseAutoRecognise;
	}

}

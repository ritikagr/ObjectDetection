package com.codeflight.ritik.objectdetection.app;

import android.graphics.Bitmap;
import android.util.Log;

import com.codeflight.ritik.objectdetection.app.database.DBAdapter;
import com.neurotec.images.NImage;
import com.neurotec.sentisight.SEEngine;
import com.neurotec.sentisight.SEEngine.SERecognition.RecognitionDetailsCollection;

public class BitmapRecognitionTask extends BaseRecognitionTask {

	private static final String TAG = BitmapRecognitionTask.class.getSimpleName();

	private SEEngine mEngine;
	private Bitmap mImage;
	private DBAdapter.ModelCollection mModelCollection;

	public BitmapRecognitionTask(SEEngine engine, Bitmap image, DBAdapter.ModelCollection collection, StatusCallback callback) {
		this(engine, image, collection);
		this.setCallback(callback);
	}

	public BitmapRecognitionTask(SEEngine engine, Bitmap image, DBAdapter.ModelCollection collection) {
		if (engine == null) throw new NullPointerException("engine");
		if (image == null) throw new NullPointerException("image");
		if (collection == null) throw new NullPointerException("collection");
		this.mEngine = engine;
		this.mImage = image;
		this.mModelCollection = collection;
	}

	@Override
	protected RecognitionResult doInBackground(Void... params) {
		boolean recognized;
		NImage image = null;
		try {
			image = NImage.fromBitmap(mImage);
			if (addModelsToEngine(mEngine, mModelCollection.toArray())) {
				recognized = mEngine.getRecognition().recognize(image);
				if (recognized && (mEngine.getRecognition().getRecognitionDetails().size() > 0)) {
					Log.i(TAG, "Recognition succeeded");
					return new RecognitionResult(RecognitionResult.ResultStatus.SUCCEEDED, mEngine.getRecognition().getRecognitionDetails());
				}
			}
		} catch (Exception e) {
			return new RecognitionResult(RecognitionResult.ResultStatus.EXCEPTION_OCCURED, e);
		} finally {
			if (image != null) {
				image.dispose();
			}
		}

		Log.i(TAG, "Recognition failed");
		return new RecognitionResult(RecognitionResult.ResultStatus.FAILED, (RecognitionDetailsCollection) null);
	}

}

package com.codeflight.ritik.objectdetection.app;

import android.graphics.Bitmap;
import android.util.Log;

import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import com.neurotec.sentisight.SEEngine;
import com.neurotec.sentisight.SEEngine.AddResult;
import com.neurotec.sentisight.SEModel;
import com.neurotec.sentisight.SEShape;
import com.neurotec.sentisight.SEStatus;

public class BitmapLearningTask extends BaseLearningTask {

	private static final String TAG = BitmapLearningTask.class.getSimpleName();

	private final SEEngine mEngine;
	private final SEModel mModel;
	private final SEShape[] mShapes;
	private final Bitmap mImage;

	public BitmapLearningTask(SEEngine engine, Bitmap image, SEShape[] shapes, StatusCallback callback) {
		this(engine, image, shapes);
		setCallback(callback);
	}

	public BitmapLearningTask(SEEngine engine, Bitmap image, SEShape[] shapes) {
		this(engine, image, shapes, (NBuffer) null);
	}

	public BitmapLearningTask(SEEngine engine, Bitmap image, SEShape[] shapes, NBuffer model, StatusCallback callback) {
		this(engine, image, shapes, model);
		setCallback(callback);
	}

	public BitmapLearningTask(SEEngine engine, Bitmap image, SEShape[] shapes, NBuffer model) {
		if (engine == null) throw new NullPointerException("engine");
		if (image == null) throw new NullPointerException("image");
		if (shapes == null) throw new NullPointerException("shapes");
		if (shapes.length == 0) throw new IllegalArgumentException("empty shape collection");

		mEngine = engine;
		mImage = image;
		mShapes = shapes;

		mModel = mEngine.createModel();
		if (model != null) {
			mModel.load(model);
		}
	}

	@Override
	protected LearningResult doInBackground(Void... params) {
		SEStatus status;
		AddResult[] results;
		NImage image = null;
		try {
			image = NImage.fromBitmap(mImage);
			results = mEngine.getLearning().addToModel(mModel, image, null, mShapes);
			for (AddResult result : results) {
				if (result.getStatus() != SEStatus.SUCCEEDED) {
					Log.i(TAG, String.format("Learning failed. Result status: %s", result.getStatus()));
					return new LearningResult(LearningResult.ResultStatus.FAILED, result.getStatus().name());
				}
			}
			Log.i(TAG, "Learning succeeded");
			Log.i(TAG, "Performing model generalization");

			if (isCancelled()) {
				Log.i(TAG, "Canceled learning process");
				return new LearningResult(LearningResult.ResultStatus.FAILED, "Canceled by user");
			}

			status = mEngine.getLearning().generalizeModel(mModel);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return new LearningResult(LearningResult.ResultStatus.EXCEPTION_OCCURED, e);
		} finally {
			if (image != null) {
				image.dispose();
			}
		}

		if (status == SEStatus.SUCCEEDED) {
			Log.i(TAG, "Model generalization succeeded");
			return new LearningResult(LearningResult.ResultStatus.SUCCEEDED, mModel, mModel.getQuality(results[0].getRefId()));
		} else {
			Log.i(TAG, "Model generalization failed");
			return new LearningResult(LearningResult.ResultStatus.FAILED, "Generalization failed");
		}
	}

}

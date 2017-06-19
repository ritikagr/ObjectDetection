package com.codeflight.ritik.objectdetection.app.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.view.BasePreferenceFragment;
import com.neurotec.sentisight.SEEngine;
import com.neurotec.sentisight.SEFeatureType;
import com.neurotec.sentisight.SERecSpeed;
import com.neurotec.sentisight.SERecTransformType;

public final class RecognitionPreferences extends AppCompatActivity {

	public static final String RECOGNITION_FEATURE_TYPE = "recognition_feature_type";
	public static final String RECOGNITION_SHAPE_SCALING_LEVEL = "recognition_shape_scaling_level";
	public static final String RECOGNITION_IMAGE_RESCALE_FACTOR = "recognition_image_rescale_factor";
	public static final String RECOGNITION_MATCHING_THRESHOLD = "recognition_matching_threshold";
	public static final String RECOGNITION_SPEED = "recognition_speed";
	public static final String RECOGNITION_TRANSFORM_TYPE = "recognition_transform_type";
	public static final String RECOGNITION_USE_AUTO_RECOGNISE = "recognition_use_auto_recognise";
	public static final String RECOGNITION_USE_COLOR_INFORMATION = "recognition_use_color_information";
	public static final String SET_DEFAULT_RECOGNITION_PREFERENCES = "set_default_recognition_preferences";

	public static void updateEngine(SEEngine engine, Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		engine.getRecognition().setFeatureType(SEFeatureType.get(PreferenceTools.getIntFromString(preferences, RECOGNITION_FEATURE_TYPE, SEFeatureType.BLOB.getValue())));
		engine.getRecognition().setShapeScalingLevel(PreferenceTools.getInt(preferences, RECOGNITION_SHAPE_SCALING_LEVEL, 0));
		engine.getRecognition().setImageRescaleFactor(PreferenceTools.getDoubleFromString(preferences, RECOGNITION_IMAGE_RESCALE_FACTOR, 1.0));
		engine.getRecognition().setThreshold(PreferenceTools.getInt(preferences, RECOGNITION_MATCHING_THRESHOLD, 100000));
		engine.getRecognition().setSpeed(SERecSpeed.get(PreferenceTools.getIntFromString(preferences, RECOGNITION_SPEED, SERecSpeed.LOW.getValue())));
		engine.getRecognition().setTransformType(SERecTransformType.get(PreferenceTools.getIntFromString(preferences, RECOGNITION_TRANSFORM_TYPE, SERecTransformType.AUTO.getValue())));
		engine.getRecognition().setUseColorInformation(PreferenceTools.getBoolean(preferences, RECOGNITION_USE_COLOR_INFORMATION, true));
	}

	public static boolean isUseAutoRecognize(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return PreferenceTools.getBoolean(preferences, RECOGNITION_USE_AUTO_RECOGNISE, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new RecognitionPreferencesFragment()).commit();
	}

	@SuppressLint("ValidFragment")
	private class RecognitionPreferencesFragment extends BasePreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.recognition_preferences);
		}

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			if (preference.getKey().equals(SET_DEFAULT_RECOGNITION_PREFERENCES)) {
				preferenceScreen.getEditor().clear().commit();
				getFragmentManager().beginTransaction().replace(android.R.id.content, new RecognitionPreferencesFragment()).commit();
			}
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
	}


}

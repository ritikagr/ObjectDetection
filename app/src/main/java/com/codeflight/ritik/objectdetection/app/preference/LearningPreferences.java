package com.codeflight.ritik.objectdetection.app.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.view.BasePreferenceFragment;
import com.neurotec.sentisight.SEEngine;
import com.neurotec.sentisight.SEFeatureType;
import com.neurotec.sentisight.SELrnMode;

public final class LearningPreferences extends AppCompatActivity {

	public static final String LEARNING_ENABLE_MASK_ENHANCEMENTS = "learning_enable_mask_enhancements";
	public static final String LEARNING_MODE = "learning_mode";
	public static final String LEARNING_FEATURE_TYPE = "learning_feature_type";
	public static final String LEARNING_SHAPE_SCALING_LEVEL = "learning_shape_scaling_level";
	public static final String LEARNING_IMAGE_RESCALE_FACTOR = "learning_image_rescale_factor";
	public static final String SET_DEFAULT_LEARNING_PREFERENCES = "set_default_learning_preferences";
	public static final String LEARNING_USE_COLOR_INFORMATION = "learning_use_color_information";

	public static void updateEngine(SEEngine engine, Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		engine.getLearning().setEnhanceMask(PreferenceTools.getBoolean(preferences, LEARNING_ENABLE_MASK_ENHANCEMENTS, true));
		engine.getLearning().setMode(SELrnMode.get(PreferenceTools.getIntFromString(preferences, LEARNING_MODE, SELrnMode.LOW_PROFILE.getValue())));
		engine.getLearning().setFeatureType(SEFeatureType.get(PreferenceTools.getIntFromString(preferences, LEARNING_FEATURE_TYPE, SEFeatureType.BLOB.getValue())));
		engine.getLearning().setShapeScalingLevel(PreferenceTools.getInt(preferences, LEARNING_SHAPE_SCALING_LEVEL, 0));
		engine.getLearning().setImageRescaleFactor(PreferenceTools.getDoubleFromString(preferences, LEARNING_IMAGE_RESCALE_FACTOR, 1.0));
		engine.getLearning().setUseColorInformation(PreferenceTools.getBoolean(preferences, LEARNING_USE_COLOR_INFORMATION, true));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new LearningPreferencesFragment()).commit();
	}


	@SuppressLint("ValidFragment")
    private class LearningPreferencesFragment extends BasePreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
            Log.i("TAG", "Problem");
			addPreferencesFromResource(R.xml.learning_preferences);
		}

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			if (preference.getKey().equals(SET_DEFAULT_LEARNING_PREFERENCES)) {
				preferenceScreen.getEditor().clear().commit();
				getFragmentManager().beginTransaction().replace(android.R.id.content, new LearningPreferencesFragment()).commit();
			}
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
	}

}

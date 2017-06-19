package com.codeflight.ritik.objectdetection.app.view;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class BaseDialogFragment extends DialogFragment {

	@Override
	public void show(FragmentManager manager, String tag) {
		FragmentTransaction ft = manager.beginTransaction();
		ft.add(this, tag);
		ft.commitAllowingStateLoss();
	}
}
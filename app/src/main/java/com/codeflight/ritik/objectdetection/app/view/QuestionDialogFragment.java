package com.codeflight.ritik.objectdetection.app.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.codeflight.ritik.objectdetection.app.R;


public class QuestionDialogFragment extends BaseDialogFragment {

	public interface QuestionDialogListener {
		void onQuestionAnswered(boolean accepted);
	}

	private static final String EXTRA_MESSAGE = "message";
	private static final String EXTRA_TITLE = "title";

	public static QuestionDialogFragment newInstance(String message) {
		return newInstance(null, message);
	}

	public static QuestionDialogFragment newInstance(String title, String message) {
		QuestionDialogFragment frag = new QuestionDialogFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_MESSAGE, message);
		if (title != null) {
			args.putString(EXTRA_TITLE, title);
		}
		frag.setArguments(args);
		return frag;
	}

	private QuestionDialogListener mListener;

	@SuppressLint("ValidFragment")
	private QuestionDialogFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (QuestionDialogListener) getTargetFragment();
			if (mListener == null) {
				mListener = (QuestionDialogListener) activity;
			}
		} catch (ClassCastException e) {
			throw new ClassCastException("Calling fragment must implement QuestionDialogListener interface or set target fragment");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString(EXTRA_MESSAGE);
		String title = getArguments().getString(EXTRA_TITLE);
		return new AlertDialog.Builder(getActivity())
			.setMessage(message)
			.setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					mListener.onQuestionAnswered(true);
					dialog.cancel();
				}
			})
			.setNegativeButton(R.string.msg_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					mListener.onQuestionAnswered(false);
					dialog.cancel();
				}
			}).setTitle(title).create();
	}

}
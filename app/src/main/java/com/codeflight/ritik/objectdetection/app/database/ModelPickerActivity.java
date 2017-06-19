package com.codeflight.ritik.objectdetection.app.database;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.view.BaseDialogFragment;
import com.codeflight.ritik.objectdetection.app.view.ErrorDialogFragment;

public class ModelPickerActivity extends DBViewActivity implements OnItemClickListener, OnClickListener {

	private interface AddModelListener {
		void addModel(String name);
	}

	public static final long RESULT_ADD_NEW_MODEL = -1;
	public static final String EXTRA_SELECTED_MODEL = ModelPickerActivity.class.getPackage().getName() + ".extra_selected_model";
	public static final String EXTRA_MODEL_NAME = ModelPickerActivity.class.getPackage().getName() + ".extra_model_name";

	private AddModelListener mAddModelListener = new AddModelListener() {

		@Override
		public void addModel(String name) {
			if (DBAdapter.getInstance().hasName(name)) {
				ErrorDialogFragment.newInstance(getString(R.string.msg_error), getString(R.string.msg_db_name_exists), false).show(getFragmentManager(), "error");
			} else {
				Intent result = new Intent();
				result.putExtra(EXTRA_SELECTED_MODEL, RESULT_ADD_NEW_MODEL);
				result.putExtra(EXTRA_MODEL_NAME, name);
				setResult(RESULT_OK, result);
				finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View addNewModelLayout = findViewById(R.id.db_add_new);
		addNewModelLayout.setVisibility(View.VISIBLE);
		addNewModelLayout.setOnClickListener(this);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent result = new Intent();
		result.putExtra(EXTRA_SELECTED_MODEL, id);
		setResult(RESULT_OK, result);
		finish();
	}

	@Override
	public void onClick(View view) {
		new AddModelDialogFragment(mAddModelListener).show(getFragmentManager(), "add_model");
	}

	private class AddModelDialogFragment extends BaseDialogFragment {

		private AddModelListener mListener;

		private AddModelDialogFragment(AddModelListener listener) {
			this.mListener = listener;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Dialog dialog = new Dialog(ModelPickerActivity.this);
			dialog.setContentView(R.layout.edit_database_element);
			dialog.setTitle(R.string.label_model_name);
			dialog.setCancelable(true);

			final EditText fieldName = (EditText) dialog.findViewById(R.id.edit_text_record_name);
			fieldName.setImeOptions(EditorInfo.IME_ACTION_DONE);

			Button buttonOk = (Button) dialog.findViewById(R.id.button_ok);
			buttonOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String name = fieldName.getText().toString();
					mListener.addModel(name);
					dialog.dismiss();
				}
			});
			Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
			buttonCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			return dialog;
		}

	}

}

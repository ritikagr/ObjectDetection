package com.codeflight.ritik.objectdetection.app.database;

import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.view.BaseDialogFragment;
import com.codeflight.ritik.objectdetection.app.view.ErrorDialogFragment;
import com.codeflight.ritik.objectdetection.app.view.QuestionDialogFragment;
import com.codeflight.ritik.objectdetection.app.view.QuestionDialogFragment.QuestionDialogListener;

public class DBEditorActivity extends DBViewActivity implements QuestionDialogListener {

	private interface RenameModelListener {
		void renameModel(long rowId, String name);
	}

	private static final String TAG = DBEditorActivity.class.getSimpleName();
	public static final String STATE_SELECTED_POSITION = DBEditorActivity.class.getPackage().getName() + ".position";

	private int mSelectedPosition;
	private RenameModelListener mRenameModelListener = new RenameModelListener() {

		@Override
		public void renameModel(long rowId, String name) {
			try {
				DBAdapter.getInstance().renameModel(rowId, name);
			} catch (SQLiteException e) {
				Log.e(TAG, e.toString());
				ErrorDialogFragment.newInstance(getString(R.string.msg_db_access_error), false).show(getFragmentManager(), "Error");
			}
			refreshList();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findViewById(R.id.db_add_new).setVisibility(View.GONE);
		mListView.setOnItemClickListener(this);
		registerForContextMenu(mListView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		if (view.getId() == R.id.db_entries) {
			mSelectedPosition = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
			getMenuInflater().inflate(R.menu.db_view_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_delete) {
			QuestionDialogFragment.newInstance(getString(R.string.msg_db_entry_delete_question), getString(R.string.label_delete)).show(getFragmentManager(), "delete");
			return true;
		} else if (item.getItemId() == R.id.menu_item_rename) {
			new RenameDialogFragment(mRenameModelListener).show(getFragmentManager(), "rename");
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, ModelViewActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(STATE_SELECTED_POSITION, position);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public void onQuestionAnswered(boolean accepted) {
		if (accepted) {
			try {
				DBAdapter.getInstance().deleteModel(DBAdapter.getInstance().getModels().get(mSelectedPosition).getId());
			} catch (SQLiteException e) {
				Log.e(TAG, e.toString());
				ErrorDialogFragment.newInstance(getString(R.string.msg_db_access_error), false).show(getFragmentManager(), "Error");
			}
			refreshList();
		}
	}

	private class RenameDialogFragment extends BaseDialogFragment {

		private RenameModelListener mRenameListener;

		private RenameDialogFragment(RenameModelListener listener) {
			mRenameListener = listener;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Dialog dialog = new Dialog(DBEditorActivity.this);
			dialog.setContentView(R.layout.edit_database_element);
			dialog.setTitle(R.string.label_rename);
			dialog.setCancelable(true);

			final EditText fieldName = (EditText) dialog.findViewById(R.id.edit_text_record_name);

			Button buttonOk = (Button) dialog.findViewById(R.id.button_ok);
			buttonOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String name = fieldName.getText().toString();
					ModelRecord model = DBAdapter.getInstance().getModels().get(mSelectedPosition);
					if (!model.getName().equals(name)) {
						mRenameListener.renameModel(model.getId(), name);
					}
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

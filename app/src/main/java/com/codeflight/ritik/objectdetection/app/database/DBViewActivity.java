package com.codeflight.ritik.objectdetection.app.database;

import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.database.DBAdapter.ModelCollection;
import com.codeflight.ritik.objectdetection.app.view.ErrorDialogFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class DBViewActivity extends AppCompatActivity implements OnItemClickListener {

	private static final String TAG = DBViewActivity.class.getSimpleName();
	public static final String EXTRA_ERROR_MESSAGE = ModelPickerActivity.class.getPackage().getName() + ".extra_error_message";

	private TextView mHeader;

	protected ListView mListView;

	private String[] makeList() throws SQLException {
		List<String> names = new ArrayList<String>();
		ModelCollection models = DBAdapter.getInstance().getModels();
		for (ModelRecord r : models) {
			names.add(r.getName());
		}
		return names.toArray(new String[names.size()]);
	}

	protected void refreshList() {
		String[] names = null;
		try {
			names = makeList();
			if ((names == null) || (names.length == 0)) {
				mHeader.setText(R.string.msg_db_empty);
			}
			if (mListView.getAdapter() != null) {
				((ModelAdapter) mListView.getAdapter()).refreshItemList();
				((ModelAdapter) mListView.getAdapter()).notifyDataSetChanged();
			} else {
				mListView.setAdapter(new ModelAdapter(this));
			}
		} catch (SQLException e) {
			Log.e(TAG, "", e);
			ErrorDialogFragment.newInstance(getString(R.string.msg_db_access_error), true).show(getFragmentManager(), "Error");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database);
		mHeader = (TextView) findViewById(R.id.header);
		mListView = (ListView) findViewById(R.id.db_entries);
		mListView.setTextFilterEnabled(true);
		DBAdapter.getInstance().open();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DBAdapter.getInstance().close();
	}

	@Override
	protected void onStart() {
		super.onStart();
		refreshList();
	}
}

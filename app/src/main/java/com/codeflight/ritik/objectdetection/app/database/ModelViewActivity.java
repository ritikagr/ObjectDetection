package com.codeflight.ritik.objectdetection.app.database;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.GridView;

import com.codeflight.ritik.objectdetection.app.R;
import com.neurotec.io.NBuffer;
import com.codeflight.ritik.objectdetection.app.view.ErrorDialogFragment;
import com.codeflight.ritik.objectdetection.app.view.QuestionDialogFragment;
import com.codeflight.ritik.objectdetection.app.view.QuestionDialogFragment.QuestionDialogListener;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ModelViewActivity extends AppCompatActivity implements QuestionDialogListener {

	private static final String TAG = ModelViewActivity.class.getSimpleName();

	private GridView mGridview;
	private ModelRecord mModel;
	private Map<Integer, Bitmap> mImages;
	private ImageAdapter mAdapter;
	private ProgressDialog mProgressDialog;

	private void refreshImages() {
		mImages.clear();
		Iterator<Entry<Integer, NBuffer>> it = mModel.getImages().entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, NBuffer> entry = it.next();
			int refId = entry.getKey();
			byte[] imageBytes = entry.getValue().toByteArray();
			mImages.put(refId, BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
		}
		mAdapter = new ImageAdapter(this, mImages);
		mGridview.setAdapter(mAdapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_view);
		mGridview = (GridView) findViewById(R.id.model_images_gridview);
		registerForContextMenu(mGridview);
		mImages = new TreeMap<Integer, Bitmap>();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mModel = DBAdapter.getInstance().getModels().get(getIntent().getExtras().getInt(DBEditorActivity.STATE_SELECTED_POSITION));
		refreshImages();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if (view.getId() == R.id.model_images_gridview) {
			super.onCreateContextMenu(menu, view, menuInfo);
			getMenuInflater().inflate(R.menu.model_view_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_delete) {
			if (mImages.size() == 1) {
				QuestionDialogFragment.newInstance(getString(R.string.label_delete), getString(R.string.msg_db_deleting_image_will_delete_model_question)).show(getFragmentManager(), "delete");
			} else {
				long newRowId = DBAdapter.getInstance().deleteImage(mModel.getId(), (int) mAdapter.getItemId((int) ((AdapterContextMenuInfo) item.getMenuInfo()).position));
				mModel = DBAdapter.getInstance().getModels().getById(newRowId);
			}
			refreshImages();
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onQuestionAnswered(boolean accepted) {
		if (accepted) {
			try {
				DBAdapter.getInstance().deleteModel(mModel.getId());
				finish();
			} catch (SQLiteException e) {
				Log.e(TAG, "", e);
				ErrorDialogFragment.newInstance(getString(R.string.msg_db_access_error), false);
			}
		}
	}
}

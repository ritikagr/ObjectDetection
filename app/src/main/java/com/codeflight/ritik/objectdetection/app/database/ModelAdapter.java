package com.codeflight.ritik.objectdetection.app.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.database.DBAdapter.ModelCollection;

public class ModelAdapter extends BaseAdapter {
	private final Context mContext;
	private ModelCollection mModels;

	public ModelAdapter(Context context) {
		super();
		this.mContext = context;
		mModels = DBAdapter.getInstance().getModels();
	}

	@Override
	public int getCount() {
		return mModels.size();
	}

	public void refreshItemList() {
		mModels = DBAdapter.getInstance().getModels();
	}

	@Override
	public Object getItem(int position) {
		return mModels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mModels.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.db_view_row, null);
		}
		TextView nameView = (TextView) convertView.findViewById(R.id.db_view_name);
		TextView sizeView = (TextView) convertView.findViewById(R.id.db_view_images_number);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.db_view_image);
		ModelRecord model = mModels.get(position);
		nameView.setText(mContext.getString(R.string.label_model_name) + " " + model.getName());
		sizeView.setText(mContext.getString(R.string.label_model_images_number) + " " + model.getImages().size());
		if (model.getImages().isEmpty()) {
			imageView.setImageBitmap(null);
		} else {
			byte[] imageBytes = model.getImages().entrySet().iterator().next().getValue().toByteArray();
			Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
			imageView.setImageBitmap(image);
		}
		return convertView;
	}

}

package com.codeflight.ritik.objectdetection.app.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ImageAdapter extends BaseAdapter {
	private final Context mContext;
	private final Map<Integer, Bitmap> mImages;

	public ImageAdapter(Context c, Map<Integer, Bitmap> images) {
		super();
		mContext = c;
		mImages = images;
	}

	private Entry<Integer, Bitmap> getEntry(int position) {
		Iterator<Entry<Integer, Bitmap>> it = mImages.entrySet().iterator();
		for (int i = 0; i < position; i++) {
			it.next();
		}
		return it.next();
	}

	@Override
	public int getCount() {
		return mImages.size();
	}

	@Override
	public Object getItem(int position) {
		return mImages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getEntry(position).getKey();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			int dimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130, mContext.getResources().getDisplayMetrics());
			imageView.setLayoutParams(new GridView.LayoutParams(dimension, dimension));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}
		Entry<Integer, Bitmap> entry = getEntry(position);
		imageView.setImageBitmap(entry.getValue());
		//imageView.setId(entry.getKey());
		return imageView;
	}

}

package com.codeflight.ritik.objectdetection.app.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class BitmapUtils {

	private static final String TAG = BitmapUtils.class.getSimpleName();
	private static final int DEFAULT_WIDTH = 1280;
	private static final int DEFAULT_HEIGHT = 760;
	private static final String ANDROID_ASSET_DESCRIPTOR = "file:///android_asset/";

	private BitmapUtils() {
	}

	private static Bitmap bitmapFromUri(Context context, Uri uri) throws IOException {
		if (context == null) throw new NullPointerException("context");
		if (uri == null) throw new NullPointerException("uri");

		InputStream is = null;
		try {
			is = context.getContentResolver().openInputStream(uri);
			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, o);
			int scale = 1;
			int h = (int) Math.ceil(o.outHeight / (float) DEFAULT_HEIGHT);
			int w = (int) Math.ceil(o.outWidth / (float) DEFAULT_WIDTH);

			if (h > 1 || w > 1) {
				if (h > w) {
					scale = h;

				} else {
					scale = w;
				}
			}
			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			//TODO: close() might cause IOException exception
			is.close();
			is = context.getContentResolver().openInputStream(uri);
			return BitmapFactory.decodeStream(is, null, o2);
		} catch (FileNotFoundException e) {
			throw new IOException("File not found");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, "Error closing InputStream", e);
				}
			}
		}
	}

	private static Bitmap bitmapFromUrl(Context context, String url) throws IOException {
		if (context == null) throw new NullPointerException("context");
		if (url == null) throw new NullPointerException("uri");
		AssetManager assetManager = context.getAssets();

		InputStream istr = null;
		Bitmap bitmap = null;
		try {
			istr = assetManager.open(url);
			bitmap = BitmapFactory.decodeStream(istr);
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
		} finally {
			if (istr != null) {
				istr.close();
			}
		}
		return bitmap;
	}

	public static Bitmap fromUri(Context context, Uri uri) throws IOException {
		if (context == null) throw new NullPointerException("context");
		if (uri == null) throw new NullPointerException("uri");
		if (uri.toString().contains(ANDROID_ASSET_DESCRIPTOR)) {
			return bitmapFromUrl(context, uri.toString().replace(ANDROID_ASSET_DESCRIPTOR, ""));
		} else {
			return bitmapFromUri(context, uri);
		}
	}
}

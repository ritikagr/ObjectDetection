package com.codeflight.ritik.objectdetection.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.neurotec.io.NBuffer;
import com.neurotec.sentisight.SEEngine;
import com.neurotec.sentisight.SEModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class DBAdapter {

	// ===========================================================
	// Private static field
	// ===========================================================

	private static final String TAG = DBAdapter.class.getSimpleName();
	private static DBAdapter sInstance;

	private static final String DATABASE_NAME = "samples_database";
	private static final String KEY_ROWID = "_id";

	private static final String TABLE_MODELS = "models";
	private static final String KEY_NAME = "name";
	private static final String KEY_MODEL = "model";

	private static final String TABLE_IMAGES = "images";
	private static final String KEY_REFID = "ref_id";
	private static final String KEY_MODELID = "model_id";
	private static final String KEY_IMAGE = "image";

	private static final int DATABASE_VERSION = 1;

	// images.model_id -> models._id
	private static final String CREATE_MODELS_TABLE
			= String.format("create table %1$s (\"%2$s\" integer primary key autoincrement, \"%3$s\" text unique not null, \"%4$s\" blob not null); ",
							TABLE_MODELS, KEY_ROWID, KEY_NAME, KEY_MODEL);
	private static final String CREATE_IMAGES_TABLE
			= String.format("create table %1$s (\"%2$s\" integer primary key autoincrement, \"%3$s\" integer, \"%4$s\" integer, \"%5$s\" blob not null, unique (\"%3$s\", \"%4$s\") on conflict rollback); ",
							TABLE_IMAGES, KEY_ROWID, KEY_REFID, KEY_MODELID, KEY_IMAGE);

	// ===========================================================
	// Private fields
	// ===========================================================

	private ModelCollection mModels;
	private final DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	// ===========================================================
	// Private constructor
	// ===========================================================

	private DBAdapter(Context ctx) {
		mDbHelper = new DatabaseHelper(ctx);
		mModels = getModels();
	}

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static synchronized void init(Context ctx) {
		if (sInstance == null) {
			sInstance = new DBAdapter(ctx);
		}
	}

	public static DBAdapter getInstance() {
		return sInstance;
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private List<ModelRecord> getAllModels() {
		Cursor c = null;
		List<ModelRecord> list = new ArrayList<ModelRecord>();
		try {
			c = mDb.query(TABLE_MODELS, new String[] {KEY_ROWID, KEY_NAME, KEY_MODEL}, null, null, null, null, null);
			if (c.moveToFirst()) {
				do {
					long id = c.getLong(c.getColumnIndex(KEY_ROWID));
					NBuffer model = NBuffer.fromArray(c.getBlob(c.getColumnIndex(KEY_MODEL)));
					String name = c.getString(c.getColumnIndex(KEY_NAME));
					list.add(new ModelRecord(id, name, model, getAllImages(id)));
				} while (c.moveToNext());
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return list;
	}

	private Map<Integer, NBuffer> getAllImages(long model) {
		Cursor c =  null;
		Map<Integer, NBuffer> images = new HashMap<Integer, NBuffer>();
		try {
			c = mDb.query(TABLE_IMAGES, new String[] {KEY_REFID, KEY_IMAGE}, KEY_MODELID + "=\"" + model + "\"", null, null, null, null);
			if (c.moveToFirst()) {
				do {
					int refId = c.getInt(c.getColumnIndex(KEY_REFID));
					NBuffer image = NBuffer.fromArray(c.getBlob(c.getColumnIndex(KEY_IMAGE)));
					images.put(refId, image);
				} while (c.moveToNext());
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return images;
	}

	public void printAllImages() {
		Cursor c =  null;
		try {
			c = mDb.query(TABLE_IMAGES, new String[] {KEY_ROWID, KEY_REFID, KEY_MODELID, KEY_IMAGE}, null, null, null, null, null);
			if (c.moveToFirst()) {
				do {
					long rowId = c.getLong(c.getColumnIndex(KEY_ROWID));
					int refId = c.getInt(c.getColumnIndex(KEY_REFID));
					int modelId = c.getInt(c.getColumnIndex(KEY_MODELID));
					byte image = c.getBlob(c.getColumnIndex(KEY_IMAGE))[0];
					Log.i("V", rowId + " -> " + refId + " -> " + modelId + " -> " + image);
				} while (c.moveToNext());
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public DBAdapter open() {
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long insertModel(String name, NBuffer model, Map<Integer, NBuffer> images) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		if (model == null) {
			throw new NullPointerException("template");
		}
		if (images == null) {
			throw new NullPointerException("images");
		}

		long modelId;
		mDb.beginTransaction();

		try {

			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, name);
			initialValues.put(KEY_MODEL, model.toByteArray());
			modelId = mDb.insert(TABLE_MODELS, null, initialValues);
			if (modelId < 0) {
				throw new SQLException("model insert failed");
			}

			Iterator<Entry<Integer, NBuffer>> it = images.entrySet().iterator();
			ContentValues imageValues = new ContentValues();
			while (it.hasNext()) {
				imageValues.clear();
				Entry<Integer, NBuffer> image = it.next();
				imageValues.put(KEY_REFID, image.getKey());
				imageValues.put(KEY_MODELID, modelId);
				imageValues.put(KEY_IMAGE, image.getValue().toByteArray());
				long imageId = mDb.insert(TABLE_IMAGES, null, imageValues);
				if (imageId < 0) {
					throw new SQLException("image insert failed");
				}
			}

			mModels.add(modelId, name, model, images);
			mDb.setTransactionSuccessful();

		} finally {
			mDb.endTransaction();
		}

		return modelId;
	}

	public boolean deleteModel(long rowId) {
		mDb.beginTransaction();
		try {
			if (mDb.delete(TABLE_MODELS, KEY_ROWID + "=" + rowId, null) > 0) {
				mModels.deleteById(rowId);
				mDb.delete(TABLE_IMAGES, KEY_MODELID + "=" + rowId, null);
				mDb.setTransactionSuccessful();
				return true;
			} else {
				return false;
			}
		} finally {
			mDb.endTransaction();
		}
	}

	public boolean deleteModelByName(String name) {
		Cursor c = null;
		long id;
		try {
			c = mDb.query(true, TABLE_MODELS, new String[] {KEY_ROWID}, KEY_NAME + "=\"" + name + "\"", null, null, null, null, null);
			if ((c != null) && c.moveToFirst()) {
				id = c.getLong(c.getColumnIndex(KEY_ROWID));
				return deleteModel(id);
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return false;
	}

	public long deleteImage(long model, int refId) {
		Cursor c = null;
		SEEngine engine = null;
		SEModel seModel = null;

		try {
			mDb.beginTransaction();
			c = mDb.query(TABLE_MODELS, new String[] {KEY_MODEL}, KEY_ROWID + "=" + model, null, null, null, null, null);
			if ((c != null) && c.moveToFirst()) {
				NBuffer bb = NBuffer.fromArray(c.getBlob(c.getColumnIndex(KEY_MODEL)));
				engine = new SEEngine();
				seModel = engine.createModel();
				seModel.load(bb);
				engine.getLearning().removeFromModel(seModel, refId);
				ModelRecord record = getModels().getById(model);
				record.getImages().remove(refId);
				deleteModel(model);
				long newRowId = insertModel(record.getName(), seModel.save(), record.getImages());
				mDb.setTransactionSuccessful();
				return newRowId;
			}
		} finally {
			mDb.endTransaction();
			if (c != null) {
				c.close();
			}
			if (engine != null) {
				engine.dispose();
			}
			if (seModel != null) {
				seModel.dispose();
			}
		}
		return -1;
	}

	public NBuffer getModel(long rowId) {
		Cursor c = null;
		try {
			c = mDb.query(true, TABLE_MODELS, new String[] {KEY_MODEL}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
			if ((c != null) && c.moveToFirst()) {
				return NBuffer.fromArray(c.getBlob(c.getColumnIndex(KEY_MODEL)));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	public NBuffer getModelByName(String name) {
		Cursor c = null;
		try {
			c = mDb.query(true, TABLE_MODELS, new String[] {KEY_MODEL}, KEY_NAME + "=\"" + name + "\"", null, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				return NBuffer.fromArray(c.getBlob(c.getColumnIndex(KEY_MODEL)));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	public boolean renameModel(long rowId, String name) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		if (mDb.update(TABLE_MODELS, args, KEY_ROWID + "=" + rowId, null) > 0) {
			mModels.rename(rowId, name);
			return true;
		} else {
			return false;
		}
	}

	public boolean renameModelByName(String name) {
		Cursor c = null;
		long id;
		try {
			c = mDb.query(true, TABLE_MODELS, new String[] {KEY_ROWID}, KEY_NAME + "=\"" + name + "\"", null, null, null, null, null);
			if ((c != null) && c.moveToFirst()) {
				id = c.getLong(c.getColumnIndex(KEY_ROWID));
				return renameModel(id, name);
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return false;
	}

	public boolean hasName(String name) {
		Cursor c = null;
		try {
			c = mDb.query(true, TABLE_MODELS, new String[] {KEY_ROWID}, KEY_NAME + "=\"" + name + "\"", null, null, null, null, null);
			if ((c != null) && c.moveToFirst()) {
				return true;
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return false;
	}

	public void clear() {
		mDb.rawQuery("DELETE FROM " + TABLE_MODELS, null);
		mDb.rawQuery("DELETE FROM " + TABLE_IMAGES, null);
		mModels.clear();
	}

	public ModelCollection getModels() {
		if (mModels == null) {
			mModels = new ModelCollection();
			try {
				mDb = mDbHelper.getWritableDatabase();
				mModels.setmModels(getAllModels());
			} finally {
				mDbHelper.close();
			}
		}
		return mModels;
	}

	public ModelCollection getModelsByIds(ArrayList<String> modelNameList,ModelCollection modelCol)
    {
        ModelCollection modelCollection = new ModelCollection();

        if(modelCollection!=null)
            Log.i("DB", String.valueOf(modelCollection));
        for (String model : modelNameList)
        {
            Log.i("Model", String.valueOf(modelCol.getByName(model)));
            modelCollection.add(modelCol.getByName(model));
        }
        return modelCollection;
    }

	// ===========================================================
	// Private inner classes
	// ===========================================================

	private static final class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_MODELS_TABLE);
			db.execSQL(CREATE_IMAGES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, String.format("Upgrading database from version %s to %s, which will destroy all old data", oldVersion, newVersion));
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_MODELS));
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_IMAGES));
			onCreate(db);
		}
	}

	public static final class ModelCollection implements Iterable<ModelRecord> {

		private List<ModelRecord> mModels;

		private ModelCollection() {
            mModels = new ArrayList<>();
		}

		private void add(long id, String name, NBuffer model, Map<Integer, NBuffer> images) {
			mModels.add(new ModelRecord(id, name, model, images));
		}

		private void add(ModelRecord modelRecord)
		{
			mModels.add(modelRecord);
		}
		private void clear() {
			mModels.clear();
		}

		private void deleteById(long id) {
			mModels.remove(getById(id));
		}

		private void rename(long id, String name) {
			ModelRecord record = getById(id);
			record.setName(name);
		}

		@Override
		public Iterator<ModelRecord> iterator() {
			return new Iterator<ModelRecord>() {
				private int current = 0;

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public ModelRecord next() {
					if (!mModels.isEmpty()) {
						return mModels.get(current++);
					}
					return null;
				}

				@Override
				public boolean hasNext() {
					return (!mModels.isEmpty()) && (current < mModels.size());
				}
			};
		}

		public int size() {
			return mModels.size();
		}

		public ModelRecord get(int i) {
			return mModels.get(i);
		}

		public ModelRecord getById(long id) {
			for (ModelRecord record : this.mModels) {
				if (record.getId() == id) {
					return record;
				}
			}
			return null;
		}

		public ModelRecord getByName(String name) {
			for (ModelRecord record : this.mModels) {
				if (record.getName().equals(name)) {
					return record;
				}
			}
			return null;
		}

		public ModelRecord[] toArray() {
			return mModels.toArray(new ModelRecord[mModels.size()]);
		}

		public void setmModels(List<ModelRecord> mModels) {
			this.mModels = mModels;
		}
	}
}

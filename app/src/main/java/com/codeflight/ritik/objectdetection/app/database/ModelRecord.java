package com.codeflight.ritik.objectdetection.app.database;

import com.neurotec.io.NBuffer;

import java.util.HashMap;
import java.util.Map;

public final class ModelRecord {
	private final long mId;
	private String mName;
	private NBuffer mModel;
	private Map<Integer, NBuffer> mImages;

	ModelRecord(long id, String name, NBuffer model, Map<Integer, NBuffer> images) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		if (model == null) {
			throw new NullPointerException("template");
		}
		if (images == null) {
			throw new NullPointerException("images");
		}

		mId = id;
		mName = name;
		mModel = model;
		mImages = new HashMap<Integer, NBuffer>();
		mImages.putAll(images);
	}

	public long getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String newName) {
		mName = newName;
	}

	public NBuffer getModel() {
		return mModel;
	}

	public void setModel(NBuffer template) {
		this.mModel = template;
	}

	public Map<Integer, NBuffer> getImages() {
		return mImages;
	}

	public void setImages(Map<Integer, NBuffer> images) {
		this.mImages = images;
	}

	public void addImage(NBuffer image) {
		int refId = mImages.size();
		while (mImages.containsKey(refId)) {
			refId++;
		}
		mImages.put(refId, image);
	}

	@Override
	public String toString() {
		return mName;
	}
}
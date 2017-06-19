package com.codeflight.ritik.objectdetection.app;

import com.neurotec.sentisight.SEModel;

public class LearningResult {

	public enum ResultStatus {
		SUCCEEDED,
		FAILED,
		EXCEPTION_OCCURED;
	}

	private ResultStatus mStatus;
	private String mStatusMessage;
	private SEModel mSEModel;
	private int mQuality;
	private Exception mException;

	public LearningResult(ResultStatus status, String statusMessage) {
		this(status, statusMessage, null, -1, null);
	}

	public LearningResult(ResultStatus status, SEModel model, int quality) {
		this(status, null, model, quality, null);
	}

	public LearningResult(ResultStatus status, Exception exception) {
		this(status, null, null, -1, exception);
	}

	public LearningResult(ResultStatus status, String statusMessage, SEModel model, int quality, Exception exception) {
		if (status == null) throw new NullPointerException("status");
		mStatus = status;
		mStatusMessage = statusMessage;
		mSEModel = model;
		mQuality = quality;
		mException = exception;
	}

	public ResultStatus getStatus() {
		return mStatus;
	}

	public void setStatus(ResultStatus status) {
		this.mStatus = status;
	}

	public String getStatusMessage() {
		return mStatusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.mStatusMessage = statusMessage;
	}

	public SEModel getSEModel() {
		return mSEModel;
	}

	public void setSEModel(SEModel SEModel) {
		this.mSEModel = SEModel;
	}

	public Exception getException() {
		return mException;
	}

	public void setException(Exception exception) {
		this.mException = exception;
	}

	public int getQuality() {
		return mQuality;
	}

	public void setQuality(int quality) {
		this.mQuality = quality;
	}

}

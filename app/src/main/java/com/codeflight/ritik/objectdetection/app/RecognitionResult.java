package com.codeflight.ritik.objectdetection.app;

import com.neurotec.sentisight.SEEngine.SERecognition.RecognitionDetailsCollection;

public class RecognitionResult {

	public enum ResultStatus {
		SUCCEEDED,
		FAILED,
		EXCEPTION_OCCURED;
	}

	private ResultStatus mStatus;
	private RecognitionDetailsCollection mRecognitionDetails;
	private Exception mException;

	public RecognitionResult(ResultStatus status, RecognitionDetailsCollection recognitionDetails) {
		this.mStatus = status;
		this.mRecognitionDetails = recognitionDetails;
	}

	public RecognitionResult(ResultStatus status, Exception exception) {
		this.mStatus = status;
		this.mException = exception;
	}

	public ResultStatus getStatus() {
		return mStatus;
	}
	public RecognitionDetailsCollection getRecognitionDetails() {
		return mRecognitionDetails;
	}
	public Exception getException() {
		return mException;
	}
	public void setStatus(ResultStatus mStatus) {
		this.mStatus = mStatus;
	}
	public void setRecognitionDetails(RecognitionDetailsCollection mRecognitionDetails) {
		this.mRecognitionDetails = mRecognitionDetails;
	}
	public void setException(Exception mException) {
		this.mException = mException;
	}

}

package com.codeflight.ritik.objectdetection.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.codeflight.ritik.objectdetection.app.R;
import com.neurotec.graphics.PointD;

import java.util.ArrayList;
import java.util.List;

public class ShapeView extends View {

	private Paint mPaint;
	private float mScale;
	private float mXOffset;
	private float mYOffset;

	private Bitmap mImage;
	private ArrayList<List<PointD>> mShapes;

	public ShapeView(Context context) {
		this(context, null);
	}

	public ShapeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ShapeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode()) {
			mScale = 1.0f;
			mShapes = new ArrayList<List<PointD>>();
			setBackgroundColor(Color.WHITE); // not necessary

			mPaint = new Paint();
			mPaint.setColor(getResources().getColor(R.color.draw_view_foreground));
			mPaint.setAntiAlias(true);
		}
	}

	private void updateOffest() {
		if (mImage != null) {
			mXOffset = (getWidth() - (mImage.getWidth() * mScale))/2;
			Log.i("XOffset", String.valueOf(mXOffset));
			mYOffset = (getHeight() - (mImage.getHeight() * mScale))/2;
			Log.i("YOffset", String.valueOf(mYOffset));
		} else {
			mXOffset = 0;
			mYOffset = 0;
		}
	}

	private void updateScale() {
		if (mImage != null) {
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
			mScale = Math.min((float) getWidth() / mImage.getWidth(),
                    ((float) getHeight() - actionBarHeight) / mImage.getHeight());
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (!isInEditMode() && mImage != null) {
			updateScale();
			updateOffest();
		}
	}

	protected Paint getPaint() {
		return mPaint;
	}

	protected float getScale() {
		return mScale;
	}

	protected Matrix getCanvasTransformMatrix() {
		Matrix m = getTransformMatrix();

        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        Log.i("ActionBarSize", String.valueOf(actionBarHeight));
		m.postTranslate(getLeft(), getTop() + actionBarHeight);
		return m;
	}

	protected Matrix getTransformMatrix() {
		Matrix m = new Matrix();
		m.postScale(mScale, mScale);
		m.postTranslate(mXOffset, mYOffset);
		return m;
	}

	protected PointD getPointFromScreen(float screenX, float screenY) {

		float[] initialPoint = {screenX, screenY};
		float[] transformedPoint = {0,0};
		Matrix inverseCopy = new Matrix();
		if(getCanvasTransformMatrix().invert(inverseCopy)){
			inverseCopy.mapPoints(transformedPoint, initialPoint);
		}
		float x = transformedPoint[0];
		float y = transformedPoint[1];
		if (mImage != null) {
			x = Math.min(Math.max(0, x), mImage.getWidth());
			y = Math.min(Math.max(0, y), mImage.getHeight());
		}
		return new PointD((int) x, (int) y);
	}

	public void setImage(Bitmap image) {
		mImage = image;
		updateScale();
		updateOffest();
		update();
	}

	public void update() {
		invalidate();
	}

	public Bitmap getImage() {
		return mImage;
	}

	public void clearImage() {
		if (mImage != null) {
			setImage(null);
		}
	}

	public Rect getImageRect() throws IllegalStateException {
		if (mImage == null) throw new IllegalStateException("No image");
		return new Rect(0, 0, mImage.getWidth(), mImage.getHeight());
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!isInEditMode() && mImage != null) {
			canvas.save();
			canvas.setMatrix(getCanvasTransformMatrix());
			mPaint.setColor(Color.BLUE); // change to bitmap painting
			canvas.drawBitmap(mImage, 0, 0, mPaint); // change to bitmap painting

			mPaint.setColor(getResources().getColor(R.color.draw_view_foreground));
			for (List<PointD> shape : mShapes) {
				for (int i = 0; i < shape.size(); i++) {
					PointD pStart = shape.get(i);
					PointD pStop = shape.get((i + 1) % shape.size());
					canvas.drawLine(((Double) pStart.x).floatValue(), ((Double) pStart.y).floatValue(), ((Double) pStop.x).floatValue(), ((Double) pStop.y).floatValue(), mPaint);
				}
			}
			canvas.restore();
		}
	}

	public ArrayList<List<PointD>> getShapes() {
		return mShapes;
	}

	public void setShapes(ArrayList<List<PointD>> shapes) {
		this.mShapes = shapes;
		update();
	}

	public void clearShapes() {
		if (mShapes != null) {
			mShapes.clear();
			update();
		}
	}
}

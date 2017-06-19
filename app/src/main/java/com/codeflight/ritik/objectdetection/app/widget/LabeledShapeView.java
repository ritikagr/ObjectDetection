package com.codeflight.ritik.objectdetection.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.neurotec.graphics.PointD;

import java.util.ArrayList;
import java.util.List;

public class LabeledShapeView extends ShapeView {

	// ===========================================================
	// Private static field
	// ===========================================================

	private static final int LABEL_MARGIN = 2;

	// ===========================================================
	// Private fields
	// ===========================================================

	private ArrayList<String> mLabels;
	private Paint mTextPaint;
	private Paint mTextBgPaint;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public LabeledShapeView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.BLACK);

		mTextBgPaint = new Paint();
		mTextBgPaint.setColor(Color.BLACK);
		mTextBgPaint.setShadowLayer(5, 0, 0, Color.BLACK);
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private PointD fixPoint(PointD point, PointD center, float margin, Rect imageRect, RectF labelBounds) {
		PointD fixedPoint = new PointD(point);
		boolean tryLeft = point.x < center.x;
		boolean tryTop = point.y < center.y;

		if (tryLeft) {
			if (point.x - margin - labelBounds.width() >= imageRect.left) {
				fixedPoint.x = point.x - margin - labelBounds.width();
			} else {
				fixedPoint.x += margin;
			}
		} else {
			if (point.x + margin + labelBounds.width() < imageRect.right) {
				fixedPoint.x = point.x + margin;
			} else {
				fixedPoint.x = fixedPoint.x - margin - labelBounds.width();
			}
		}

		if (tryTop) {
			if (point.y - margin - labelBounds.height() >= imageRect.top) {
				fixedPoint.y = point.y - margin - labelBounds.height();
			} else {
				fixedPoint.y += margin;
			}
		} else {
			if (point.x + margin + labelBounds.width() < imageRect.bottom) {
				fixedPoint.y = point.y + margin;
			} else {
				fixedPoint.y = fixedPoint.y - margin - labelBounds.height();
			}
		}

		return fixedPoint;
	}

	private PointD getLabelLocationFromShape(List<List<PointD>> shapes, int index, Rect imageRect, RectF labelBounds, float margin) {
		PointD left = null;
		PointD right = null;
		PointD top = null;
		PointD bottom = null;
		PointD center = null;
		PointD result = null;

		if (shapes != null && shapes.size() > 0) {
			for (PointD p : shapes.get(index)) {
				if (left == null || p.x < left.x) {
					left = p;
				}

				if (right == null || p.x > right.x) {
					right = p;
				}

				if (top == null || p.y < top.y) {
					top = p;
				}

				if (bottom == null || p.y > bottom.y) {
					bottom = p;
				}
			}

			center = new PointD((left.x + right.x + top.x + bottom.x) / 4 - labelBounds.width() / 2, (left.y + right.y + top.y + bottom.y) / 4 - labelBounds.height() / 2);

			left = fixPoint(left, center, margin, imageRect, labelBounds);
			right = fixPoint(right, center, margin, imageRect, labelBounds);
			top = fixPoint(top, center, margin, imageRect, labelBounds);
			bottom = fixPoint(bottom, center, margin, imageRect, labelBounds);

			double minLeftDistSquared = Double.MAX_VALUE;
			double minRightDistSquared = Double.MAX_VALUE;
			double minTopDistSquared = Double.MAX_VALUE;
			double minBottomDistSquared = Double.MAX_VALUE;
			double minCenterDistSquared = Double.MAX_VALUE;

			for (int i = 0; i < shapes.size(); i++) {
				if (i == index) {
					continue;
				}

				for (PointD p : shapes.get(i)) {
					double leftDistSquared = Math.pow(p.x - left.x, 2) + Math.pow(p.y - left.y, 2);
					double rightDistSquared = Math.pow(p.x - right.x, 2) + Math.pow(p.y - right.y, 2);
					double topDistSquared = Math.pow(p.x - top.x, 2) + Math.pow(p.y - top.y, 2);
					double bottomDistSquared = Math.pow(p.x - bottom.x, 2) + Math.pow(p.y - bottom.y, 2);
					double centerDistSquared = Math.pow(p.x - center.x, 2) + Math.pow(p.y - center.y, 2);

					if (leftDistSquared < minLeftDistSquared) {
						minLeftDistSquared = leftDistSquared;
					}
					if (rightDistSquared < minRightDistSquared) {
						minRightDistSquared = rightDistSquared;
					}
					if (topDistSquared < minTopDistSquared) {
						minTopDistSquared = topDistSquared;
					}
					if (bottomDistSquared < minBottomDistSquared) {
						minBottomDistSquared = bottomDistSquared;
					}
					if (centerDistSquared < minCenterDistSquared) {
						minCenterDistSquared = centerDistSquared;
					}
				}
			}

			double maxDistSquared = Math.max(minLeftDistSquared, Math.max(minRightDistSquared, Math.max(minTopDistSquared, Math.max(minBottomDistSquared, minCenterDistSquared))));

			if (minLeftDistSquared == maxDistSquared) {
				result = left;
			} else if (minRightDistSquared == maxDistSquared) {
				result = right;
			} else if (minTopDistSquared == maxDistSquared) {
				result = top;
			} else if (minBottomDistSquared == maxDistSquared) {
				result = bottom;
			} else {
				result = center;
			}

			return result;
		} else {
			return null;
		}
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Matrix matrix = getCanvasTransformMatrix();
		Rect labelBounds = new Rect();
		float scale = getScale();

		if (mLabels != null) {
			for (int i = 0; i < mLabels.size(); i++) {
				mTextPaint.getTextBounds(mLabels.get(i), 0, mLabels.get(i).length(), labelBounds);
				RectF scaledLabelBounds = new RectF(labelBounds.left * scale, labelBounds.top * scale, labelBounds.right * scale, labelBounds.bottom * scale);
				float scaledMargin = LABEL_MARGIN * scale;
				PointD p = getLabelLocationFromShape(getShapes(), i, getImageRect(), scaledLabelBounds, scaledMargin);
				if (p != null) {
					float[] fp = new float[] {(float) p.x, (float) p.y};
					matrix.mapPoints(fp);
					canvas.drawRect(fp[0] + labelBounds.left - LABEL_MARGIN, fp[1] + labelBounds.top - LABEL_MARGIN, fp[0] + labelBounds.right + LABEL_MARGIN, fp[1] + labelBounds.bottom + LABEL_MARGIN, mTextBgPaint);
					canvas.drawText(mLabels.get(i), fp[0], fp[1], mTextPaint);
				}
			}
		}
	}

	public void setLabelSize(float textSize) {
		mTextPaint.setTextSize(textSize);
		mTextBgPaint.setTextSize(textSize);
		invalidate();
	}

	public ArrayList<String> getLabels() {
		return mLabels;
	}

	public void clearLabels() {
		if (mLabels != null) {
			mLabels.clear();
			update();
		}
	}

	public void clear() {
		clearImage();
		clearShapes();
		clearLabels();
	}

	public void setShapes(ArrayList<List<PointD>> shapes, ArrayList<String> labels) {
		if (shapes == null || labels == null) {
			throw new RuntimeException("Null parameter passed");
		}
		if (shapes.size() != labels.size()) {
			throw new RuntimeException("Shape list size and label list size differ.");
		}
		mLabels = labels;
		setShapes(shapes);
		invalidate();
	}

}

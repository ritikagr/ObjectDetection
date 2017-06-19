package com.codeflight.ritik.objectdetection.app.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codeflight.ritik.objectdetection.app.BaseLearningTask.StatusCallback;
import com.codeflight.ritik.objectdetection.app.LearningResult;
import com.codeflight.ritik.objectdetection.app.LearningTaskManager;
import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.database.DBAdapter;
import com.codeflight.ritik.objectdetection.app.database.ModelPickerActivity;
import com.codeflight.ritik.objectdetection.app.database.ModelRecord;
import com.codeflight.ritik.objectdetection.app.licensing.LicensingManager;
import com.neurotec.graphics.PointD;
import com.neurotec.io.NBuffer;
import com.neurotec.sentisight.SEShape;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codeflight.ritik.objectdetection.app.util.BitmapUtils.fromUri;

public final class LearningActivity extends AppCompatActivity implements StatusCallback {

	private static final String TAG = LearningActivity.class.getSimpleName();
	private static final int REQUEST_CODE_PICK_MODEL = 0;
	private static final int NUMBER_OF_POINTS_NEEDED_FOR_SHAPE = 3;
	private static final int NUMBER_OF_POINTS_NEEDED_FOR_CLEAR = 1;

	public static final String URI_OF_LEARNING_IMAGE = "uri_of_learning_image";
	public static final String SHAPE_LISTS = "shape_lists";
	public static final String STATUS_MESSAGE = "status_message";
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;

	private Button mButtonAdd;
	private Button mButtonSelectImage;
	private TextView mLabelStatus;
	private long mSelectedModel;
	private String mNewModelName;
    private int mDeviceWidth;
    private int mDeviceHeight;
	private ImageView mLearnImage;

    private ProgressDialog mProgressDialog;

	private Uri mImageUri;
	private ArrayList<List<PointD>> mPointsList = new ArrayList<List<PointD>>();

    private void updateView() {
		try {
			if (mImageUri != null) {
				Bitmap image = fromUri(this, mImageUri);
				if (image != null) {
					mLearnImage.setImageBitmap(image);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}
	}

	private SEShape[] pointListsToShapes(List<List<PointD>> pointLists) {
		ArrayList<SEShape> shapes = new ArrayList<SEShape>();
		if (pointLists == null || !(pointLists.size() > 0)) {
			return null;
		} else {
			for (List<PointD> points : pointLists) {
				SEShape shape = new SEShape();
				shape.getPoints().addAll(points);
				shapes.add(shape);
			}
		}
		return shapes.toArray(new SEShape[shapes.size()]);
	}

	private void learn(Bitmap image, SEShape[] shapes, NBuffer model) {
		try {
			for (SEShape shape : shapes) {
				if (!shape.isValid()) {
					//showError(R.string.msg_invalid_shape);
					return;
				}
			}
			LearningTaskManager.getInstance().setLearningCallback(this);
			if (model == null) {
				LearningTaskManager.getInstance().learn(this,image , shapes);
			} else {
				LearningTaskManager.getInstance().learn(this, image, shapes, model);
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDeviceWidth = size.x;
        mDeviceHeight = size.y;

        setContentView(R.layout.learning);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mLearnImage = (ImageView) findViewById(R.id.learn_imageview);

        mButtonAdd = (Button) findViewById(R.id.button_add);
        mButtonAdd.setVisibility(View.VISIBLE);
        mButtonAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLearnImage.getDrawable() != null) {
                    startActivityForResult(new Intent(LearningActivity.this, ModelPickerActivity.class), REQUEST_CODE_PICK_MODEL);
                }
            }
        });

        mButtonSelectImage = (Button) findViewById(R.id.button_select_image);
        mButtonSelectImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!LicensingManager.isSentiSightActivated())
                {
                    showToast(getString(R.string.msg_operation_is_not_activated));
                    return;
                }
                else {
                    Intent getImageIntent = ImagePicker.getPickImageIntent(LearningActivity.this);
                    startActivityForResult(getImageIntent, REQUEST_CODE_SELECT_IMAGE);
                }
            }
        });

        mLabelStatus = (TextView) findViewById(R.id.label_status);

        if ((savedInstanceState == null) || (savedInstanceState.getCharSequence(STATUS_MESSAGE) == null)) {
            mLabelStatus.setText("");
        } else {
            mLabelStatus.setText(savedInstanceState.getCharSequence(STATUS_MESSAGE));
        }

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(URI_OF_LEARNING_IMAGE);
        }

        LearningTaskManager.getInstance().setLearningCallback(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	@Override
	protected void onStop() {
		super.onStop();
		LearningTaskManager.getInstance().cancelLearningTask();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(URI_OF_LEARNING_IMAGE, mImageUri);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_PICK_MODEL) {

                try {
                    ArrayList<List<PointD>> imagePoints = new ArrayList<List<PointD>>();

					//getting image width and height
                    double x = fromUri(LearningActivity.this, mImageUri).getWidth();
                    double y = fromUri(LearningActivity.this, mImageUri).getHeight();

					//creating a list for four corner points
                    List<PointD> list = new ArrayList<PointD>();
                    list.add(new PointD(0.0,0.0));
                    list.add(new PointD(x, 0.0));
                    list.add(new PointD(x,y));
                    list.add(new PointD(0.0, y));

                    imagePoints.add(list);

					//creating shape array
                    SEShape[] shapes = pointListsToShapes(imagePoints);

                    mSelectedModel = data.getExtras().getLong(ModelPickerActivity.EXTRA_SELECTED_MODEL);
                    if (mSelectedModel == ModelPickerActivity.RESULT_ADD_NEW_MODEL) {
                        mNewModelName = data.getStringExtra(ModelPickerActivity.EXTRA_MODEL_NAME);
                        learn(fromUri(LearningActivity.this, mImageUri), shapes, null);
                    } else {
                        learn(fromUri(LearningActivity.this, mImageUri), shapes, DBAdapter.getInstance().getModel(mSelectedModel));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
			}
			else if(requestCode == REQUEST_CODE_SELECT_IMAGE)
            {
                if(data != null)
                {
                    mImageUri = data.getData();
                    updateView();
                }
            }
		}
	}

	@Override
	public void onLearningStart() {
		showProgress(getString(R.string.msg_learning_in_progress));
	}

	@Override
	public void onLearningFinish(LearningResult result) {
		switch (result.getStatus()) {
		case SUCCEEDED:
			try {
				mLabelStatus.setText(getString(R.string.format_learning_successfull_quality, result.getQuality()));
				Bitmap bitmap = fromUri(LearningActivity.this, mImageUri);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 0, bos);
				DBAdapter.getInstance().open();

				if (mSelectedModel == ModelPickerActivity.RESULT_ADD_NEW_MODEL) {
					Map<Integer, NBuffer> images = new HashMap<Integer, NBuffer>();
					images.put(0, NBuffer.fromArray(bos.toByteArray()));
					DBAdapter.getInstance().insertModel(mNewModelName, result.getSEModel().save(), images);
				} else {
					ModelRecord record = DBAdapter.getInstance().getModels().getById(mSelectedModel);
					record.addImage(NBuffer.fromArray(bos.toByteArray()));
					Map<Integer, NBuffer> images = record.getImages();
					DBAdapter.getInstance().deleteModel(mSelectedModel);
					DBAdapter.getInstance().insertModel(record.getName(), result.getSEModel().save(), images);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				DBAdapter.getInstance().close();
				hideProgress();
			}
			break;
		case FAILED:
			hideProgress();
			showToast(getString(R.string.msg_learning_failed) + result.getStatusMessage());
			break;
		case EXCEPTION_OCCURED:
			hideProgress();
			break;
		default:
			throw new AssertionError("Not recognised status");
		}
	}

    private void showProgress(final String string) {
        hideProgress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = ProgressDialog.show(LearningActivity.this, "", string);
            }
        });
    }

    private void hideProgress() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mProgressDialog != null && mProgressDialog.isShowing())
                {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}

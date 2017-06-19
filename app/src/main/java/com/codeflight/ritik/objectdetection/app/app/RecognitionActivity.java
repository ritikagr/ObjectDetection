package com.codeflight.ritik.objectdetection.app.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.codeflight.ritik.objectdetection.app.BaseRecognitionTask;
import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.RecognitionResult;
import com.codeflight.ritik.objectdetection.app.RecognitionTaskManager;
import com.codeflight.ritik.objectdetection.app.database.ModelRecord;
import com.codeflight.ritik.objectdetection.app.licensing.LicensingManager;
import com.codeflight.ritik.objectdetection.app.widget.LabeledShapeView;
import com.neurotec.lang.NCore;
import com.neurotec.sentisight.SERecognitionDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.codeflight.ritik.objectdetection.app.database.DBAdapter.ModelCollection;
import static com.codeflight.ritik.objectdetection.app.database.DBAdapter.getInstance;
import static com.codeflight.ritik.objectdetection.app.util.BitmapUtils.fromUri;
import static com.neurotec.sentisight.SEEngine.SERecognition.RecognitionDetailsCollection;

public class RecognitionActivity extends AppCompatActivity implements BaseRecognitionTask.StatusCallback {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private Button mRecognise;
    private Button mSelectImage;
    private ImageView mRecognitionImage;
    private ArrayList<Integer> selectedItemsIndexList = null;
    private Uri mImageUri;
    private int mDeviceWidth;
    private int mDeviceHeight;
    private ProgressDialog mProgressDialog;

    private TableLayout mTlObjects;

    private static final String ANDROID_ASSET_DESCRIPTOR = "file:///android_asset/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NCore.setContext(this);

        setContentView(R.layout.activity_recognition);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDeviceHeight = size.y;
        mDeviceWidth = size.x;

        getInstance().open();

        mRecognitionImage = (ImageView) findViewById(R.id.recognition_image);
        mRecognitionImage.setMinimumHeight((2*mDeviceHeight)/3);
        mTlObjects = (TableLayout) findViewById(R.id.objectCountTable);

        mRecognise = (Button) findViewById(R.id.button_recognise);
        mRecognise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognise();
            }
        });

        mSelectImage = (Button) findViewById(R.id.button_select_image);
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!LicensingManager.isSentiSightActivated())
                {
                    showToast(getString(R.string.msg_operation_is_not_activated));
                    return;
                }
                else {
                    Intent getImageIntent = ImagePicker.getPickImageIntent(RecognitionActivity.this);
                    startActivityForResult(getImageIntent, REQUEST_CODE_SELECT_IMAGE);
                }
            }
        });

        try {
            setImage(mImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_CODE_SELECT_IMAGE)
            {
                if(data != null)
                {
                    mTlObjects.setVisibility(View.GONE);
                    mImageUri = data.getData();
                    try {
                        setImage(mImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setImage(Uri mImageUri) throws IOException {

        if(mImageUri != null)
        {
            Bitmap bitmap = fromUri(this, mImageUri);
            if(bitmap != null) {
                Log.i(TAG, "Bitmap is not null");
                mRecognitionImage.setImageBitmap(bitmap);
            }
        }
    }

    private Bitmap decodeBitmapUri(Context context, Uri mImageUri) {

        // Get the dimensions of the View
        int targetW = 1280;
        int targetH = 720;
        Bitmap bitmap = null;
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(mImageUri), null, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(targetW/photoW, targetH/photoH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(mImageUri), null, bmOptions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void recognise() {

        if(mRecognitionImage.getDrawable() != null)
        {
            mTlObjects.setVisibility(View.GONE);
            RecognitionTaskManager.getInstance().setRecognitionCallback(this);
            final ModelCollection modelCollection = getInstance().getModels();

            ModelRecord[] modelRecords = modelCollection.toArray();

            final ArrayList<String> modelNameList = new ArrayList<>();

            for (ModelRecord modelRecord: modelRecords) {
                modelNameList.add(modelRecord.getName());
            }

            final CharSequence[] modelChoiceList = modelNameList.toArray(new CharSequence[modelNameList.size()]);

            selectedItemsIndexList = new ArrayList<>();

            boolean[] isSelectedArray = new boolean[modelRecords.length];

            for(int i=0;i<modelRecords.length;i++)
            {
                isSelectedArray[i] = false;
            }

            AlertDialog.Builder builder;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert);
            }
            else
                builder = new AlertDialog.Builder(this);

            builder.setTitle("Select Model");
            builder.setMultiChoiceItems(modelChoiceList, isSelectedArray, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                    if(isChecked)
                    {
                        selectedItemsIndexList.add(Integer.valueOf(which));
                    }
                    else if(selectedItemsIndexList.contains(Integer.valueOf(which)))
                    {
                        selectedItemsIndexList.remove(Integer.valueOf(which));
                    }
                }
            });

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ArrayList<String> selectedModelNameList = new ArrayList<String>();
                    for (Integer which: selectedItemsIndexList)
                    {
                        selectedModelNameList.add(modelNameList.get(which));
                    }

                    LabeledShapeView shapeView = new LabeledShapeView(RecognitionActivity.this, null);
                    try {
                        shapeView.setImage(fromUri(RecognitionActivity.this, mImageUri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ModelCollection selectedModelCollection = getInstance().getModelsByIds(selectedModelNameList,modelCollection);
                    RecognitionTaskManager.getInstance().recognise(RecognitionActivity.this, shapeView.getImage(), selectedModelCollection);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
        {
            showToast("Recognition needs an image");
        }
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecognitionStart() {
        showProgress(getString(R.string.msg_recognition_in_progress));
    }

    private void showProgress(final String string) {
        hideProgress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = ProgressDialog.show(RecognitionActivity.this, "", string);
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

    @Override
    public void onRecognitionFinish(RecognitionResult result) {

        hideProgress();

        switch (result.getStatus())
        {
            case SUCCEEDED:
                displayRecognitionResult(result.getRecognitionDetails());
                break;
            case FAILED:
                showToast("No Objects Recognised");
                break;
            case EXCEPTION_OCCURED:
                showToast("Error Occured: " + result.getException().getMessage());
                break;
            default:
                break;
        }
    }

    private void displayRecognitionResult(RecognitionDetailsCollection recognitionDetails) {
        HashMap<String, Integer> recognitionResult = new HashMap<>();

        for (SERecognitionDetails details : recognitionDetails)
        {
            String modelName = getInstance().getModels().getById((long) details.getModelId()).getName();

            if(recognitionResult.containsKey(modelName))
            {
                Integer quantity = recognitionResult.get(modelName);
                recognitionResult.replace(modelName, quantity, quantity + 1);
            }
            else
            {
                recognitionResult.put(modelName, 1);
            }
        }

        Log.i(TAG, String.valueOf(recognitionDetails.size()));

        Iterator iterator = recognitionResult.keySet().iterator();

        while(iterator.hasNext())
        {
            String key = (String) iterator.next();
            String value = String.valueOf(recognitionResult.get(key));

            Log.i(TAG, key + " : " + value);

            TableRow tr = new TableRow(RecognitionActivity.this);
            TextView tvLeft = new TextView(RecognitionActivity.this);
            setTextViewAttribute(tvLeft, key);
            tvLeft.setPadding(5,5,5,5);

            TextView tvRight = new TextView(RecognitionActivity.this);
            setTextViewAttribute(tvRight, value);
            tvRight.setGravity(5);
            tvRight.setPadding(5,5,40,5);

            tr.addView(tvLeft);
            tr.addView(tvRight);

            mTlObjects.addView(tr);
        }

        mTlObjects.setVisibility(View.VISIBLE);

    }

    private void setTextViewAttribute(TextView view, String key)
    {
        view.setText(key);
        view.setAllCaps(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setTextAppearance(android.R.style.TextAppearance_Medium);
        }
        else
        {
            view.setTextAppearance(RecognitionActivity.this, android.R.style.TextAppearance_Medium);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getInstance().close();
        if(mImageUri != null)
        {
            Log.d(TAG, "Deleting temp: " + (new File((new File(mImageUri.getPath())).getParent())).getAbsolutePath());
            (new File((new File(mImageUri.getPath())).getParent())).delete();
        }
    }
}

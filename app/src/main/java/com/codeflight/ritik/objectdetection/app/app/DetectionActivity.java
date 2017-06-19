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
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.widget.Button;
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
import com.neurotec.graphics.PointD;
import com.neurotec.lang.NCore;
import com.neurotec.sentisight.SERecognitionDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.codeflight.ritik.objectdetection.app.database.DBAdapter.ModelCollection;
import static com.codeflight.ritik.objectdetection.app.database.DBAdapter.getInstance;
import static com.codeflight.ritik.objectdetection.app.util.BitmapUtils.fromUri;
import static com.neurotec.sentisight.SEEngine.SERecognition.RecognitionDetailsCollection;

public class DetectionActivity extends AppCompatActivity implements BaseRecognitionTask.StatusCallback {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private LabeledShapeView mShapeView;
    private Button mRecognise;
    private Button mSelectImage;
    private ArrayList<Integer> selectedItemsIndexList = null;
    private Uri mImageUri;
    private int mDeviceWidth;
    private int mDeviceHeight;
    private ProgressDialog mProgressDialog;
    private TableLayout mTlObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NCore.setContext(this);

        setContentView(R.layout.activity_detection);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDeviceHeight = size.y;
        mDeviceWidth = size.x;

        getInstance().open();

        mTlObjects = (TableLayout) findViewById(R.id.objectCountTable);
        mShapeView = (LabeledShapeView) findViewById(R.id.recognition_shape_view);
        mShapeView.setLabelSize(R.style.TextAppearance_AppCompat_Medium);
        mShapeView.setMinimumHeight((mDeviceHeight)/2);
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
                    Intent getImageIntent = ImagePicker.getPickImageIntent(DetectionActivity.this);
                    startActivityForResult(getImageIntent, REQUEST_CODE_SELECT_IMAGE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_CODE_SELECT_IMAGE)
            {
                mTlObjects.setVisibility(View.GONE);
                mTlObjects.removeViewsInLayout(1, mTlObjects.getChildCount()-1);
                if(data != null)
                {
                    mImageUri = data.getData();
                    mShapeView.clear();
                    try {
                        updateShapeImage(mImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void updateShapeImage(Uri mImageUri) throws IOException {

        if(mImageUri != null)
        {
            Bitmap bitmap = fromUri(this, mImageUri);
            if(bitmap != null) {
                Log.i("ImageSize",String.valueOf(bitmap.getWidth()) + ":" + String.valueOf(bitmap.getHeight()));
                Log.i(TAG, "Bitmap is not null");
                mShapeView.setImage(bitmap);
            }
        }
    }

    ArrayList<List<PointD>> shapesToPointList(RecognitionDetailsCollection recognitionDetailsCollection)
    {
        ArrayList<List<PointD>> pointLists = new ArrayList<>();

        List<PointD> tmplist = new ArrayList<>();
        List<Pair<Integer,Integer>> centerList = new ArrayList<>();

        for(SERecognitionDetails details : recognitionDetailsCollection)
        {
            List<PointD> list = new ArrayList<>();

            list.addAll(details.getShape().getPoints());

            Collections.sort(list, new Comparator<PointD>() {
                @Override
                public int compare(PointD point1, PointD point2) {

                    Integer x1 = (int) point1.x;
                    Integer x2 = (int) point2.x;

                    int comp = x1.compareTo(x2);

                    if(comp != 0)
                        return comp;
                    else
                    {
                        Integer y1 = (int) point1.y;
                        Integer y2 = (int) point2.y;

                        return y1.compareTo(y2);
                    }
                }
            });
            if(list.size()==4)
            {
                int cx = (int) (list.get(0).x + list.get(1).x + list.get(2).x + list.get(3).x)/4;
                int cy = (int) (list.get(0).y + list.get(1).y + list.get(2).y + list.get(3).y)/4;
                centerList.add(new Pair<Integer, Integer>(cx, cy));
            }
            tmplist.addAll(details.getShape().getPoints());
            pointLists.add(list);
        }

        Collections.sort(tmplist, new Comparator<PointD>() {
            @Override
            public int compare(PointD point1, PointD point2) {

                Integer x1 = (int) point1.x;
                Integer x2 = (int) point2.x;

                int comp = x1.compareTo(x2);

                if(comp != 0)
                    return comp;
                else
                {
                    Integer y1 = (int) point1.y;
                    Integer y2 = (int) point2.y;

                    return y1.compareTo(y2);
                }
            }
        });

        Collections.sort(centerList, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> object1, Pair<Integer, Integer> object2) {

                Integer x1 = object1.first;
                Integer x2 = object2.first;

                int comp = x1.compareTo(x2);

                return comp;
            }
        });

        for(Pair<Integer,Integer> pair : centerList)
        {
            Log.i("Center", String.valueOf((int) pair.first) + ":" + String.valueOf((int) pair.second));
        }

        for(PointD pointD : tmplist)
        {
            Log.i("Point", String.valueOf((int) pointD.x) + ":" + String.valueOf((int) pointD.y));
        }

        return pointLists;
    }

    private void displayRecognitionResult(RecognitionDetailsCollection recognitionDetails) {
        HashMap<String, Integer> recognitionResult = new HashMap<>();

        shapesToPointList(recognitionDetails);
        for (SERecognitionDetails details : recognitionDetails)
        {
            String modelName = getInstance().getModels().getById((long) details.getModelId()).getName();


            if(recognitionResult.containsKey(modelName))
            {
                Integer quantity = recognitionResult.get(modelName);
                recognitionResult.remove(modelName);
                recognitionResult.put(modelName, quantity + 1);
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

            TableRow tr = new TableRow(DetectionActivity.this);
            TextView tvLeft = new TextView(DetectionActivity.this);
            setTextViewAttribute(tvLeft, key);
            tvLeft.setPadding(5,5,5,5);

            TextView tvRight = new TextView(DetectionActivity.this);
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
            view.setTextAppearance(DetectionActivity.this, android.R.style.TextAppearance_Medium);
        }
    }

    private Bitmap decodeBitmapUri(Context context, Uri mImageUri) {

        // Get the dimensions of the View
        int targetW = 1000;
        int targetH = 1000;
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

        mTlObjects.setVisibility(View.GONE);
        mTlObjects.removeViewsInLayout(1, mTlObjects.getChildCount()-1);
        if(mShapeView.getImage() != null)
        {
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

                    ModelCollection selectedModelCollection = getInstance().getModelsByIds(selectedModelNameList,modelCollection);
                    RecognitionTaskManager.getInstance().recognise(DetectionActivity.this, mShapeView.getImage(), selectedModelCollection);
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

    protected List<String> getComponents() {
        return Arrays.asList(LicensingManager.LICENSE_SENTISIGHT);
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
                mProgressDialog = ProgressDialog.show(DetectionActivity.this, "", string);
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
        mShapeView.clearShapes();

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

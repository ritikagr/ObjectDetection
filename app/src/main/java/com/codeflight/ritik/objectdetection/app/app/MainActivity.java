package com.codeflight.ritik.objectdetection.app.app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codeflight.ritik.objectdetection.app.EngineManager;
import com.codeflight.ritik.objectdetection.app.R;
import com.codeflight.ritik.objectdetection.app.database.DBEditorActivity;
import com.codeflight.ritik.objectdetection.app.licensing.LicensingManager;
import com.codeflight.ritik.objectdetection.app.licensing.LicensingState;
import com.codeflight.ritik.objectdetection.app.preference.LearningPreferences;
import com.codeflight.ritik.objectdetection.app.preference.RecognitionPreferences;

import java.util.Arrays;
import java.util.List;

public final class MainActivity extends AppCompatActivity implements LicensingManager.LicensingStateCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 1;

    private Button mBtRecognition;
    private Button mBtLearning;
    private Button mBtActivation;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        this.requestPermission();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mBtRecognition = (Button) findViewById(R.id.button_recognise_activity);
        mBtRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LicensingManager.isSentiSightActivated())
                    startActivity(new Intent(MainActivity.this, DetectionActivity.class));
                else
                    showToast("Activate License Before Using, For Acitvation Click on Activation");
            }
        });

        mBtLearning = (Button) findViewById(R.id.button_learn_activity);
        mBtLearning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LicensingManager.isSentiSightActivated())
                    startActivity(new Intent(MainActivity.this, LearningActivity.class));
                else
                    showToast("Activate License Before Using, For Acitvation Click on Activation");
            }
        });

        mBtActivation = (Button) findViewById(R.id.button_activation_activity);
        mBtActivation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainActivity.this, ActivationActivity.class));
                LicensingManager.getInstance().obtain(MainActivity.this, MainActivity.this, getComponents());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_learning_preferences: {
                startActivity(new Intent(this, LearningPreferences.class));
                break;
            }
            case R.id.menu_recognition_preferences: {
                startActivity(new Intent(this, RecognitionPreferences.class));
                break;
            }
            case R.id.menu_db_view: {
                startActivity(new Intent(this, DBEditorActivity.class));
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean requestPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)
                return true;
            else
                return false;
        }
        return false;
    }

    @Override
    public void onLicensingStateChanged(LicensingState state) {
        switch (state) {
            case OBTAINING:
                showProgress(getString(R.string.msg_obtaining_licenses));
                break;
            case OBTAINED:
                new MainActivity.ResourceLoader().execute(this);
                showToast(getString(R.string.msg_licenses_obtained));
                break;
            case NOT_OBTAINED:
                new MainActivity.ResourceLoader().execute(this);
                showToast(getString(R.string.msg_licenses_not_obtained));
                break;
            default:
                throw new AssertionError("Unreachable");
        }
    }

    final class ResourceLoader extends AsyncTask<Context, String, Void> {

        @Override
        protected Void doInBackground(Context... context) {
            publishProgress(getString(R.string.msg_initializing_engine));
            EngineManager.getInstance().updateEngine(context[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            hideProgress();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            showProgress(values[0]);
        }
    }

    private void showProgress(final String string) {
        hideProgress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = ProgressDialog.show(MainActivity.this, "", string);
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

    protected List<String> getComponents() {
        return Arrays.asList(LicensingManager.LICENSE_SENTISIGHT);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        LicensingManager.getInstance().release(getComponents());
    }
}


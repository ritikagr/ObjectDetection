package com.codeflight.ritik.objectdetection.app.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;

import com.codeflight.ritik.objectdetection.app.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ritik on 6/7/2017.
 */

public class ImagePicker {

    private static final String TAG = ImagePicker.class.getName();
    private static String mCurrentPhotoPath = null;

    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();

        Intent chooseImageIntent = new Intent();
        chooseImageIntent.setType("image/*");
        chooseImageIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent captureImageIntent = new Intent();
        /*captureImageIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageIntent.putExtra("return-data",true);
        File file = createImageFile(context);
        captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));*/
        intentList = addIntentsToList(context, intentList, chooseImageIntent);
        //intentList = addIntentsToList(context, intentList, captureImageIntent);
        if (intentList.size() > 0)
        {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), context.getString(R.string.pick_image_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> intentList, Intent intent) {

        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo: resInfo)
        {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            intentList.add(targetedIntent);
            Log.i(TAG, "Intent: " + intent.getAction() + " package: " + packageName);
        }
        return intentList;
    }

    private static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "OBJECT_" + timeStamp + "_";
        File storageDir = getAlbumDir();

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private static File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = getAlbumStorageDir("ObjectDetection");
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("Camera", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.i(TAG, "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    public static File getAlbumStorageDir(String albumName) {
        return new File (
                Environment.getExternalStorageDirectory()
                        + "/dcim/"
                        + albumName);
    }
}

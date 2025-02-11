package com.synconset;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.ContentResolver;
import android.util.Log;

import android.net.Uri;
import android.os.Bundle;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;

public class MultiImageChooserActivity extends AppCompatActivity {

    private static final String TAG = "ImagePicker";
    public static final int NOLIMIT = -1;
    public static final String MAX_IMAGES_KEY = "MAX_IMAGES";

    private int maxImages;
    private int maxImageCount;

    private Cursor imagecursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        maxImages = getIntent().getIntExtra(MAX_IMAGES_KEY, NOLIMIT);
        maxImageCount = maxImages;

        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia = registerForActivityResult(
                maxImageCount > 1 ? new PickMultipleVisualMedia(maxImageCount) : new PickMultipleVisualMedia(),
                uris -> {
                    if (!uris.isEmpty()) {
                        int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver contentResolver = getApplicationContext().getContentResolver();
                        ArrayList<String> stringList = new ArrayList<>();
                        for (Uri uri : uris) {
                            stringList.add(uri.toString());
                            try {
                                contentResolver.takePersistableUriPermission(uri, flag);
                            } catch (SecurityException e) {
                                Log.e("PickMedia", "Failed to take persistable URI permission: " +
                                        e.getMessage());
                            }

                        }
                        postImages(stringList);
                    } else {
                        super.onBackPressed();
                    }

                });

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
                new PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver contentResolver = getApplicationContext().getContentResolver();
                        ArrayList<String> stringList = new ArrayList<>();
                        stringList.add(uri.toString());
                        try {
                            contentResolver.takePersistableUriPermission(uri, flag);
                        } catch (SecurityException e) {
                            Log.e("PickMedia", "Failed to take persistable URI permission: " +
                                    e.getMessage());
                        }
                        postImages(stringList);
                    } else {
                        super.onBackPressed();
                    }

                });

        if (maxImageCount > 1) {
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
            return;
        }

        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        return;

    }

    void postImages(ArrayList<String> contentPaths) {
        Intent data = new Intent();

        if (contentPaths.size() > 0) {
            Bundle res = new Bundle();
            res.putStringArrayList("MULTIPLEFILENAMES", contentPaths);

            if (imagecursor != null) {
                res.putInt("TOTALFILES", imagecursor.getCount());
            }

            int sync = ResultIPC.get().setLargeData(res);
            data.putExtra("bigdata:synccode", sync);
            setResult(RESULT_OK, data);

        } else {
            setResult(RESULT_CANCELED, data);
        }
        finish();
        super.onBackPressed();
    }
}
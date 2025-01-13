/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.Context;

import android.os.Build;
import android.os.Bundle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ImagePicker extends CordovaPlugin {

    private static final String ACTION_GET_PICTURES = "getPictures";
    private static final String ACTION_HAS_READ_PERMISSION = "hasReadPermission";
    private static final String ACTION_REQUEST_READ_PERMISSION = "requestReadPermission";
    private static final String SETTINGS_NAME = "image_picker_settings";
    private static final String PERMISSION_REQUESTED = "permission_requested";

    private static final int PERMISSION_REQUEST_CODE = 100;

    private CallbackContext callbackContext;

    private Intent imagePickerIntent;
    private static Activity cordovaActivity = null;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        cordovaActivity = this.cordova.getActivity();
    }

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        this.callbackContext = callbackContext;

        if (ACTION_GET_PICTURES.equals(action)) {
            final JSONObject params = args.getJSONObject(0);
            imagePickerIntent = getImagePickerIntent(params);
            cordova.startActivityForResult(this, imagePickerIntent, 0);
            return true;
        }

        return false;
    }

    private void setPreference(String name, boolean value) {
        SharedPreferences settings = cordovaActivity.getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    private boolean getPreference(String name) {
        SharedPreferences settings = cordovaActivity.getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        return settings.getBoolean(name, false);
    }

    private Intent getImagePickerIntent(JSONObject params) throws JSONException {
        final Intent imagePickerIntent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
        int max = 20;
        int desiredWidth = 0;
        int desiredHeight = 0;
        int quality = 100;
        int outputType = 0;
        if (params.has("maximumImagesCount")) {
            max = params.getInt("maximumImagesCount");
        }
        if (params.has("width")) {
            desiredWidth = params.getInt("width");
        }
        if (params.has("height")) {
            desiredHeight = params.getInt("height");
        }
        if (params.has("quality")) {
            quality = params.getInt("quality");
        }
        if (params.has("outputType")) {
            outputType = params.getInt("outputType");
        }

        imagePickerIntent.putExtra("MAX_IMAGES", max);
        imagePickerIntent.putExtra("WIDTH", desiredWidth);
        imagePickerIntent.putExtra("HEIGHT", desiredHeight);
        imagePickerIntent.putExtra("QUALITY", quality);
        imagePickerIntent.putExtra("OUTPUT_TYPE", outputType);

        return imagePickerIntent;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            int sync = data.getIntExtra("bigdata:synccode", -1);
            final Bundle bigData = ResultIPC.get().getLargeData(sync);

            ArrayList<String> fileNames = bigData.getStringArrayList("MULTIPLEFILENAMES");

            JSONArray res = new JSONArray(fileNames);
            callbackContext.success(res);

        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            String error = data.getStringExtra("ERRORMESSAGE");
            callbackContext.error(error);

        } else if (resultCode == Activity.RESULT_CANCELED) {
            JSONArray res = new JSONArray();
            callbackContext.success(res);

        } else {
            callbackContext.error("이미지가 선택되지 않았습니다.");
        }
    }

    private String getApplicationName() {
        Context context = cordovaActivity.getApplicationContext();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}
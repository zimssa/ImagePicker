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

        if (ACTION_HAS_READ_PERMISSION.equals(action)) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, hasReadPermission()));
            return true;

        } else if (ACTION_REQUEST_READ_PERMISSION.equals(action)) {
            requestReadPermission();
            return true;

        } else if (ACTION_GET_PICTURES.equals(action)) {
            final JSONObject params = args.getJSONObject(0);
            imagePickerIntent = getImagePickerIntent(params);

            if (hasReadPermission()) {
                cordova.startActivityForResult(this, imagePickerIntent, 0);
            } else {
                if (getPreference(PERMISSION_REQUESTED) == false) {
                    requestReadPermission();
                    // callbackContext.success();
                } else {
                    callbackContext
                            .error("저장소 접근이 제한되었습니다. [설정 > 앱 > " + getApplicationName() + "]에서 저장소 접근 권한을 허용해 주세요.");
                }
            }
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

    @SuppressLint("InlinedApi")
    private boolean hasReadPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return cordova.hasPermission(permission);
        }
        return true;
    }

    @SuppressLint("InlinedApi")
    private void requestReadPermission() {
        if (!hasReadPermission()) {
            String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };

            setPreference(PERMISSION_REQUESTED, true);
            cordova.requestPermissions(this,
                    PERMISSION_REQUEST_CODE,
                    permissions);
        }
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

    /**
     * Choosing a picture launches another Activity, so we need to implement the
     * save/restore APIs to handle the case where the CordovaActivity is killed by
     * the OS
     * before we get the launched Activity's result.
     *
     * @see ://cordova.apache.org/docs/en/dev/guide/platforms/android/plugin.html#launching-other-activities
     */
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onRequestPermissionResult(int requestCode,
            String[] permissions,
            int[] grantResults) throws JSONException {

        // For now we just have one permission, so things can be kept simple...
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cordova.startActivityForResult(this, imagePickerIntent, 0);
        } else {
            // Tell the JS layer that something went wrong...
            callbackContext.error("저장소 접근이 제한되었습니다. [설정 > 앱 > " + getApplicationName() + "]에서 저장소 접근 권한을 허용해 주세요");
        }
    }

    private String getApplicationName() {
        Context context = cordovaActivity.getApplicationContext();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
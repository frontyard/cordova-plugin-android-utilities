/*
The MIT License (MIT)

Copyright (c) 2017 Nedim Cholich

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package co.frontyard.cordova.plugin.utilities;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.media.AudioManager;
import android.net.*;
import android.os.Build;
import android.util.*;
import android.webkit.MimeTypeMap;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.apache.cordova.*;
import org.json.*;

/**
 * AndroidUtilities Cordova Plugin
 *
 * @author Nedim Cholich
 */
public class AndroidUtilities extends CordovaPlugin {
    private static final String TAG = "AndroidUtilities";
    private Map<String, String> appInfo = new HashMap<String, String>();

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.i(TAG, "Initializing AndroidUtilities");
        populateApplicationInfo();
    }

    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("createDesktopShortcut")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    createDesktopShortcut(callbackContext);
                }
            });
            return true;
        }
        else if (action.equals("getApplicationInfo")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    getApplicationInfo(callbackContext);
                }
            });
            return true;
        }
        else if (action.equals("installApk")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    installApk(callbackContext, args.optString(0, null));
                }
            });
            return true;
        }
        else if (action.equals("uninstallApk")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    uninstallApk(callbackContext, args.optString(0, null));
                }
            });
            return true;
        }
        else if (action.equals("isApkInstalled")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    isApkInstalled(callbackContext, args.optString(0, null));
                }
            });
            return true;
        }
        else if (action.equals("getAudioVolume")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    getAudioVolume(callbackContext);
                }
            });
            return true;
        }
        else if (action.equals("setAudioVolume")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    setAudioVolume(callbackContext, args.optInt(0, 100));
                }
            });
            return true;
        }
        else {
            callbackContext.error("Action '" + action + "' not recognized");
            return false;
        }
    }

    /**
     * Add desktop shortcut for your Cordova app.
     *
     * @param callbackContext
     */
    private void createDesktopShortcut(CallbackContext callbackContext) {
        Activity activity = cordova.getActivity();
        String packageName = activity.getPackageName();
        CharSequence displayName = null;
        PackageManager pm = activity.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            if (null != pi.applicationInfo) {
                displayName = pi.applicationInfo.loadLabel(pm);
            }

            if (null != displayName) {
                ApplicationInfo appInfo = activity.getApplicationInfo();

                Intent shortcutIntent = new Intent(activity, Class.forName(appInfo.packageName + ".MainActivity"));
                shortcutIntent.setAction(Intent.ACTION_MAIN);

                Intent addIntent = new Intent();
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, displayName);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(activity, appInfo.icon));
                addIntent.putExtra("duplicate", false);

                addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
                activity.sendBroadcast(addIntent);

                addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                activity.sendBroadcast(addIntent);

                callbackContext.success();
            }
            else {
                callbackContext.error("Failed to created shortcut, no displayName");
            }
        }
        catch (PackageManager.NameNotFoundException ex) {
            callbackContext.error("Failed to create shortcut, package name not found");
        }
        catch (ClassNotFoundException ex) {
            callbackContext.error("Failed to create shortcut, main activity class not found");
        }
    }

    private void getApplicationInfo(CallbackContext callbackContext) {
        callbackContext.success(new org.json.JSONObject(appInfo));
    }

    /**
     * Initializes application build info. Will be returned from #getApplicationInfo
     * Borrowed from https://github.com/lynrin/cordova-plugin-buildinfo
     *
     * @param callbackContext
     */
    private void populateApplicationInfo() {
        Log.i(TAG, "Getting app info");
        Activity activity = cordova.getActivity();
        String packageName = activity.getPackageName();
        String buildConfigClassName = packageName + ".BuildConfig";
        String basePackageName = packageName;
        CharSequence displayName = "";

        PackageManager pm = activity.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            if (null != pi.applicationInfo) {
                displayName = pi.applicationInfo.loadLabel(pm);
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Class c = null;
        try {
            c = Class.forName(buildConfigClassName);
        }
        catch (ClassNotFoundException e) {
        }

        if (null == c) {
            basePackageName = activity.getClass().getPackage().getName();
            buildConfigClassName = basePackageName + ".BuildConfig";

            try {
                c = Class.forName(buildConfigClassName);
            }
            catch (ClassNotFoundException e) {
                Log.e(TAG, "BuildConfig ClassNotFoundException", e);
                return;
            }
        }

        appInfo.put("debug", String.valueOf(getClassFieldBoolean(c, "DEBUG", false)));
        appInfo.put("displayName", displayName.toString());
        appInfo.put("basePackageName", basePackageName);
        appInfo.put("packageName", packageName);
        appInfo.put("versionName", getClassFieldString(c, "VERSION_NAME", ""));
        appInfo.put("versionCode", String.valueOf(getClassFieldInt(c, "VERSION_CODE", 0)));
        appInfo.put("buildType", getClassFieldString(c, "BUILD_TYPE", ""));
        appInfo.put("flavor", getClassFieldString(c, "FLAVOR", ""));
    }

    /**
     * Get boolean of field from Class
     *
     * @param klass
     * @param fieldName
     * @param defaultValue
     * @return
     */
    private static boolean getClassFieldBoolean(Class klass, String fieldName, boolean defaultValue) {
        boolean value = defaultValue;
        Field field = getClassField(klass, fieldName);

        if (null != field) {
            try {
                value = field.getBoolean(klass);
            }
            catch (IllegalAccessException ex) {
                Log.e(TAG, "Failed to get boolean field " + fieldName + " from class " + klass.getName(), ex);
            }
        }

        return value;
    }

    /**
     * Get string of field from Class
     *
     * @param klass
     * @param fieldName
     * @param defaultValue
     * @return
     */
    private static String getClassFieldString(Class klass, String fieldName, String defaultValue) {
        String value = defaultValue;
        Field field = getClassField(klass, fieldName);

        if (null != field) {
            try {
                value = (String) field.get(klass);
            }
            catch (IllegalAccessException ex) {
                Log.e(TAG, "Failed to get string field " + fieldName + " from class " + klass.getName(), ex);
            }
        }

        return value;
    }

    /**
     * Get int of field from Class
     *
     * @param klass
     * @param fieldName
     * @param defaultValue
     * @return
     */
    private static int getClassFieldInt(Class klass, String fieldName, int defaultValue) {
        int value = defaultValue;
        Field field = getClassField(klass, fieldName);

        if (null != field) {
            try {
                value = field.getInt(klass);
            }
            catch (IllegalAccessException ex) {
                Log.e(TAG, "Failed to get int field " + fieldName + " from class " + klass.getName(), ex);
            }
        }

        return value;
    }

    /**
     * Get field from Class
     *
     * @param klass
     * @param fieldName
     * @return
     */
    private static Field getClassField(Class klass, String fieldName) {
        Field field = null;

        try {
            field = klass.getField(fieldName);
        }
        catch (NoSuchFieldException ex) {
            Log.e(TAG, "Failed to get field " + fieldName + " from class " + klass.getName(), ex);
        }

        return field;
    }

    private static String getMimeType(String path) {
        String mimeType = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            mimeType = mime.getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }

    private void installApk(CallbackContext callbackContext, String fullPath) {
        if (null == fullPath) {
            callbackContext.error("Must provide full path to the apk file");
        }
        else {
            //String mime = "application/vnd.android.package-archive";
            String mime = getMimeType(fullPath);
            Uri uri = Uri.parse(fullPath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT > 15) {
                intent.setDataAndTypeAndNormalize(uri, mime); // API Level 16 -> Android 4.1
            }
            else {
                intent.setDataAndType(uri, mime);
            }
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Activity activity = cordova.getActivity();
            activity.startActivity(intent);
            callbackContext.success();
        }
    }

    /**
     * Uninstalles APK through PackageManager. Will prompt user for confirmation.
     *
     * @param callbackContext
     * @param packageName
     */
    private void uninstallApk(CallbackContext callbackContext, String packageName) {
        if (null == packageName) {
            callbackContext.error("Must provide package name of the apk file");
        }
        else {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            Activity activity = cordova.getActivity();
            activity.startActivity(intent);
            callbackContext.success();
        }
    }

    /**
     * Uses PackageManager to check if specific APK is installed on the system.
     *
     * @param callbackContext
     * @param packageName
     */
    private void isApkInstalled(CallbackContext callbackContext, String packageName) {
        if (null == packageName) {
            callbackContext.error("Must provide package name of the apk file");
        }
        else {
            try {
                Activity activity = cordova.getActivity();
                PackageManager pm = activity.getPackageManager();
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                callbackContext.success();
            }
            catch (PackageManager.NameNotFoundException e) {
                callbackContext.error("Apk " + packageName + " is not installed");
            }
        }
    }

    /**
     * Set audio volume.
     *
     * @param volume normalized int value between 0 and 100
     * @return
     * @throws Exception
     */
    private void setAudioVolume(CallbackContext callbackContext, int volume) {
        Activity activity = cordova.getActivity();
        AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = (volume > 100 ? 100 : volume < 0 ? 0 : volume) * max / 100;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        callbackContext.success();
    }

    /**
     * Get audio volume. Sends int value noramlized between 0 and 100.
     */
    protected void getAudioVolume(CallbackContext callbackContext) {
        Activity activity = cordova.getActivity();
        AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = Math.round((current * 100) / max);
        callbackContext.success(volume);
    }
}
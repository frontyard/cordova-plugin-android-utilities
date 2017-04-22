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
import android.util.*;
import java.lang.reflect.*;
import org.apache.cordova.*;
import org.json.*;

/**
 * AndroidUtilities Cordova Plugin
 *
 * @author Nedim Cholich
 * @since 1.0.0
 */
public class AndroidUtilities extends CordovaPlugin {
    private static final String TAG = "AndroidUtilities";

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        PluginResult.Status status = PluginResult.Status.NO_RESULT;
        try {
            if (action.equals("shortcut")) {
                status = addDesktopShortcut(callbackContext);
            }
        }
        catch(Exception ex) {
            Log.e(TAG, "Failed to execute action '" + action + "'", ex);
            status = PluginResult.Status.ERROR;
        }

        PluginResult result = new PluginResult(status);
        callbackContext.sendPluginResult(result);

        return true;
    }

    private PluginResult.Status addDesktopShortcut(CallbackContext callbackContext) throws ClassNotFoundException {
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

                Log.d(TAG, "Created desktop shortcut for " + displayName);
                return PluginResult.Status.OK;
            }
            else {
                Log.i(TAG, "Failed to created shortcut, no displayName");
                return PluginResult.Status.ERROR;
            }
        }
        catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, "Failed to create shortcut, no packageName", ex);
            return PluginResult.Status.ERROR;
        }
    }
}
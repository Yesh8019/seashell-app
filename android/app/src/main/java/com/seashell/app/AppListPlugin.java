package com.seashell.app;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Native helper plugin exposing two things to the web layer:
 *   - listApps(): every launchable installed app on the device, each with its
 *     display label, package name, and icon encoded as a base64 PNG data URL.
 *     Used both for the temporary "pick your apps" debug listing screen and,
 *     longer-term, for showing each app's real icon in the seashell ring.
 *   - launchApp(packageName): fires the same Intent Android itself uses when
 *     you tap an icon on the home screen / in the app drawer.
 *
 * This only ever queries apps that declare a MAIN/LAUNCHER intent (i.e. things
 * that actually show up as a tappable icon somewhere) — it does not request
 * the broader QUERY_ALL_PACKAGES permission, since we don't need visibility
 * into background services/components, just regular user-facing apps.
 */
@CapacitorPlugin(name = "AppList")
public class AppListPlugin extends Plugin {

    @PluginMethod
    public void listApps(PluginCall call) {
        PackageManager pm = getContext().getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);

        JSArray apps = new JSArray();
        for (ResolveInfo info : resolveInfos) {
            try {
                ApplicationInfo appInfo = info.activityInfo.applicationInfo;
                String packageName = appInfo.packageName;
                String label = String.valueOf(info.loadLabel(pm));

                Drawable iconDrawable = info.loadIcon(pm);
                String iconBase64 = drawableToBase64(iconDrawable);

                JSObject app = new JSObject();
                app.put("label", label);
                app.put("packageName", packageName);
                app.put("icon", "data:image/png;base64," + iconBase64);
                apps.put(app);
            } catch (Exception e) {
                // Skip any single app that fails to resolve/encode rather than
                // failing the entire listing.
            }
        }

        JSObject result = new JSObject();
        result.put("apps", apps);
        call.resolve(result);
    }

    @PluginMethod
    public void launchApp(PluginCall call) {
        String packageName = call.getString("packageName");
        if (packageName == null || packageName.isEmpty()) {
            call.reject("packageName is required");
            return;
        }

        PackageManager pm = getContext().getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);

        if (launchIntent == null) {
            call.reject("No launchable activity found for package: " + packageName);
            return;
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(launchIntent);
        call.resolve();
    }

    /**
     * Sends this app to the background exactly like pressing the device's
     * Home button, instead of destroying/finishing the Activity. Used when
     * the user taps the shell to close it without picking an app — the
     * collapse animation plays first (in JS), then this is called so the
     * user lands back on their real home screen rather than being left
     * sitting on our closed-but-still-foregrounded shell screen.
     */
    @PluginMethod
    public void exitToHome(PluginCall call) {
        if (getActivity() != null) {
            getActivity().moveTaskToBack(true);
        }
        call.resolve();
    }

    private String drawableToBase64(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
            bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
        } else {
            int width = Math.max(drawable.getIntrinsicWidth(), 1);
            int height = Math.max(drawable.getIntrinsicHeight(), 1);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }
}

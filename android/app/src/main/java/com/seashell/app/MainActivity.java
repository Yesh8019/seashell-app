package com.seashell.app;

import android.app.WallpaperManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.WindowManager;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AppListPlugin.class);
        super.onCreate(savedInstanceState);

        // Tell Android to composite the device wallpaper behind this window.
        // Without this, a standalone launch (new task, e.g. tapping the home
        // screen icon) has nothing behind the translucent window but solid
        // black, since there's no previous Activity in the stack to show
        // through. This shows the real wallpaper image — not live home-screen
        // icons/widgets, which belong to the separate Launcher app process
        // and can't be rendered behind an ordinary standalone Activity.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        // Fallback for OEM skins (e.g. vivo/iQOO FunTouch, MIUI, etc.) that
        // silently ignore FLAG_SHOW_WALLPAPER for third-party apps: read the
        // wallpaper bitmap ourselves via WallpaperManager and set it directly
        // as this window's background drawable. This doesn't depend on the OS
        // agreeing to composite a live wallpaper layer — it's just a static
        // snapshot of the current wallpaper, drawn by us.
        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            Drawable wallpaperDrawable = wm.getDrawable();
            if (wallpaperDrawable != null) {
                getWindow().setBackgroundDrawable(wallpaperDrawable);
            }
        } catch (Exception ex) {
            // If this fails for any reason (missing permission on some OEMs,
            // no wallpaper set, etc.) just silently keep whatever background
            // is already in place rather than crashing the app.
        }

        // Make the WebView itself transparent so there is no opaque background
        // behind the HTML content, letting the wallpaper (set above) and/or
        // whatever Activity is paused underneath (e.g. if launched from
        // another app) show through instead of a solid color.
        getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
    }
}

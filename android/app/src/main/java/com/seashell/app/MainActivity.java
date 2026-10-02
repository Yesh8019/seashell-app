package com.seashell.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AppListPlugin.class);
        super.onCreate(savedInstanceState);

        // Make the WebView itself transparent so there is no opaque background
        // behind the HTML content — combined with the transparent window theme
        // (styles.xml) and the HTML body having no opaque background, this is
        // what lets the shell appear to float rather than sit on a visible
        // "app window". Per Android's security model, a separate app cannot
        // literally render the real home-screen wallpaper behind itself; a
        // transparent window falls back to black. The real "sits on your
        // actual home screen" effect requires the native App Widget wrapper
        // planned for a later step.
        getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);

        // Extend content under the system bars and dim them, so there is no
        // visible status bar / navigation bar chrome making this look like a
        // separate app window rather than something native to the home screen.
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Re-apply immersive flags whenever focus returns (e.g. after a
            // system dialog), since Android clears them in several cases.
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}

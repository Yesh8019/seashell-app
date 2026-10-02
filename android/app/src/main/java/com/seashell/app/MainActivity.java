package com.seashell.app;

import android.graphics.Color;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AppListPlugin.class);
        super.onCreate(savedInstanceState);

        // Make the WebView itself transparent so there is no opaque background
        // behind the HTML content. Combined with the translucent/overlay
        // Activity theme (styles.xml: AppTheme.NoActionBar) this lets the real,
        // live home screen underneath (wallpaper, widgets, icons) show through
        // behind our shell UI, instead of rendering as solid black — the
        // launcher Activity is paused, not destroyed, while we're on top of it.
        getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
    }
}

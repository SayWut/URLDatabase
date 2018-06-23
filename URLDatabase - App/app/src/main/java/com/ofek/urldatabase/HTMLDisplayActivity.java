package com.ofek.urldatabase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HTMLDisplayActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_htmldisplay);

        WebView wv = (WebView)findViewById(R.id.wv_html_display);
        wv.setWebViewClient(new WebViewClient());

        WebSettings wvS = wv.getSettings();
        wvS.setJavaScriptEnabled(true);
        wvS.setLoadWithOverviewMode(true);
        wvS.setUseWideViewPort(true);
        wvS.setSupportZoom(true);

        wv.loadUrl("file:///android_asset/my_website/index.html");
    }
}

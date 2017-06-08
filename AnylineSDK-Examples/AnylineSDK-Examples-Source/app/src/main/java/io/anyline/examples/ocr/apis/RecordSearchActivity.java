package io.anyline.examples.ocr.apis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import io.anyline.examples.R;

public class RecordSearchActivity extends AppCompatActivity {

    public static final String RECORD_INPUT = "RECORD_INPUT";
    private WebView webView;
    private TextView recordText;
    private String scannedRecord;
    private TextView textNotFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_search);

        recordText = (TextView) findViewById(R.id.record_scanned);
        webView = (WebView) findViewById(R.id.records_webview);
        textNotFound = (TextView) findViewById(R.id.records_not_found);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            scannedRecord = extras.getString(RECORD_INPUT, "").trim();
            recordText.setText("Searching on Google for Record: " + scannedRecord);
        }

        startGoogleSearch();
    }

    private void startGoogleSearch() {
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.setVisibility(View.GONE);
                textNotFound.setVisibility(View.VISIBLE);

            }
        });
        webView.loadUrl("https://www.google.at/search?q=\"" + scannedRecord + "\" site:discogs.com OR site:musbrainz.org OR site:allmusic.com");
    }


}

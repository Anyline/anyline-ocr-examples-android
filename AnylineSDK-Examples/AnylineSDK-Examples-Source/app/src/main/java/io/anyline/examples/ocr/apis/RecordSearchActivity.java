package io.anyline.examples.ocr.apis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ScanningConfigurationActivity;

public class RecordSearchActivity extends ScanningConfigurationActivity {

    public static final String RECORD_INPUT = "RECORD_INPUT";
    private WebView webView;
    private TextView recordText;
    private String scannedRecord;
    private TextView textNotFound;
    private int awardedPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base_no_menu);
        getLayoutInflater().inflate(R.layout.activity_record_search, (ViewGroup) findViewById(R.id.placeholder));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recordText = (TextView) findViewById(R.id.record_scanned);
        webView = (WebView) findViewById(R.id.records_webview);
        textNotFound = (TextView) findViewById(R.id.records_not_found);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            scannedRecord = extras.getString(RECORD_INPUT, "").trim();
            recordText.setText("Searching on Google for Record: " + scannedRecord);
        }

        setupScanResult();
        startGoogleSearch();
    }

    private void startGoogleSearch() {
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.setVisibility(View.GONE);
                textNotFound.setVisibility(View.VISIBLE);

            }
        });
        webView.loadUrl("https://www.google.at/search?q=\"" +scannedRecord +"\" site:discogs.com OR site:musbrainz.org OR site:allmusic.com");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return false;
    }

    public void goBack(View view) {
        this.onBackPressed();
    }


    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return null;
    }
}

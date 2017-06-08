package io.anyline.examples.ocr.apis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import io.anyline.examples.R;


public class IsbnActivity extends AppCompatActivity implements RequestListener {

    private static final String TAG = IsbnActivity.class.getSimpleName();

    public final String GOOGLE_BOOKS_API_CALL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    public static final String ISBN_INPUT = "ISBN_INPUT";

    private String isbn;
    private LinearLayout layoutNotFound;
    private LinearLayout layoutBookInfo;
    private WebView googleWebView;
    private ImageView bookImage;
    private TextView titleText;
    private TextView subTitleText;
    private TextView authorText;
    private TextView publisherText;
    private TextView descriptionText;
    private TextView productDetailsText;
    private TextView linkText;
    private TextView previewLinkText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn);

        layoutNotFound = (LinearLayout) findViewById(R.id.isbn_book_not_found);
        layoutBookInfo = (LinearLayout) findViewById(R.id.isbn_book_info);

        googleWebView = (WebView) findViewById(R.id.isbn_google_webview);

        bookImage = (ImageView) findViewById(R.id.isbn_img);

        titleText = (TextView) findViewById(R.id.isbn_title);
        subTitleText = (TextView) findViewById(R.id.isbn_subtitle);
        authorText = (TextView) findViewById(R.id.isbn_author);
        publisherText = (TextView) findViewById(R.id.isbn_publisher);

        descriptionText = (TextView) findViewById(R.id.isbn_description);
        productDetailsText = (TextView) findViewById(R.id.isbn_product_details);

        linkText = (TextView) findViewById(R.id.isbn_link);
        previewLinkText = (TextView) findViewById(R.id.isbn_preview_link);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isbn = extras.getString(ISBN_INPUT, "").trim();
            isbn = isbn.replaceAll("-", "").replaceAll(" ", "").replaceAll("ISBN10", "").replaceAll("ISBN13", "").replaceAll("ISBN", "").replaceAll(":", "");
            new RequestTask(GOOGLE_BOOKS_API_CALL + isbn, this).
                    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void onResult(JSONObject result) {
        if (result == null || result.optInt("totalItems") == 0) {
            startGoogleSearch();
            return;
        }
        BookInfo isbn = null;
        try {
            isbn = new BookInfo(result.getJSONArray("items").getJSONObject(0));
        } catch (JSONException e) {
            //should not be possible, as item count has been checked
            Log.e(TAG, "Error while getting items from json.", e);
            startGoogleSearch();
            return;
        }

        titleText.setText(isbn.getTitle());
        if (isbn.getSubTitle() != null) {
            subTitleText.setVisibility(View.VISIBLE);
            subTitleText.setText(isbn.getSubTitle());
        }

        authorText.setText(isbn.getAuthors());

        publisherText.setText(isbn.getPublisherInfo());

        if (isbn.getImageLink() != null) {
            new DownloadImageTask().execute(isbn.getImageLink());
        }

        descriptionText.setText(isbn.getDescription());
        productDetailsText.setText(isbn.getAdditionalProductDetails());
        if (isbn.getPreviewLink() != null) {
            previewLinkText.setText(Html.fromHtml("<a href='" + isbn.getPreviewLink()
                    + "'>Preview available on Google Books</a>"));
            previewLinkText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (isbn.getHtmlLink() != null) {
            linkText.setText(Html.fromHtml("<a href='" + isbn.getHtmlLink()
                    + "'>See more details on Google Books</a>"));
            linkText.setMovementMethod(LinkMovementMethod.getInstance());
        }
        findViewById(R.id.isbn_progress).setVisibility(View.GONE);
        layoutBookInfo.setVisibility(View.VISIBLE);
    }

    private void startGoogleSearch() {
        findViewById(R.id.isbn_progress).setVisibility(View.GONE);
        layoutBookInfo.setVisibility(View.GONE);
        layoutNotFound.setVisibility(View.VISIBLE);
        googleWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                googleWebView.setVisibility(View.GONE);
                ((TextView) layoutNotFound.findViewById(R.id.isbn_not_found_info)).
                        setText(String.format(getString(R.string.isbn_no_information_found_no_internet), isbn) +
                                "\n\n" + getString(R.string.no_internet));

            }
        });
        googleWebView.loadUrl("https://www.google.at/search?q=isbn+" + isbn);
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {


        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //  Any error handling would go here
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null && !isFinishing()) {
                bookImage.setImageBitmap(result);
            }
        }

    }
}

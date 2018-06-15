/*
 * Anyline
 * BookInfo.java
 *
 * Copyright (c) 2016 9yards GmbH
 *
 * Created by Stefanie at 2016-02-02
 */
package io.anyline.examples.ocr.apis;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to hold information about a book.
 */
public class BookInfo {

    private static final String TAG = BookInfo.class.getSimpleName();

    private String title;
    private String subtitle;
    private String[] authors;
    private String publisher;
    private String publishedDate;
    private String description = "-";
    private String imageLink;
    private String listPrice;
    private String epubDownloadlink;
    private String pdfDownloadlink;
    private boolean epubAvailable = false;
    private boolean pdfAvailable = false;
    private String buyLink;
    private String country;
    private String infoLink;
    private String isbn10 = "-";
    private String isbn13 = "-";
    private int pageCount = -1;
    private String previewLink;

    /**
     * Create a book info from a google books json.
     * Detailed description of the json object can be found here:
     * <a href="https://developers.google.com/books/docs/v1/reference/volumes">
     * https://developers.google.com/books/docs/v1/reference/volumes</a>
     *
     * @param json the json object with the book information
     */
    public BookInfo(JSONObject json) {
        if (json != null) {

            try {
                if (json.has("volumeInfo")) {
                    extractVolumeInfo(json.getJSONObject("volumeInfo"));
                }
                if (json.has("saleInfo")) {
                    extractSalesInfo(json.getJSONObject("saleInfo"));
                }
                if (json.has("accessInfo")) {
                    extractAccessInfo(json.getJSONObject("accessInfo"));
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error while parsing json file", e);
            }

        }
    }


    private void extractVolumeInfo(JSONObject json) throws JSONException {
        title = json.optString("title", null);
        subtitle = json.optString("subtitle", null);

        if (json.has("authors")) {
            JSONArray tmp = json.getJSONArray("authors");
            authors = new String[tmp.length()];

            for (int i = 0; i < tmp.length(); i++) {
                authors[i] = tmp.get(i).toString();
            }
        }
        publisher = json.optString("publisher", null);
        publishedDate = json.optString("publishedDate", null);
        description = json.optString("description", null);
        pageCount = json.optInt("pageCount", 0);

        if (json.has("imageLinks")) {
            JSONObject imageJ = json.getJSONObject("imageLinks");
            if (imageJ.has("small")) {
                imageLink = imageJ.getString("small");
            } else if (imageJ.has("thumbnail")) {
                imageLink = imageJ.getString("thumbnail");
            }
        }
        infoLink = json.optString("infoLink", null);
        previewLink = json.optString("previewLink", null);

        if (json.has("industryIdentifiers")) {
            JSONArray array = json.getJSONArray("industryIdentifiers");

            for (int i = 0; i < array.length(); i++) {
                JSONObject tmp = array.getJSONObject(i);
                if (tmp.has("type")) {
                    if (tmp.getString("type").equals("ISBN_10")) {
                        isbn10 = tmp.getString("identifier");
                    } else if (tmp.getString("type").equals("ISBN_13")) {
                        isbn13 = tmp.getString("identifier");
                    }
                }
            }
        }
    }

    private void extractSalesInfo(JSONObject json) throws JSONException {
        if (json.has("listPrice")) {
            JSONObject price = json.getJSONObject("listPrice");
            listPrice = price.optString("amount") + " " + price.optString("currencyCode");
        }
        buyLink = json.optString("buyLink", null);
    }

    private void extractAccessInfo(JSONObject json) throws JSONException {
        country = json.optString("country", null);
        if (json.has("epub")) {
            JSONObject epubJ = json.getJSONObject("epub");
            epubDownloadlink = epubJ.optString("downloadLink", null);
            epubAvailable = epubJ.optBoolean("isAvailable", false);
        }
        if (json.has("pdf")) {
            JSONObject pdfJ = json.getJSONObject("pdf");
            pdfDownloadlink = pdfJ.optString("downloadLink", null);
            pdfAvailable = pdfJ.optBoolean("isAvailable", false);
        }
    }

    public String getImageLink() {
        return imageLink;
    }

    public String getHtmlLink() {
        return infoLink;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public String getAuthors() {
        if (authors == null) {
            return "unknown";
        }

        String authorsString = "";
        for (String s : authors) {
            authorsString += s + ", ";
        }
        authorsString = authorsString.substring(0, authorsString.length() - 2);
        return authorsString;
    }

    public String getPublisherInfo() {
        String info = "";
        if (publisher != null) {
            info += publisher + " ";
        }
        if (publishedDate != null) {
            info += "(" + publishedDate + ")";
        }
        return info;
    }

    public String getDescription() {
        return description;
    }

    public String getAdditionalProductDetails() {
        String s = "";

        if (pageCount != -1) {
            s += "Pages: " + pageCount + "\n\n";
        }
        s += "ISBN 10: " + isbn10 + "\n";
        s += "ISBN 13: " + isbn13 + "\n";
        s += "\n\n";

        if (listPrice != null) {
            s += "List Price: " + listPrice;
            if (country != null) {
                s += " (" + country + ")";
            }
            s += "\n";
        }

        if (epubAvailable && epubDownloadlink != null) {
            s += "Available as epub: " + epubDownloadlink + "\n";
        }
        if (pdfAvailable && pdfDownloadlink != null) {
            s += "Available as pdf: " + pdfDownloadlink + "\n\n";
        }
        return s;
    }

    public String getPreviewLink() {
        return previewLink;
    }
}

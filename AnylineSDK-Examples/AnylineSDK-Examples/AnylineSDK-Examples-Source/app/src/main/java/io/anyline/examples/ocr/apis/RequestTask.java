package io.anyline.examples.ocr.apis;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Simple asynchronous RequestTask: sends a Request to a REST API.
 * The result is interpreted as a JSONObject and handed back to the listener.
 */
class RequestTask extends AsyncTask<Void, Void, JSONObject> {

    private final String api;
    RequestListener listener;

    public RequestTask(String url, RequestListener listener) {
        this.api = url;
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(Void... nothing) {
        HttpURLConnection urlConnection = null;
        JSONObject result = null;

        try {
            URL url = new URL(api);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader streamReader = new BufferedReader
                    (new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            result = new JSONObject(responseStrBuilder.toString());
        } catch (IOException | JSONException e) {
            // any exception handling would go here
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        if (listener != null) {
            listener.onResult(result);
        }
    }
}


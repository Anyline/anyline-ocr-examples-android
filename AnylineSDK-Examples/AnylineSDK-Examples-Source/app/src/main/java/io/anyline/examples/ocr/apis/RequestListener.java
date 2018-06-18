package io.anyline.examples.ocr.apis;


import org.json.JSONObject;


/**
 * Simple listener to be used in the RequestTask
 */
interface RequestListener {


    /**
     * Called with the result
     *
     * @param json the result of the request
     */
    void onResult(JSONObject json);

}

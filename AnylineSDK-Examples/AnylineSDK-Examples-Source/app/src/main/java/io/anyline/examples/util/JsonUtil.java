package io.anyline.examples.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


/**
 * Created by andrea on 10/10/16.
 */

public class JsonUtil {

    public static String getJsonStringForSampleRequest(Context context) {
        Preferences preferences = Preferences.getInstance(context);
        JSONObject json = new JSONObject();

        try {
            json.put("mail", preferences.getStoredEmailAddress());
            json.put("vendorUUID", preferences.getUuid());
            json.put("appBundleId", context.getApplicationContext().getPackageName());
            json.put("platform", "Android");
            json.put("intercomUserId", preferences.getUuid()); // Intercom: user_id
            json.put("locale", Locale.getDefault().getLanguage());

            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

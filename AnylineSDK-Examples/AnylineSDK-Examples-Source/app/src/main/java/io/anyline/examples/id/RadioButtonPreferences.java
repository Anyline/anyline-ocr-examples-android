package io.anyline.examples.id;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.anyline.examples.barcode.BarcodeModel;


public class RadioButtonPreferences {

    private static final String SHARED_PREFS_ANYLINE = "AnylinePreferences";
    private static final String ALL_STATES_COUNTRIES = "All States/Countries";

    private static RadioButtonPreferences instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final String preferenceType;

    @SuppressLint("CommitPrefEdits")
    private RadioButtonPreferences(Context context, String preferenceType) {
        prefs = context.getSharedPreferences(SHARED_PREFS_ANYLINE, Context.MODE_PRIVATE);
        this.preferenceType = preferenceType;
        editor = prefs.edit();
    }

    public static RadioButtonPreferences getInstance(Context context, String preferenceType) {
        if (instance == null) {
            instance = new RadioButtonPreferences(context, preferenceType);
        }
        return instance;
    }

    public BarcodeModel getDefault() {
        return new BarcodeModel(ALL_STATES_COUNTRIES, ALL_STATES_COUNTRIES);
    }


    public void setDefault() {
        BarcodeModel item = getDefault();
        Gson gson = new Gson();
        String json = gson.toJson(item);
        editor.putString(preferenceType, json);
        editor.commit();
    }


    public void setBarcodeType(BarcodeModel barcodeType) {
        Gson gson = new Gson();
        String json = gson.toJson(barcodeType);
        editor.putString(preferenceType, json);
        editor.commit();
    }


    public BarcodeModel get() {
        Gson gson = new Gson();
        String json = prefs.getString(preferenceType, "");
        Type type = new TypeToken<BarcodeModel>() {
        }.getType();
        return gson.fromJson(json, type);
    }

}

package io.anyline.examples.barcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BarcodePrefferences {


    private static final String SHARED_PREFS_ANYLINE = "AnylinePreferences";

    private static BarcodePrefferences instance;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    private BarcodePrefferences(Context context) {
        prefs = context.getSharedPreferences(SHARED_PREFS_ANYLINE, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static BarcodePrefferences getInstance(Context context) {
        if (instance == null) {
            instance = new BarcodePrefferences(context);
        }
        return instance;
    }

    public ArrayList<BarcodeModel> getDefault() {

        ArrayList<BarcodeModel> items = new ArrayList<>();
        items.add(new BarcodeModel("UPC/EAN", "1D Symbologies - Retail Usages"));
        items.add(new BarcodeModel("Code 128", "1D Symbologies - Logistics / Inventory Usage"));
        items.add(new BarcodeModel("Code 39", "1D Symbologies - Logistics / Inventory Usage"));
        items.add(new BarcodeModel("Interleaved 2 of 5", "1D Symbologies - Logistics / Inventory Usage"));
        items.add(new BarcodeModel("Data Matrix", "2D Symbologies"));
        items.add(new BarcodeModel("PDF417", "2D Symbologies"));
        items.add(new BarcodeModel("QR Code", "2D Symbologies"));
        return items;
    }


    public void setDefault() {

        ArrayList<BarcodeModel> items = getDefault(); //new ArrayList<>();
//        items.add(new BarcodeModel("UPC/EAN", "1D Symbologies - Retail Usages"));
//        items.add(new BarcodeModel("Code 128", "1D Symbologies - Logistics / Inventory Usage"));
//        items.add(new BarcodeModel("Code 39", "1D Symbologies - Logistics / Inventory Usage"));
//        items.add(new BarcodeModel("Interleaved 2 of 5", "1D Symbologies - Logistics / Inventory Usage"));
//        items.add(new BarcodeModel("Data Matrix", "2D Symbologies"));
//        items.add(new BarcodeModel("PDF417", "2D Symbologies"));
//        items.add(new BarcodeModel("QR Code", "2D Symbologies"));

        Gson gson = new Gson();
        String json = gson.toJson(items);
        editor.putString("BarcodeList", json);
        editor.commit();
    }

    public void setBarcodeTypes(ArrayList<BarcodeModel> barcodeTypes) {
        Gson gson = new Gson();
        String json = gson.toJson(barcodeTypes);
        editor.putString("BarcodeList", json);
        editor.commit();
    }

    public ArrayList<BarcodeModel> get() {
        Gson gson = new Gson();
        String json = prefs.getString("BarcodeList", "");
        Type type = new TypeToken<List<BarcodeModel>>() {
        }.getType();
        ArrayList<BarcodeModel> arrayList = gson.fromJson(json, type);
        return arrayList;
    }

    public ArrayList<String> getArrayString() {
        Gson gson = new Gson();
        String json = prefs.getString("BarcodeList", "");
        Type type = new TypeToken<List<BarcodeModel>>() {
        }.getType();
        ArrayList<BarcodeModel> arrayList = gson.fromJson(json, type);
        ArrayList<String> arrayList1 = new ArrayList<>();
        if (arrayList != null && arrayList.size() != 0) {
            for (BarcodeModel item :
                    arrayList) {
                arrayList1.add(item.getBarcodeType());

            }
        }
        return arrayList1;
    }
}

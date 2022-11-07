package io.anyline.examples.barcode;

import static io.anyline.examples.barcode.BarcodePreferences.BARCODE_CATEGORY.CATEGORY_1D;
import static io.anyline.examples.barcode.BarcodePreferences.BARCODE_CATEGORY.CATEGORY_2D;
import static io.anyline.examples.barcode.BarcodePreferences.BARCODE_CATEGORY.CATEGORY_POSTAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BarcodePreferences {
    static final String SHARED_PREFS_ANYLINE = "AnylinePreferences";

    private static BarcodePreferences instance;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    static final BarcodePreference<Boolean> SingleScanButtonPreference = new BarcodePreference("barcodesetting_singlescanbutton", false);
    static final BarcodePreference<Boolean> MultiScanButtonPreference = new BarcodePreference("barcodesetting_multiscanbutton", true);

    enum BARCODE_CATEGORY {
        CATEGORY_1D("1D Symbologies"),
        CATEGORY_2D("2D Symbologies"),
        CATEGORY_POSTAL("Postal Codes");

        final String categoryName;
        BARCODE_CATEGORY(String categoryName) {
            this.categoryName = categoryName;
        }
    }

    @SuppressLint("CommitPrefEdits")
    private BarcodePreferences(Context context) {
        prefs = context.getSharedPreferences(SHARED_PREFS_ANYLINE, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static BarcodePreferences getInstance(Context context) {
        if (instance == null) {
            instance = new BarcodePreferences(context);
        }
        return instance;
    }

    static ArrayList<BarcodeModel> getAll() {

        ArrayList<BarcodeModel> items = new ArrayList<>();
        items.add(new BarcodeModel("UPC/EAN", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("GS1 Databar & Composite Codes", CATEGORY_1D.categoryName));

        items.add(new BarcodeModel("Code 128", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("GS1-128", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("ISBT 128", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Code 39", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Trioptic Code 39", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Code 32", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Code 93", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Interleaved 2 of 5", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Matrix 2 of 5", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("One D Inverse", CATEGORY_1D.categoryName));

        items.add(new BarcodeModel("Code 25", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Codabar", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("MSI", CATEGORY_1D.categoryName));
        items.add(new BarcodeModel("Code 11", CATEGORY_1D.categoryName));

        items.add(new BarcodeModel("PDF417", CATEGORY_2D.categoryName));
        items.add(new BarcodeModel("MicroPDF417", CATEGORY_2D.categoryName));
        items.add(new BarcodeModel("Data Matrix", CATEGORY_2D.categoryName));
        items.add(new BarcodeModel("QR Code", CATEGORY_2D.categoryName));
        items.add(new BarcodeModel("MicroQR", CATEGORY_2D.categoryName));
        items.add(new BarcodeModel("GS1 QR Code", CATEGORY_2D.categoryName));
        items.add(new BarcodeModel("Aztec", CATEGORY_2D.categoryName));
        items.add(new BarcodeModel("MaxiCode", CATEGORY_2D.categoryName));

        items.add(new BarcodeModel("US Postnet", CATEGORY_POSTAL.categoryName));
        items.add(new BarcodeModel("US Planet", CATEGORY_POSTAL.categoryName));
        items.add(new BarcodeModel("UK Postal", CATEGORY_POSTAL.categoryName));
        items.add(new BarcodeModel("USPS 4CB / OneCode / Intelligent Mail", CATEGORY_POSTAL.categoryName));

        return items;
    }

    public ArrayList<BarcodeModel> getDefault() {
        return getAll();
    }

    public void setDefault() {
        ArrayList<BarcodeModel> items = getDefault();
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

    public Object getPreferenceValue(BarcodePreference preference) {
        if (preference.getDefaultValue() instanceof String)
            return prefs.getString(preference.getKey(), (String) preference.getDefaultValue());
        else if (preference.getDefaultValue() instanceof Boolean)
            return prefs.getBoolean(preference.getKey(), (Boolean) preference.getDefaultValue());
        else if (preference.getDefaultValue() instanceof Float)
            return prefs.getFloat(preference.getKey(), (Float) preference.getDefaultValue());
        else if (preference.getDefaultValue() instanceof Integer)
            return prefs.getInt(preference.getKey(), (Integer) preference.getDefaultValue());
        else if (preference.getDefaultValue() instanceof Long)
            return prefs.getLong(preference.getKey(), (Long) preference.getDefaultValue());
        return null;
    }
}

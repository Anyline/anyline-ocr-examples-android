package io.anyline.examples.id;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ScanningConfigurationActivity;
import io.anyline.examples.baseadapters.BaseGridAdapter;
import io.anyline.examples.util.BitmapUtil;
import io.anyline.examples.util.Constant;
import io.anyline.view.ScanView;

public class ScanUniversalIdResultActivity extends ScanningConfigurationActivity {

    private HashMap<String, String> resultMap = new HashMap<>();
    LinkedHashMap<String, String> orderedHashMap = new LinkedHashMap<>();
    LinkedHashMap<String, String> sortedData = new LinkedHashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_scan_view);
        RecyclerView recyclerView = findViewById(R.id.rv_results);
        Button confirmButton = findViewById(R.id.confirmation_button);
        ImageView controlImage = findViewById(R.id.control_image);
        ImageView faceImageView = findViewById(R.id.face_image);
        TextView frontSideTextView = findViewById(R.id.text);
        TextView backSideTextView = findViewById(R.id.textFaceImage);
        backSideTextView.setVisibility(View.VISIBLE);
        frontSideTextView.setTypeface(Typeface.DEFAULT);
        backSideTextView.setTypeface(Typeface.DEFAULT);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        confirmButton.setOnClickListener(v ->
                                                 finish());
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = getIntent().getExtras();
            try {
                resultMap = (HashMap<String, String>) intent.getSerializableExtra("resultData");
                orderedHashMap = new LinkedHashMap<>(resultMap);

                // define keys in the order how they should appear on the result screen - keep keysOrig and keysNew in Sync!:
                String[] keysOrig = {"SURNAME", "GIVENNAMES",   "DATEOFBIRTH",  "DOCUMENTNUMBER",   "DOCUMENTTYPE",     "NATIONALITYCOUNTRYCODE",   "PLACEOFBIRTH",     "DATEOFISSUE",      "DATEOFEXPIRY",     "ADDRESS",  "AUTHORITY",    "CATEGORIES"};
                String[] keysNew =  {"Surname", "Given Names",  "Date Of Birth","Document Number",  "Document Type",    "Country",                  "Place Of Birth",   "Date Of Issue",    "Date Of Expiry",   "Address",  "Authority",    "Categories"};
                //String[] values = {"", "", "", "", "", "", "", "", "", "", "", ""};
                String[] values = new String[keysOrig.length];
                Arrays.fill(values, "");

                // put surname, givenNames, ... at the top of the list:
                // 1. detect keys, fill values-array and remove pair from hashmap:
                Iterator it = orderedHashMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();

                    for( int i = 0; i <= keysOrig.length - 1; i++) {
                        if (pair.getKey().toString().toUpperCase().equals(keysOrig[i])) {
                            values[i] = pair.getValue().toString();
                            it.remove();
                        }
                    }
                }

                // 2. convert key: add spaces before capital letters, then convert everything to lower case, except 1st char:
                LinkedHashMap<String, String> renamedOrderedHashMap = new LinkedHashMap<>();
                it = orderedHashMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String oldKey = pair.getKey().toString();
                    String newKey = "";
                    for (int i = 0; i < oldKey.length(); i++) {
                        if (Character.isUpperCase(oldKey.charAt(i))) {
                            newKey += " ";
                        }
                        newKey += oldKey.charAt(i);
                    }
                    //newKey=newKey.toLowerCase();
                    renamedOrderedHashMap.put (newKey.substring(0, 1).toUpperCase() + newKey.substring(1), pair.getValue().toString());
                    }

                // 3. add key-value pairs to sorted hashMap:
                for( int i = 0; i <= keysOrig.length - 1; i++) {
                    if (values[i].length()>0) {
                        sortedData.put (keysNew[i], values[i]);
                    }
                }

                // 4. add remaining key/value pairs to sorted hashMap:
                sortedData.putAll(renamedOrderedHashMap);

                Bitmap bmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FULL_PICTURE_PATH));
                if (bmp==null) {
                    frontSideTextView.setText(Html.fromHtml(
                            String.format(getResources().getString(R.string.card_front), getString(R.string.not_available))));
                    controlImage.setVisibility(View.GONE);
                } else {
                    frontSideTextView.setText(Html.fromHtml(
                            String.format(getResources().getString(R.string.card_front), "")));
                    controlImage.setVisibility(View.VISIBLE);
                    controlImage.setImageBitmap(bmp);
                }

                Bitmap faceBmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FACE_PICTURE_PATH));
                if (faceBmp == null) {
                    backSideTextView.setText(Html.fromHtml(
                            String.format(getResources().getString(R.string.card_back), getString(R.string.not_available))));
                    faceImageView.setVisibility(View.GONE);
                } else {
                    backSideTextView.setText(Html.fromHtml(
                            String.format(getResources().getString(R.string.card_back), "")));
                    faceImageView.setVisibility(View.VISIBLE);
                    faceImageView.setImageBitmap(faceBmp);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        BaseGridAdapter adapter = new BaseGridAdapter(this, sortedData);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected ScanView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return null;
    }
}

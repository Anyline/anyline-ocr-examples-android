package io.anyline.examples.id;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedHashMap;

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

    private LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
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
            String[] resultKeys = (String[]) intent.getSerializableExtra("resultDataKeys");
            String[] resultValues = (String[]) intent.getSerializableExtra("resultDataValues");
            resultMap = new LinkedHashMap<>();
            for (int i = 0; i < resultKeys.length; i++) {
                resultMap.put(resultKeys[i], resultValues[i]);
            }

            Bitmap bmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FULL_PICTURE_PATH));
            if (bmp == null) {
                frontSideTextView.setVisibility(View.VISIBLE);
                frontSideTextView.setText(Html.fromHtml(
                        String.format(getResources().getString(R.string.card_front), getString(R.string.not_available))));
                controlImage.setVisibility(View.GONE);
            } else {
                frontSideTextView.setVisibility(View.GONE);
                controlImage.setVisibility(View.VISIBLE);
                controlImage.setImageBitmap(bmp);
            }

            Bitmap faceBmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FACE_PICTURE_PATH));
            if (faceBmp == null) {
                backSideTextView.setVisibility(View.VISIBLE);
                backSideTextView.setText(Html.fromHtml(
                        String.format(getResources().getString(R.string.card_back), getString(R.string.not_available))));
                faceImageView.setVisibility(View.GONE);
            } else {
                backSideTextView.setVisibility(View.GONE);
                faceImageView.setVisibility(View.VISIBLE);
                faceImageView.setImageBitmap(faceBmp);
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));

        BaseGridAdapter adapter = new BaseGridAdapter(this, resultMap); //sortedData);
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

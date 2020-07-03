package io.anyline.examples.id;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.LinkedHashMap;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_scan_view);
        RecyclerView recyclerView = findViewById(R.id.rv_results);
        TextView confirmButton = findViewById(R.id.confirmation_button);
        ImageView controlImage = findViewById(R.id.control_image);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        confirmButton.setOnClickListener(v->
                finish());
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = getIntent().getExtras();
            try {
                resultMap = (HashMap<String, String>) intent.getSerializableExtra("resultData");
                orderedHashMap = new LinkedHashMap<>(resultMap);


                Bitmap bmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FULL_PICTURE_PATH));
                controlImage.setImageBitmap(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        BaseGridAdapter adapter = new BaseGridAdapter(this, orderedHashMap);
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

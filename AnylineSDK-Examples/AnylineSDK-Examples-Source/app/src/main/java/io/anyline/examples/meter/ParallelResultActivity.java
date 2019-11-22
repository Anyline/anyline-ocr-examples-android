package io.anyline.examples.meter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import at.nineyards.anyline.models.AnylineImage;
import io.anyline.examples.R;
import io.anyline.examples.baseadapters.BaseGridAdapter;
import io.anyline.examples.util.BitmapUtil;
import io.anyline.examples.util.Constant;

public class ParallelResultActivity extends Activity {
    private RecyclerView recyclerView;
    private HashMap<String, String> result = new HashMap<>();
    private HashMap<String, AnylineImage> cutoutImages = new HashMap<>();
    private BaseGridAdapter scanResultAdapter;
    ImageView meterIV;
    ImageView barcodeIV;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_result_parallel);
        meterIV = findViewById(R.id.meter_image);
        //barcodeIV = findViewById(R.id.barcode_image);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));

        TextView btnConfirm = findViewById(R.id.confirmation_button);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        Intent intent = getIntent();
        if (intent != null) {
            try {
                result.put("Meter", intent.getStringExtra("result0"));
                result.put("Barcode", intent.getStringExtra("result1"));
                Bitmap meterImage = BitmapUtil.getBitmap(intent.getStringExtra("0"));
                meterIV.setImageBitmap(meterImage);
                Bitmap barcodeImage = BitmapUtil.getBitmap(intent.getStringExtra("1"));
                barcodeIV.setImageBitmap(barcodeImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scanResultAdapter = new BaseGridAdapter(this.getApplicationContext(), result);
        recyclerView.setAdapter(scanResultAdapter);
    }

}

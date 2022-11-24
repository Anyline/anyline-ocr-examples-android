/*
 * Anyline
 * ScanAnalogDigitalMeterActivity.java
 *
 * Copyright (c) 2017 Anyline GmbH
 *
 * Created by Hanna at 2017-04-07
 */
package io.anyline.examples.meter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.anyline.examples.R;
import io.anyline.examples.scanviewresult.ScanViewResultActivity;
import io.anyline.examples.util.Constant;
import io.anyline.models.AnylineImage;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.barcode.BarcodeScanPlugin;
import io.anyline.plugin.barcode.BarcodeScanResult;
import io.anyline.plugin.barcode.BarcodeScanViewPlugin;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanPlugin;
import io.anyline.plugin.meter.MeterScanResult;
import io.anyline.plugin.meter.MeterScanViewPlugin;
import io.anyline.view.AbstractScanViewPluginComposite;
import io.anyline.view.ParallelScanViewComposite;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginConfig;


/**
 * Example activity for the automatic Analog/Digital Scanmode of the Anyline-Energy-Module
 * See the {@link io.anyline.examples.meter.baseactivities.AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanParallelAutoAnalogDigitalMeterActivity extends AppCompatActivity {

    private Switch barcodeSwitch;
    protected ScanView energyScanView;
    protected String foundBarcodeString;
    HashMap<String, String> cutoutImagePaths = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_energy_parallel);


        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Meter");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        foundBarcodeString = "";

        // get the view from the layout (check out the xml for the configuration of the view)
        energyScanView = (ScanView) findViewById(R.id.energy_scan_view);

        // Usually the default scan mode would be set here, in this specific case it is done in the subclasses
        //energyScanView.setScanMode(EnergyScanView.ScanMode.ELECTRIC_METER);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        barcodeSwitch = (Switch) findViewById(R.id.barcode_scanner_switch);

        final AbstractScanViewPluginComposite composite = new ParallelScanViewComposite("PARALLEL_METER_BARCODE");


        ScanViewPluginConfig meterScanViewPluginConfig = new ScanViewPluginConfig(getApplicationContext(), "meter_parallel_view_config.json");
        MeterScanPlugin meterScanPlugin = new MeterScanPlugin(this, "METER_PARALLEL");
        MeterScanViewPlugin meterSVP = new MeterScanViewPlugin(this, meterScanPlugin, meterScanViewPluginConfig);
        meterSVP.setScanMode(MeterScanMode.AUTO_ANALOG_DIGITAL_METER);

        ScanViewPluginConfig barcodeScanViewPluginConfig = new ScanViewPluginConfig(getApplicationContext(), "barcode_parallel_view_config.json");
        final BarcodeScanPlugin barcodeScanPlugin = new BarcodeScanPlugin(this, "barcodePlugin");
        final BarcodeScanViewPlugin barcodeSVP = new BarcodeScanViewPlugin(this, barcodeScanPlugin, barcodeScanViewPluginConfig);


        meterSVP.addScanResultListener(new ScanResultListener<MeterScanResult>() {
            @Override
            public void onResult(MeterScanResult result) {
                if (!barcodeSwitch.isChecked()) {
                    cutoutImagePaths.put("meter", setupImagePath(result.getCutoutImage()));
                }
            }
        });

        barcodeSVP.addScanResultListener(new ScanResultListener<BarcodeScanResult>() {
            @Override
            public void onResult(BarcodeScanResult barcodeScanResult) {
                if (!barcodeSwitch.isChecked()) {
                    cutoutImagePaths.put("barcode", setupImagePath(barcodeScanResult.getCutoutImage()));
                }
            }
        });


        barcodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // enabled barcode detection and google play services are available
                energyScanView.stop();
                if (isChecked) {
                    composite.add(barcodeSVP);
                } else {
                    composite.remove(barcodeSVP);
                }
                energyScanView.start();
            }
        });

        composite.add(meterSVP);
        composite.add(barcodeSVP);

        composite.addScanResultListener(new ScanResultListener() {
            @Override
            public void onResult(ScanResult result) {
                Intent intent = new Intent(ScanParallelAutoAnalogDigitalMeterActivity.this, ParallelResultActivity.class);
                for (ScanResult subResult : (Collection<ScanResult>) result.getResult()) {
                    if (subResult instanceof MeterScanResult) {
                        intent.putExtra("result0", subResult.getResult().toString());
                    } else if (subResult instanceof BarcodeScanResult) {
                        intent.putExtra("result1", ((BarcodeScanResult) subResult).getResult().toString());
                    }
                }
                intent.putExtra("0", cutoutImagePaths.get("meter"));
                intent.putExtra("1", cutoutImagePaths.get("barcode"));

                startActivity(intent);
            }
        });

        energyScanView.setScanViewPlugin(composite);

    }


    protected LinkedHashMap<String, String> getMeterReadingResult(String result) {
        LinkedHashMap<String, String> meterReadingResult = new LinkedHashMap();
        meterReadingResult.put(getResources().getString(R.string.reading_result), (result.isEmpty() || result == null) ? getResources().getString(R.string.not_available) : result);
        meterReadingResult.put(getResources().getString(R.string.barcode), (foundBarcodeString.isEmpty() || foundBarcodeString == null) ? getResources().getString(R.string.not_available) : foundBarcodeString);
        return meterReadingResult;
    }


    protected void startScanResultIntent(String scanMode, LinkedHashMap<String, String> scanResult, String... path) {
        // String path = setupImagePath(anylineOcrResult.getCutoutImage());
        Intent i = new Intent(getBaseContext(), ScanViewResultActivity.class);
        i.putExtra(Constant.SCAN_MODULE, scanMode);
        i.putExtra(Constant.SCAN_RESULT_DATA, scanResult);
        if (path.length == 2) {
            i.putExtra(Constant.SCAN_FULL_PICTURE_PATH, path[0]);
            i.putExtra(Constant.SCAN_FACE_PICTURE_PATH, path[1]);
        } else if (path.length == 1) {
            i.putExtra(Constant.SCAN_FULL_PICTURE_PATH, path[0]);
        }

        overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);
        startActivity(i);
    }


    protected String setupImagePath(AnylineImage image) {
        String imagePath = "";
        long time = System.currentTimeMillis();
        try {
            if (this.getExternalFilesDir(null) != null) {

                imagePath = this
                        .getExternalFilesDir(null)
                        .toString() + "/results/" + "mrz_image" + time;

            } else if (this.getFilesDir() != null) {

                imagePath = this
                        .getFilesDir()
                        .toString() + "/results/" + "mrz_image" + time;

            }
            File fullFile = new File(imagePath);
            //create the directory
            fullFile.mkdirs();
            image.save(fullFile, 100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return imagePath;
    }


    @Override
    protected void onResume() {
        super.onResume();
        energyScanView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onStop();
    }
}






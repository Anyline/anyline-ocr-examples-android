package io.anyline.examples;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.appcompat.app.AppCompatActivity;


import at.nineyards.anyline.models.AnylineScanResult;
import io.anyline.view.ScanView;

public abstract class ScanningConfigurationActivity extends AppCompatActivity{

    protected abstract ScanView getScanView();

    /**
     * @return the module type view {@link io.anyline.examples.ScanModuleEnum.ScanModule}
     */
    protected abstract ScanModuleEnum.ScanModule getScanModule();


    protected <T extends AnylineScanResult> void setupScanProcessView(Context context, T result, ScanModuleEnum.ScanModule scanModule){


        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  static void setupScanProcessView(Context context, String result, ScanModuleEnum.ScanModule scanModule, Bitmap bmp, Bitmap bmp2, Bitmap faceBitmap) {


        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  static void setupScanProcessView(Context context, String result, ScanModuleEnum.ScanModule scanModule, String barcode, Bitmap bmp) {


        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setupScanResult(){

    }
}

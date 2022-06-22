package io.anyline.examples;


import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import at.nineyards.anyline.core.LicenseException;
import io.anyline.AnylineSDK;
import io.anyline.examples.baseactivities.BaseToolbarActivity;
import io.anyline.examples.basefragments.ViewPagerFragment;

/**
 * Main Activity
 */

public class MainActivity extends BaseToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            AnylineSDK.init(getString(R.string.anyline_license_key), this);
        } catch (LicenseException e) {
            // setup the alert-builder:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.error_internet_connection));
            builder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // create and show the alert dialog:
            AlertDialog dialog = builder.create();
            dialog.show();
        }


        setContentView(R.layout.activity_base_no_menu_bundle);

        //add the view pager
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,
                            new ViewPagerFragment()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

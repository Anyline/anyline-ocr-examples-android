/*
 * Anyline
 * MainActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import at.nineyards.anyline.util.DimensUtil;

/**
 * A simple activity to select one of the examples
 */
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private Intent targetIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(getString(R.string.app_name));

        ListView listView = (ListView) findViewById(R.id.list_view);
        final ActivityListAdapter adapter = new ActivityListAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItemViewType(position) == ActivityListAdapter.TYPE_HEADER) {
                    return;
                }
                try {
                    Intent intent = new Intent(MainActivity.this, Class.forName(adapter.getClassName(position)));
                    checkedStartActivity(intent);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        TextView textView = new TextView(this);
        String versionInfo = String.format("Version Info:\nApp: %s (%s), %s\nSDK: %s (%s)",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.FLAVOR ,
                at.nineyards.anyline.BuildConfig.VERSION_NAME,at.nineyards.anyline.BuildConfig.VERSION_CODE);
        textView.setText(versionInfo);
        int padding = DimensUtil.getPixFromDp(this, 16);
        textView.setPadding(padding, padding, padding, padding);
        listView.addFooterView(textView);
    }

    private void checkedStartActivity(Intent intent) {
        this.targetIntent = intent;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            //use this if the usage of the permission is not clear
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.CAMERA},
//                        PERMISSIONS_REQUEST_CAMERA);
//            }

            // No explanation needed, it should be obvious that the camera is needed to scan something
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);

            return;
        }

        startActivity(intent);
    }

    // part of the Example of how to use the new Permission model of Android Marshmallow
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivity(targetIntent);

                } else {

                    new AlertDialog.Builder(this)
                            .setTitle(R.string.camera_permission_required)
                            .setMessage(R.string.camera_permission_required_details)
                            .show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}

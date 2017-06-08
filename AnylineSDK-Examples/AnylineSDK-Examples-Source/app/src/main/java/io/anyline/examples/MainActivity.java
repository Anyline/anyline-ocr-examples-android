/*
 * Anyline
 * MainActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import at.nineyards.anyline.camera.CameraPermissionHelper;
import at.nineyards.anyline.util.DimensUtil;

/**
 * A simple activity to select one of the examples
 */
public class MainActivity extends AppCompatActivity {

    private Intent targetIntent;
    private CameraPermissionHelper cameraPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(getString(R.string.app_name));

        ListView listView = (ListView) findViewById(R.id.list_view);
        final ActivityListAdapter adapter = new ActivityListAdapter(this);
        listView.setAdapter(adapter);

        final TextView versionTextView = new TextView(this);
        String versionInfo = String.format("Version Info:\nApp: %s (%s), %s\nSDK: %s (%s)",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.FLAVOR,
                at.nineyards.anyline.BuildConfig.VERSION_NAME, at.nineyards.anyline.BuildConfig.VERSION_CODE);
        versionTextView.setText(versionInfo);
        int padding = DimensUtil.getPixFromDp(this, 16);
        versionTextView.setPadding(padding, padding, padding, padding);
        listView.addFooterView(versionTextView);

        // CameraPermissionHelper simplifies the request for the camera permission
        cameraPermissionHelper = new CameraPermissionHelper(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view == versionTextView || adapter.getItemViewType(position) == ActivityListAdapter.TYPE_HEADER) {
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
    }

    private void checkedStartActivity(Intent intent) {

        // ask if permissions were already granted
        if (cameraPermissionHelper.hasPermissions()) {
            startActivity(intent);
        } else {
            // otherwise request the permissions
            cameraPermissionHelper.requestPermissions();
            this.targetIntent = intent;
        }
    }

    /**
     * Callback from the permission request. Can be directly forwarded to the
     * {@link CameraPermissionHelper#onRequestPermissionsResult(int, String[], int[])} to check the Camera
     * Permissions for Anyline.
     * <p>
     * Any other permissions result requested by the app will also be forwarded to here, and should be checked after
     * the call to the {@link CameraPermissionHelper}
     *
     * @param requestCode  the code of the permission request
     * @param permissions  the requested permissions
     * @param grantResults the results of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        // CameraPermissionHelper will return true if the permission for the camera was granted (and was made via the
        // CameraPermissionHelper class)
        if (cameraPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            startActivity(targetIntent);
        } else {
            // Displays a message to the user, asking to grant the permissions for the camera in order for Anyline to
            // work
            cameraPermissionHelper.showPermissionMessage(null);
        }

        // Other app permission checks go here
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

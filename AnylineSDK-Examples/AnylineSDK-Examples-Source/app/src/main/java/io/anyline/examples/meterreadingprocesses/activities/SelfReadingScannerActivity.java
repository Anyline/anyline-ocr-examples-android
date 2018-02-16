package io.anyline.examples.meterreadingprocesses.activities;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;
import io.anyline.examples.meterreadingprocesses.fragments.SelfReadingScannerFragment;

public class SelfReadingScannerActivity extends BaseToolbarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order);

        if (getIntent() != null) {
            SelfReadingScannerFragment selfReadingScannerFramgnet = new SelfReadingScannerFragment();
            FragmentTransaction transaction =  this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, selfReadingScannerFramgnet);
            transaction.commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

package io.anyline.examples.baseactivities;

import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.anyline.examples.R;

/**
 * CThis class represents the toolbar itself
 */

public abstract class BaseToolbarActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    @Override
    protected void onStart() {
        super.onStart();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null && getSupportActionBar() == null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public void setTitle(@StringRes int res) {
        if (mToolbar != null) {
            mToolbar.setTitle(res);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }
}

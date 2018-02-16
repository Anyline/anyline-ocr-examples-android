package io.anyline.examples.baseadapters;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import io.anyline.examples.basefragments.ProductsFragment;

/**
 * This  class represents the main View Pager Adapter
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    Context context=null;

    public ViewPagerAdapter(Context context, FragmentManager mgr) {
        super(mgr);
        this.context=context;
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new ProductsFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 1;
    }

    @Override
    public String getPageTitle(int position) {
        switch (position){
            case 0:
                return "Products".toLowerCase();
        }
        return null;
    }

}
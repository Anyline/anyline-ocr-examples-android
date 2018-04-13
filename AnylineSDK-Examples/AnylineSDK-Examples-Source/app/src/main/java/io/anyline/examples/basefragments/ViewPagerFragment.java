package io.anyline.examples.basefragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import io.anyline.examples.R;
import io.anyline.examples.baseadapters.ViewPagerAdapter;
//import io.anyline.examples.settings.SettingsActivity;

public class ViewPagerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View result=inflater.inflate(R.layout.view_pager, container, false);
        ViewPager pager=(ViewPager)result.findViewById(R.id.pager);
        pager.setAdapter(buildAdapter());

        return(result);
    }

    private PagerAdapter buildAdapter() {
        return(new ViewPagerAdapter(getActivity(), getChildFragmentManager()));
    }
}

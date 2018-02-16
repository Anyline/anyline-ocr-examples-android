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
import io.anyline.examples.settings.SettingsActivity;

public class ViewPagerFragment extends Fragment {


    LinearLayout logoLinearLayout;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View result=inflater.inflate(R.layout.view_pager, container, false);
        ViewPager pager=(ViewPager)result.findViewById(R.id.pager);
        pager.setAdapter(buildAdapter());

        logoLinearLayout = (LinearLayout) result.findViewById (R.id.anyline_logo_container);
        logoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);
            }
        });
        return(result);
    }

    private PagerAdapter buildAdapter() {
        return(new ViewPagerAdapter(getActivity(), getChildFragmentManager()));
    }
}

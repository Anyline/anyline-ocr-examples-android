package io.anyline.examples.basefragments;


import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import io.anyline.examples.BuildConfig;
import io.anyline.examples.R;
import io.anyline.examples.baseadapters.ViewPagerAdapter;
//import io.anyline.examples.settings.SettingsActivity;

public class ViewPagerFragment extends Fragment {

    private LinearLayout logoLinearLayout;

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
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()); //Read Update
                alertDialog.setTitle("Build Version");
                alertDialog.setMessage(
                        getString(R.string.app_build_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE) +
                                "\n" +
                                getString(R.string.sdk_build_version, at.nineyards.anyline.BuildConfig.VERSION_NAME_ANYLINE_SDK)
                );

                alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });

                alertDialog.create();
                alertDialog.show();  //<-- See This!
            }
        });

        return(result);
    }

    private PagerAdapter buildAdapter() {
        return(new ViewPagerAdapter(getActivity(), getChildFragmentManager()));
    }

}

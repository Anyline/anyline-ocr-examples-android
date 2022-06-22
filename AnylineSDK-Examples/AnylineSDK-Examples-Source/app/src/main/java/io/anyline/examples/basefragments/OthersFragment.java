package io.anyline.examples.basefragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.anyline.examples.baseadapters.BaseGridListAdapter;
import io.anyline.examples.R;

public class OthersFragment extends BaseFragment implements BaseGridListAdapter.OnItemClickListener {

    public static final String VEHICLE = "VEHICLE_FRAGMENT";
    public static final String DOCUMENT_IDENTITY_FRAGMENT = "DOCUMENT_IDENTITY_FRAGMENT";

    private boolean isVehicle = false;
    private boolean isIdentityDocument = false;

    private String[] classes;
    private String[] names;

    public OthersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isVehicle = getArguments().getBoolean(VEHICLE);
            isIdentityDocument = getArguments().getBoolean(DOCUMENT_IDENTITY_FRAGMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Resources res = getContext().getResources();
        if (isIdentityDocument) {

            classes = res.getStringArray(R.array.example_identity_documents_classes);
            names = res.getStringArray(R.array.example_identity_documents_names);

        } else if (isVehicle) {

            classes = res.getStringArray(R.array.example_vehicle_classes);
            names = res.getStringArray(R.array.example_vehicle_names);

        } else {

            classes = res.getStringArray(R.array.example_others_classes);
            names = res.getStringArray(R.array.example_others_names);
        }

        View view = inflateFragment(R.layout.recycler_viewer_toolbar_fragment, inflater, container, names, classes, this);
        setHasOptionsMenu(true);

        return view;
    }


    @Override
    public void onItemClick(int position) {

        try {
            if (mAdapter.getItemViewType(position) == 1) {
                Fragment fragment = null;
                switch (mAdapter.getItemName(position)) {

                    default:
                        Intent intent = new Intent(getActivity(), Class.forName(mAdapter.getClassName(position)));
                        checkedStartActivity(intent);
                        break;

                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                // Do onlick on menu action here
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

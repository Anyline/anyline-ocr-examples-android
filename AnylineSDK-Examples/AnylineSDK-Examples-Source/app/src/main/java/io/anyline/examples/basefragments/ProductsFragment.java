package io.anyline.examples.basefragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.anyline.examples.R;
import io.anyline.examples.baseadapters.BaseGridListAdapter;


public class ProductsFragment extends BaseFragment implements BaseGridListAdapter.OnItemClickListener {

    protected String[] classes;
    protected String[] names;

    public ProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Resources res = getContext().getResources();
        classes = res.getStringArray(R.array.example_products_classes);
        names = res.getStringArray(R.array.example_products_names);
        View view = inflateFragment(R.layout.recycle_viewer_fragment, inflater, container, names, classes,this);

        return view;
    }


    @Override
    public void onItemClick(int position) {

        try {
            if(mAdapter.getItemViewType(position) == 1) {
                Fragment fragment = null;
                switch (mAdapter.getItemName(position)) {
                    case "Meter Reading":

                        fragment = new EnergyFragment();
                        replaceFragment(fragment, "ENERGY_FRAGMENT");
                        break;

                    case "Others":
                        fragment = OthersFragment.newInstance(false);
                        replaceFragment(fragment, "OTHERS_FRAGMENT");
                        break;

                    case "MRO":
                        fragment = OthersFragment.newInstance(true);
                        replaceFragment(fragment, "OTHERS_FRAGMENT");
                        break;


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
}

package io.anyline.examples.basefragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.anyline.examples.baseadapters.BaseGridListAdapter;
import io.anyline.examples.R;
import io.anyline.examples.meterreadingprocesses.activities.SelfReadingScannerActivity;
import io.anyline.examples.meterreadingprocesses.activities.WorkOrdersActivity;

/**
 * Created by lorena on 24.11.17.
 */

public class EnergyFragment extends BaseFragment implements BaseGridListAdapter.OnItemClickListener {

    public EnergyFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Resources res = getContext().getResources();
        String[] classes = res.getStringArray(R.array.example_energy_classes);
        String[] names = res.getStringArray(R.array.example_energy_names);
        View view = inflateEnergyFragment(R.layout.recycler_viewer_toolbar_fragment, inflater, container, names, classes, this);

        mRecyclerView.setAdapter(mAdapter);

        return view;
    }


    @Override
    public void onItemClick(int position) {

        try {
            if(mAdapter.getItemViewType(position) == mAdapter.TYPE_ITEM) {

                    switch (mAdapter.getItemName(position)) {

                        case "Workforce":
                            Intent workForceIntent = new Intent(getActivity(), WorkOrdersActivity.class);
                            getActivity().startActivity(workForceIntent);
                            break;
                        case "Self Reading":
                            Intent customerSelfReadingIntent = new Intent(getActivity(),SelfReadingScannerActivity.class);
                            getActivity().startActivity(customerSelfReadingIntent);
                            break;
                        default:
                            Intent intent = new Intent(getActivity(), Class.forName(mAdapter.getClassName(position)));
                            checkedStartActivity(intent);
                            break;
                    }
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

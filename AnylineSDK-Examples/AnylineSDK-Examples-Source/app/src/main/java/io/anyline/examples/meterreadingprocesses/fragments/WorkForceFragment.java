package io.anyline.examples.meterreadingprocesses.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;
import io.anyline.examples.R;
import io.anyline.examples.meterreadingprocesses.adapters.WorkForceAdapter;
import io.anyline.examples.model.Order;
import io.anyline.examples.database.DataBaseProcessesAdapter;
/**
 * Created by lorena on 06.12.17.
 */

public class WorkForceFragment extends Fragment {

    private boolean isSyncState = true;
    private int unsyncCount = 0;
    private List<Order> orders;

    private RecyclerView recyclerView;
    private WorkForceAdapter adapter;
    private TextView syncStatusTextView;
    private TextView syncButtonTextView;
    private TextView processTitleTextView;
    private ImageView syncStatusIconImageView;
    private DataBaseProcessesAdapter dataBaseHelper;
    private ProgressBar progressBar;

    public WorkForceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.work_orders_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        syncStatusTextView = (TextView) view.findViewById(R.id.status);
        syncButtonTextView = (TextView) view.findViewById(R.id.sync_button);
        processTitleTextView = (TextView) view.findViewById(R.id.process_name);
        syncStatusIconImageView = (ImageView) view.findViewById(R.id.war_icon);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar_view);

        setupOrdersDetails();

        return view;
    }

    private void loadData() {

        try {
            dataBaseHelper = new DataBaseProcessesAdapter(getActivity().getApplicationContext());
            if (orders == null) {
                orders = dataBaseHelper.getOrders();

            }

            if (dataBaseHelper.unsyncCustomers() > 0) {
                unsyncCount = dataBaseHelper.unsyncCustomers();
                isSyncState = false;
            }
        }catch(Exception ex){
            System.out.print(ex);
        }
    }

    private void setupOrdersDetails(){

        //set the title for the specific meter reading process
        processTitleTextView.setText(getResources().getString(R.string.workforce_title));

        loadData();
        adapter = new WorkForceAdapter(getActivity(), orders);
        recyclerView.setAdapter(adapter);
        //check if all orders are synchronized
        setSyncState();
    }

    private void setSyncState() {

        //if orders are synchronized
        if(isSyncState) {
            syncStatusIconImageView.setImageResource(R.drawable.blue_round_checkmark);
            syncStatusTextView.setText(R.string.screen_workorders_synced);
            syncButtonTextView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
        else {
            if(unsyncCount == 1) {
                syncStatusTextView.setText(
                        String.format(getString(R.string.screen_workorders_unsynced_reports_singular), unsyncCount));
            }else {
                syncStatusTextView.setText(
                        String.format(getString(R.string.screen_workorders_unsynced_reports_plural), unsyncCount));
            }

            syncStatusIconImageView.setImageResource(R.drawable.ic_warning_dark_grey_24dp);
            syncButtonTextView.setVisibility(View.VISIBLE);
            syncButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    synchronizeOrders();
                }
            });
        }
    }

    private void synchronizeOrders() {

        progressBar.setVisibility(View.VISIBLE);
        dataBaseHelper.syncCustomers();
        isSyncState = true;
        setSyncState();

    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}

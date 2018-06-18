package io.anyline.examples.meterreadingprocesses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import at.nineyards.anyline.camera.CameraPermissionHelper;
import io.anyline.examples.R;
import io.anyline.examples.meterreadingprocesses.adapters.WorkOrderAdapter;
import io.anyline.examples.basefragments.BaseFragment;
import io.anyline.examples.meterreadingprocesses.activities.CustomerIdentifyScannerActivity;
import io.anyline.examples.model.Customer;
import io.anyline.examples.database.DataBaseProcessesAdapter;

/**
 * Created by lorena on 06.12.17.
 */

public class WorkOrderFragment extends BaseFragment {

    private final static String ORDER_ID = "KEY_ORDER_ID";

    private int orderId = -1;
    private List<Customer> customerList;

    private TextView mainOrderNameTextView;
    private TextView processName;
    private Button startScanningButton;
    private RecyclerView recyclerView;
    private WorkOrderAdapter adapter;
    private DataBaseProcessesAdapter dataBase;

    public static WorkOrderFragment newInstance(int orderId) {
        WorkOrderFragment fragment = new WorkOrderFragment();
        Bundle args = new Bundle();
        args.putInt(ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getInt(ORDER_ID);
        }
    }

    @Override
    public void onResume() {
        recyclerView.getAdapter().notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.work_order_fragment, container, false);

        mainOrderNameTextView = (TextView) view.findViewById(R.id.order_name);
        processName = (TextView) view.findViewById(R.id.process_name);
        startScanningButton = (Button) view.findViewById(R.id.button_start);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        setupCustomerDetails();
        processName.setVisibility(View.GONE);
        mainOrderNameTextView.setText("Order #" + orderId);
        startScanningButton.setText(getString(R.string.start));
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                startScanningProcess();

            }
        });

        return view;

    }

    private void loadData() {
        try {
            if (orderId < 0) {
                getActivity().finish();
                return;
            }
            dataBase = new DataBaseProcessesAdapter(getActivity().getApplicationContext());
            customerList = dataBase.getCustomersByOrderId(orderId);
        }catch (Exception ex){}
    }

    private void setupCustomerDetails(){
        //get the customer list from dataBase
        loadData();
        //setup the adapter for customers list
        adapter = new WorkOrderAdapter(getActivity(), customerList);
        recyclerView.setAdapter(adapter);

    }

    private void startScanningProcess() {
        Intent intent = new Intent(getActivity(), CustomerIdentifyScannerActivity.class);
        cameraPermissionHelper = new CameraPermissionHelper(getActivity());
        intent.putExtra(CustomerIdentifyScannerActivity.KEY_ORDER_ID, orderId);
        intent.putExtra(CustomerIdentifyScannerActivity.IS_WORKFORCE_PROCESS, true);
        checkedStartActivity(intent);
    }

}

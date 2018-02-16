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

import io.anyline.examples.R;
import io.anyline.examples.meterreadingprocesses.activities.CustomerIdentifyScannerActivity;
import io.anyline.examples.meterreadingprocesses.adapters.CustomerSelfReadingAdapter;
import io.anyline.examples.basefragments.BaseFragment;
import io.anyline.examples.model.Reading;
import io.anyline.examples.database.DataBaseProcessesAdapter;

/**
 * Created by lorena on 21.12.17.
 */

public class SelfReadingScannerFragment extends BaseFragment {

    private List<Reading> readingList;
    private CustomerSelfReadingAdapter adapter;
    private DataBaseProcessesAdapter dataBase;

    private Button startScanningButton;
    private RecyclerView recyclerView;
    private TextView mainOrderNameTextView;
    private TextView processNameTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.work_order_fragment, container, false);

        mainOrderNameTextView = (TextView) view.findViewById(R.id.order_name);
        processNameTextView = (TextView) view.findViewById(R.id.process_name);
        startScanningButton = (Button) view.findViewById(R.id.button_start);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        setupSelfReadingHistory();

        processNameTextView.setText(getResources().getString(R.string.self_reading_title));
        mainOrderNameTextView.setText(getResources().getString(R.string.customer_self_reading_history));
        startScanningButton.setText(getString(R.string.scan));
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanningProcess();

            }
        });

        return view;

    }

    private void setupSelfReadingHistory(){
        loadData();
        adapter = new CustomerSelfReadingAdapter(getActivity(), readingList);
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        try {
            //interrogate dataBase for all readings which are customerSelfReading
            dataBase = new DataBaseProcessesAdapter(getActivity().getApplicationContext());
            readingList = dataBase.getHistorySelfScan();
        }
        catch(Exception ex){

        }
    }

    private void startScanningProcess() {
        Intent intent = new Intent(this.getActivity(), CustomerIdentifyScannerActivity.class);
        intent.putExtra(CustomerIdentifyScannerActivity.IS_WORKFORCE_PROCESS, false);
        //check the rights for the camera access
        checkedStartActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSelfReadingHistory();
    }

}
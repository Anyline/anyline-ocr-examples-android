package io.anyline.examples.meterreadingprocesses.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;
import io.anyline.examples.meterreadingprocesses.fragments.CustomerFragment;
import io.anyline.examples.model.Customer;
import io.anyline.examples.model.Reading;


public class CustomerActivity extends BaseToolbarActivity {

    public static final String KEY_CUSTOMER = "customer_detail";
    public static final String KEY_READING = "reading";

    Customer customer = null;
    Reading reading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order);

        if(getIntent() != null) {
            Bundle readingBundle = getIntent().getBundleExtra(KEY_CUSTOMER);
            Bundle readingB = getIntent().getBundleExtra(KEY_READING);
            if(readingBundle!=null)
                customer = readingBundle.getParcelable("bundleCustomer");
            if(readingB != null)
                reading = readingB.getParcelable("bundleReading");
        }

        //if both, customer and reading, are null then activity should stop
        //it means no result were find in the processes database
        if(customer == null && reading == null){
            finish();
            return;
        }

        setupCustomerFragment();

    }


    private void setupCustomerFragment(){
        CustomerFragment customerFragment = new CustomerFragment();

        Bundle args = new Bundle();
        args.putParcelable(customerFragment.KEY_CUSTOMER, customer);
        args.putParcelable(customerFragment.KEY_READING, reading);
        customerFragment.setArguments(args);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, customerFragment);
        transaction.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

}

package io.anyline.examples.meterreadingprocesses.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.anyline.examples.R;
import io.anyline.examples.model.Customer;
import io.anyline.examples.model.Reading;
import io.anyline.examples.util.BitmapUtil;
import io.anyline.examples.database.DataBaseProcessesAdapter;
/**
 * Created by lorena on 13.12.17.
 */

public class CustomerFragment extends Fragment {

    public static final String KEY_CUSTOMER = "customer";
    public static final String KEY_READING = "reading";
    public static final String KEY_PARTIAL = "key_partial";
    public static final String KEY_RESULT_ACTIVITY = "key_result_Activity";

    private boolean isResultActivity = false;
    private boolean isPartialScreen = false;

    protected TextView customerTitleTextView;
    protected TextView meterIdTextView;
    protected TextView addressTextView;
    protected TextView lastValueReadingTextView;
    protected TextView lastDateReadingTextView;
    protected ImageView resultProcessReadingImage;

    private Customer mCustomer;
    private Reading mReading;
    private DataBaseProcessesAdapter dataBase;
    private LinearLayout scannedImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCustomer = getArguments().getParcelable(KEY_CUSTOMER);
            mReading = getArguments().getParcelable(KEY_READING);
            isResultActivity = getArguments().getBoolean(KEY_RESULT_ACTIVITY);
            isPartialScreen = getArguments().getBoolean(KEY_PARTIAL);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment, container, false);

        customerTitleTextView = (TextView) view.findViewById(R.id.name);
        meterIdTextView = (TextView) view.findViewById(R.id.meter_id);
        addressTextView = (TextView) view.findViewById(R.id.address);
        lastValueReadingTextView = (TextView)view.findViewById(R.id.last_value);
        lastDateReadingTextView = (TextView) view.findViewById(R.id.last_reading_date);
        resultProcessReadingImage = (ImageView) view.findViewById(R.id.meter_image);
        scannedImage = (LinearLayout) view.findViewById(R.id.scanned_image);

        loadData();
        setupCustomerDetails();

        return view;

    }

    private void loadData() {

        try {
            if (mCustomer == null && mReading == null) {
                getActivity().finish();
            }
            if (isResultActivity) {
                if (mCustomer.getReading() != null) {
                    mReading = mCustomer.getReading();
                }
            } else {
                dataBase = new DataBaseProcessesAdapter(getActivity().getApplicationContext());
                if (mReading == null && mCustomer != null) {
                    mReading = dataBase.getLastReadingByCustomerId(mCustomer.getId());
                } else if (mCustomer == null && mReading != null) {
                    mCustomer = dataBase.getCustomerById(mReading.getCustomerId());
                }
            }

            if (mReading == null) {
                getActivity().finish();
                return;
            }
        }catch (Exception ex){

        }


    }

    private void setupCustomerDetails(){

        customerTitleTextView.setText(mCustomer.getName());
        meterIdTextView.setText("#" + mCustomer.getMeterId());
        addressTextView.setText(mCustomer.getAddress());
        resultProcessReadingImage.setImageBitmap(BitmapUtil.getBitmap(mReading.getFullImageLocalPath()));

        if(isResultActivity) {
            lastValueReadingTextView.setText(mReading.getNewReading());
            lastDateReadingTextView.setText("Last Reading Date: " + mReading.getNewReadingDate());
        }else{
            lastValueReadingTextView.setText(mReading.getLastReadingValue());
            lastDateReadingTextView.setText("Last Reading Date: " + mReading.getLastReadingDate());
        }

        if(isPartialScreen){
            scannedImage.setVisibility(View.GONE);
        }else{
            scannedImage.setVisibility(View.VISIBLE);
        }
    }

}


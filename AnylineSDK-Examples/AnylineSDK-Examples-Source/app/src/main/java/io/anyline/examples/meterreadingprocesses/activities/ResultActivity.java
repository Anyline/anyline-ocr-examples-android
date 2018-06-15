package io.anyline.examples.meterreadingprocesses.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.baseactivities.BaseToolbarActivity;
import io.anyline.examples.database.DataBaseProcessesAdapter;
import io.anyline.examples.dialog.SingleMessageDialog;
import io.anyline.examples.meterreadingprocesses.fragments.CustomerFragment;
import io.anyline.examples.model.Customer;
import io.anyline.examples.model.Reading;
import io.anyline.examples.util.BitmapUtil;

public class ResultActivity extends BaseToolbarActivity {


    public static final int ACTION_SAVE_RESULT = 101;
    public static final int ACTION_SEND_RESULT = 102;
    public static final int ACTION_RESCAN_RESULT = 103;

    public static final String KEY_CUSTOMER = "key_customer";
    public static final String KEY_MODE_SEND= "key_mode_send";
    public static final String KEY_MODE_SAVE= "key_mode_save";

    private SingleMessageDialog dialog;
    private boolean isCustomerSelfReadingProcess = false;

    private Customer customer;
    private Reading mReading;

    private TextView mMeterResult;
    private TextView mConfirmationTextView;
    private TextView button_send;
    private TextView button_finish;
    private TextView buttonScanAgain;
    private TextView cancelButton;
    private TextView confirmAcceptButton;
    private CheckBox acceptConditionCheckBox;
    protected View mControlsContainer;
    protected View mConfirmTextContainer;
    protected TextView mConfirmMsgContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        button_send = (TextView) findViewById(R.id.button_send);
        button_finish = (TextView) findViewById(R.id.button_done);
        buttonScanAgain = (TextView)findViewById(R.id.button_scan_again);
        cancelButton = (TextView) findViewById(R.id.confirm_cancel_button);
        confirmAcceptButton = (TextView) findViewById(R.id.confirm_accept_button);
        mControlsContainer = (View) findViewById(R.id.buttons_container);
        mConfirmTextContainer = (View) findViewById(R.id.confirm_container);
        mConfirmMsgContainer = (TextView) findViewById(R.id.confirm_msg_container);
        mConfirmationTextView = (TextView) findViewById(R.id.confirmation_text_view);
        acceptConditionCheckBox = (CheckBox) findViewById(R.id.confirmation_checkbox);
        mMeterResult = (TextView) findViewById(R.id.message);


        if (getIntent() != null) {
            customer = getIntent().getParcelableExtra(KEY_CUSTOMER);
            isCustomerSelfReadingProcess = getIntent().getBooleanExtra(KEY_MODE_SEND, false);
            if(customer != null) {
                mReading = customer.getReading();
            }
        }

        mMeterResult.setText(setMeterFormatResult());
        setMessageTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);

        CustomerFragment customerFragment = new CustomerFragment();
        Bundle args = new Bundle();
        args.putBoolean(CustomerFragment.KEY_RESULT_ACTIVITY, true);
        args.putParcelable(customerFragment.KEY_CUSTOMER, customer);
        customerFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, customerFragment);
        transaction.commit();

        if (isCustomerSelfReadingProcess) {
            button_finish.setVisibility(View.VISIBLE);
            buttonScanAgain.setVisibility(View.VISIBLE);
            button_send.setVisibility(View.GONE);
            button_finish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmSend();
                }
            });
            buttonScanAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteImage(mReading.getFullImageLocalPath());
                    setResult(ACTION_RESCAN_RESULT);
                    finish();
                }
            });

        } else {
            button_finish.setVisibility(View.GONE);
            buttonScanAgain.setVisibility(View.GONE);
            button_send.setVisibility(View.VISIBLE);
            button_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveResult();
                }
            });
        }

        setTitle(R.string.screen_scan_result_title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(R.string.screen_scan_result_title);
    }

    private SpannableStringBuilder setMeterFormatResult(){
        String result = customer.getReading().getNewReading();
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int i = 0, n = result.length(); i < n; i++) {
            char text = result.charAt(i);
            sb.append(" ");
            sb.append(text);
            sb.append(" ");
            sb.setSpan(new BackgroundColorSpan(Color.BLACK), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(Color.WHITE), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(" ");
        }

        return sb;
    }

    private void saveResult() {

        try {
            DataBaseProcessesAdapter db = new DataBaseProcessesAdapter(this.getApplicationContext());

            //update the reading
            mReading.setLastReadingValue(mReading.getNewReading());
            mReading.setLastReadingDate(mReading.getNewReadingDate());
            mReading.setFullImageLocalPath(mReading.getFullImageLocalPath());

            //set the flag in order to know it is customerSelfScanning
            if (isCustomerSelfReadingProcess) {
                mReading.setIsScanned(true);
            }

            customer.setIsSynced(0);
            customer.setIsCompleted(1);

            //need a transaction
            db.insertReading(mReading);
            db.updateCustomer(customer);
            ScanActivity.setupScanProcessView(ResultActivity.this, mReading.getNewReading().toString() ,ScanModuleEnum.ScanModule.BARCODE, BitmapUtil.getBitmap(mReading.getCutoutImageLocalPath()));


            dialog = new SingleMessageDialog(this, R.string.saved, R.drawable.blue_round_checkmark);

            dialog.setAutoDismissMs(1000);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    setResult(RESULT_OK);
                    finish();
                }
            });

            dialog.show();
        }catch (Exception ex){

        }
    }

    @Override
    public void onBackPressed() {
        deleteImage(mReading.getFullImageLocalPath());
        finish();

    }

    private void confirmSend() {

        mMeterResult.setVisibility(View.GONE);
        mControlsContainer.setVisibility(View.GONE);
        mConfirmMsgContainer.setVisibility(View.VISIBLE);
        mConfirmTextContainer.setVisibility(View.VISIBLE);
        mConfirmationTextView.setText(String.format(getString(R.string.screen_scan_result_confirm_text),customer.getName()));

        setTitle(R.string.screen_scan_result_confirm_title);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelConfirmation();
            }
        });

        acceptConditionCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // onConfirmationTextClicked();
                onConfirmationCheckedChanged(acceptConditionCheckBox.isChecked());
            }
        });

        confirmAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm(false);
            }
        });
    }

    private void cancelConfirmation() {

        mControlsContainer.setVisibility(View.VISIBLE);
        mMeterResult.setVisibility(View.VISIBLE);
        mConfirmMsgContainer.setVisibility(View.GONE);
        mConfirmTextContainer.setVisibility(View.GONE);

        setTitle(R.string.screen_scan_result_title);
    }

    protected void onConfirmationCheckedChanged(boolean checked) {
        confirmAcceptButton.setTextColor(getResources().getColor(checked ? R.color.blue_light : R.color.black_10));
    }

    private void confirm(boolean confirmed) {

        try {
            if (!acceptConditionCheckBox.isChecked())
                return;
            //
            if (!isDataInRange() && !confirmed) {

                new AlertDialog.Builder(this)
                        .setMessage(String.format(getString(R.string.screen_scan_result_range_confirm_msg),
                                                  String.format("%1$s", mReading.getNewReading())))
                        .setCancelable(false)
                        .setPositiveButton(R.string.screen_scan_result_range_confirm_ok,
                                           new DialogInterface.OnClickListener() {

                                               public void onClick(DialogInterface dialog, int id) {
                                                   confirm(true);
                                               }
                                           })
                        .setNegativeButton(R.string.screen_scan_result_range_confirm_back, null)
                        .show();

                return;
            }else {

                DataBaseProcessesAdapter db = new DataBaseProcessesAdapter(this.getApplicationContext());

                mReading.setLastReadingValue(mReading.getNewReading());
                mReading.setLastReadingDate(mReading.getNewReadingDate());
                mReading.setFullImageLocalPath(mReading.getFullImageLocalPath());
                mReading.setIsScanned(true);

                customer.setIsSynced(0);
                db.insertReading(mReading);
                db.updateCustomer(customer);

                ScanActivity.setupScanProcessView(ResultActivity.this, mReading.getNewReading().toString() ,ScanModuleEnum.ScanModule.BARCODE, BitmapUtil.getBitmap(mReading.getCutoutImageLocalPath()));

                dialog = new SingleMessageDialog(ResultActivity.this, R.string.self_reading_screen_received_msg,
                                                 R.drawable.blue_round_checkmark);
                dialog.setAutoDismissMs(1500);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });

                dialog.show();
            }
        }catch(Exception ex){}


    }

    private boolean isDataInRange() {

        float min = (float) (Float.parseFloat(mReading.getLastReadingValue()) + customer.getAnnualConsumption() * 0.8);
        float max = (float) (Float.parseFloat(mReading.getLastReadingValue()) + customer.getAnnualConsumption() * 1.2);

        return Float.parseFloat(mReading.getNewReading()) >= min && Float.parseFloat(mReading.getNewReading()) <= max;
    }

    private void deleteImage(String path){
        File file = new File(path);
        file.delete();
        if (file.exists()) {
            try {
                file.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                getApplicationContext().deleteFile(file.getName());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home && mConfirmMsgContainer.getVisibility() == View.VISIBLE) {
            cancelConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setMessageTextSize(int unit, int size) {
        this.mMeterResult.setTextSize(unit, size);
    }

}
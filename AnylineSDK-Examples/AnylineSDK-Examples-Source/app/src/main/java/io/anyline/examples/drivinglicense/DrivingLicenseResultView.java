/*
 * Anyline
 * MrzResultView.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.drivinglicense;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.nineyards.anyline.util.DimensUtil;
import io.anyline.examples.R;

/**
 * A view to show Driving License Scan Results
 */
public class DrivingLicenseResultView extends RelativeLayout {

    private TextView numberText;
    private TextView numberText2;
    private TextView givenNamesText;
    private TextView dayOfBirthText;

    public DrivingLicenseResultView(Context context) {
        super(context);
        init();
    }

    public DrivingLicenseResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrivingLicenseResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

        setPadding(DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16),
                DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16));

        setBackgroundResource(R.drawable.driving_license_background);

        inflate(getContext(), R.layout.driving_license_result, this);

        numberText = (TextView) findViewById(R.id.text_number);
        numberText2 = (TextView) findViewById(R.id.text_number2);
        givenNamesText = (TextView) findViewById(R.id.text_given_names);
        dayOfBirthText = (TextView) findViewById(R.id.text_day_of_birth);
    }
    public void setName(String name){
        givenNamesText.setText(name.replace(" ", "\n"));
    }
    public void setDayOfBirth(String dayOfBirth){

        String dateString = dayOfBirth;
        String inputFormat = "ddMMyyyy";
        String outputFormat = "yyyy-MM-dd";
        if(Integer.parseInt(dayOfBirth.substring(2,4)) > 12 || Integer.parseInt(dayOfBirth.substring(4,6)) <= 12){
            inputFormat = "yyyyMMdd";
        }
        SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
        try {
            dateString = outputDateFormat.format(inputDateFormat.parse(dayOfBirth));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dayOfBirthText.setText(dateString);
    }
    public void setDocumentNumber(String number) {
//        numberText.setText(number);
        numberText2.setText(number);
    }
}

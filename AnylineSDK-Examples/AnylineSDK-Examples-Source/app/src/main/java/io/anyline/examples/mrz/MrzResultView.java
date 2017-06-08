/*
 * Anyline
 * MrzResultView.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.mrz;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Locale;

import at.nineyards.anyline.modules.mrz.Identification;
import at.nineyards.anyline.util.DimensUtil;
import io.anyline.examples.R;

/**
 * A view to show MRZ-Scan-Results
 */
public class MrzResultView extends RelativeLayout {

    private TextView typeText;
    private TextView codeText;
    private TextView numberText;
    private TextView surNamesText;
    private TextView givenNamesText;
    private TextView dayOfBirthText;
    private TextView expirationDateText;
    private TextView sexText;
    private TextView mrzText;

    private DateFormat mrzDateFormat;

    public MrzResultView(Context context) {
        super(context);
        init();
    }

    public MrzResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MrzResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

        setPadding(DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16),
                DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16));

        setBackgroundResource(R.drawable.passport_background);

        inflate(getContext(), R.layout.mrz_result, this);

        typeText = (TextView) findViewById(R.id.text_type);
        codeText = (TextView) findViewById(R.id.text_country_code);
        numberText = (TextView) findViewById(R.id.text_number);
        surNamesText = (TextView) findViewById(R.id.text_surnames);
        givenNamesText = (TextView) findViewById(R.id.text_given_names);
        dayOfBirthText = (TextView) findViewById(R.id.text_day_of_birth);
        expirationDateText = (TextView) findViewById(R.id.text_expiration_date);
        sexText = (TextView) findViewById(R.id.text_sex);
        mrzText = (TextView) findViewById(R.id.text_mrz);

        mrzDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, getCurrentLocale());
    }

    public void setIdentification(Identification identification) {
        typeText.setText(identification.getDocumentType());
        codeText.setText(identification.getNationalityCountryCode()
                + " [" + identification.getIssuingCountryCode() + "]");
        numberText.setText(identification.getDocumentNumber());
        surNamesText.setText(identification.getSurNames());
        givenNamesText.setText(identification.getGivenNames());

        if (identification.getDayOfBirthObject() != null) {
            dayOfBirthText.setText(mrzDateFormat.format(identification.getDayOfBirthObject()));
        }
        else {
            dayOfBirthText.setText(identification.getDayOfBirth());
        }

        if (identification.getExpirationDateObject() != null) {
            expirationDateText.setText(mrzDateFormat.format(identification.getExpirationDateObject()));
        }
        else {
            expirationDateText.setText(identification.getExpirationDate());
        }
        sexText.setText(identification.getSex());

        String mrzString = identification.getMrzString().replace("\\n", "\n");
        mrzText.setText(mrzString);
    }

    private Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }
}

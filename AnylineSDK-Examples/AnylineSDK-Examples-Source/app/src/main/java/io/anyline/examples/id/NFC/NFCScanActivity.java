package io.anyline.examples.id.NFC;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Arrays;

import at.nineyards.anyline.core.LicenseException;
import io.anyline.examples.R;
import io.anyline.nfc.NFC.DataGroup1;
import io.anyline.nfc.NFC.NFCResult;
import io.anyline.nfc.NFC.SOD;
import io.anyline.nfc.NfcDetector;
import io.anyline.nfc.TagProvider;

public class NFCScanActivity extends AppCompatActivity implements NfcDetector.NfcDetectionHandler {



    private String passportNumber;
    private String dateOfExpiry;
    private String dateOfBirth;

    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;

    private TextView tvMrzPassportNumber;
    private TextView tvMrzDateOfBirth;
    private TextView tvMrzDateOfExpiry;
    private TextView tvNFCName;
    private TextView tvNFCIssuingAuthority;
    private TextView tvNFCNationality;
    private TextView tvNFCGender;
    private TextView tvNFCDocumentNumber;
    private TextView tvNFCDateOfBirth;
    private TextView tvNFCIssuingAuthorityCode;
    private TextView tvNFCValidFrom;
    private TextView tvNFCValidUntil;
    private TextView tvNFCIssuerCountry;
    private TextView tvNFCOrganizationalUnit;
    private TextView tvNFCCertificationAuthority;
    private TextView tvConfirmButton;


    private TextView tvName;
    private TextView tvIssuingAuthority;
    private TextView tvNationality;
    private TextView tvGender;
    private TextView tvDocumentNumber;
    private TextView tvDateOfBirth;
    private TextView tvIssuingAuthorityCode;
    private TextView tvValidFrom;
    private TextView tvValidUntil;
    private TextView tvIssuerCountry;
    private TextView tvOrganizationalUnit;
    private TextView tvCertificationAuthority;

    private TextView tvLoadingHint;
    private ProgressBar progressBar;
    private TextView tvProgressNumber;
    private ImageView ivCheck;


    Handler handler = new Handler(Looper.getMainLooper());



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("NFC");
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tvMrzPassportNumber = findViewById(R.id.tv_mrz_document_number);
        tvMrzDateOfBirth = findViewById(R.id.tv_mrz_date_of_birth);
        tvMrzDateOfExpiry = findViewById(R.id.tv_mrz_date_of_expiry);
        tvNFCName = findViewById(R.id.tv_nfc_name);
        tvNFCIssuingAuthority = findViewById(R.id.tv_nfc_issuing_authority);
        tvNFCNationality = findViewById(R.id.tv_nfc_nationality);
        tvNFCGender = findViewById(R.id.tv_nfc_gender);
        tvNFCDocumentNumber = findViewById(R.id.tv_nfc_document_number);
        tvNFCDateOfBirth = findViewById(R.id.tv_nfc_date_of_birth);
        tvNFCIssuingAuthorityCode = findViewById(R.id.tv_nfc_issuing_country_code);
        tvNFCValidFrom = findViewById(R.id.tv_nfc_valid_from);
        tvNFCValidUntil = findViewById(R.id.tv_nfc_valid_until);
        tvNFCIssuerCountry = findViewById(R.id.tv_nfc_issuer_country);
        tvNFCOrganizationalUnit = findViewById(R.id.tv_nfc_organizational_unit);
        tvNFCCertificationAuthority = findViewById(R.id.tv_nfc_certification_authority);
        tvConfirmButton = findViewById(R.id.confirmation_button);
        tvConfirmButton.setVisibility(View.INVISIBLE);
        tvConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        tvName = findViewById(R.id.tv_name);
        tvIssuingAuthority = findViewById(R.id.tv_issuing_authority);
        tvNationality = findViewById(R.id.tv_nationality);
        tvGender = findViewById(R.id.tv_gender);
        tvDocumentNumber = findViewById(R.id.tv_document_number);
        tvDateOfBirth = findViewById(R.id.tv_date_of_birth);
        tvIssuingAuthorityCode = findViewById(R.id.tv_issuing_country_code);
        tvValidFrom = findViewById(R.id.tv_valid_from);
        tvValidUntil = findViewById(R.id.tv_valid_until);
        tvIssuerCountry = findViewById(R.id.tv_issuer_country);
        tvOrganizationalUnit =  findViewById(R.id.tv_organizational_unit);
        tvCertificationAuthority = findViewById(R.id.tv_certification_authority);
        progressBar = findViewById(R.id.pb_scan_waiting);
        tvLoadingHint = findViewById(R.id.tv_loading_hint);

        tvProgressNumber = findViewById(R.id.tv_progress_number);
        ivCheck = findViewById(R.id.check_mark);
        ivCheck.animate().alpha(0.0f).setDuration(0);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //already checked in starting activity if nfc exists and is switched on
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


        Intent intent = getIntent();
        if(intent != null){
            try {
                passportNumber = intent.getStringExtra("pn");
                tvMrzPassportNumber.setText(passportNumber);
                dateOfBirth = intent.getStringExtra("bd");
                tvMrzDateOfBirth.setText(dateOfBirth);
                dateOfExpiry = intent.getStringExtra("de");
                tvMrzDateOfExpiry.setText(dateOfExpiry);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        TagProvider.setTag(IsoDep.get(tag));

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {
                try {
                    NfcDetector nfcDetector = new NfcDetector(getApplicationContext(), this);
                    nfcDetector.startNfcDetection(passportNumber, dateOfBirth, dateOfExpiry);
                } catch (LicenseException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onNfcSuccess(NFCResult nfcResult) {

    }

    @Override
    public void onDg1Success(DataGroup1 dataGroup1) {
        final String firstName = dataGroup1.getFirstName();
        final String lastName = dataGroup1.getLastName();
        final String birthday = dataGroup1.getDateOfBirth();
        final String documentNumber = dataGroup1.getDocumentNumber();
        final String documentType = dataGroup1.getDocumentType();
        final String gender = dataGroup1.getGender();
        final String  issuingStateCode= dataGroup1.getIssuingStateCode();
        final String nationality = dataGroup1.getNationality();
        final String name = firstName + " " + lastName;


        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                tvLoadingHint.setVisibility(View.INVISIBLE);
                tvName.setVisibility(View.VISIBLE);
                tvIssuingAuthority.setVisibility(View.VISIBLE);
                tvNationality.setVisibility(View.VISIBLE);
                tvGender.setVisibility(View.VISIBLE);
                tvDocumentNumber.setVisibility(View.VISIBLE);
                tvDateOfBirth.setVisibility(View.VISIBLE);
                tvIssuingAuthorityCode.setVisibility(View.VISIBLE);
                tvValidFrom.setVisibility(View.VISIBLE);
                tvValidUntil.setVisibility(View.VISIBLE);
                tvIssuerCountry.setVisibility(View.VISIBLE);
                tvOrganizationalUnit.setVisibility(View.VISIBLE);
                tvCertificationAuthority.setVisibility(View.VISIBLE);
                tvConfirmButton.setVisibility(View.VISIBLE);



                tvNFCName.setText(name);
                tvNFCNationality.setText(nationality);
                tvNFCGender.setText(gender);
                tvNFCDateOfBirth.setText(birthday);
                tvNFCDocumentNumber.setText(documentNumber);
                tvNFCIssuingAuthorityCode.setText(issuingStateCode);
            }
        });
    }


    public void onDg2Success(final Bitmap faceImage) {
        //
        //        if(faceImage != null){
        //            NFCScanActivity.this.runOnUiThread(new Runnable() {
        //                @Override
        //                public void run() {
        //                    imageView.setImageBitmap(faceImage);
        //                }
        //            });
        //        }
    }


    @Override
    public void onSODSuccess(final SOD sod) {
        View[] animatedViews = new View[]{tvProgressNumber, ivCheck};
        long delayBetweenAnimations = 100L;
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvNFCValidFrom.setText(sod.getValidFromString());
                tvNFCValidUntil.setText(sod.getValidUntilString());
                tvNFCIssuerCountry.setText(sod.getIssuerCountry());
                tvNFCOrganizationalUnit.setText(sod.getIssuerOrganizationalUnit());
                tvNFCCertificationAuthority.setText(sod.getIssuerCertificationAuthority());
                tvNFCIssuingAuthority.setText(sod.getIssuerOrganization());
                tvProgressNumber.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvProgressNumber.animate().alpha(0.0f);
                    }
                }, 0);
                ivCheck.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivCheck.animate().alpha(1.0f);
                    }
                }, 1000);

            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivCheck.setVisibility(View.VISIBLE);
                ivCheck.animate().alpha(1.0f);
            }
        }, 1000);
    }

    @Override
    public void onNfcFailure(String e) {
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mNfcAdapter != null && pendingIntent != null) {
                mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        } catch (Exception e) {
            System.out.println("onResume error");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter != null){
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }
}

package io.anyline.examples.ocr;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;


public class SerialNumberSettingsMenuActivity extends BaseToolbarActivity {


    /**********************************************************************************************************************
     P R E F E R E N C E S
     *********************************************************************************************************************/

    SerialNumberPreferences prefs;

    private int prefCutoutRatioWidth;
    private int prefCutoutMaxWidth;
    private int prefCutoutCornerRadius;
    private SerialNumberPreferences.ScanAreaAlignment prefCutoutAlign;
    private int prefBasicLengthFrom;
    private int prefBasicLengthTo;
    private SerialNumberPreferences.ScanType prefBasicType;
    private String prefBasicExclude;
    private String prefAdvancedRegex;
    private Boolean prefUseBasicCharacters;


    /**********************************************************************************************************************
     V I E W S
     *********************************************************************************************************************/

    private TextView basicTextView;
    private TextView advancedTextView;

    private TextView cutoutRatioTextView;
    private TextView cutoutMaxWidthTextView;
    private TextView cutoutAlignTextView;
    private TextView cutoutCornerRadiusTextView;
    private TextView basicLengthTextView;
    private TextView basicTypeTextView;
    private TextView basicExcludeTextView;
    private TextView advancedRegexTextView;
    private TextView invalidRegexTextView;

    private RadioButton basicRadioButton;
    private RadioButton advancedRadioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_number_settings_menu);

        prefs = SerialNumberPreferences.getInstance(this);

        LinearLayout scanAreaLinearLayout = findViewById(R.id.llScanArea);
        LinearLayout basicCharacterLinearLayout = findViewById(R.id.llBasicCharacter);
        LinearLayout advancedCharacterLinearLayout = findViewById(R.id.llAdvancedCharacter);

        cutoutRatioTextView = findViewById(R.id.tvCutoutRatio);
        cutoutMaxWidthTextView = findViewById(R.id.tvCutoutWidth);
        cutoutAlignTextView = findViewById(R.id.tvCutoutAlign);
        cutoutCornerRadiusTextView = findViewById(R.id.tvCutoutCornerRadius);
        basicLengthTextView = findViewById(R.id.tvBasicLength);
        basicTypeTextView = findViewById(R.id.tvBasicType);
        basicExcludeTextView = findViewById(R.id.tvBasicExclude);
        advancedRegexTextView = findViewById(R.id.tvAdvancedRegex);
        invalidRegexTextView = findViewById(R.id.tvInvalidRegex);

        advancedTextView = findViewById(R.id.tvAdvanced);
        basicTextView = findViewById(R.id.tvBasic);

        basicRadioButton = findViewById(R.id.rbBasic);
        advancedRadioButton = findViewById(R.id.rbAdvanced);

        ImageView scanAreaImageView = findViewById(R.id.ivScanArea);
        ImageView basicImageView = findViewById(R.id.ivBasicCharacter);
        ImageView advancedImageView = findViewById(R.id.ivAdvancedCharacter);

        basicRadioButton.setOnClickListener(basicRadioButtonListener);
        advancedRadioButton.setOnClickListener(advancedRadioButtonListener);

        scanAreaImageView.setOnClickListener(
                v -> startActivity(SerialNumberSettingsScanAreaActivity.class));

        scanAreaLinearLayout.setOnClickListener(
                v -> startActivity(SerialNumberSettingsScanAreaActivity.class));


        basicImageView.setOnClickListener(
                v -> startActivity(SerialNumberSettingsBasicActivity.class));

        basicCharacterLinearLayout.setOnClickListener(
                v -> startActivity(SerialNumberSettingsBasicActivity.class));

        advancedImageView.setOnClickListener(
                v -> startActivity(SerialNumberSettingsAdvancedActivity.class));

        advancedCharacterLinearLayout.setOnClickListener(
                v -> startActivity(SerialNumberSettingsAdvancedActivity.class));
    }


    private void startActivity(final Class<? extends Activity> ActivityToOpen) {
        Intent myIntent = new Intent(SerialNumberSettingsMenuActivity.this, ActivityToOpen);
        SerialNumberSettingsMenuActivity.this.startActivity(myIntent);
    }


    private void setBasicAdvancedViewColors(int colorBasicViews, int colorAdvancedViews) {
        basicTextView.setTextColor(ContextCompat.getColor(SerialNumberSettingsMenuActivity.this, colorBasicViews));
        basicLengthTextView.setTextColor(ContextCompat.getColor(SerialNumberSettingsMenuActivity.this, colorBasicViews));
        basicTypeTextView.setTextColor(ContextCompat.getColor(SerialNumberSettingsMenuActivity.this, colorBasicViews));
        basicExcludeTextView.setTextColor(ContextCompat.getColor(SerialNumberSettingsMenuActivity.this, colorBasicViews));

        advancedTextView.setTextColor(ContextCompat.getColor(SerialNumberSettingsMenuActivity.this, colorAdvancedViews));
        advancedRegexTextView.setTextColor(ContextCompat.getColor(SerialNumberSettingsMenuActivity.this, colorAdvancedViews));
        invalidRegexTextView.setVisibility(View.GONE);

        if (prefAdvancedRegex.length() > 0) {
            try {
                Pattern.compile(prefAdvancedRegex);
            } catch (PatternSyntaxException e) {
                advancedRegexTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
                invalidRegexTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    View.OnClickListener basicRadioButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            advancedRadioButton.setChecked(false);
            setBasicAdvancedViewColors(R.color.black_50, R.color.black_25);

            prefs.putPrefUseBasicCharacters(true);

        }
    };


    View.OnClickListener advancedRadioButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            basicRadioButton.setChecked(false);
            setBasicAdvancedViewColors(R.color.black_25, R.color.black_50);

            prefs.putPrefUseBasicCharacters(false);
        }
    };


    protected void getPrefs() {
        prefCutoutRatioWidth = prefs.getPrefCutoutRatioWidth();
        prefCutoutMaxWidth = prefs.getPrefCutoutMaxWidth();
        prefCutoutCornerRadius = prefs.getPrefCutoutCornerRadius();
        prefCutoutAlign = prefs.getPrefCutoutAlign();
        prefBasicLengthFrom = prefs.getPrefBasicLengthFrom();
        prefBasicLengthTo = prefs.getPrefBasicLengthTo();
        prefBasicType = prefs.getPrefBasicType();
        prefBasicExclude = prefs.getPrefBasicExclude();
        prefAdvancedRegex = prefs.getPrefAdvancedRegex();
        prefUseBasicCharacters = prefs.getPrefUseBasicCharacters();
    }


    private void drawViews() {
        Resources res = getResources();
        cutoutRatioTextView.setText(res.getString(R.string.cutoutRatio, prefCutoutRatioWidth));
        cutoutMaxWidthTextView.setText(res.getString(R.string.cutoutMaxWidth, prefCutoutMaxWidth, "%"));
        cutoutAlignTextView.setText(res.getString(R.string.alignCutout, getCutoutAlign(prefCutoutAlign)));
        cutoutCornerRadiusTextView.setText(res.getString(R.string.cutoutCornerRadius, prefCutoutCornerRadius, "%"));
        basicLengthTextView.setText(res.getString(R.string.serialNumberLength, prefBasicLengthFrom, prefBasicLengthTo));
        basicTypeTextView.setText(res.getString(R.string.scanType, getScanType(prefBasicType)));
        basicExcludeTextView.setText(res.getString(R.string.excludeCharacters, prefBasicExclude));
        advancedRegexTextView.setText(res.getString(R.string.regex, prefAdvancedRegex));

        basicRadioButton.setChecked(prefUseBasicCharacters);
        advancedRadioButton.setChecked(!prefUseBasicCharacters);

        if (prefUseBasicCharacters) {
            setBasicAdvancedViewColors(R.color.black_50, R.color.black_25);
        } else {
            setBasicAdvancedViewColors(R.color.black_25, R.color.black_50);
        }
    }


    protected String getCutoutAlign(SerialNumberPreferences.ScanAreaAlignment align) {
        switch (align) {
            case TOP_HALF:
                return getString(R.string.alignTop);
            case CENTER:
                return getString(R.string.alignCenter);
            default:
                return getString(R.string.alignBottom);
        }
    }


    protected String getScanType(SerialNumberPreferences.ScanType scanType) {
        switch (scanType) {
            case NUMBERS_LETTERS:
                return getString(R.string.scanTypeNumbersLetters);
            case NUMBERS:
                return getString(R.string.scanTypeNumbers);
            default:
                return getString(R.string.scanTypeLetters);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        getPrefs();
        drawViews();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToolbar.setTitle(getString(R.string.serialNumberSettingsTitle));
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_50));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem edit_item = menu.add(0, 0, 0, getString(R.string.reset));
        edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.resetAll))
                   .setPositiveButton(getString(R.string.reset), dialogClickListener)
                   .setNegativeButton(getString(R.string.no), dialogClickListener);
            AlertDialog alert = builder.create();
            alert.show();

//            in case the positive button should have a different color remove the comment from the 2 lines below:
//            Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
//            pbutton.setTextColor(Color.RED);
        }

        return super.onOptionsItemSelected(item);
    }


    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    prefs.putDefaultPrefsScanArea();
                    prefs.putDefaultPrefsBasic();
                    prefs.putDefaultPrefsAdvanced();

                    // get the default prefs and redraw the views:
                    getPrefs();
                    drawViews();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

}

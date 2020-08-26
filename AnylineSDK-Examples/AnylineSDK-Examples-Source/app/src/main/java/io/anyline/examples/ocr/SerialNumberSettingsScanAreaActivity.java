package io.anyline.examples.ocr;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import androidx.appcompat.app.AlertDialog;
import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;

import static io.anyline.examples.ocr.SerialNumberPreferences.ScanAreaAlignment.BOTTOM_HALF;
import static io.anyline.examples.ocr.SerialNumberPreferences.ScanAreaAlignment.CENTER;
import static io.anyline.examples.ocr.SerialNumberPreferences.ScanAreaAlignment.TOP_HALF;


public class SerialNumberSettingsScanAreaActivity extends BaseToolbarActivity {
    private static final int WIDTH_MIN = 25;
    private static final int WIDTH_MAX = 100;

    SerialNumberPreferences prefs;

    int prefCutoutRatioWidth;
    int prefCutoutMaxWidth;
    int prefCutoutCornerRadius;
    SerialNumberPreferences.ScanAreaAlignment prefCutoutAlign;

    private TextView cutoutMaxWidthTextView;
    private TextView cutoutCornerRadiusTextView;

    private RadioButton topRadioButton;
    private RadioButton centerRadioButton;
    private RadioButton bottomRadioButton;

    Spinner widthSpinner;
    Slider cutoutMaxWidthSlider;
    Slider cutoutCornerRadiusSlider;
    ArrayAdapter<Integer> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_number_settings_scan_area);

        prefs = SerialNumberPreferences.getInstance(this);
        getPrefs();

        Resources res = getResources();

        widthSpinner= findViewById(R.id.spWidth);
        Integer[] items = new Integer[]{1,2,3,4,5,6,7,8,9,10};
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        widthSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int spinnerPosition = adapter.getPosition(prefCutoutRatioWidth);
        widthSpinner.setSelection(spinnerPosition);
        widthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                prefs.putPrefCutoutRatioWidth(position+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        cutoutMaxWidthSlider = findViewById(R.id.slCutoutMaxWidth);
        cutoutMaxWidthSlider.setValue(prefCutoutMaxWidth);
        cutoutMaxWidthSlider.setValueFrom(WIDTH_MIN);
        cutoutMaxWidthSlider.setValueTo(WIDTH_MAX);
        cutoutMaxWidthSlider.setStepSize(1);
        cutoutMaxWidthTextView = findViewById(R.id.tvCutoutMaxWidth);
        cutoutMaxWidthTextView.setText(res.getString(R.string.cutoutMaxWidth, prefCutoutMaxWidth, "%"));
        TextView widthMinTextView = findViewById(R.id.tvWidthMin);
        TextView widthMaxTextView = findViewById(R.id.tvWidthMax);
        widthMinTextView.setText(String.valueOf(WIDTH_MIN));
        widthMaxTextView.setText(String.valueOf(WIDTH_MAX));

        cutoutCornerRadiusSlider = findViewById(R.id.slCutoutCornerRadius);
        cutoutCornerRadiusSlider.setValue(prefCutoutCornerRadius);
        cutoutCornerRadiusSlider.setValueFrom(0);
        cutoutCornerRadiusSlider.setValueTo(100);
        cutoutCornerRadiusSlider.setStepSize(1);
        cutoutCornerRadiusTextView = findViewById(R.id.tvCutoutCornerRadius);
        cutoutCornerRadiusTextView.setText(res.getString(R.string.cutoutCornerRadius, prefCutoutCornerRadius, "%"));

        TextView cutoutAlignTextView = findViewById(R.id.tvCutoutAlign);
        cutoutAlignTextView.setText(res.getString(R.string.alignCutout, ""));
        topRadioButton = findViewById(R.id.rbTop);
        centerRadioButton = findViewById(R.id.rbCenter);
        bottomRadioButton = findViewById(R.id.rbBottom);

        RadioGroup rGroup = findViewById(R.id.rg);
        topRadioButton.setChecked(prefCutoutAlign == TOP_HALF);
        centerRadioButton.setChecked(prefCutoutAlign == CENTER);
        bottomRadioButton.setChecked(prefCutoutAlign == BOTTOM_HALF);

        rGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SerialNumberPreferences.ScanAreaAlignment  align;
            if (topRadioButton.isChecked()) {
                align = TOP_HALF;
            } else if (centerRadioButton.isChecked()) {
                align = CENTER;
            } else {
                align = BOTTOM_HALF;
            }
            prefs.putPrefCutoutAlign(align);
        });

        cutoutMaxWidthSlider.addOnChangeListener((slider, value, fromUser) -> {
            cutoutMaxWidthTextView.setText(res.getString(R.string.cutoutMaxWidth, (int) value, "%"));
            prefs.putPrefCutoutMaxWidth((int) value);
        });

        cutoutCornerRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            cutoutCornerRadiusTextView.setText(res.getString(R.string.cutoutCornerRadius, (int) value, "%"));
            prefs.putPrefCutoutCornerRadius((int) value);
        });
    }


    private void getPrefs() {
        prefCutoutRatioWidth = prefs.getPrefCutoutRatioWidth();
        prefCutoutMaxWidth = prefs.getPrefCutoutMaxWidth();
        prefCutoutCornerRadius = prefs.getPrefCutoutCornerRadius();
        prefCutoutAlign = prefs.getPrefCutoutAlign();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToolbar.setTitle(getString(R.string.scanAreaSettingsTitle));
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
            builder.setMessage(getString(R.string.resetScanArea))
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

                    // get the default prefs and redraw the ui:
                    getPrefs();
                    int spinnerPosition = adapter.getPosition(prefCutoutRatioWidth);
                    widthSpinner.setSelection(spinnerPosition);
                    cutoutMaxWidthSlider.setValue(prefCutoutMaxWidth);
                    cutoutCornerRadiusSlider.setValue(prefCutoutCornerRadius);
                    topRadioButton.setChecked(prefCutoutAlign == TOP_HALF);
                    centerRadioButton.setChecked(prefCutoutAlign == CENTER);
                    bottomRadioButton.setChecked(prefCutoutAlign == BOTTOM_HALF);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //"No"-button clicked
                    break;
            }
        }
    };

}

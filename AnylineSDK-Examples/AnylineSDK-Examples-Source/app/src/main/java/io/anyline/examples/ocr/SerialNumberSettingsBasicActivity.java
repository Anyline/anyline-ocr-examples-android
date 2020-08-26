package io.anyline.examples.ocr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.slider.RangeSlider;

import androidx.appcompat.app.AlertDialog;
import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;


public class SerialNumberSettingsBasicActivity extends BaseToolbarActivity {


    /**********************************************************************************************************************
     V I E W S
     *********************************************************************************************************************/

    private static final int LENGTH_MIN = 4;
    private static final int LENGTH_MAX = 20;

    SerialNumberPreferences prefs;
    int prefBasicLengthFrom;
    int prefBasicLengthTo;
    SerialNumberPreferences.ScanType prefBasicType;
    String prefBasicExclude;

    private TextView basicLengthTextView;
    private EditText basicExcludeEditText;

    private RadioButton numbersLettersRadioButton;
    private RadioButton numbersRadioButton;
    private RadioButton lettersRadioButton;
    RangeSlider lengthSlider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_number_settings_basic);

        prefs = SerialNumberPreferences.getInstance(this);
        getPrefs();

        Resources res = getResources();

        basicLengthTextView = findViewById(R.id.tvBasicLength);
        basicLengthTextView.setText(res.getString(R.string.serialNumberLength, prefBasicLengthFrom, prefBasicLengthTo));

        lengthSlider = findViewById(R.id.slLength);
        lengthSlider.setValues((float) prefBasicLengthFrom, (float) prefBasicLengthTo);
        lengthSlider.setValueFrom(LENGTH_MIN);
        lengthSlider.setValueTo(LENGTH_MAX);
        lengthSlider.setStepSize(1);
        TextView lengthMinTextView = findViewById(R.id.tvLengthMin);
        TextView lengthMaxTextView = findViewById(R.id.tvLengthMax);
        lengthMinTextView.setText(String.valueOf(LENGTH_MIN));
        lengthMaxTextView.setText(String.valueOf(LENGTH_MAX));

        lengthSlider.addOnChangeListener((slider1, value, fromUser) -> {
            int from = (int) (float) slider1.getValues().get(0);
            int to =   (int) (float) slider1.getValues().get(1);
            basicLengthTextView.setText(res.getString(R.string.serialNumberLength, from, to));
            prefs.putPrefBasicLengthFrom(from);
            prefs.putPrefBasicLengthTo(to);
        });

        RadioGroup rGroup = findViewById(R.id.rg);
        TextView basicTypeTextView = findViewById(R.id.tvInclude);
        basicTypeTextView.setText(res.getString(R.string.scanType, ""));
        numbersLettersRadioButton = findViewById(R.id.rbNumbersLetters);
        numbersRadioButton = findViewById(R.id.rbNumbers);
        lettersRadioButton = findViewById(R.id.rbLetters);

        numbersLettersRadioButton.setChecked(prefBasicType == SerialNumberPreferences.ScanType.NUMBERS_LETTERS);
        numbersRadioButton.setChecked(prefBasicType == SerialNumberPreferences.ScanType.NUMBERS);
        lettersRadioButton.setChecked(prefBasicType == SerialNumberPreferences.ScanType.LETTERS);

        rGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SerialNumberPreferences.ScanType include;
            if (numbersLettersRadioButton.isChecked()) {
                include = SerialNumberPreferences.ScanType.NUMBERS_LETTERS;
            } else if (numbersRadioButton.isChecked()) {
                include = SerialNumberPreferences.ScanType.NUMBERS;
            } else {
                include = SerialNumberPreferences.ScanType.LETTERS;
            }
            prefs.putPrefBasicType(include);
        });

        TextView basicExcludeTextView = findViewById(R.id.tvExclude);
        basicExcludeEditText = findViewById(R.id.etExclude);
        basicExcludeTextView.setText(res.getString(R.string.excludeCharacters, ""));
        basicExcludeEditText.setText(prefBasicExclude);

    }


    private void getPrefs() {
        prefBasicLengthFrom = prefs.getPrefBasicLengthFrom();
        prefBasicLengthTo = prefs.getPrefBasicLengthTo();
        prefBasicType = prefs.getPrefBasicType();
        prefBasicExclude = prefs.getPrefBasicExclude();

    }


    // if touch outside of edittext: close soft keyboard
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToolbar.setTitle(getString(R.string.basicSettingsTitle));
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_50));
    }


    @Override
    public void onBackPressed() {
        prefs.putPrefBasicExclude(basicExcludeEditText.getText().toString());
        super.onBackPressed();  // optional depending on your needs
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
            builder.setMessage(getString(R.string.resetBasic))
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
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    prefs.putDefaultPrefsBasic();

                    getPrefs(); // reads default preferences

                    // draw views with default preferences:
                    lengthSlider.setValues((float) prefBasicLengthFrom, (float) prefBasicLengthTo);
                    numbersLettersRadioButton.setChecked(prefBasicType == SerialNumberPreferences.ScanType.NUMBERS_LETTERS);
                    numbersRadioButton.setChecked(prefBasicType == SerialNumberPreferences.ScanType.NUMBERS);
                    lettersRadioButton.setChecked(prefBasicType == SerialNumberPreferences.ScanType.LETTERS);
                    basicExcludeEditText.setText(prefBasicExclude);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //"No"-button clicked
                    break;
            }
        }
    };

}

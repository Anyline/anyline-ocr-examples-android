package io.anyline.examples.ocr;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;


public class SerialNumberSettingsAdvancedActivity extends BaseToolbarActivity {


    /**********************************************************************************************************************
     V I E W S
     *********************************************************************************************************************/

    SerialNumberPreferences prefs;
    String prefAdvancedRegex;

    private EditText advancedRegexEditText;
    private TextView invalidRegexTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_number_settings_advanced);

        prefs = SerialNumberPreferences.getInstance(this);
        getPrefs();

        Resources res = getResources();

        TextView advancedRegexTextView;

        advancedRegexTextView = findViewById(R.id.tvRegex);
        advancedRegexEditText = findViewById(R.id.etRegex);
        invalidRegexTextView = findViewById(R.id.tvInvalidRegex);
        advancedRegexTextView.setText(res.getString(R.string.regex, ""));
        advancedRegexEditText.setText(prefAdvancedRegex);

        setRegexEditTextColor(prefAdvancedRegex);

        advancedRegexEditText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                setRegexEditTextColor(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }


    private void getPrefs() {
        prefAdvancedRegex = prefs.getPrefAdvancedRegex();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToolbar.setTitle(getString(R.string.advancedSettingsTitle));
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_50));
    }


    @Override
    public void onBackPressed() {
        prefs.putPrefAdvancedRegex(advancedRegexEditText.getText().toString());
        if (!prefs.getPrefUseBasicCharacters() && (advancedRegexEditText.getText().toString().length() > 0)) {
            try {
                Pattern.compile(advancedRegexEditText.getText().toString());

                // if pattern compiles without an error: go back to previous activity
                super.onBackPressed();
            } catch (PatternSyntaxException e) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.invalidRegexSwitchedToBasic))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                prefs.putPrefUseBasicCharacters(true);
                                finish();
                            }}).show();
            }
        } else {
            super.onBackPressed();
        }
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
            builder.setMessage(getString(R.string.resetAdvanced))
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
                    prefs.putDefaultPrefsAdvanced();
                    getPrefs();

                    advancedRegexEditText.setText(prefAdvancedRegex);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //"No"-button clicked
                    break;
            }
        }
    };


    private void setRegexEditTextColor(String regex) {
        advancedRegexEditText.setTextColor(ContextCompat.getColor(this, R.color.black_50));
        invalidRegexTextView.setVisibility(View.GONE);
        if (regex.length() > 0) {
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                advancedRegexEditText.setTextColor(ContextCompat.getColor(this, R.color.red));
                invalidRegexTextView.setVisibility(View.VISIBLE);
            }
        }
    }

}

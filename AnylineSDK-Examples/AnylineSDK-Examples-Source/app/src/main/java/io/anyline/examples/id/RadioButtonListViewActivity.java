package io.anyline.examples.id;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import io.anyline.examples.R;
import io.anyline.examples.barcode.BarcodeModel;
import io.anyline.examples.baseactivities.BaseToolbarActivity;

import static android.graphics.Typeface.BOLD;

public class RadioButtonListViewActivity extends BaseToolbarActivity {
    final static String PREFERENCE_TYPE_STATES = "STATES_COUNTRIES";

    private RadioButtonPreferences radioButtonPreferences;
    BarcodeModel preselectedItem;
    BarcodeModel preselectedItemUponStart; // save the selection at the beginning - to check if changes occurred
    ArrayList<BarcodeModel> itemsList;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_list_view_activity);

        radioButtonPreferences = RadioButtonPreferences.getInstance(this, PREFERENCE_TYPE_STATES);
        preselectedItem = radioButtonPreferences.get();
        if (preselectedItem == null) {
            radioButtonPreferences.setDefault();
            preselectedItem = radioButtonPreferences.get();
        }
        preselectedItemUponStart = radioButtonPreferences.get();

        itemsList = new ArrayList<>();
        try {
            JSONObject typeRegionJO = new JSONObject(Objects.requireNonNull(loadTypeRegionJSONFromAsset(this)));
            loopThroughJson(typeRegionJO, 0, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listView = findViewById(R.id.listview);
        ListAdapter adapter = new ListAdapter(this, itemsList);
        listView.setAdapter(adapter);
    }


    private void loopThroughJson(Object input, int level, String header) throws JSONException {

        if (input instanceof JSONObject) {
            Iterator<?> keys = ((JSONObject) input).keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (level == 0) {// header
                    BarcodeModel sectionCell = new BarcodeModel(null, key);
                    sectionCell.setToSectionHeader();
                    itemsList.add(sectionCell);
                } else if (level == 1) {
                    itemsList.add(new BarcodeModel(key, header));
                    itemsList.get(itemsList.size() - 1).setSelected(itemsList.get(itemsList.size() - 1).equals(preselectedItem));
                }

                if (!(((JSONObject) input).get(key) instanceof JSONArray)) {
                    if (((JSONObject) input).get(key) instanceof JSONObject) {
                        loopThroughJson(((JSONObject) input).get(key), level + 1, key);
                    }
                }
            }
        }
    }


    static String loadTypeRegionJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("template_type_and_region_mapping.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SpannableString s = new SpannableString("Select Countries / States");
        s.setSpan((new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)), 0, s.length(), 0);
        mToolbar.setTitle(s);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.clear);
    }


    public class ListAdapter extends ArrayAdapter {

        LayoutInflater inflater;
        ArrayList<BarcodeModel> itemList;

        public ListAdapter(Context context, ArrayList items) {
            super(context, 0, items);
            itemList = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BarcodeModel cell = (BarcodeModel) getItem(position);

            //If the cell is a section header we inflate the header layout
            if (cell.isSectionHeader()) {
                convertView = inflater.inflate(R.layout.raw_header, null);
                convertView.setClickable(true);
                View section_header_divider = convertView.findViewById(R.id.section_header_divider);
                // no not show a divider line above the first header:
                if (position == 0) {
                    section_header_divider.setVisibility(View.GONE);
                }
                TextView header = (TextView) convertView.findViewById(R.id.section_header);
                header.setText(cell.getBarcodeCategory());
            } else {
                convertView = inflater.inflate(R.layout.row_item_radio_button, null);
                RadioButton radioButton = convertView.findViewById(R.id.list_item_text_child);
                radioButton.setSelected(cell.isSelected());

                if (cell.isSelected()) {
                    radioButton.setTextColor(Color.BLACK);
                } else {
                    radioButton.setTextColor(Color.GRAY);
                }
                radioButton.setText(cell.getBarcodeType());
                radioButton.setChecked(cell.isSelected());

                radioButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (radioButton.isChecked()) {
                            preselectedItem = cell;
                            // de-select all elements in the list:
                            for (int i = 0; i < itemsList.size(); i++) {
                                BarcodeModel listElement = (BarcodeModel) getItem(i);
                                listElement.setSelected(false);
                            }
                            // select the currently checked element in the list:
                            cell.setSelected(true);
                        }
                        // redraw the listview to make changes visible:
                        listView.invalidateViews();
                    }
                });
            }
            return convertView;
        }
    }


    private Boolean selectionChanged() {
        return !preselectedItemUponStart.equals(preselectedItem);
    }


    private void finishActivity() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }


    private void handleBackButton() {
        // user pushed the android back button or the home button
        if (selectionChanged()) { // user changed barcode selection. ask if discard changes
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.go_back)).
                    setMessage(getString(R.string.barcode_back_message))
                   .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           finishActivity();
                       }
                   })
                   .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                       }
                   });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            finishActivity();
        }
    }


    @Override
    public void onBackPressed() {
        handleBackButton();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SpannableString s = new SpannableString("SAVE");
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 0, s.length(), 0);
        s.setSpan(new StyleSpan(BOLD), 0, s.length(), 0);
        MenuItem edit_item = menu.add(0, 0, 0, s);

        edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackButton();
            return false;
        } else if (item.getItemId() == 0) { //Save
            radioButtonPreferences.setBarcodeType(preselectedItem);
            Intent intent = new Intent();
            setResult(2, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

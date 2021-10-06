package io.anyline.examples.barcode;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;

import static android.graphics.Typeface.BOLD;

public class BarcodeListViewActivity extends BaseToolbarActivity {

    private Button restoreButton;
    private ListView list;
    private ListAdapter adapter;
    private CheckedTextView allBarcodeTypesCheckBox;
    private BarcodePrefferences barcodePrefferences;
    ArrayList<BarcodeModel> preselectedItems;
    ArrayList<BarcodeModel> preselectedItemsUponStart; // save the selected barcodes at the beginning - to check if changes occured
    ArrayList<BarcodeModel> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_list_view_activity);

        barcodePrefferences = BarcodePrefferences.getInstance(this);
        preselectedItems = barcodePrefferences.get();
        if (preselectedItems == null || preselectedItems.size() == 0) {
            barcodePrefferences.setDefault();
            preselectedItems = barcodePrefferences.get();
        }
        preselectedItemsUponStart = barcodePrefferences.get();
        itemsList = new ArrayList<>();

        list = findViewById(R.id.listview);
        itemsList = sortAndAddSections(getItems());
        View footer = LayoutInflater.from(this).inflate(R.layout.list_item_foot, null);
        restoreButton = footer.findViewById(R.id.btnRestore);
        View header = LayoutInflater.from(this).inflate(R.layout.list_item_header, null);
        allBarcodeTypesCheckBox = header.findViewById(R.id.customCheck);

        list.addFooterView(footer);
        list.addHeaderView(header);
        adapter = new ListAdapter(this, itemsList);
        list.setAdapter(adapter);

        updateAllBarcodeTypesCheckBox();

        allBarcodeTypesCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allBarcodeTypesCheckBox.isChecked()) {
                    allBarcodeTypesCheckBox.setChecked(true);
                    allBarcodeTypesCheckBox.setTextColor(Color.BLACK);
                    selectAll(true);

                } else {
                    allBarcodeTypesCheckBox.setChecked(false);
                    allBarcodeTypesCheckBox.setTextColor(Color.GRAY);
                    selectAll(false);
                }
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(BarcodeListViewActivity.this)
                        .setTitle("Restore default values")
                        .setMessage("Are you sure you want to restore the default values?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //barcodePrefferences.setDefault();
                                preselectedItems = barcodePrefferences.getDefault();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SpannableString s = new SpannableString("Barcode Types");
        s.setSpan((new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)), 0, s.length(), 0);
        mToolbar.setTitle(s);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable backIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24).mutate();
        backIcon.setColorFilter(ContextCompat.getColor(this, R.color.black_100), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backIcon);
    }


    private void updateAllBarcodeTypesCheckBox() {
        int countItems = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            if (!itemsList.get(i).isSectionHeader()) {
                countItems++;
            }
        }

        int countChecked = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            if (!itemsList.get(i).isSectionHeader()) {
                if (preselectedItems.contains(itemsList.get(i))) {
                    countChecked = countChecked + 1;
                } else {
                    if (countChecked != 0) {
                        countChecked = countChecked - 1;
                    }
                }
            }
        }

        allBarcodeTypesCheckBox.setChecked(countItems == countChecked);

        if (allBarcodeTypesCheckBox.isChecked()) {
            allBarcodeTypesCheckBox.setTextColor(Color.BLACK);
        } else {
            allBarcodeTypesCheckBox.setTextColor(Color.GRAY);
        }

        TextView textView = findViewById(R.id.countTextView);
        textView.setText(preselectedItems.size() + " Selected");

    }


    private void selectAll(Boolean add) {
        for (int i = 0; i < itemsList.size(); i++) {
            if (!itemsList.get(i).isSectionHeader()) {
                if (add) {
                    if (!preselectedItems.contains(itemsList.get(i))) {
                        preselectedItems.add(itemsList.get(i));
                    }
                } else {
                    preselectedItems.remove(itemsList.get(i));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }


    private ArrayList sortAndAddSections(ArrayList<BarcodeModel> itemList) {

        ArrayList<BarcodeModel> tempList = new ArrayList<>();

        //Loops thorugh the list and add a section before each sectioncell start
        String header = "";

        for (int i = 0; i < itemList.size(); i++) {
            //If it is the start of a new section we create a new listcell and add it to our array
            if (!(header.equals(itemList.get(i).getBarcodeCategory()))) {
                BarcodeModel sectionCell = new BarcodeModel(null, itemList.get(i).getBarcodeCategory());
                sectionCell.setToSectionHeader();
                tempList.add(sectionCell);
                header = itemList.get(i).getBarcodeCategory();
            }
            tempList.add(itemList.get(i));
        }

        return tempList;
    }


    public class ListAdapter extends ArrayAdapter {

        LayoutInflater inflater;
        ArrayList<BarcodeModel> itemList;

        int count = 0;
        int countChecked = 0;

        public ListAdapter(Context context, ArrayList items) {
            super(context, 0, items);
            itemList = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            BarcodeModel cell = (BarcodeModel) getItem(position);
            cell.setSelected(preselectedItems.contains(cell));

            //If the cell is a section header we inflate the header layout
            if (cell.isSectionHeader()) {
                v = inflater.inflate(R.layout.raw_header, null);
                v.setClickable(true);

                TextView header = (TextView) v.findViewById(R.id.section_header);
                header.setText(cell.getBarcodeCategory());
            } else {
                v = inflater.inflate(R.layout.row_item, null);
                CheckedTextView time_time = (CheckedTextView) v.findViewById(R.id.list_item_text_child);

                if (cell.isSelected()) {
                    time_time.setTextColor(Color.BLACK);
                } else {
                    time_time.setTextColor(Color.GRAY);
                }
                time_time.setText(cell.getBarcodeType());
                time_time.setChecked(cell.isSelected());

                time_time.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (!time_time.isChecked()) {
                            preselectedItems.add(cell);
                            time_time.setChecked(true);
                            time_time.setTextColor(Color.BLACK);
//                            Toast.makeText(getApplicationContext(),
//                                           "Clicked on Checkbox: " + time_time.getText() +
//                                           " is " + time_time.isChecked(),
//                                           Toast.LENGTH_LONG).show();
                        } else {
                            preselectedItems.remove(cell);
                            time_time.setChecked(false);
                            time_time.setTextColor(Color.GRAY);
//                            Toast.makeText(getApplicationContext(),
//                                           "Clicked on Checkbox: " + time_time.getText() +
//                                           " is " + time_time.isChecked(),
//                                           Toast.LENGTH_LONG).show();
                        }
                        cell.setSelected(time_time.isChecked());
                        count = 0;
                        countChecked = 0;
                        updateAllBarcodeTypesCheckBox();
                    }
                });

            }
            count = 0;
            countChecked = 0;
            updateAllBarcodeTypesCheckBox();
            return v;
        }
    }


    private Boolean selectionChanged(){
        Boolean changed = !preselectedItemsUponStart.equals(preselectedItems);
        return !preselectedItemsUponStart.equals(preselectedItems);
    }


    private void finishActivity(){
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private void handleBackButton() {
        // user pushed the android back button or the home button
        if (preselectedItems.size() == 0) { // no barcodes selected. do not allow to go back
            showAlertNoBarcodeSelected();
        } else if (selectionChanged()) { // user changed barcode selection. ask if discard changes
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


    private void showAlertNoBarcodeSelected(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("No items are selected").
                setMessage("Please select at least one barcode type before going back to scanning")
                .setPositiveButton(getString(R.string.ok), null);

        AlertDialog alert = builder1.create();
        alert.show();
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
            if (preselectedItems.size() == 0) {
                showAlertNoBarcodeSelected();
                return false;
            } else {
                barcodePrefferences.setBarcodeTypes(preselectedItems);
                //onBackPressed();
                Intent intent = new Intent();
                setResult(2, intent);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private ArrayList<BarcodeModel> getItems() {

        ArrayList<BarcodeModel> items = new ArrayList<>();
        items.add(new BarcodeModel("UPC/EAN", "1D Symbologies - Retail"));
        items.add(new BarcodeModel("GS1 Databar & Composite Codes", "1D Symbologies - Retail"));

        items.add(new BarcodeModel("Code 128", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("GS1-128", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("ISBT 128", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("Code 39", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("Trioptic Code 39", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("Code 32", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("Code 93", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("Interleaved 2 of 5", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("Matrix 2 of 5", "1D Symbologies - Logistics & Inventory Usage"));
        items.add(new BarcodeModel("One D Inverse", "1D Symbologies - Logistics & Inventory Usage"));

        items.add(new BarcodeModel("Code 25", "1D Symbologies - Legacy"));
        items.add(new BarcodeModel("Codabar", "1D Symbologies - Legacy"));
        items.add(new BarcodeModel("MSI", "1D Symbologies - Legacy"));
        items.add(new BarcodeModel("Code 11", "1D Symbologies - Legacy"));

        items.add(new BarcodeModel("US Postnet", "Postal Service"));
        items.add(new BarcodeModel("US Planet", "Postal Service"));
        items.add(new BarcodeModel("UK Postal", "Postal Service"));
        items.add(new BarcodeModel("USPS 4CB / OneCode / Intelligent Mail", "Postal Service"));

        items.add(new BarcodeModel("PDF417", "2D Symbologies"));
        items.add(new BarcodeModel("MicroPDF417", "2D Symbologies"));
        items.add(new BarcodeModel("Data Matrix", "2D Symbologies"));
        items.add(new BarcodeModel("QR Code", "2D Symbologies"));
        items.add(new BarcodeModel("MicroQR", "2D Symbologies"));
        items.add(new BarcodeModel("GS1 QR Code", "2D Symbologies"));
        items.add(new BarcodeModel("Aztec", "2D Symbologies"));
        items.add(new BarcodeModel("MaxiCode", "2D Symbologies"));

        return items;
    }
}

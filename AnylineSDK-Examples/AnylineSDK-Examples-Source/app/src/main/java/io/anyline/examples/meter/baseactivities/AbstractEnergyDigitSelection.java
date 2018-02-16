package io.anyline.examples.meter.baseactivities;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import at.nineyards.anyline.util.DimensUtil;
import io.anyline.examples.R;

/**
 * Created by stefanie on 19/05/16.
 */
abstract public class AbstractEnergyDigitSelection extends AbstractEnergyActivity {

    private RelativeLayout selectionLayout;
    private LinearLayout digitLayout;

    /**
     * @return the min amount of main digits the mode {@link at.nineyards.anyline.modules.energy.EnergyScanView.ScanMode} supports
     */
    protected abstract int getMinDigits();


    /**
     * @return the max amount of main digits the mode {@link at.nineyards.anyline.modules.energy.EnergyScanView.ScanMode} supports
     */
    protected abstract int getMaxDigits();

    /**
     * Chooses the correct mode {@link at.nineyards.anyline.modules.energy.EnergyScanView.ScanMode}
     * depending on the numVisible
     *
     * @param numVisible
     */
    protected abstract void chooseSelectionMode(int numVisible);

    /**
     * the value determines the starting scanmode; the value is linked to a
     * {@link at.nineyards.anyline.modules.energy.EnergyScanView.ScanMode}
     *
     * @return
     */
    protected abstract int startWithVisibleDigits();

    /**
     * inflates the helper for "digit selection" to the corresponding placeholder
     */
    protected abstract void inflateDigitSelectionView();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateDigitSelectionView();

        selectionLayout = (RelativeLayout) findViewById(R.id.selection_layout);
        selectionLayout.setVisibility(View.GONE);
        digitLayout = (LinearLayout) selectionLayout.findViewById(R.id.selection_area);

        int numVisible = countVisibleDigits();

        if (numVisible == getMaxDigits()) {
            selectionLayout.findViewById(R.id.add_digit).setEnabled(false);
        }
        if (numVisible == getMinDigits()) {
            selectionLayout.findViewById(R.id.remove_digit).setEnabled(false);
        }

        chooseSelectionMode(numVisible);

        selectionLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectionLayout.setVisibility(View.VISIBLE);
            }
        }, 800);


        int startingMode = startWithVisibleDigits();
        int visibleDigits = countVisibleDigits();

        //make sure the amount of visible digits == starting mode
        while (startingMode < visibleDigits) {
            visibleDigits--;
            digitLayout.getChildAt(visibleDigits).setVisibility(View.GONE);
        }
        while (startingMode > visibleDigits) {
            digitLayout.getChildAt(visibleDigits).setVisibility(View.VISIBLE);
            visibleDigits++;
        }

        //check if buttons should be disabled, because min or max amount has been selected
        if (visibleDigits == getMaxDigits()) {
            selectionLayout.findViewById(R.id.add_digit).setEnabled(false);
        } else if(visibleDigits == getMinDigits()){
            selectionLayout.findViewById(R.id.remove_digit).setEnabled(false);
        }

        chooseSelectionMode(startingMode);

    }


    /**
     *
     * @return the number of currently visible digits (refers to currently selected scanmode
     * {@link at.nineyards.anyline.modules.energy.EnergyScanView.ScanMode})
     */
    protected int countVisibleDigits() {
        int childCount = digitLayout.getChildCount();
        int count = 0;
        for (int i = 0; i < childCount; i++) {
            if (digitLayout.getChildAt(i).getVisibility() == View.VISIBLE) {
                count++;
            }
        }
        return count;
    }


    /**
     * Adds one digit to the currently selected visible digits and selects the corresponding mode
     * {@link at.nineyards.anyline.modules.energy.EnergyScanView.ScanMode}
     * @param view
     */
    public void addDigit(View view) {
        int visibleDigits = countVisibleDigits();

        if (visibleDigits == getMaxDigits()) {
            return;
        }
        digitLayout.getChildAt(visibleDigits).setVisibility(View.VISIBLE);
        visibleDigits++;
        if (visibleDigits == getMaxDigits()) {
            selectionLayout.findViewById(R.id.add_digit).setEnabled(false);
        }
        if (visibleDigits > getMinDigits()) {
            selectionLayout.findViewById(R.id.remove_digit).setEnabled(true);
        }
        chooseSelectionMode(visibleDigits);
    }

    /**
     * removes one digit to the currently selected visible digits and selects the corresponding mode
     * {@link at.nineyards.anyline.modules.energy.EnergyScanView.ScanMode}
     * @param view
     */
    public void removeDigit(View view) {
        int visibleDigits = countVisibleDigits();

        if (visibleDigits == getMinDigits()) {
            return;
        }
        digitLayout.getChildAt(visibleDigits - 1).setVisibility(View.GONE);
        visibleDigits--;
        if (visibleDigits == getMinDigits()) {
            selectionLayout.findViewById(R.id.remove_digit).setEnabled(false);
        }
        if (visibleDigits < getMaxDigits()) {
            selectionLayout.findViewById(R.id.add_digit).setEnabled(true);
        }

        chooseSelectionMode(visibleDigits);
    }

}

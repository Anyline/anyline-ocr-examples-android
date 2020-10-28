package io.anyline.examples.barcode;


/**
 * Created by Enamul on 11/1/17.
 */

public class BarcodeModel {
    private  boolean isSectionHeader;
    private boolean isSelected;

    private String barcodeType;
    private String barcodeCategory;

    public BarcodeModel(String barcodeType, String barcodeCategory) {

        this.barcodeType = barcodeType;
        this.barcodeCategory = barcodeCategory;
        isSectionHeader = false;
        isSelected = false;

    }

    public String getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(String barcodeType) {
        this.barcodeType = barcodeType;
    }

    public String getBarcodeCategory() {
        return barcodeCategory;
    }

    public void setBarcodeCategory(String barcodeCategory) {
        this.barcodeCategory = barcodeCategory;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSectionHeader() {
        return isSectionHeader;
    }

    public void setToSectionHeader() {
        isSectionHeader = true;
    }

    @Override
    public boolean equals(Object obj) {
        if(this.getBarcodeType() != null && ((BarcodeModel)obj).getBarcodeType() != null )
        if (this.getBarcodeType().equals(((BarcodeModel)obj).barcodeType)) {
            return true;
        }

        return false;
    }
}
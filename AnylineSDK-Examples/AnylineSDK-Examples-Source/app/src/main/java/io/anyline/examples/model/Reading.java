package io.anyline.examples.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

import at.nineyards.anyline.models.AnylineImage;

public class Reading implements Parcelable {


    public static final String TABLE_NAME = "Reading";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_LAST_READING_DATE = "readingDate";
    public static final String COLUMN_LAST_READING_VALUE = "readingValue";
    public static final String COLUMN_CUSTOMER_ID = "customerId";
    public static final String COLUMN_SCANNED = "isScanned";
    public static final String COLUMN_IMAGE_PATH = "imageFilePath";



    private boolean scanned;
    private int id;
    private int customerId;
    private String fullImageLocalPath;
    private String lastReadingValue;
    private String lastReadingDate;
    private String newReadingDate;
    private String newReading;
    private Customer customer;

    //this field is used only for the whole history
    private String cutoutImageLocalPath;

    public Reading(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getLastReadingValue() {
        return lastReadingValue;
    }

    public void setLastReadingValue(String lastReadingValue) {
        this.lastReadingValue = lastReadingValue;
    }

    public void setLastReadingDate(String lastReadingDate) {
        this.lastReadingDate = lastReadingDate;
    }

    public String getLastReadingDate() {
        return lastReadingDate;
    }

    public String getFullImageLocalPath() {
        return fullImageLocalPath;
    }

    public void setFullImageLocalPath(String fullImageLocalPath) {
        this.fullImageLocalPath = fullImageLocalPath;
    }

    public String getCutoutImageLocalPath() {
        return cutoutImageLocalPath;
    }

    public void setCutoutImageLocalPath(String cutoutImageLocalPath) {
        this.cutoutImageLocalPath = cutoutImageLocalPath;
    }
    //
    //    public void setFullImageLocalPathSecondTariff(String fullImageLocalPathSecondTariff) {
    //        this.fullImageLocalPathSecondTariff = fullImageLocalPath;
    //    }
    //
    public boolean isScanned() {
        return scanned;
    }

    public void setIsScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public String getNewReading() {
        return newReading;
    }

    public void setNewReading(String newReading) {
        this.newReading = newReading;
    }

    public String getNewReadingDate() {
        return newReadingDate;
    }

    public void setNewReadingDate(String newReadingDate) {
        this.newReadingDate = newReadingDate;
    }

    public Customer getCustomer(){
        return customer;
    }

    public void setCustomer(Customer readingCustomer){
        this.customer = readingCustomer;
    }

    // Parcelling part

    public Reading(Parcel in){

        this.id = in.readInt();
        this.customerId = in.readInt();
        this.lastReadingValue = in.readString();
        this.lastReadingDate = in.readString();
        this.newReading = in.readString();
        this.newReadingDate = in.readString();
        this.fullImageLocalPath = in.readString();
        this.cutoutImageLocalPath = in.readString();
        this.customer = in.readParcelable(Customer.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Reading createFromParcel(Parcel in) {
            return new Reading(in);
        }

        public Reading[] newArray(int size) {
            return new Reading[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.customerId);
        dest.writeString(this.lastReadingValue);
        dest.writeString(this.lastReadingDate);
        dest.writeString(this.newReading);
        dest.writeString(this.newReadingDate);
        dest.writeString(this.fullImageLocalPath);
        dest.writeString(this.cutoutImageLocalPath);
        dest.writeParcelable(this.customer, flags);
    }
}

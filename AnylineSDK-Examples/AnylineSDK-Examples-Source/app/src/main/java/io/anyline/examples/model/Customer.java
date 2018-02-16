package io.anyline.examples.model;

/**
 * Created by lorena on 07.12.17.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Customer implements Parcelable{

    public static final String TABLE_NAME = "Customer";
    //primary key
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ANNUAL_CONSUMPTION = "annualConsumption";
    public static final String COLUMN_METER_ID = "meterId";
    public static final String COLUMN_METER_Type = "meterType";
    public static final String COLUMN_SYNC = "isSynced";
    public static final String COLUMN_COMPLETE = "isCompleted";
    //foreign key
    public static final String COLUMN_ORDER_ID = "orderId";

    private int id;
    private int orderId;
    private int isSynced;
    private int isCompleted;
    private long annualConsumption;
    private long meterId;
    private String name;
    private String address;
    private String meterType;
    private Reading reading;


    public Customer() {
    }

    public Customer(int id, int orderId, int isSynced, int isCompleted, long annualConsumption, long meterId, String name, String address, String meterType) {
        this.id = id;
        this.orderId = orderId;
        this.isSynced = isSynced;
        this.isCompleted = isCompleted;
        this.meterId = meterId;
        this.annualConsumption = annualConsumption;
        this.name = name;
        this.address = address;
        this.meterType = meterType;
    }

    //this parts implements the Parcelable
    public Customer(Parcel in){

        this.id = in.readInt();
        this.orderId = in.readInt();
        this.isSynced = in.readInt();
        this.isCompleted = in.readInt();
        this.annualConsumption = in.readLong();
        this.meterId = in.readLong();
        this.name = in.readString();
        this.address = in.readString();
        this.meterType = in.readString();
        this.reading = in.readParcelable(Reading.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(this.id);
        dest.writeInt(this.orderId);
        dest.writeInt(this.isSynced);
        dest.writeInt(this.isCompleted);
        dest.writeLong(this.annualConsumption);
        dest.writeLong(this.meterId);
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.meterType);
        dest.writeParcelable(this.reading, flags);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }

    public int getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }

    public long getAnnualConsumption() {
        return annualConsumption;
    }

    public void setAnnualConsumption(long annualConsumption) {
        this.annualConsumption = annualConsumption;
    }

    public long getMeterId() {
        return meterId;
    }

    public void setMeterId(long meterId) {
        this.meterId = meterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMeterType() {
        return meterType;
    }

    public void setMeterType(String meterType) {
        this.meterType = meterType;
    }

    public Reading getReading(){
        return reading;
    }

    public void setReading(Reading reading){
        this.reading = reading;
    }

}


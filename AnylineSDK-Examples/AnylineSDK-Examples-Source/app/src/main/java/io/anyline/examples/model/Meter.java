package io.anyline.examples.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import at.nineyards.anyline.models.AnylineImage;

public class Meter implements Parcelable {


    public enum Type {
        ANALOG,
        EL_5_DIGITS,
        EL_6_DIGITS,
        EL_7_DIGITS,
        GAS,
        SERIAL_NUMBER,
        WATER_LIGHT,
        WATER,
        HEAT_4,
        HEAT_5,
        HEAT_6,
        ELECTRIC_DIGITAL

    }


    public static final String TABLE_NAME = "Meter";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_SPECIFIC_ID = "SpecifiId";

    private String id;
    private String type;

    public Meter() {
        setMeterType(Type.ANALOG);
    }

    public Meter(String id, String type) {
        this.id = id;
        this.type = type;
    }

    //implements parcelable
    public Meter(Parcel in){

        this.id = in.readString();
        this.type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Meter createFromParcel(Parcel in) {
            return new Meter(in);
        }

        public Meter[] newArray(int size) {
            return new Meter[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.type);
    }

    public Meter(String id, Type type) {
        this.id = id;
        setMeterType(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Type getMeterType() {
        if(TextUtils.isEmpty(type))
            return Type.ANALOG;

        if(type.equals("analog")) {
            return Type.ANALOG;
        }
        else if(type.equals("heat_4")) {
            return Type.HEAT_4;
        }
        else if(type.equals("heat_5")) {
            return Type.HEAT_5;
        }
        else if(type.equals("heat_6")) {
            return Type.HEAT_6;
        }
        else if(type.equals("electric_digital")) {
            return Type.ELECTRIC_DIGITAL;
        }

        return Type.ANALOG;
    }

    public void setMeterType(Type meterType) {

        switch (meterType) {
            case ANALOG:
                type = "analog";
                break;
            case HEAT_4:
                type = "heat_4";
                break;
            case HEAT_5:
                type = "heat_5";
                break;
            case HEAT_6:
                type = "heat_6";
                break;
            case ELECTRIC_DIGITAL:
                type = "electric_digital";
                break;
            default:
                type = "analog";
                break;
        }
    }
}

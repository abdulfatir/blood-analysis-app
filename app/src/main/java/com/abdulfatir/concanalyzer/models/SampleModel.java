package com.abdulfatir.concanalyzer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abdul on 3/12/2016.
 */
public class SampleModel implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(mIntensity);
        parcel.writeDouble(mConcentration);
        parcel.writeInt(mDataPointType.ordinal());
        parcel.writeByte((byte) (mUpdated ? 1 : 0));
    }

    public static final Parcelable.Creator<SampleModel> CREATOR = new Parcelable.Creator<SampleModel>() {
        @Override
        public SampleModel createFromParcel(Parcel parcel) {
            return new SampleModel(parcel);
        }

        @Override
        public SampleModel[] newArray(int i) {
            return new SampleModel[i];
        }
    };

    public enum DataPointType {
        NONE,
        UNKNOWN,
        QUALITY_CONTROL,
        KNOWN

    }

    private double mIntensity;
    private double mConcentration;
    private DataPointType mDataPointType;
    private boolean mUpdated;

    public SampleModel() {
        this.mIntensity = 0;
        this.mConcentration = 0;
        this.mDataPointType = DataPointType.NONE;
        this.mUpdated = false;
    }

    private SampleModel(Parcel in) {
        this.mIntensity = in.readDouble();
        this.mConcentration = in.readDouble();
        this.mDataPointType = DataPointType.values()[in.readInt()];
        this.mUpdated = in.readByte() != 0;
    }

    public SampleModel(double mIntensity) {
        this.mIntensity = mIntensity;
        this.mConcentration = 0;
        this.mDataPointType = DataPointType.NONE;
        this.mUpdated = false;
    }

    public boolean isUpdated() {
        return mUpdated;
    }

    public void setUpdated(boolean updated) {
        this.mUpdated = updated;
    }

    public double getConcentration() {
        return mConcentration;
    }

    public void setConcentration(double concentration) {
        this.mConcentration = concentration;
    }

    public DataPointType getDataPointType() {
        return mDataPointType;
    }

    public void setDataPointType(DataPointType dataPointType) {
        this.mDataPointType = dataPointType;
    }

    public double getIntensity() {
        return mIntensity;
    }

    public void setIntensity(double intensity) {
        this.mIntensity = intensity;
    }

    @Override
    public String toString() {
        return "{Intensity:" + this.mIntensity
                + ", Concentration:" + this.mConcentration
                + ", Updated:" + this.mUpdated
                + "}";
    }
}
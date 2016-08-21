package com.abdulfatir.concanalyzer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abdul on 3/12/2016.
 * An instance of this class represents a detected region with an average intensity, type, and concentration.
 */
public class SampleModel implements Parcelable {
    /**
     * The constant CREATOR for Parcelable interface.
     */
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
    private double mIntensity;
    private double mConcentration;
    private DataPointType mDataPointType;
    private boolean mUpdated;

    /**
     * Instantiates a new Sample model.
     */
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

    /**
     * Instantiates a new Sample model.
     *
     * @param mIntensity the intensity value
     */
    public SampleModel(double mIntensity) {
        this.mIntensity = mIntensity;
        this.mConcentration = 0;
        this.mDataPointType = DataPointType.NONE;
        this.mUpdated = false;
    }

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

    /**
     * Returns true if this SampleModel has been updated with valid data.
     *
     * @return the boolean
     */
    public boolean isUpdated() {
        return mUpdated;
    }

    /**
     * Sets if this SampleModel has been updated with valid data.
     *
     * @param updated the updated
     */
    public void setUpdated(boolean updated) {
        this.mUpdated = updated;
    }

    /**
     * Gets concentration.
     *
     * @return the concentration
     */
    public double getConcentration() {
        return mConcentration;
    }

    /**
     * Sets concentration.
     *
     * @param concentration the concentration
     */
    public void setConcentration(double concentration) {
        this.mConcentration = concentration;
    }

    /**
     * Gets data point type.
     *
     * @return the data point type
     */
    public DataPointType getDataPointType() {
        return mDataPointType;
    }

    /**
     * Sets data point type.
     *
     * @param dataPointType the data point type
     */
    public void setDataPointType(DataPointType dataPointType) {
        this.mDataPointType = dataPointType;
    }

    /**
     * Gets intensity.
     *
     * @return the intensity
     */
    public double getIntensity() {
        return mIntensity;
    }

    /**
     * Sets intensity.
     *
     * @param intensity the intensity
     */
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

    /**
     * The enum Data point type.
     */
    public enum DataPointType {
        /**
         * None data point type.
         */
        NONE,
        /**
         * Unknown data point type.
         */
        UNKNOWN,
        /**
         * Quality control data point type.
         */
        QUALITY_CONTROL,
        /**
         * Known data point type.
         */
        KNOWN
    }
}
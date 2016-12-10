package com.abdulfatir.concanalyzer.models;

/**
 * Created by abdulfatir on 10/12/16.
 */
public class DefaultsModel {
    private int index;
    private double concentration;
    private SampleType sampleType;
    public enum SampleType
    {
        KNOWN,
        QUALITY_CONTROL
    }

    public DefaultsModel(int index, double concentration, SampleType sampleType) {
        this.index = index;
        this.concentration = concentration;
        this.sampleType = sampleType;
    }

    public double getConcentration() {
        return concentration;
    }

    public void setConcentration(double concentration) {
        this.concentration = concentration;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public int getIndex() {
        return index;
    }

}

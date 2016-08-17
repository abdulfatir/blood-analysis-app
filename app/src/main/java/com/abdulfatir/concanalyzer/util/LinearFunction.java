package com.abdulfatir.concanalyzer.util;

import java.util.Locale;

/**
 * Created by Abdul on 3/22/2016.
 */
public class LinearFunction {
    private double slope;
    private double intercept;

    public LinearFunction(double intercept, double slope) {
        this.intercept = intercept;
        this.slope = slope;
    }

    public LinearFunction(LinearFunction l) {
        this.slope = l.slope;
        this.intercept = l.intercept;
    }

    public LinearFunction swapAxes() {
        return new LinearFunction(this.intercept / this.slope, -1 / this.slope);
    }

    public double getIntercept() {
        return intercept;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public double eval(double x) {
        return slope * x + intercept;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s%.2f%s%.2f", "y = ", slope, "x + ", intercept);
    }
}

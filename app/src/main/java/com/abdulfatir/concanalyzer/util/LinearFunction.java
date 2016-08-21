package com.abdulfatir.concanalyzer.util;

import java.util.Locale;

/**
 * Created by Abdul on 3/22/2016.
 * Represents a function of type y = mx + c
 */
public class LinearFunction {
    private double slope;
    private double intercept;

    /**
     * Instantiates a new Linear function.
     *
     * @param intercept the intercept
     * @param slope     the slope
     */
    public LinearFunction(double intercept, double slope) {
        this.intercept = intercept;
        this.slope = slope;
    }

    /**
     * Instantiates a new Linear function.
     *
     * @param l the l
     */
    public LinearFunction(LinearFunction l) {
        this.slope = l.slope;
        this.intercept = l.intercept;
    }

    /**
     * Swap axes linear function.
     *
     * @return the linear function
     */
    public LinearFunction swapAxes() {
        return new LinearFunction(this.intercept / this.slope, -1 / this.slope);
    }

    /**
     * Gets intercept.
     *
     * @return the intercept
     */
    public double getIntercept() {
        return intercept;
    }

    /**
     * Sets intercept.
     *
     * @param intercept the intercept
     */
    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }

    /**
     * Gets slope.
     *
     * @return the slope
     */
    public double getSlope() {
        return slope;
    }

    /**
     * Sets slope.
     *
     * @param slope the slope
     */
    public void setSlope(double slope) {
        this.slope = slope;
    }

    /**
     * Eval double.
     *
     * @param x the x
     * @return the double
     */
    public double eval(double x) {
        return slope * x + intercept;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s%.2f%s%.2f", "y = ", slope, "x + ", intercept);
    }
}

package com.abdulfatir.concanalyzer.models;

import java.util.Comparator;

/**
 * Created by abdulfatir on 13/08/16.
 */
public class IdAbscissaMap implements Comparable {
    private int id;
    private int abscissa;

    public IdAbscissaMap(int abscissa, int id) {
        this.abscissa = abscissa;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAbscissa() {
        return abscissa;
    }

    public void setAbscissa(int abscissa) {
        this.abscissa = abscissa;
    }

    @Override
    public int compareTo(Object o) {
        IdAbscissaMap that = (IdAbscissaMap) o;
        if (getAbscissa() > that.getAbscissa())
            return 1;
        else if (getAbscissa() < that.getAbscissa())
            return -1;
        return 0;
    }

    @Override
    public String toString() {
        return "{id:" + id + ", X:" + abscissa + "}";
    }
}

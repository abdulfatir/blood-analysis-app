package com.abdulfatir.concanalyzer.models;

/**
 * Created by abdulfatir on 13/08/16.
 * A helper model to sort detected blobs by x co-ordinate.
 */
public class IdAbscissaMap implements Comparable {
    private int id;
    private int abscissa;

    /**
     * Instantiates a new Id abscissa map.
     *
     * @param abscissa the abscissa
     * @param id       the id
     */
    public IdAbscissaMap(int abscissa, int id) {
        this.abscissa = abscissa;
        this.id = id;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets abscissa.
     *
     * @return the abscissa
     */
    public double getAbscissa() {
        return abscissa;
    }

    /**
     * Sets abscissa.
     *
     * @param abscissa the abscissa
     */
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

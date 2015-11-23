package com.photovacances.francescozanoli.photovacances.Exception;

/**
 * Created by francescozanoli on 24/09/15.
 */
//Exception lancée quand la period n'est pas valide
public class PeriodDateException extends Exception {
    public PeriodDateException(String message) {
        super(message);
    }
}

package com.taiwei.reggie.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Catalogue prices (dish, setmeal) are stored in cents while cart and order amounts are stored in currency units.
 * This is the single place that converts between the two.
 */
public final class Money {

    private Money() {
    }

    public static BigDecimal centsToUnits(BigDecimal cents) {
        return cents.movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }
}

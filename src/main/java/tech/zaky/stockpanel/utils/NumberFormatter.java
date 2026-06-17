package tech.zaky.stockpanel.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class NumberFormatter {
    public static String formatBigDecimal(BigDecimal num) {
        DecimalFormat df = new DecimalFormat("#,###.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        return df.format(num);

    }
}

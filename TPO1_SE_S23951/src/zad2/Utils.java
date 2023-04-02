package zad2;

import java.util.Currency;
import java.util.Locale;

public final class Utils {

    private Utils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String getCountryCode(String country) {
        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            if (locale.getDisplayCountry().equalsIgnoreCase(country)) {
                return countryCode;
            }
        }
        return null;
    }

    public static String getCurrencyCode(String country) {
        String countryCode = getCountryCode(country);
        if (countryCode != null) {
            try {
                Currency currency = Currency.getInstance(new Locale("", countryCode));
                return currency.getCurrencyCode();
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

}

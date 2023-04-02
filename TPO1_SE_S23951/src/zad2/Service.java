/**
 *
 *  @author Śnieżko Eugeniusz S23951
 *
 */

package zad2;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Service {
    private final String countryCode;

    private final String currencyCode;

    private static final String WEATHER_SERVICE_BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";

    private static final String WEATHER_SERVICE_API_KEY = "502ccf4bfc1617afe8b58ce105bb4e07";

    private static final String EXCHANGE_SERVICE_BASE_URL = "https://api.exchangerate.host/latest?base=";

    private static final String PNB_EXCHANGE_SERVICE_BASE_URL = "http://api.nbp.pl/api/exchangerates/rates/";

    private static final String PNB_EXCHANGE_REQUEST_SUFFIX = "/?format=json";

    public Service(String country) {
        this.countryCode = Utils.getCountryCode(country);
        this.currencyCode = Utils.getCurrencyCode(country);
    }

    public String getWeather(String city) {
        String weatherRequest = WEATHER_SERVICE_BASE_URL + city + "," + countryCode + "&appid=" + WEATHER_SERVICE_API_KEY;
        return sendRequest(weatherRequest);
    }

    public Double getRateFor(String currencyCode) {
        String urlString = EXCHANGE_SERVICE_BASE_URL + currencyCode + "&symbols=" + this.currencyCode;
        String response = sendRequest(urlString);
        JSONObject jsonResponse = new JSONObject(response);
        if(this.currencyCode==null) {
            return null;
        }
        return jsonResponse.getJSONObject("rates").getDouble(this.currencyCode);
    }

    public Double getNBPRate() {

        if (currencyCode == null) {
            return null;
        }

        if (currencyCode.equals("PLN")) {
            return 1.0;
        }

        String urlA = PNB_EXCHANGE_SERVICE_BASE_URL + "a/" + currencyCode + PNB_EXCHANGE_REQUEST_SUFFIX;
        String urlB = PNB_EXCHANGE_SERVICE_BASE_URL + "b/" + currencyCode + PNB_EXCHANGE_REQUEST_SUFFIX;

        String responseA = sendRequest(urlA);
        String responseB = sendRequest(urlB);

        Double rate = null;

        try {
            JSONObject jsonResponseA = new JSONObject(responseA);
            rate = jsonResponseA.getJSONArray("rates").getJSONObject(0).getDouble("mid");
        } catch (JSONException ignored) {
        }
        if (rate == null) {
            try {
                JSONObject jsonResponseB = new JSONObject(responseB);
                rate = jsonResponseB.getJSONArray("rates").getJSONObject(0).getDouble("mid");
            } catch (JSONException ignored) {
            }
        }
        return rate;
    }

    private String sendRequest(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

}

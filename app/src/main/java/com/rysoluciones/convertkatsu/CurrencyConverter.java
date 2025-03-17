package com.rysoluciones.convertkatsu;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrencyConverter {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ConversionCallback {
        void onConversionSuccess(double rate, double convertedAmount);
        void onConversionError(String error);
    }

    public void fetchConversionRate(String fromCurrency, String toCurrency, double amount, ConversionCallback callback) {
        executorService.execute(() -> {
            try {
                String apiUrl = "https://api.exchangerate-api.com/v4/latest/" + fromCurrency;
                String jsonResponse = getApiResponse(apiUrl);

                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONObject rates = jsonObject.getJSONObject("rates");

                if (rates.has(toCurrency)) {
                    double rate = rates.getDouble(toCurrency);
                    double convertedAmount = amount * rate;

                    mainHandler.post(() -> callback.onConversionSuccess(rate, convertedAmount));
                } else {
                    mainHandler.post(() -> callback.onConversionError("Moneda no encontrada"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onConversionError("Error en la conversión"));
            }
        });
    }

    private String getApiResponse(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            result.append(line);
        }
        in.close();

        return result.toString();
    }
}

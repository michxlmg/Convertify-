package com.rysoluciones.convertkatsu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class ConvertActivity extends AppCompatActivity {
    private EditText editTextAmount;
    private TextView textViewFrom, textViewTo;
    private Button btnConvert, btnUpdateRates;
    private CardView resultCard;
    private TextView resultAmount, exchangeRate;

    private List<String> currencyList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert);

        // Inicializar vistas
        editTextAmount = findViewById(R.id.editTextAmount);
        textViewFrom = findViewById(R.id.textViewFrom);
        textViewTo = findViewById(R.id.textViewTo);
        btnConvert = findViewById(R.id.btnConvert);
        btnUpdateRates = findViewById(R.id.btnUpdateRates);
        resultCard = findViewById(R.id.resultCard);
        resultAmount = findViewById(R.id.resultText);
        exchangeRate = findViewById(R.id.exchangeRateText);

        // Cargar monedas y tasas de cambio
        fetchCurrencyRates("USD");

        // Asignar eventos de clic con setOnClickListener
        textViewFrom.setOnClickListener(v -> showCurrencyDialog(textViewFrom));
        textViewTo.setOnClickListener(v -> showCurrencyDialog(textViewTo));

        // Evento de conversión
        btnConvert.setOnClickListener(v -> convertCurrency());

        // Botón para actualizar tasas de cambio
        btnUpdateRates.setOnClickListener(v -> {
            fetchCurrencyRates("USD");
            Toast.makeText(ConvertActivity.this, "Tasas actualizadas", Toast.LENGTH_SHORT).show();
        });
    }

    private void showCurrencyDialog(TextView targetView) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_currency_selector);

        EditText searchEditText = dialog.findViewById(R.id.searchCurrency);
        ListView currencyListView = dialog.findViewById(R.id.currencyList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currencyList);
        currencyListView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        currencyListView.setOnItemClickListener((parent, view, position, id) -> {
            targetView.setText(adapter.getItem(position));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void convertCurrency() {
        String fromCurrency = textViewFrom.getText().toString();
        String toCurrency = textViewTo.getText().toString();
        String amountStr = editTextAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            fetchConversionRate(fromCurrency, toCurrency, amount);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de número incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCurrencyRates(String baseCurrency) {
        executorService.execute(() -> {
            try {
                String apiUrl = "https://api.exchangerate-api.com/v4/latest/" + baseCurrency;
                String jsonResponse = getApiResponse(apiUrl);

                // Log para ver la respuesta de la API
                Log.d("API_RESPONSE", "Respuesta: " + jsonResponse);

                if (jsonResponse == null || jsonResponse.isEmpty()) {
                    mainHandler.post(() ->
                            Toast.makeText(ConvertActivity.this, "Error: Respuesta vacía de la API", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (!jsonObject.has("rates")) {
                    mainHandler.post(() ->
                            Toast.makeText(ConvertActivity.this, "Error: No se encontraron tasas en la respuesta", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                JSONObject rates = jsonObject.getJSONObject("rates");

                List<String> tempList = new ArrayList<>();
                Iterator<String> keys = rates.keys();
                while (keys.hasNext()) {
                    tempList.add(keys.next());
                }

                mainHandler.post(() -> {
                    currencyList.clear();
                    currencyList.addAll(tempList);
                    if (adapter != null) adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("API_ERROR", "Error al obtener monedas: " + e.getMessage());
                mainHandler.post(() ->
                        Toast.makeText(ConvertActivity.this, "Error al obtener monedas: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void fetchConversionRate(String fromCurrency, String toCurrency, double amount) {
        executorService.execute(() -> {
            try {
                String apiUrl = "https://api.exchangerate-api.com/v4/latest/" + fromCurrency;
                String jsonResponse = getApiResponse(apiUrl);

                // Log para ver la respuesta
                Log.d("API_RESPONSE", "Respuesta de conversión: " + jsonResponse);

                if (jsonResponse == null || jsonResponse.isEmpty()) {
                    mainHandler.post(() ->
                            Toast.makeText(ConvertActivity.this, "Error: Respuesta vacía en conversión", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (!jsonObject.has("rates")) {
                    mainHandler.post(() ->
                            Toast.makeText(ConvertActivity.this, "Error: No hay tasas de cambio disponibles", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                JSONObject rates = jsonObject.getJSONObject("rates");

                if (rates.has(toCurrency)) {
                    double rate = rates.getDouble(toCurrency);
                    double convertedAmount = amount * rate;

                    mainHandler.post(() -> {
                        resultAmount.setText(String.format("%.2f %s", convertedAmount, toCurrency));
                        exchangeRate.setText(String.format("1 %s = %.2f %s", fromCurrency, rate, toCurrency));
                        resultCard.setVisibility(View.VISIBLE);
                    });
                } else {
                    mainHandler.post(() ->
                            Toast.makeText(ConvertActivity.this, "Error: Moneda no encontrada en la conversión", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("API_ERROR", "Error en conversión: " + e.getMessage());
                mainHandler.post(() ->
                        Toast.makeText(ConvertActivity.this, "Error en la conversión: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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

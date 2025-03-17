package com.rysoluciones.convertkatsu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConvertActivity extends AppCompatActivity {
    private EditText editTextAmount;
    private TextView textViewFrom, textViewTo, resultAmount, exchangeRate;
    private Button btnConvert, btnClear, btnHistory, btnUpdateRates;  // Se añadió el botón btnUpdateRates
    private CardView resultCard;
    private DatabaseHelper dbHelper;
    private CurrencyConverter currencyConverter;

    private List<String> currencies = new ArrayList<>();
    private ArrayAdapter<String> adapter;

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
        btnClear = findViewById(R.id.btnClear);
        btnHistory = findViewById(R.id.btnHistory);
        btnUpdateRates = findViewById(R.id.btnUpdateRates);  // Se inicializó btnUpdateRates
        resultCard = findViewById(R.id.resultCard);
        resultAmount = findViewById(R.id.resultText);
        exchangeRate = findViewById(R.id.exchangeRateText);

        dbHelper = new DatabaseHelper(this);
        currencyConverter = new CurrencyConverter();

        // Cargar monedas desde la API
        fetchCurrencies();

        // Eventos
        btnConvert.setOnClickListener(v -> convertCurrency());
        btnClear.setOnClickListener(v -> editTextAmount.setText(""));
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ConvertActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Abrir diálogo de selección de monedas
        textViewFrom.setOnClickListener(v -> showCurrencySelectorDialog(true)); // true para moneda base
        textViewTo.setOnClickListener(v -> showCurrencySelectorDialog(false)); // false para moneda de cambio

        // Conversión en tiempo real
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performRealTimeConversion();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Lógica del botón "Actualizar tasas"
        btnUpdateRates.setOnClickListener(v -> {
            // Llamada a la API para obtener las tasas de cambio más recientes
            fetchCurrencies();
            Toast.makeText(ConvertActivity.this, "Tasas actualizadas", Toast.LENGTH_SHORT).show();
        });
    }

    // Método para obtener monedas desde la API
    private void fetchCurrencies() {
        new Thread(() -> {
            try {
                // URL de la API para obtener las tasas de cambio (con la moneda base USD)
                String apiUrl = "https://api.exchangerate-api.com/v4/latest/USD";
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");

                // Leer la respuesta de la API
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parsear la respuesta JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject rates = jsonResponse.getJSONObject("rates");

                // Obtener todas las monedas
                List<String> fetchedCurrencies = new ArrayList<>();
                for (int i = 0; i < rates.length(); i++) {
                    fetchedCurrencies.add(rates.names().getString(i)); // Añadir el nombre de la moneda
                }

                // Actualizar la lista de monedas en el hilo principal
                runOnUiThread(() -> {
                    currencies.clear();
                    currencies.addAll(fetchedCurrencies);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ConvertActivity.this, "Error al obtener monedas", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Método para mostrar el diálogo de selección de monedas
    private void showCurrencySelectorDialog(boolean isFromCurrency) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_currency_selector, null);
        builder.setView(dialogView);

        // Inicializar vistas del diálogo
        EditText searchCurrency = dialogView.findViewById(R.id.searchCurrency);
        ListView currencyList = dialogView.findViewById(R.id.currencyList);

        // Usar las monedas obtenidas de la API
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currencies);
        currencyList.setAdapter(adapter);

        // Filtrar la lista al escribir en el campo de búsqueda
        searchCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Crear el diálogo
        AlertDialog dialog = builder.create();

        // Manejar la selección de una moneda
        currencyList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCurrency = adapter.getItem(position);
            if (isFromCurrency) {
                textViewFrom.setText(selectedCurrency);
            } else {
                textViewTo.setText(selectedCurrency);
            }
            dialog.dismiss(); // Cerrar el diálogo

            // Realizar la conversión si hay un monto ingresado
            if (!editTextAmount.getText().toString().trim().isEmpty()) {
                performRealTimeConversion();
            }
        });

        // Mostrar el diálogo
        dialog.show();
    }

    private void performRealTimeConversion() {
        String fromCurrency = textViewFrom.getText().toString();
        String toCurrency = textViewTo.getText().toString();
        String amountStr = editTextAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            resultAmount.setText("");
            exchangeRate.setText("");
            resultCard.setVisibility(View.GONE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            currencyConverter.fetchConversionRate(fromCurrency, toCurrency, amount, new CurrencyConverter.ConversionCallback() {
                @Override
                public void onConversionSuccess(double rate, double convertedAmount) {
                    resultAmount.setText(String.format("%.2f %s", convertedAmount, toCurrency));
                    exchangeRate.setText(String.format("1 %s = %.2f %s", fromCurrency, rate, toCurrency));
                    resultCard.setVisibility(View.VISIBLE);
                }

                @Override
                public void onConversionError(String error) {
                    resultAmount.setText("Error");
                    exchangeRate.setText("");
                    resultCard.setVisibility(View.GONE);
                }
            });
        } catch (NumberFormatException e) {
            resultAmount.setText("Error");
            exchangeRate.setText("");
            resultCard.setVisibility(View.GONE);
        }
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
            currencyConverter.fetchConversionRate(fromCurrency, toCurrency, amount, new CurrencyConverter.ConversionCallback() {
                @Override
                public void onConversionSuccess(double rate, double convertedAmount) {
                    dbHelper.insertConversion(fromCurrency, toCurrency, rate, amount, convertedAmount);
                    Toast.makeText(ConvertActivity.this, "Conversión guardada en el historial", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConversionError(String error) {
                    Toast.makeText(ConvertActivity.this, "Error en la conversión", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de número incorrecto", Toast.LENGTH_SHORT).show();
        }
    }
}

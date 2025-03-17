package com.rysoluciones.convertkatsu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerViewHistory;
    private HistoryAdapter adapter;
    private ImageButton btnDeleteAll;
    private List<ConversionModel> historial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Inicializar base de datos y RecyclerView
        databaseHelper = new DatabaseHelper(this);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        // Botón para regresar a ConvertActivity
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, ConvertActivity.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });

        // Cargar historial
        cargarHistorial();

        // Eliminar todo el historial
        btnDeleteAll.setOnClickListener(v -> {
            databaseHelper.eliminarTodoHistorial();
            cargarHistorial(); // Recargar la lista vacía
        });
    }

    private void cargarHistorial() {
        historial = databaseHelper.obtenerHistorial();
        adapter = new HistoryAdapter(this, historial, id -> {
            // Eliminar conversión individual
            databaseHelper.eliminarConversion(id);
            cargarHistorial(); // Recargar el historial actualizado
        });
        recyclerViewHistory.setAdapter(adapter);
    }
}

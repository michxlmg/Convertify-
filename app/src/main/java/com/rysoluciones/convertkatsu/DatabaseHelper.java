// DatabaseHelper.java
package com.rysoluciones.convertkatsu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CurrencyDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "conversion_history";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FROM_CURRENCY = "from_currency";
    private static final String COLUMN_TO_CURRENCY = "to_currency";
    private static final String COLUMN_RATE = "rate";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_RESULT = "result";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FROM_CURRENCY + " TEXT, "
                + COLUMN_TO_CURRENCY + " TEXT, "
                + COLUMN_RATE + " REAL, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_RESULT + " REAL, "
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertConversion(String fromCurrency, String toCurrency, double rate, double amount, double result) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM_CURRENCY, fromCurrency);
        values.put(COLUMN_TO_CURRENCY, toCurrency);
        values.put(COLUMN_RATE, rate);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_RESULT, result);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<ConversionModel> obtenerHistorial() {
        List<ConversionModel> historial = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_TIMESTAMP + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                ConversionModel conversion = new ConversionModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FROM_CURRENCY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TO_CURRENCY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RESULT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                );
                historial.add(conversion);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return historial;
    }

    public void eliminarConversion(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void eliminarTodoHistorial() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
package org.smartregister.stock.management.repository;

import android.util.Log;
import android.util.Pair;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;
import org.smartregister.stock.management.domain.Gtin;
import org.smartregister.stock.management.domain.TradeItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.smartregister.stock.management.util.Utils.INSERT_OR_REPLACE;

public class TradeItemRepository extends BaseRepository {

    public static final String TAG = TradeItemRepository.class.getName();
    public static final String TRADE_ITEM_TABLE = "trade_items";
    public static final String ID = "id";
    public static final String GTIN = "gtin";
    public static final String MANUFACTURER_OF_TRADE_ITEM = "manufacturer_of_trade_item";
    public static final String DATE_UPDATED = "date_updated";
    public static final String[] TRADE_ITEM_TABLE_COLUMNS = new String[]{ID, GTIN, MANUFACTURER_OF_TRADE_ITEM, DATE_UPDATED};
    private static final String[] SELECT_TABLE_COLUMNS = new String[]{ID, GTIN, MANUFACTURER_OF_TRADE_ITEM};

    public static final String CREATE_TRADE_ITEM_TABLE =

            "CREATE TABLE " + TRADE_ITEM_TABLE
            + "("
                    + ID + " VARCHAR NOT NULL PRIMARY KEY,"
                    + GTIN + " VARCHAR NOT NULL,"
                    + MANUFACTURER_OF_TRADE_ITEM + " VARCHAR NOT NULL,"
                    + DATE_UPDATED + " INTEGER"
            + ")";

    public TradeItemRepository(Repository repository) { super(repository); }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TRADE_ITEM_TABLE);
    }

    public void addOrUpdate(TradeItem tradeItem) {

        if (tradeItem == null) {
            return;
        }

        if (tradeItem.getDateUpdated() == null) {
            tradeItem.setDateUpdated(Calendar.getInstance().getTimeInMillis());
        }

        try {
            SQLiteDatabase database = getWritableDatabase();
            String query = String.format(INSERT_OR_REPLACE, TRADE_ITEM_TABLE);
            query += "(" + formatTableValues(tradeItem) + ")";
            database.execSQL(query);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public List<TradeItem> findTradeItems(String id, String gtin, String manufacturerOfTradeItem) {

        List<TradeItem> tradeItems = new ArrayList<>();
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{id, gtin, manufacturerOfTradeItem};
            Pair<String, String[]> query= createQuery(selectionArgs);

            String querySelectString =  query.first;
            selectionArgs = query.second;

            cursor = getReadableDatabase().query(TRADE_ITEM_TABLE, TRADE_ITEM_TABLE_COLUMNS, querySelectString, selectionArgs, null, null, null);
            tradeItems = readTradeItemsFromCursor(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return tradeItems;
    }

    /**
     *
     * This method takes an array of {@param columnValues} and returns a {@code Pair} comprising of
     * the query string select statement and the query string arguments array.
     *
     * It assumes that {@param columnValues} is the same size as {@link SELECT_TABLE_COLUMNS} and
     * that select arguments are in the same order as {@link SELECT_TABLE_COLUMNS} column values.
     *
     * @param columnValues
     * @return
     */
    private Pair<String, String[]> createQuery(String[] columnValues) {

        String queryString = "";
        List<String> selectionArgs = new ArrayList<>();
        for (int i = 0; i < columnValues.length; i++) {
            if (columnValues[i] == null) {
                continue;
            }

            queryString += SELECT_TABLE_COLUMNS[i] + "=?";
            if (i != columnValues.length - 1) {
                queryString += " AND ";
            }
            selectionArgs.add(columnValues[i]);
        }

        String[] args = new String[selectionArgs.size()];
        args = selectionArgs.toArray(args);

        return new Pair<>(queryString, args);
    }


    private List<TradeItem> readTradeItemsFromCursor(Cursor cursor) {

        List<TradeItem> tradeItems = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    tradeItems.add(createTradeItemFromCursor(cursor));
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tradeItems;
    }

    private TradeItem createTradeItemFromCursor(Cursor cursor) {

        try {
            return new TradeItem(
                    UUID.fromString(cursor.getString(cursor.getColumnIndex(ID))),
                    new Gtin(cursor.getString(cursor.getColumnIndex(GTIN))),
                    cursor.getString(cursor.getColumnIndex(MANUFACTURER_OF_TRADE_ITEM)),
                    cursor.getLong(cursor.getColumnIndex(DATE_UPDATED))
            );
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return  null;
    }

    private String formatTableValues(TradeItem tradeItem) {

        String values = "";
        values += "'" + tradeItem.getId().toString() + "'" + ",";
        values += "'" + tradeItem.getGtin().toString() + "'" +  ",";
        values += "'" + tradeItem.getManufacturerOfTradeItem() + "'" + ",";
        values += tradeItem.getDateUpdated();

        return values;
    }
}

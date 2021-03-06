package org.smartregister.stock.openlmis.repository.openlmis;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;
import org.smartregister.stock.openlmis.domain.openlmis.CommodityType;
import org.smartregister.stock.openlmis.repository.TradeItemRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.smartregister.stock.openlmis.repository.TradeItemRepository.COMMODITY_TYPE_ID;
import static org.smartregister.stock.openlmis.repository.TradeItemRepository.HAS_LOTS;
import static org.smartregister.stock.openlmis.repository.TradeItemRepository.TRADE_ITEM_TABLE;
import static org.smartregister.stock.openlmis.repository.openlmis.LotRepository.EXPIRATION_DATE;
import static org.smartregister.stock.openlmis.repository.openlmis.LotRepository.LOT_TABLE;
import static org.smartregister.stock.openlmis.util.Utils.INSERT_OR_REPLACE;
import static org.smartregister.stock.openlmis.util.Utils.createQuery;

public class CommodityTypeRepository extends BaseRepository {

    public static final String TAG = CommodityType.class.getName();
    public static final String COMMODITY_TYPE_TABLE = "commodity_types";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PARENT = "parent";
    public static final String CLASSIFICATION_SYSTEM = "classification_system";
    public static final String CLASSIFICATION_ID = "classification_id";
    public static final String DATE_UPDATED = "date_updated";
    public static final String[] COMMODITY_TYPE_TABLE_COLUMNS = {ID, NAME, PARENT, CLASSIFICATION_SYSTEM, CLASSIFICATION_ID, DATE_UPDATED};
    public static final String[] SELECT_TABLE_COLUMNS = {ID, NAME, PARENT, CLASSIFICATION_SYSTEM, CLASSIFICATION_ID};

    public static final String CREATE_COMMODITY_TYPE_TABLE =

            "CREATE TABLE " + COMMODITY_TYPE_TABLE
                    + "("
                    + ID + " VARCHAR NOT NULL PRIMARY KEY,"
                    + NAME + " VARCHAR NOT NULL,"
                    + PARENT + " VARCHAR,"
                    + CLASSIFICATION_SYSTEM + " VARCHAR,"
                    + CLASSIFICATION_ID + " VARCHAR,"
                    + DATE_UPDATED + " INTEGER"
                    + ")";

    public CommodityTypeRepository(Repository repository) {
        super();
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_COMMODITY_TYPE_TABLE);
    }

    public void addOrUpdate(CommodityType commodityType) {

        if (commodityType == null) {
            return;
        }

        if (commodityType.getDateUpdated() == null) {
            commodityType.setDateUpdated(Calendar.getInstance().getTimeInMillis());
        }

        try {
            SQLiteDatabase database = getWritableDatabase();

            String query = String.format(INSERT_OR_REPLACE, COMMODITY_TYPE_TABLE);
            query += "(" + StringUtils.repeat("?", ",", COMMODITY_TYPE_TABLE_COLUMNS.length) + ")";
            database.execSQL(query, createQueryValues(commodityType));
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public List<CommodityType> findCommodityTypes(String id, String name, String parentId, String classificationSystem, String classificationId) {

        List<CommodityType> commodityTypes = new ArrayList<>();
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{id, name, parentId, classificationSystem, classificationId};
            Pair<String, String[]> query = createQuery(selectionArgs, SELECT_TABLE_COLUMNS);

            String querySelectString = query.first;
            selectionArgs = query.second;

            cursor = getReadableDatabase().query(COMMODITY_TYPE_TABLE, COMMODITY_TYPE_TABLE_COLUMNS, querySelectString, selectionArgs, null, null, null);
            commodityTypes = readCommodityTypes(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return commodityTypes;
    }

    public List<CommodityType> findAllCommodityTypes() {

        List<CommodityType> commodityTypes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + COMMODITY_TYPE_TABLE, null);
            commodityTypes = readCommodityTypes(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return commodityTypes;
    }


    public List<CommodityType> findCommodityTypesByIds(Set<String> commodityTypeIds) {
        List<CommodityType> commodityTypes = new ArrayList<>();

        if (commodityTypeIds == null)
            return commodityTypes;

        commodityTypeIds.remove(null);
        if (commodityTypeIds.isEmpty())
            return commodityTypes;
        int len = commodityTypeIds.size();
        Cursor cursor = null;
        try {
            String query = String.format("SELECT * FROM %s WHERE %s IN (%s)", COMMODITY_TYPE_TABLE,
                    ID, TextUtils.join(",", Collections.nCopies(len, "?")));
            cursor = getReadableDatabase().rawQuery(query, commodityTypeIds.toArray(new String[len]));
            commodityTypes = readCommodityTypes(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return commodityTypes;
    }

    public List<CommodityType> findActiveCommodityTypesWithoutLotsByIds(Set<String> commodityTypeIds) {
        List<CommodityType> commodityTypes = new ArrayList<>();

        if (commodityTypeIds == null)
            return commodityTypes;

        commodityTypeIds.remove(null);
        if (commodityTypeIds.isEmpty())
            return commodityTypes;
        int len = commodityTypeIds.size();
        Cursor cursor = null;
        try {
            String query = String.format("SELECT DISTINCT c.* FROM %s c JOIN %s t on c.%s = t.%s " +
                            " WHERE c.%s IN (%s) AND t.%s = 0",
                    COMMODITY_TYPE_TABLE, TRADE_ITEM_TABLE, ID, COMMODITY_TYPE_ID,
                    ID, TextUtils.join(",", Collections.nCopies(len, "?")), HAS_LOTS);
            cursor = getReadableDatabase().rawQuery(query, commodityTypeIds.toArray(new String[len]));
            commodityTypes = readCommodityTypes(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return commodityTypes;
    }

    public List<CommodityType> findCommodityTypesWithActiveLotsByIds(Set<String> commodityTypeIds) {
        List<CommodityType> commodityTypes = new ArrayList<>();
        if (commodityTypeIds == null)
            return commodityTypes;
        commodityTypeIds.remove(null);
        if (commodityTypeIds.isEmpty())
            return commodityTypes;
        int len = commodityTypeIds.size();
        Cursor cursor = null;
        try {
            String query = String.format("SELECT DISTINCT c.* FROM %s c JOIN  %s t on c.%s=t.%s JOIN %s l on l.%s=t.%s" +
                            " WHERE %s IN (%s) AND l.%s >= ? AND %s =1",
                    COMMODITY_TYPE_TABLE, TRADE_ITEM_TABLE, ID, TradeItemRepository.COMMODITY_TYPE_ID,
                    LOT_TABLE, LotRepository.TRADE_ITEM_ID, TradeItemRepository.ID, ID,
                    TextUtils.join(",", Collections.nCopies(len, "?")), EXPIRATION_DATE, HAS_LOTS);
            String[] params = commodityTypeIds.toArray(new String[len + 1]);
            params[len] = String.valueOf(new LocalDate().toDate().getTime());
            cursor = getReadableDatabase().rawQuery(query, params);
            commodityTypes = readCommodityTypes(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return commodityTypes;
    }


    public CommodityType findCommodityTypeById(String commodityTypeId) {
        if (commodityTypeId == null)
            return null;
        Cursor cursor = null;
        try {
            String query = String.format("SELECT * FROM %s WHERE %s=?", COMMODITY_TYPE_TABLE, ID);
            cursor = getReadableDatabase().rawQuery(query, new String[]{commodityTypeId});
            if (cursor.moveToFirst())
                return createCommodityType(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private List<CommodityType> readCommodityTypes(Cursor cursor) {

        List<CommodityType> commodityTypes = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                commodityTypes.add(createCommodityType(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return commodityTypes;
    }

    private CommodityType createCommodityType(Cursor cursor) {

        return new CommodityType(
                cursor.getString(cursor.getColumnIndex(ID)),
                cursor.getString(cursor.getColumnIndex(NAME)),
                new CommodityType(cursor.getString(cursor.getColumnIndex(PARENT))),
                cursor.getString(cursor.getColumnIndex(CLASSIFICATION_SYSTEM)),
                cursor.getString(cursor.getColumnIndex(CLASSIFICATION_ID)),
                cursor.getLong(cursor.getColumnIndex(DATE_UPDATED))
        );
    }

    private Object[] createQueryValues(CommodityType commodityType) {

        Object[] values = new Object[]{
                commodityType.getId().toString(),
                commodityType.getName(),
                commodityType.getParent() == null ? null : commodityType.getParent().getId(),
                commodityType.getClassificationSystem(),
                commodityType.getClassificationId(),
                commodityType.getDateUpdated()
        };
        return values;
    }

}

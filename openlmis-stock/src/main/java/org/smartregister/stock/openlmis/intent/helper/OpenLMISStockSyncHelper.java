package org.smartregister.stock.openlmis.intent.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.service.ActionService;
import org.smartregister.service.HTTPAgent;
import org.smartregister.stock.openlmis.OpenLMISLibrary;
import org.smartregister.stock.openlmis.domain.Stock;
import org.smartregister.stock.openlmis.repository.StockRepository;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.smartregister.repository.BaseRepository.TYPE_Synced;
import static org.smartregister.stock.openlmis.util.Utils.BASE_URL;
import static org.smartregister.stock.openlmis.util.Utils.PREV_SYNC_SERVER_VERSION;
import static org.smartregister.stock.openlmis.util.Utils.makeGetRequest;
import static org.smartregister.stock.openlmis.util.Utils.makePostRequest;
import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

public class OpenLMISStockSyncHelper extends BaseSyncHelper {

    private static final String STOCK_Add_PATH = "rest/stockresource/add/";
    private static final String STOCK_SYNC_PATH = "rest/stockresource/sync/";
    private static final String LAST_STOCK_SYNC = "last_stock_sync";

    private HTTPAgent httpAgent;
    private ActionService actionService;
    private StockRepository stockRepository;

    public OpenLMISStockSyncHelper(Context context, ActionService actionService, HTTPAgent httpAgent) {
        this.stockRepository = OpenLMISLibrary.getInstance().getStockRepository();
        this.context = context;
        this.actionService = actionService;
        this.httpAgent = httpAgent;
    }

    protected String pullFromServer() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String anmId = allSharedPreferences.fetchRegisteredANM();

        long timestamp = preferences.getLong(PREV_SYNC_SERVER_VERSION, 0);
        String timeStampString = String.valueOf(timestamp);
        String uri = MessageFormat.format("{0}/{1}?providerid={2}&serverVersion={3}",
                BASE_URL,
                STOCK_SYNC_PATH,
                anmId,
                timeStampString
        );
        String jsonPayload = makeGetRequest(uri);
        if (jsonPayload == null) {
            logError("Stock pull failed.");
        }
        logInfo("Stock pulled successfully!");
        return jsonPayload;
    }

    @Override
    public void saveResponse(String jsonPayload, SharedPreferences preferences) {

        ArrayList<Stock> Stock_arrayList = getStockFromPayload(jsonPayload);
        Long highestTimestamp = getHighestTimestampFromStockPayLoad(jsonPayload);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(LAST_STOCK_SYNC, highestTimestamp);
        editor.commit();
        if (Stock_arrayList.isEmpty()) {
            return;
        } else {
            for (int j = 0; j < Stock_arrayList.size(); j++) {
                Stock fromServer = Stock_arrayList.get(j);
                List<Stock> existingStock = stockRepository.findUniqueStock(fromServer.getStockTypeId(), fromServer.getTransactionType(), fromServer.getProviderid(),
                        String.valueOf(fromServer.getValue()), String.valueOf(fromServer.getDateCreated()), fromServer.getToFrom());
                if (!existingStock.isEmpty()) {
                    for (Stock stock : existingStock) {
                        fromServer.setId(stock.getId());
                    }
                }
                stockRepository.addOrUpdate(fromServer);
            }

        }
    }

    private void pushStockToServer() {

        boolean keepSyncing = true;
        int limit = 50;
        try {
            while (keepSyncing) {
                ArrayList<Stock> stocks = (ArrayList<Stock>) stockRepository.findUnSyncedWithLimit(limit);
                JSONArray stocksarray = createJsonArrayFromStockArray(stocks);
                if (stocks.isEmpty()) {
                    return;
                }
                // create request body
                JSONObject request = new JSONObject();
                request.put(context.getString(org.smartregister.stock.R.string.stocks_key), stocksarray);

                String jsonPayload = request.toString();
                String response = makePostRequest(
                        MessageFormat.format(
                                "{0}/{1}",
                                BASE_URL,
                                STOCK_Add_PATH),
                        jsonPayload);
                if (response == null) {
                    Log.e(getClass().getName(), "Stock push sync failed.");
                    return;
                }
                stockRepository.markEventsAsSynced(stocks);
                Log.i(getClass().getName(), "Stock successfully pushed.");
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    private Long getHighestTimestampFromStockPayLoad(String jsonPayload) {
        Long toreturn = 0l;
        try {
            JSONObject stockContainer = new JSONObject(jsonPayload);
            if (stockContainer.has(context.getString(org.smartregister.stock.R.string.stocks_key))) {
                JSONArray stockArray = stockContainer.getJSONArray(context.getString(org.smartregister.stock.R.string.stocks_key));
                for (int i = 0; i < stockArray.length(); i++) {
                    JSONObject stockObject = stockArray.getJSONObject(i);
                    if (stockObject.getLong(context.getString(org.smartregister.stock.R.string.server_version_key)) > toreturn) {
                        toreturn = stockObject.getLong(context.getString(org.smartregister.stock.R.string.server_version_key));
                    }

                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        return toreturn;
    }

    private ArrayList<Stock> getStockFromPayload(String jsonPayload) {
        ArrayList<Stock> Stock_arrayList = new ArrayList<>();
        try {
            JSONObject stockcontainer = new JSONObject(jsonPayload);
            if (stockcontainer.has(context.getString(org.smartregister.stock.R.string.stocks_key))) {
                JSONArray stockArray = stockcontainer.getJSONArray(context.getString(org.smartregister.stock.R.string.stocks_key));
                for (int i = 0; i < stockArray.length(); i++) {
                    JSONObject stockObject = stockArray.getJSONObject(i);
                    Stock stock = new Stock(null,
                            stockObject.getString(context.getString(org.smartregister.stock.R.string.transaction_type_key)),
                            stockObject.getString(context.getString(org.smartregister.stock.R.string.providerid_key)),
                            stockObject.getInt(context.getString(org.smartregister.stock.R.string.value_key)),
                            stockObject.getLong(context.getString(org.smartregister.stock.R.string.date_created_key)),
                            stockObject.getString(context.getString(org.smartregister.stock.R.string.to_from_key)),
                            TYPE_Synced,
                            stockObject.getLong(context.getString(org.smartregister.stock.R.string.date_updated_key)),
                            stockObject.getString(context.getString(org.smartregister.stock.R.string.stock_type_id_key)),
                            stockObject.getString(context.getString(org.smartregister.stock.R.string.lot_id))
                    );
                    Stock_arrayList.add(stock);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        return Stock_arrayList;
    }

    private JSONArray createJsonArrayFromStockArray(ArrayList<Stock> stocks) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < stocks.size(); i++) {
            JSONObject stock = new JSONObject();
            try {
                stock.put("identifier", stocks.get(i).getId());
                stock.put(context.getString(org.smartregister.stock.R.string.stock_type_id_key), stocks.get(i).getStockTypeId());
                stock.put(context.getString(org.smartregister.stock.R.string.transaction_type_key), stocks.get(i).getTransactionType());
                stock.put(context.getString(org.smartregister.stock.R.string.providerid_key), stocks.get(i).getProviderid());
                stock.put(context.getString(org.smartregister.stock.R.string.date_created_key), stocks.get(i).getDateCreated());
                stock.put(context.getString(org.smartregister.stock.R.string.value_key), stocks.get(i).getValue());
                stock.put(context.getString(org.smartregister.stock.R.string.to_from_key), stocks.get(i).getToFrom());
                stock.put(context.getString(org.smartregister.stock.R.string.date_updated_key), stocks.get(i).getUpdatedAt());
                stock.put(context.getString(org.smartregister.stock.R.string.lot_id), stocks.get(i).getLotId());
                array.put(stock);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public HTTPAgent getHttpAgent() {
        return httpAgent;
    }

    public void setHttpAgent(HTTPAgent httpAgent) {
        this.httpAgent = httpAgent;
    }

    public ActionService getActionService() {
        return actionService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public StockRepository getStockRepository() {
        return stockRepository;
    }

    public void setStockRepository(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
}

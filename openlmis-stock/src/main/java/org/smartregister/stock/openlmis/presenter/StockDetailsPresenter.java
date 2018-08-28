package org.smartregister.stock.openlmis.presenter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.view.View;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.stock.openlmis.domain.Stock;
import org.smartregister.stock.openlmis.domain.TradeItem;
import org.smartregister.stock.openlmis.domain.openlmis.Lot;
import org.smartregister.stock.openlmis.dto.TradeItemDto;
import org.smartregister.stock.openlmis.interactor.StockDetailsInteractor;
import org.smartregister.stock.openlmis.view.contract.StockDetailsView;
import org.smartregister.stock.openlmis.widget.LotFactory;
import org.smartregister.stock.openlmis.widget.helper.LotDto;
import org.smartregister.stock.openlmis.wrapper.StockWrapper;
import org.smartregister.util.JsonFormUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.smartregister.stock.domain.Stock.issued;
import static org.smartregister.stock.domain.Stock.received;
import static org.smartregister.stock.openlmis.adapter.LotAdapter.DATE_FORMAT;
import static org.smartregister.stock.openlmis.widget.LotFactory.TRADE_ITEM_ID;
import static org.smartregister.stock.openlmis.widget.ReviewFactory.STEP2;
import static org.smartregister.stock.openlmis.widget.ReviewFactory.STOCK_LOTS;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.KEY;
import static org.smartregister.util.JsonFormUtils.STEP1;
import static org.smartregister.util.JsonFormUtils.getJSONObject;

public class StockDetailsPresenter {

    private static final String TAG = StockDetailsPresenter.class.getName();

    private StockDetailsInteractor stockDetailsInteractor;

    private StockDetailsView stockDetailsView;


    public StockDetailsPresenter(StockDetailsView stockDetailsView) {
        this.stockDetailsView = stockDetailsView;
        stockDetailsInteractor = new StockDetailsInteractor();

    }

    @VisibleForTesting
    public StockDetailsPresenter(StockDetailsView stockDetailsView, StockDetailsInteractor stockDetailsInteractor) {
        this.stockDetailsView = stockDetailsView;
        this.stockDetailsInteractor = stockDetailsInteractor;

    }

    public int getTotalStockByLot(UUID lotId) {
        return stockDetailsInteractor.getTotalStockByLot(lotId);
    }

    public List<Lot> findLotsByTradeItem(String tradeItemId) {
        List<Lot> lots = stockDetailsInteractor.findLotsByTradeItem(tradeItemId);
        if (!lots.isEmpty())
            stockDetailsView.showLotsHeader();
        return lots;
    }

    public TradeItem findTradeItem(String tradeItemId) {
        return stockDetailsInteractor.findTradeItem(tradeItemId);
    }

    public List<Stock> findStockByTradeItem(String tradeItemId) {
        List<Stock> stockList = stockDetailsInteractor.getStockByTradeItem(tradeItemId);

        if (!stockList.isEmpty())
            stockDetailsView.showTransactionsHeader();
        return stockList;
    }

    public List<StockWrapper> populateLotNamesAndBalance(TradeItemDto tradeItem, List<Stock> stockTransactions) {
        List<StockWrapper> stockWrapperList = new ArrayList<>();
        Map<String, String> lotName = stockDetailsInteractor.findLotNames(tradeItem.getId());
        int stockCounter = 0;
        for (Stock stock : stockTransactions) {
            stockWrapperList.add(new StockWrapper(stock, lotName.get(stock.getLotId()),
                    tradeItem.getTotalStock() - stockCounter));
            stockCounter += stock.getValue();
        }
        return stockWrapperList;
    }

    public void collapseExpandClicked(int visibility) {
        if (visibility == View.GONE) {
            stockDetailsView.expandLots();
        } else if (visibility == View.VISIBLE) {
            stockDetailsView.collapseLots();
        }
    }

    public void processFormJsonResult(String jsonString) {
        try {
            JSONObject jsonForm = new JSONObject(jsonString);
            JSONObject step = jsonForm.getJSONObject(STEP1);
            String FormTitle = step.getString("title");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(stockDetailsView.getContext());
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            boolean processed = false;
            if (FormTitle.contains("Issue")) {
                processed = processStockIssued(jsonForm, allSharedPreferences.fetchRegisteredANM());
            } else if (FormTitle.contains("Receive")) {
                processed = processStockReceived(jsonForm, allSharedPreferences.fetchRegisteredANM());
            }
            if (processed)
                stockDetailsView.refreshStockTransactions();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean processStockIssued(JSONObject jsonString, String provider) throws JSONException {
        JSONArray stepFields = JsonFormUtils.fields(jsonString);

        String date = JsonFormUtils.getFieldValue(stepFields, "Date_Stock_Issued");
        String facility = JsonFormUtils.getFieldValue(stepFields, "Issued_Stock_To");
        if (StringUtils.isBlank(facility)) {
            facility = JsonFormUtils.getFieldValue(stepFields, "Issued_Stock_TO_Other");
        }
        String reason = JsonFormUtils.getFieldValue(stepFields, "Issued_Stock_Reason");
        if (StringUtils.isBlank(facility)) {
            reason = JsonFormUtils.getFieldValue(stepFields, "Issued_Stock_Reason_Other");
        }

        return processStock(jsonString, provider, date, facility, reason, issued);
    }

    private boolean processStockReceived(JSONObject jsonString, String provider) throws JSONException {
        JSONArray stepFields = JsonFormUtils.fields(jsonString);

        String date = JsonFormUtils.getFieldValue(stepFields, "Date_Stock_Received");
        String facility = JsonFormUtils.getFieldValue(stepFields, "Receive_Stock_From");
        if (StringUtils.isBlank(facility)) {
            facility = JsonFormUtils.getFieldValue(stepFields, "Receive_Stock_From_Other");
        }
        String reason = JsonFormUtils.getFieldValue(stepFields, "Receive_Stock_Reason");
        if (StringUtils.isBlank(facility)) {
            reason = JsonFormUtils.getFieldValue(stepFields, "Receive_Stock_Reason_Other");
        }

        return processStock(jsonString, provider, date, facility, reason, received);


    }

    private boolean processStock(JSONObject jsonString, String provider, String date, String facility, String reason, String transactionType) throws JSONException {
        JSONArray stepFields = jsonString.getJSONObject(STEP2).getJSONArray(FIELDS);

        String lotsJSON = JsonFormUtils.getFieldValue(stepFields, STOCK_LOTS);

        Type listType = new TypeToken<List<LotDto>>() {
        }.getType();

        List<LotDto> selectedLotDTos = LotFactory.gson.fromJson(lotsJSON, listType);

        String tradeItem = null;

        for (int i = 0; i < stepFields.length(); i++) {
            JSONObject jsonObject = getJSONObject(stepFields, i);
            String keyValue = jsonObject.getString(KEY);
            if (keyValue != null && keyValue.equals(STOCK_LOTS)) {
                tradeItem = jsonObject.getString(TRADE_ITEM_ID);
            }
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        Date encounterDate;
        try {
            encounterDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "error passing stock issue/received date", e);
            encounterDate = new Date();
        }

        for (LotDto lot : selectedLotDTos) {
            Stock stock = new Stock(null, transactionType,
                    provider, transactionType.equals(issued) ? -lot.getQuantity() : lot.getQuantity(),
                    encounterDate.getTime(), facility, "unsynched",
                    System.currentTimeMillis(), tradeItem);
            stock.setLotId(lot.getLotId());
            stock.setReason(reason);
            stockDetailsInteractor.addStock(stock);
        }
        return true;
    }

}

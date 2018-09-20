package org.smartregister.stock.openlmis.presenter;

import android.support.annotation.VisibleForTesting;
import android.view.View;

import org.smartregister.stock.openlmis.adapter.ListCommodityTypeAdapter;
import org.smartregister.stock.openlmis.domain.openlmis.CommodityType;
import org.smartregister.stock.openlmis.domain.openlmis.Program;
import org.smartregister.stock.openlmis.interactor.StockListInteractor;
import org.smartregister.stock.openlmis.view.contract.StockListView;
import org.smartregister.stock.openlmis.wrapper.TradeItemWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by samuelgithengi on 7/13/18.
 */
public class StockListPresenter extends StockListBasePresenter{

    private StockListInteractor stockListInteractor;

    private StockListView stockListView;

    private ListCommodityTypeAdapter commodityTypeAdapter;

    public StockListPresenter(StockListView stockListView) {
        this(stockListView, new StockListInteractor());
    }

    @VisibleForTesting
    protected StockListPresenter(StockListView stockListView, StockListInteractor stockListInteractor) {
        this.stockListView = stockListView;
        this.stockListInteractor = stockListInteractor;
    }

    public void setCommodityTypeAdapter(ListCommodityTypeAdapter commodityTypeAdapter) {
        this.commodityTypeAdapter = commodityTypeAdapter;
    }

    public List<Program> getPrograms() {
        return stockListInteractor.getPrograms();
    }

    public List<CommodityType> getCommodityTypes() {
        return stockListInteractor.getCommodityTypes();
    }

    public void stockActionClicked(View view) {
        stockListView.showStockActionMenu(view);
    }

    public void expandAllClicked() {
        commodityTypeAdapter.expandAllViews();
    }

    public void collapseAllClicked() {
        commodityTypeAdapter.collapseAllViews();
    }

    public List<TradeItemWrapper> getTradeItems(CommodityType commodityType) {
        return stockListInteractor.getTradeItems(commodityType);
    }

    public Map<String, Set<String>> searchIds(String searchPhrase) {
        return stockListInteractor.searchIds(searchPhrase);
    }

    public List<TradeItemWrapper> findTradeItemsByIds(Set<String> tradeItemIds) {
        return stockListInteractor.findTradeItemsByIds(tradeItemIds);
    }

    public Map<String, Set<String>> filterValidPrograms(Map<String, Set<String>> programIds,
                                                        Map<String, Set<String>> searchedIds) {
        if (programIds == null)
            return searchedIds;
        Map<String, Set<String>> filteredIds = new HashMap<>();
        for (String key : searchedIds.keySet()) {
            if (programIds.containsKey(key)) {
                Set<String> tradeItems = searchedIds.get(key);
                tradeItems.retainAll(programIds.get(key));
                filteredIds.put(key, tradeItems);
            }
        }
        return filteredIds;
    }

    @Override
    public StockListInteractor getStockListInteractor() {
        return stockListInteractor;
    }
}

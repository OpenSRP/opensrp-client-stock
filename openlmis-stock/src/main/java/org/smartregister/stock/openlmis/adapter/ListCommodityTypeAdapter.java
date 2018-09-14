package org.smartregister.stock.openlmis.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.stock.openlmis.R;
import org.smartregister.stock.openlmis.domain.openlmis.CommodityType;
import org.smartregister.stock.openlmis.listener.ExpandCollapseListener;
import org.smartregister.stock.openlmis.presenter.StockListPresenter;
import org.smartregister.stock.openlmis.view.viewholder.CommodityTypeViewHolder;
import org.smartregister.stock.openlmis.wrapper.TradeItemWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by samuelgithengi on 7/13/18.
 */
public class ListCommodityTypeAdapter extends RecyclerView.Adapter<CommodityTypeViewHolder> {

    private final StockListPresenter stockListPresenter;
    private List<CommodityType> commodityTypes;

    private List<ExpandCollapseListener> expandCollapseListeners = new ArrayList<>();

    private Map<String, List<String>> searchedIds;

    private Context context;

    public ListCommodityTypeAdapter(StockListPresenter stockListPresenter, Context context) {
        this.stockListPresenter = stockListPresenter;
        this.commodityTypes = stockListPresenter.getCommodityTypes();
        this.context = context;

    }

    public void filterCommodityTypes(String searchPhrase) {
        if (StringUtils.isBlank(searchPhrase)) {
            commodityTypes = stockListPresenter.getCommodityTypes();
            searchedIds = null;
            notifyDataSetChanged();
            collapseAllViews();
        } else {
            searchedIds = stockListPresenter.searchIds(searchPhrase);
            commodityTypes = stockListPresenter.findCommodityTypesByIds(searchedIds.keySet());
            notifyDataSetChanged();
            expandAllViews();
        }
    }

    @NonNull
    @Override
    public CommodityTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.commodity_type_row, parent, false);
        CommodityTypeViewHolder holder = new CommodityTypeViewHolder(itemView);
        expandCollapseListeners.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommodityTypeViewHolder holder, int position) {
        CommodityType commodityType = commodityTypes.get(position);
        List<TradeItemWrapper> tradeItems;
        if (searchedIds == null)
            tradeItems = stockListPresenter.getTradeItems(commodityType);
        else
            tradeItems = stockListPresenter.findTradeItemsByIds(searchedIds.get(commodityType.getId().toString()));
        int totalDoses = 0;
        for (TradeItemWrapper tradeItem : tradeItems)
            totalDoses += tradeItem.getTotalStock() * tradeItem.getTradeItem().getNetContent();
        holder.getCommodityTypeTextView().setText(context.getString(R.string.commodity_type_formatter,
                commodityType.getName(), tradeItems.size()));
        holder.getDoseTextView().setText(context.getString(R.string.dose_formatter, totalDoses));
        holder.getTradeItemsRecyclerView().setAdapter(new ListTradeItemAdapter(tradeItems, context));
    }

    @Override
    public int getItemCount() {
        return commodityTypes.size();
    }

    public void expandAllViews() {
        for (ExpandCollapseListener listener : expandCollapseListeners)
            listener.expandView();
    }

    public void collapseAllViews() {
        for (ExpandCollapseListener listener : expandCollapseListeners)
            listener.collapseView();
    }

    public void registerExpandCollapseListeners(ExpandCollapseListener listener) {
        expandCollapseListeners.add(listener);
    }
}
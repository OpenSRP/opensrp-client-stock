package org.smartregister.stock.openlmis.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartregister.stock.openlmis.R;
import org.smartregister.stock.openlmis.adapter.LotAdapter;
import org.smartregister.stock.openlmis.adapter.StockTransactionAdapter;
import org.smartregister.stock.openlmis.dto.TradeItemDto;
import org.smartregister.stock.openlmis.presenter.StockDetailsPresenter;
import org.smartregister.stock.openlmis.util.OpenLMISConstants;
import org.smartregister.stock.openlmis.view.contract.StockDetailsView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StockDetailsActivity extends AppCompatActivity implements StockDetailsView, View.OnClickListener {

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mma dd MMM, yyyy");

    private StockDetailsPresenter stockDetailsPresenter;

    private RecyclerView lotsRecyclerView;

    private LinearLayout lotsHeader;

    private ImageView collapseExpandButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        stockDetailsPresenter = new StockDetailsPresenter(this);

        TradeItemDto tradeItemDto = getIntent().getParcelableExtra(OpenLMISConstants.tradeItem);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.stock_details_title, tradeItemDto.getName()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView itemNameTextView = findViewById(R.id.itemNameTextView);
        itemNameTextView.setText(tradeItemDto.getName());

        TextView dosesTextView = findViewById(R.id.doseTextView);
        dosesTextView.setText(getString(R.string.stock_balance, tradeItemDto.getTotalStock(),
                tradeItemDto.getDispensingUnit(), tradeItemDto.getNetContent() * tradeItemDto.getTotalStock()));

        TextView lastUpdatedTextView = findViewById(R.id.lastUpdatedTextView);
        Date lastUpdated = new Date(tradeItemDto.getLastUpdated());
        lastUpdatedTextView.setText(getString(R.string.stock_last_updated,
                simpleDateFormat.format(lastUpdated)));

        TextView lots = findViewById(R.id.number_of_lots);
        lots.setText(getString(R.string.lots_details, tradeItemDto.getNumberOfLots()));

        lotsHeader = findViewById(R.id.lot_header);
        collapseExpandButton = findViewById(R.id.collapseExpandButton);

        lotsRecyclerView = findViewById(R.id.lotsRecyclerView);
        lotsRecyclerView.setAdapter(new LotAdapter(tradeItemDto, stockDetailsPresenter));


        RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setAdapter(new StockTransactionAdapter(tradeItemDto, stockDetailsPresenter));

        collapseExpandButton.setOnClickListener(this);
        findViewById(R.id.number_of_lots).setOnClickListener(this);
    }

    @Override
    public void showLotsHeader() {
        lotsHeader.setVisibility(View.VISIBLE);
    }

    @Override
    public void showTransactionsHeader() {
        findViewById(R.id.transactions_header).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.collapseExpandButton || view.getId() == R.id.number_of_lots) {
            stockDetailsPresenter.collapseExpandClicked(lotsRecyclerView.getVisibility());
        }
    }

    @Override
    public void collapseLots() {
        lotsHeader.setVisibility(View.GONE);
        lotsRecyclerView.setVisibility(View.GONE);
        collapseExpandButton.setImageResource(R.drawable.ic_keyboard_arrow_down);
    }

    @Override
    public void expandLots() {
        lotsHeader.setVisibility(View.VISIBLE);
        lotsRecyclerView.setVisibility(View.VISIBLE);
        collapseExpandButton.setImageResource(R.drawable.ic_keyboard_arrow_up);
    }

}

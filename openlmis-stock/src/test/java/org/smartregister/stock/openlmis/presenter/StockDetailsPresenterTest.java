package org.smartregister.stock.openlmis.presenter;

import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.stock.openlmis.BaseUnitTest;
import org.smartregister.stock.openlmis.domain.Stock;
import org.smartregister.stock.openlmis.domain.TradeItem;
import org.smartregister.stock.openlmis.domain.openlmis.Lot;
import org.smartregister.stock.openlmis.dto.TradeItemDto;
import org.smartregister.stock.openlmis.interactor.StockDetailsInteractor;
import org.smartregister.stock.openlmis.view.contract.StockDetailsView;
import org.smartregister.stock.openlmis.wrapper.StockWrapper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.smartregister.stock.domain.Stock.issued;
import static org.smartregister.stock.domain.Stock.loss_adjustment;
import static org.smartregister.stock.domain.Stock.received;
import static org.smartregister.stock.openlmis.TestData.ADJUST_WIDGET_FORM_DATA;
import static org.smartregister.stock.openlmis.TestData.ISSUE_JSON_FORM_DATA;
import static org.smartregister.stock.openlmis.TestData.RECEIVE_JSON_FORM_DATA;

/**
 * Created by samuelgithengi on 8/3/18.
 */
public class StockDetailsPresenterTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private StockDetailsView stockDetailsView;

    @Mock
    private StockDetailsInteractor stockDetailsInteractor;

    @Mock
    private AllSharedPreferences sharedPreferences;

    @Captor
    private ArgumentCaptor<Stock> argumentCaptor;

    private StockDetailsPresenter stockDetailsPresenter;

    @Before
    public void setUp() {
        stockDetailsPresenter = new StockDetailsPresenter(stockDetailsView, stockDetailsInteractor);

    }

    @Test
    public void testGetTotalStockByLot() {
        UUID lotId = UUID.randomUUID();
        when(stockDetailsInteractor.getTotalStockByLot(lotId.toString())).thenReturn(2);
        assertEquals(2, stockDetailsPresenter.getTotalStockByLot(lotId.toString()));
    }

    @Test
    public void testFindLotsByTradeItemWithoutLots() {
        String tradeItemId = UUID.randomUUID().toString();
        when(stockDetailsInteractor.findLotsByTradeItem(tradeItemId)).thenReturn(new ArrayList<Lot>());
        assertTrue(stockDetailsPresenter.findLotsByTradeItem(tradeItemId).isEmpty());
        verify(stockDetailsView, never()).showLotsHeader();

    }

    @Test
    public void testFindLotsByTradeItemWithLots() {
        String tradeItemId = UUID.randomUUID().toString();
        List<Lot> expectedLots = new ArrayList<>();
        Lot lot = new Lot(UUID.randomUUID().toString(), "LC2018G", 49398438l,
                893943l,
                tradeItemId,
                true);
        expectedLots.add(lot);
        when(stockDetailsInteractor.findLotsByTradeItem(tradeItemId)).thenReturn(expectedLots);
        List<Lot> returnedLots = stockDetailsPresenter.findLotsByTradeItem(tradeItemId);
        assertEquals(1, returnedLots.size());
        assertEquals("LC2018G", returnedLots.get(0).getLotCode());
        assertEquals(49398438l, returnedLots.get(0).getExpirationDate().longValue());
        assertEquals(893943l, returnedLots.get(0).getManufactureDate().longValue());
        assertEquals(tradeItemId, returnedLots.get(0).getTradeItemId());
        verify(stockDetailsView).showLotsHeader();
    }


    @Test
    public void testFindTradeItem() {
        String commodityTypeId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        String tradeItemId = UUID.randomUUID().toString();
        TradeItem expected = new TradeItem(tradeItemId);
        expected.setName("CIntervax BCG 20");
        expected.setNetContent(20l);
        expected.setDateUpdated(now);
        expected.setCommodityTypeId(commodityTypeId);

        when(stockDetailsInteractor.findTradeItem(tradeItemId)).thenReturn(expected);

        TradeItem tradeItem = stockDetailsPresenter.findTradeItem(tradeItemId);
        assertEquals(tradeItemId, tradeItem.getId());
        assertEquals("CIntervax BCG 20", tradeItem.getName());
        assertEquals(20l, tradeItem.getNetContent().longValue());
        assertEquals(now, tradeItem.getDateUpdated().longValue());
        assertEquals(commodityTypeId, tradeItem.getCommodityTypeId());

    }


    @Test
    public void testFindStockByTradeItemWithStock() {

        List<Stock> expected = new ArrayList<>();
        long now = System.currentTimeMillis();
        String tradeItemId = UUID.randomUUID().toString();
        Stock stock = new Stock(null, received, "tester11", 50, now,
                "wareHouse123", "unsynched", now, tradeItemId);
        expected.add(stock);

        stock = new Stock(null, issued, "tester11", -12, now,
                "HO", "unsynched", now, tradeItemId);
        expected.add(stock);

        when(stockDetailsInteractor.getStockByTradeItem(tradeItemId)).thenReturn(expected);

        List<Stock> actual = stockDetailsPresenter.findStockByTradeItem(tradeItemId);
        assertEquals(2, actual.size());
        for (Stock stock1 : actual) {
            assertEquals(tradeItemId, stock1.getStockTypeId());
        }
        verify(stockDetailsView).showTransactionsHeader();

    }

    @Test
    public void testFindStockByTradeItemWithoutStock() {
        String tradeItemId = UUID.randomUUID().toString();
        when(stockDetailsInteractor.getStockByTradeItem(tradeItemId)).thenReturn(new ArrayList<Stock>());

        List<Stock> actual = stockDetailsPresenter.findStockByTradeItem(tradeItemId);
        assertEquals(0, actual.size());
        verify(stockDetailsView, never()).showTransactionsHeader();

    }

    @Test
    public void testPopulateLotNamesAndBalance() {
        String tradeItemId = UUID.randomUUID().toString();
        String lotId = UUID.randomUUID().toString();
        String lot2Id = UUID.randomUUID().toString();

        Map<String, String> expected = new HashMap<>();
        expected.put(lotId, "LC2134K");
        expected.put(lot2Id, "LC5453K");

        when(stockDetailsInteractor.findLotNames(tradeItemId)).thenReturn(expected);

        List<Stock> stockList = new ArrayList<>();
        long now = System.currentTimeMillis();

        Stock stock = new Stock("1", received, "tester11", 50, now,
                "wareHouse123", "unsynched", now, tradeItemId);
        stock.setLotId(lotId);
        stockList.add(stock);

        stock = new Stock("2", issued, "tester11", -12, now,
                "HO", "unsynched", now, tradeItemId);
        stock.setLotId(lotId);
        stockList.add(stock);


        stock = new Stock("3", loss_adjustment, "tester11", -2, now,
                "HO", "unsynched", now, tradeItemId);
        stock.setLotId(lot2Id);
        stockList.add(stock);

        List<StockWrapper> actual = stockDetailsPresenter.populateLotNamesAndBalance(new TradeItemDto(tradeItemId,
                        "GHGR", 100, now, 2, "vials", 5l, "doses"),
                stockList);

        assertEquals(3, actual.size());
        for (StockWrapper stockWrapper : actual) {
            assertEquals(tradeItemId, stockWrapper.getStock().getStockTypeId());
            if (stockWrapper.getStock().getLotId().equals(lotId)) {
                assertEquals("LC2134K", stockWrapper.getLotCode());
                if (stockWrapper.getStock().getId().equals("1"))
                    assertEquals(100, stockWrapper.getStockBalance());
                else
                    assertEquals(50, stockWrapper.getStockBalance());

            } else {
                assertEquals(lot2Id, stockWrapper.getStock().getLotId());
                assertEquals("LC5453K", stockWrapper.getLotCode());
                assertEquals(62, stockWrapper.getStockBalance());
            }
        }
    }


    @Test
    public void testCollapseClicked() {
        stockDetailsPresenter.collapseExpandClicked(View.VISIBLE);
        verify(stockDetailsView).collapseLots();
        verify(stockDetailsView, never()).expandLots();
    }

    @Test
    public void testExpandClicked() {
        stockDetailsPresenter.collapseExpandClicked(View.GONE);
        verify(stockDetailsView).expandLots();
        verify(stockDetailsView, never()).collapseLots();
    }

    @Test
    public void testProcessReceiveFormJsonResult() {
        stockDetailsPresenter.processFormJsonResult(RECEIVE_JSON_FORM_DATA, "openlmis_id_1", sharedPreferences);
        verify(stockDetailsInteractor, times(2)).addStock(argumentCaptor.capture());
        verify(stockDetailsView).refreshStockDetails(15);
        for (Stock stock : argumentCaptor.getAllValues()) {
            assertEquals(received, stock.getTransactionType());
            assertEquals("Balaka District Warehouse", stock.getToFrom());
            assertEquals("Receipts", stock.getReason());
        }
    }

    @Test
    public void testProcessIssueFormJsonResult() {
        stockDetailsPresenter.processFormJsonResult(ISSUE_JSON_FORM_DATA, "openlmis_id_1", sharedPreferences);
        verify(stockDetailsInteractor).addStock(argumentCaptor.capture());
        verify(stockDetailsView).refreshStockDetails(-7);
        for (Stock stock : argumentCaptor.getAllValues()) {
            assertEquals(issued, stock.getTransactionType());
            assertEquals("Comfort Health Clinic", stock.getToFrom());
            assertEquals("Consumed", stock.getReason());
        }
    }

    @Test
    public void testProcessAdjustFormJsonResult() throws ParseException {
        stockDetailsPresenter.processFormJsonResult(ADJUST_WIDGET_FORM_DATA, "openlmis_id_1", sharedPreferences);
        verify(stockDetailsInteractor).addStock(argumentCaptor.capture());
        verify(stockDetailsView).refreshStockDetails(-2);
        for (Stock stock : argumentCaptor.getAllValues()) {
            assertEquals(loss_adjustment, stock.getTransactionType());
            assertEquals(-2, stock.getValue());
            assertEquals("Transferred", stock.getReason());
            assertNull(stock.getToFrom());
            assertEquals(stockDetailsPresenter.simpleDateFormat.parse("05-10-2018").getTime(), stock.getDateCreated().getMillis());
        }
    }

    @Test
    public void testProcessInvalidJsonResult() {
        stockDetailsPresenter.processFormJsonResult("12" + ADJUST_WIDGET_FORM_DATA, "openlmis_id_1", sharedPreferences);
        verify(stockDetailsInteractor, never()).addStock(any(Stock.class));
        verify(stockDetailsView, never()).refreshStockDetails(anyInt());
    }

}

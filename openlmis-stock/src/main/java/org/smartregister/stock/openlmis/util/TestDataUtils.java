package org.smartregister.stock.openlmis.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.LocalDate;
import org.smartregister.stock.openlmis.OpenLMISLibrary;
import org.smartregister.stock.openlmis.domain.Stock;
import org.smartregister.stock.openlmis.domain.TradeItem;
import org.smartregister.stock.openlmis.domain.openlmis.Code;
import org.smartregister.stock.openlmis.domain.openlmis.CommodityType;
import org.smartregister.stock.openlmis.domain.openlmis.Dispensable;
import org.smartregister.stock.openlmis.domain.openlmis.Lot;
import org.smartregister.stock.openlmis.domain.openlmis.Program;
import org.smartregister.stock.openlmis.repository.SearchRepository;
import org.smartregister.stock.openlmis.repository.StockRepository;
import org.smartregister.stock.openlmis.repository.TradeItemRepository;
import org.smartregister.stock.openlmis.repository.openlmis.CommodityTypeRepository;
import org.smartregister.stock.openlmis.repository.openlmis.LotRepository;
import org.smartregister.stock.openlmis.repository.openlmis.ProgramRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by samuelgithengi on 7/30/18.
 */
public class TestDataUtils {


    private CommodityTypeRepository commodityTypeRepository = new CommodityTypeRepository(OpenLMISLibrary.getInstance().getRepository());
    private TradeItemRepository tradeItemRepository = new TradeItemRepository(OpenLMISLibrary.getInstance().getRepository());
    private LotRepository lotRepository = new LotRepository(OpenLMISLibrary.getInstance().getRepository());

    private Map<TradeItem, List<Lot>> lotHashMap = new HashMap<>();

    private static TestDataUtils instance;

    public static TestDataUtils getInstance() {
        if (instance == null)
            instance = new TestDataUtils();
        return instance;
    }

    public void populateTestData() {

        populatePrograms();
        populateCommodityTypes();
        populateTradeItems();
        populateLots();
        //populateStock();
    }


    private void populatePrograms() {
        ProgramRepository programRepository = new ProgramRepository(OpenLMISLibrary.getInstance().getRepository());
        Program program = new Program(UUID.randomUUID(), new Code("PRG003"), "EPI", null, true, true, true, true, true, null);
        programRepository.addOrUpdate(program);

        program = new Program(UUID.randomUUID(), new Code("PRG002"), "Essential Drugs", null, true, true, true, true, true, null);
        programRepository.addOrUpdate(program);
    }

    private void populateCommodityTypes() {
        commodityTypeRepository.addOrUpdate(new CommodityType(UUID.randomUUID(), "BCG", "", null, null, System.currentTimeMillis()));
        commodityTypeRepository.addOrUpdate(new CommodityType(UUID.randomUUID(), "OPV", "", null, null, System.currentTimeMillis()));
        commodityTypeRepository.addOrUpdate(new CommodityType(UUID.randomUUID(), "Penta", "", null, null, System.currentTimeMillis()));
        commodityTypeRepository.addOrUpdate(new CommodityType(UUID.randomUUID(), "Rotavirus", "", null, null, System.currentTimeMillis()));
        commodityTypeRepository.addOrUpdate(new CommodityType(UUID.randomUUID(), "Measles", "", null, null, System.currentTimeMillis()));
    }


    private void populateTradeItems() {
        SearchRepository searchRepository = new SearchRepository(OpenLMISLibrary.getInstance().getRepository());

        for (CommodityType commodityType : commodityTypeRepository.findAllCommodityTypes()) {
            List<TradeItem> tradeItems = createTradeItems(commodityType);
            for (TradeItem tradeItem : tradeItems) {
                tradeItemRepository.addOrUpdate(tradeItem);
                lotHashMap.put(tradeItem, null);
            }
            searchRepository.addOrUpdate(commodityType, tradeItems);
        }
    }

    private void populateLots() {
        for (TradeItem tradeItem : lotHashMap.keySet()) {
            List<Lot> createdLots = createLots(tradeItem.getId());
            lotHashMap.put(tradeItem, createdLots);
            for (Lot lot : createdLots)
                lotRepository.addOrUpdate(lot);
        }
    }


    private void populateStock() {
        StockRepository stockRepository = new StockRepository(OpenLMISLibrary.getInstance().getRepository());
        Random random = new Random();
        for (TradeItem tradeItem : lotHashMap.keySet()) {
            List<Lot> lots = lotHashMap.get(tradeItem);
            for (Lot lot : lots) {
                for (int i = 0; i < random.nextInt(2); i++) {
                    Calendar dateCreated = Calendar.getInstance();
                    dateCreated.add(Calendar.DATE, -random.nextInt(120));
                    int type = random.nextInt(3);
                    String transactionType = type == 0 ? Stock.received : type == 1 ? Stock.issued : Stock.loss_adjustment;
                    int value = 5 + random.nextInt(50);
                    if (transactionType.equals(Stock.issued) ||
                            (transactionType.equals(Stock.loss_adjustment) && random.nextInt(2) == 0))
                        value = -random.nextInt(5);

                    Stock stock = new Stock(null, transactionType, "tester11", value, dateCreated.getTimeInMillis(),
                            RandomStringUtils.randomAlphabetic(6 + random.nextInt(6)), "unsynched", System.currentTimeMillis(), tradeItem.getId());
                    stock.setLotId(lot.getId().toString());
                    stockRepository.addOrUpdate(stock);
                }
            }
        }

    }

    public List<TradeItem> createTradeItems(CommodityType commodityType) {
        Calendar calendar = Calendar.getInstance();
        List<TradeItem> tradeItems = new ArrayList<>();
        Random random = new Random();
        if (commodityType.getName().equals("Rotavirus"))
            return tradeItems;
        TradeItem tradeItem = new TradeItem(UUID.randomUUID().toString());
        tradeItem.setName("Intervax " + commodityType.getName() + " 20");
        tradeItem.setNetContent((long) (2 + random.nextInt(18)));
        tradeItem.setCommodityTypeId(commodityType.getId().toString());
        tradeItem.setDispensable(new Dispensable(UUID.randomUUID(), "vials", "20 vials", null));
        calendar.add(Calendar.DATE, -random.nextInt(300));
        tradeItem.setDateUpdated(calendar.getTimeInMillis());


        tradeItems.add(tradeItem);


        tradeItem = new TradeItem(UUID.randomUUID().toString());
        tradeItem.setName("GSK " + commodityType.getName() + " 20");
        tradeItem.setCommodityTypeId(commodityType.getId().toString());
        tradeItem.setNetContent((long) (2 + random.nextInt(8)));
        tradeItem.setDispensable(new Dispensable(UUID.randomUUID(), "vials", "20 vials", null));
        calendar.add(Calendar.DATE, -random.nextInt(300));
        tradeItem.setDateUpdated(calendar.getTimeInMillis());


        tradeItems.add(tradeItem);
        if (commodityType.getName().equals("Penta"))
            return tradeItems;

        tradeItem = new TradeItem(UUID.randomUUID().toString());
        tradeItem.setName("SII " + commodityType.getName() + " 10");
        tradeItem.setCommodityTypeId(commodityType.getId().toString());
        tradeItem.setNetContent((long) (2 + random.nextInt(18)));
        tradeItem.setDispensable(new Dispensable(UUID.randomUUID(), "vials", "10 vials", null));
        calendar.add(Calendar.DATE, -random.nextInt(300));
        tradeItem.setDateUpdated(calendar.getTimeInMillis());


        tradeItems.add(tradeItem);

        tradeItem = new TradeItem(UUID.randomUUID().toString());
        tradeItem.setName("Sanofi " + commodityType.getName() + " 5");
        tradeItem.setCommodityTypeId(commodityType.getId().toString());
        tradeItem.setNetContent((long) (2 + random.nextInt(8)));
        tradeItem.setDispensable(new Dispensable(UUID.randomUUID(), "vials", "5 vials", null));
        calendar.add(Calendar.DATE, -random.nextInt(300));
        tradeItem.setDateUpdated(calendar.getTimeInMillis());

        tradeItems.add(tradeItem);
        return tradeItems;
    }


    private List<Lot> createLots(String tradeItemId) {
        List<Lot> lots = new ArrayList<>();
        Random random = new Random();
        int numberOfLots = random.nextInt(8);

        for (int i = 0; i < numberOfLots; i++) {
            Calendar calendar = Calendar.getInstance();
            int expiry = random.nextInt(300);
            expiry = random.nextBoolean() ? expiry : -expiry;
            calendar.add(Calendar.DATE, expiry);
            Lot lot = new Lot(UUID.randomUUID(), "LC" + (1000 + random.nextInt(8000)), new LocalDate(calendar.getTimeInMillis()),
                    new LocalDate(System.currentTimeMillis()),
                    new org.smartregister.stock.openlmis.domain.openlmis.TradeItem(UUID.fromString(tradeItemId)),
                    false);
            lot.setLotStatus("VVM1");
            lots.add(lot);
        }
        if (numberOfLots > 1) {
            Lot lot = new Lot(UUID.randomUUID(), "LC" + (1000 + random.nextInt(8000)),
                    lots.get(random.nextInt(numberOfLots - 1)).getExpirationDate(),
                    new LocalDate(System.currentTimeMillis()),
                    new org.smartregister.stock.openlmis.domain.openlmis.TradeItem(UUID.fromString(tradeItemId)),
                    false);
            lot.setLotStatus("VVM2");
            lots.add(lot);
        }
        return lots;
    }
}

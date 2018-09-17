package org.smartregister.stock.openlmis.util;

public interface OpenLMISConstants {

    int EXPIRING_MONTHS_WARNING = 3;

    String TRADE_ITEM = "TRADE_ITEM";

    String LOT_WIDGET = "lot";

    String REVIEW_WIDGET = "review";

    interface JsonForm {

        String TRADE_ITEM = "[trade_item]";

        String TRADE_ITEM_ID = "[trade_item_id]";

        String NET_CONTENT = "[net_content]";

        String DISPENSING_UNIT = "[dispensing_unit]";

        String PREVIOUS = "previous";

        String PREVIOUS_LABEL = "previous_label";

        String NEXT = "next";

        String NEXT_TYPE = "next_type";

        String NEXT_LABEL = "next_label";

        String NEXT_ENABLED = "next_enabled";

        String SUBMIT = "submit";

        String NO_PADDING = "no_padding";

        String LIST_OPTIONS = "list_options";

    }

    interface Forms {

        String INDIVIDUAL_ISSUED_FORM = "individual_issued_form";

        String INDIVIDUAL_RECEIVED_FORM = "individual_received_form";

        String INDIVIDUAL_ADJUST_FORM = "individual_adjustment_form";

        String INDIVIDUAL_NON_LOT_RECEIPT_FORM = "non_lot_individual_receipt_form";
    }
}

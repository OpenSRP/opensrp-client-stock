package org.smartregister.stock.openlmis.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.stock.openlmis.R;
import org.smartregister.stock.openlmis.adapter.ReviewAdapter;
import org.smartregister.stock.openlmis.domain.openlmis.StockCardLineItemReason;
import org.smartregister.stock.openlmis.domain.openlmis.ValidSourceDestination;
import org.smartregister.stock.openlmis.fragment.OpenLMISJsonFormFragment;
import org.smartregister.stock.openlmis.repository.openlmis.ReasonRepository;
import org.smartregister.stock.openlmis.repository.openlmis.ValidSourceDestinationRepository;
import org.smartregister.stock.openlmis.widget.helper.LotDto;
import org.smartregister.util.JsonFormUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.smartregister.stock.openlmis.widget.LotFactory.DISPENSING_UNIT;
import static org.smartregister.stock.openlmis.widget.LotFactory.IS_STOCK_ISSUE;
import static org.smartregister.stock.openlmis.widget.LotFactory.NET_CONTENT;
import static org.smartregister.stock.openlmis.widget.LotFactory.TRADE_ITEM;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.STEP1;


/**
 * Created by samuelgithengi on 8/27/18.
 */
public class ReviewFactory implements FormWidgetFactory {

    public final static String REVIEW_TYPE = "review_type";
    public final static String DATE = "date";
    public final static String FACILITY = "facility";
    public final static String REASON = "reason";
    public final static String OTHER = "Other";
    public final static String STEP2 = "step2";
    public final static String STOCK_LOTS = "stockLots";

    private boolean isStockIssue;

    private ReasonRepository reasonRepository;

    private ValidSourceDestinationRepository validSourceDestinationRepository;

    public ReviewFactory(ReasonRepository reasonRepository, ValidSourceDestinationRepository validSourceDestinationRepository) {
        this.reasonRepository = reasonRepository;
        this.validSourceDestinationRepository = validSourceDestinationRepository;
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return null;
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, final JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {

        String tradeItem = jsonObject.getString(TRADE_ITEM);
        long netContent = jsonObject.getLong(NET_CONTENT);
        String dispensingUnit = jsonObject.getString(DISPENSING_UNIT);
        isStockIssue = jsonObject.optBoolean(IS_STOCK_ISSUE);

        List<View> views = new ArrayList<>(1);
        View root = LayoutInflater.from(context).inflate(R.layout.openlmis_native_form_item_review, null);
        ((TextView) root.findViewById(R.id.review_type)).setText(jsonObject.getString(REVIEW_TYPE));

        JSONObject formJSon = new JSONObject(formFragment.getCurrentJsonState());
        JSONArray step1Fields = JsonFormUtils.fields(formJSon);

        ((TextView) root.findViewById(R.id.date)).setText(JsonFormUtils.getFieldValue(step1Fields, jsonObject.getString(DATE)));
        String facilityId = JsonFormUtils.getFieldValue(step1Fields, jsonObject.getString(FACILITY));
        String reasonId = JsonFormUtils.getFieldValue(step1Fields, jsonObject.getString(REASON));
        StockCardLineItemReason reason = reasonRepository.findReasonById(reasonId).getStockCardLineItemReason();

        ValidSourceDestination facility = validSourceDestinationRepository.findValidSourceDestination(facilityId);
        ((TextView) root.findViewById(R.id.facility)).setText(facility == null ? facilityId : facility.getFacilityName());
        ((TextView) root.findViewById(R.id.reason)).setText(reason == null ? reasonId : reason.getName());
        views.add(root);

        root.findViewById(R.id.edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToFirstStep(formFragment);
            }
        });
        step1Fields = formJSon.getJSONObject(STEP2).getJSONArray(FIELDS);

        String lotsJSON = JsonFormUtils.getFieldValue(step1Fields, STOCK_LOTS);
        RecyclerView reviewRecyclerView = root.findViewById(R.id.review_recyclerView);
        Type listType = new TypeToken<List<LotDto>>() {
        }.getType();
        List<LotDto> selectedLotDTos = LotFactory.gson.fromJson(lotsJSON, listType);
        reviewRecyclerView.setAdapter(new ReviewAdapter(tradeItem, selectedLotDTos));

        displayDosesQuantity((OpenLMISJsonFormFragment) formFragment, context, selectedLotDTos, dispensingUnit, netContent);

        if (isStockIssue) {
            ((TextView) root.findViewById(R.id.facility_label)).setText(R.string.issue_to);
        }
        return views;
    }

    @NonNull
    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return null;
    }

    private void displayDosesQuantity(OpenLMISJsonFormFragment jsonFormFragment, Context context,
                                      List<LotDto> selectedLotDTos, String dispensingUnit, long netContent) {
        int totalQuantity = 0;
        for (LotDto lot : selectedLotDTos)
            totalQuantity += lot.getQuantity();
        jsonFormFragment.setBottomNavigationText(context.getString(
                isStockIssue ? R.string.issued_dose_formatter : R.string.received_dose_formatter,
                totalQuantity, dispensingUnit, totalQuantity * netContent));
    }

    private void navigateToFirstStep(JsonFormFragment formFragment) {
        JsonFormFragment next = OpenLMISJsonFormFragment.getFormFragment(STEP1);
        formFragment.hideKeyBoard();
        formFragment.transactThis(next);
    }
}

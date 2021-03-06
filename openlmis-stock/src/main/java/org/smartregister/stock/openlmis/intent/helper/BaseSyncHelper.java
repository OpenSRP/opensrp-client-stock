package org.smartregister.stock.openlmis.intent.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static org.smartregister.stock.openlmis.util.Utils.sendSyncCompleteBroadCast;

public abstract class BaseSyncHelper {

    protected Context context;

    protected abstract String pullFromServer(String url);

    protected abstract boolean saveResponse(String response, SharedPreferences preferences);

    public void processIntent(String url) {
        String response = pullFromServer(url);
        if (response == null) {
            return;
        }
        boolean isEmptyResponse = saveResponse(response, PreferenceManager.getDefaultSharedPreferences(context));
        if (!isEmptyResponse) {
            sendSyncCompleteBroadCast(context);
        }
    }
}

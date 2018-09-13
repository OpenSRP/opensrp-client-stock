package org.smartregister.stock.openlmis.intent.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static org.smartregister.stock.openlmis.util.Utils.sendSyncCompleteBroadCast;

public abstract class BaseSyncHelper {

    protected Context context;

    protected abstract String pullFromServer();

    protected abstract void saveResponse(String response, SharedPreferences preferences);

    public void processIntent() {
        String response = pullFromServer();
        if (response == null) {
            return;
        }
        saveResponse(response, PreferenceManager.getDefaultSharedPreferences(context));
        sendSyncCompleteBroadCast(context);
    }
}

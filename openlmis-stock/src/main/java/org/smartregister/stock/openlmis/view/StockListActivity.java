package org.smartregister.stock.openlmis.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.smartregister.stock.openlmis.R;

public class StockListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private FloatingActionButton mfFloatingActionButton;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);
        toolbar = findViewById(R.id.location_switching_toolbar);
        setSupportActionBar(toolbar);

        mfFloatingActionButton = findViewById(R.id.fab);
        mfFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });
    }

    private void showPopupMenu(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

}

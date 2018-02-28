package org.smartregister.stock.sample.activity;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.smartregister.stock.activity.StockActivity;
import org.smartregister.stock.activity.StockControlActivity;
import org.smartregister.stock.sample.R;

public class SampleStockActivity extends StockActivity {

    private CustomNavbarListener customNavbarListener = new CustomNavbarListener();

    @Override
    protected String getLoggedInUserInitials() {
        return "RW";
    }

    @Override
    protected NavigationView getNavigationView() {
        NavigationView view = (NavigationView) getLayoutInflater().inflate(R.layout.nav_view, null);
        view.findViewById(R.id.logout_b).setOnClickListener(customNavbarListener);
        view.findViewById(R.id.child_register).setOnClickListener(customNavbarListener);
        view.findViewById(R.id.stock_control).setOnClickListener(customNavbarListener);
        return view;
    }

    @Override
    protected NavigationView.OnNavigationItemSelectedListener getNavigationViewListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                return true;
            }
        };
    }

    @Override
    protected int getNavigationViewWith() {
        return (int) getResources().getDimension(R.dimen.nav_view_width);
    }

    @Override
    protected Class getControlActivity() {
        return StockControlActivity.class;
    }

    private class CustomNavbarListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.logout_b:
                    Toast.makeText(SampleStockActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.child_register:
                    Toast.makeText(SampleStockActivity.this, R.string.app_name + " clicked ", Toast.LENGTH_SHORT).show();
                    ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
                    finish();
                    break;
                case R.id.stock_control:
                    Toast.makeText(SampleStockActivity.this, "Stock Module clicked ", Toast.LENGTH_SHORT).show();
                    ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
                    break;
            }
        }
    }

}
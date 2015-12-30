package com.backdoor.moove;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.backdoor.moove.core.adapters.RemindersRecyclerAdapter;
import com.backdoor.moove.core.data.ReminderDataProvider;
import com.backdoor.moove.core.data.ReminderModel;
import com.backdoor.moove.core.helper.Reminder;
import com.backdoor.moove.core.interfaces.ActionCallbacks;
import com.backdoor.moove.core.interfaces.RecyclerListener;

public class MainActivity extends AppCompatActivity implements RecyclerListener, ActionCallbacks {

    /**
     * Recycler view field.
     */
    private RecyclerView currentList;

    /**
     * Containers.
     */
    private LinearLayout emptyItem;

    /**
     * Reminder data provider for recycler view.
     */
    private ReminderDataProvider provider;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emptyItem = (LinearLayout) findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        ImageView emptyImage = (ImageView) findViewById(R.id.emptyImage);
        emptyImage.setImageResource(R.drawable.ic_alarm_off_48px_white);

        currentList = (RecyclerView) findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        currentList.setLayoutManager(mLayoutManager);

        loaderAdapter();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ReminderManager.class));
            }
        });
    }

    /**
     * Load data to recycler view.
     */
    public void loaderAdapter() {
        provider = new ReminderDataProvider(this);
        reloadView();
        RemindersRecyclerAdapter adapter = new RemindersRecyclerAdapter(this, provider);
        adapter.setEventListener(this);
        currentList.setHasFixedSize(true);
        currentList.setItemAnimator(new DefaultItemAnimator());
        currentList.setAdapter(adapter);
    }

    /**
     * Hide/show recycler view depends on data.
     */
    private void reloadView() {
        int size = provider.getCount();
        if (size > 0) {
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_places:
                startActivity(new Intent(MainActivity.this, PlacesList.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loaderAdapter();
    }

    @Override
    public void onItemSwitched(final int position, final View view) {
        Reminder.toggle(provider.getItem(position).getId(), this, this);
        loaderAdapter();
    }

    @Override
    public void onItemClicked(final int position, final View view) {
        Reminder.edit(provider.getItem(position).getId(), MainActivity.this);
    }

    @Override
    public void onItemLongClicked(final int position, final View view) {
        final CharSequence[] items = {getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                ReminderModel item1 = provider.getItem(position);
                switch (item){
                    case 0:
                        Reminder.edit(item1.getId(), MainActivity.this);
                        break;
                    case 1:
                        Reminder.delete(item1.getId(), MainActivity.this);
                        showSnackbar(R.string.deleted);
                        loaderAdapter();
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void showSnackbar(int message) {
        Snackbar.make(fab, message, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showSnackbar(final int message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(fab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }
}

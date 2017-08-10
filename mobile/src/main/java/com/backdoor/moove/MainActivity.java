package com.backdoor.moove;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.backdoor.moove.core.adapters.RemindersRecyclerAdapter;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.consts.QuickReturnViewType;
import com.backdoor.moove.core.data.ReminderDataProvider;
import com.backdoor.moove.core.data.ReminderModel;
import com.backdoor.moove.core.dialogs.ChangeDialog;
import com.backdoor.moove.core.dialogs.RateDialog;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.core.helper.Reminder;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.interfaces.ActionCallbacks;
import com.backdoor.moove.core.interfaces.RecyclerListener;
import com.backdoor.moove.core.utils.QuickReturnUtils;
import com.backdoor.moove.core.utils.SuperUtil;
import com.backdoor.moove.core.views.ReturnScrollListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerListener, ActionCallbacks {

    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private ArrayList<ReminderModel> arrayList;

    private FloatingActionButton fab;
    private ReturnScrollListener scrollListener;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Module.isLollipop()) {
            Slide slideTransition = new Slide();
            slideTransition.setSlideEdge(Gravity.START);
            slideTransition.setDuration(getResources().getInteger(R.integer.anim_duration_long));
            getWindow().setReenterTransition(slideTransition);
            getWindow().setExitTransition(slideTransition);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emptyItem = findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        ImageView emptyImage = findViewById(R.id.emptyImage);
        emptyImage.setImageResource(R.drawable.ic_alarm_off_48px_white);

        currentList = findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        currentList.setLayoutManager(mLayoutManager);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Permissions.checkPermission(MainActivity.this, Permissions.WRITE_EXTERNAL)) {
                    Intent intent = new Intent(MainActivity.this, ReminderManager.class);
                    startActivity(intent);
                } else {
                    Permissions.requestPermission(MainActivity.this, 1116,
                            Permissions.WRITE_EXTERNAL);
                }
            }
        });

        Moove application = (Moove) getApplication();
        mTracker = application.getDefaultTracker();
    }

    /**
     * Load data to recycler view.
     */
    public void loaderAdapter() {
        arrayList = ReminderDataProvider.load(this);
        reloadView();
        RemindersRecyclerAdapter adapter = new RemindersRecyclerAdapter(this, arrayList);
        adapter.setEventListener(this);
        currentList.setHasFixedSize(true);
        currentList.setItemAnimator(new DefaultItemAnimator());
        currentList.setAdapter(adapter);

        if (scrollListener != null) {
            currentList.removeOnScrollListener(scrollListener);
        }
        scrollListener = new ReturnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(fab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build();

        if (Module.isLollipop()) {
            currentList.addOnScrollListener(scrollListener);
        } else {
            currentList.setOnScrollListener(scrollListener);
        }
    }

    /**
     * Hide/show recycler view depends on data.
     */
    private void reloadView() {
        int size = arrayList.size();
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
            case R.id.action_directions:
                startActivity(new Intent(MainActivity.this, LocationsMap.class));
                return true;
            case R.id.action_more:
                SuperUtil.showMore(MainActivity.this);
                return true;
            case R.id.action_donate:
                startActivity(new Intent(MainActivity.this, DonateActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loaderAdapter();
        mTracker.setScreenName("Screen~" + getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        showRate();
        isChangesShown();
    }

    private void showChanges() {
        startActivity(new Intent(this, ChangeDialog.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void isChangesShown() {
        SharedPrefs sPrefs = new SharedPrefs(this);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = null;
        if (pInfo != null) {
            version = pInfo.versionName;
        }
        boolean ranBefore = sPrefs.loadVersionBoolean(version);
        if (!ranBefore) {
            sPrefs.saveVersionBoolean(version);
            showChanges();
        }
    }

    private void showRate() {
        SharedPrefs sPrefs = new SharedPrefs(this);

        if (sPrefs.isString(Prefs.RATE_SHOW)) {
            if (!sPrefs.loadBoolean(Prefs.RATE_SHOW)) {
                int counts = sPrefs.loadInt(Prefs.APP_RUNS_COUNT);
                if (counts < 10) {
                    sPrefs.saveInt(Prefs.APP_RUNS_COUNT, counts + 1);
                } else {
                    sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
                    startActivity(new Intent(this, RateDialog.class));
                }
            }
        } else {
            sPrefs.saveBoolean(Prefs.RATE_SHOW, false);
            sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
        }
    }

    @Override
    public void onItemSwitched(final int position, final View view) {
        Reminder.toggle(arrayList.get(position).getId(), this, this);
        loaderAdapter();
    }

    @Override
    public void onItemClicked(final int position, final View view) {
        Reminder.edit(arrayList.get(position).getId(), MainActivity.this);
    }

    @Override
    public void onItemLongClicked(final int position, final View view) {
        final CharSequence[] items = {getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                ReminderModel item1 = arrayList.get(position);
                switch (item) {
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

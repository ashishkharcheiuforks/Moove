package com.backdoor.moove.core.dialogs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.shared.SharedConst;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class WearTest extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private static final String TAG = "WearPhone";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_test);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button button = findViewById(R.id.button1);
        Button button1 = findViewById(R.id.button2);
        Button button2 = findViewById(R.id.button3);

        button.setOnClickListener(v -> testReminder(Constants.TYPE_LOCATION_OUT_CALL));
        button1.setOnClickListener(v -> testReminder(Constants.TYPE_LOCATION_MESSAGE));
        button2.setOnClickListener(v -> testReminder(Constants.TYPE_LOCATION));


        Log.d(TAG, "On service create");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void testReminder(String type) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_REMINDER);

        DataMap map = putDataMapReq.getDataMap();
        map.putString(SharedConst.KEY_TYPE, type);
        map.putString(SharedConst.KEY_TASK, "Hello Wear!");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        Log.d(TAG, "Data sent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "On connected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "On connection suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "On connection failed");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "Data changed");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(SharedConst.PHONE_REMINDER) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    int keyCode = dataMap.getInt(SharedConst.REQUEST_KEY);
                    updateCount(keyCode);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private void updateCount(int anInt) {
        Toast.makeText(this, "Received " + anInt, Toast.LENGTH_SHORT).show();
    }
}

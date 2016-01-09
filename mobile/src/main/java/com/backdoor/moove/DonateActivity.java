package com.backdoor.moove;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

public class DonateActivity extends AppCompatActivity {

    private static final int REQUEST_BASE = 1005;
    private static final int REQUEST_STANDARD = 1006;
    private static final int REQUEST_PRO = 1007;
    private static final int REQUEST_TOP = 1008;

    private static final String SKU_1 = "donate01";
    private static final String SKU_2 = "donate02";
    private static final String SKU_3 = "donate03";
    private static final String SKU_4 = "donate04";

    private static final char[] symbols = new char[36];

    static {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }

    private IInAppBillingService mService;

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            loadItems();
        }
    };

    private Button buyButton, buyButton1, buyButton2, buyButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buyButton = (Button) findViewById(R.id.buyButton);
        buyButton1 = (Button) findViewById(R.id.buyButton1);
        buyButton2 = (Button) findViewById(R.id.buyButton2);
        buyButton3 = (Button) findViewById(R.id.buyButton3);

        buyButton.setEnabled(false);
        buyButton2.setEnabled(false);
        buyButton1.setEnabled(false);
        buyButton3.setEnabled(false);

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(SKU_1, REQUEST_BASE);
            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(SKU_2, REQUEST_STANDARD);
            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(SKU_3, REQUEST_PRO);
            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(SKU_4, REQUEST_TOP);
            }
        });

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void buyItem(String sku, int requestCode) {
        try {
            RandomString randomString = new RandomString(36);
            String payload = randomString.nextString();
            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    sku, "inapp", payload);
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            startIntentSenderForResult(pendingIntent.getIntentSender(),
                    requestCode, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                    Integer.valueOf(0));
        } catch (RemoteException | IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void loadItems() {
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add(SKU_1);
        skuList.add(SKU_2);
        skuList.add(SKU_3);
        skuList.add(SKU_4);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        try {
            Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList
                        = skuDetails.getStringArrayList("DETAILS_LIST");

                if (responseList != null) {
                    for (String thisResponse : responseList) {
                        JSONObject object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");
                        String price = object.getString("price");
                        if (sku.equals(SKU_1)) {
                            buyButton.setText(price);
                            buyButton.setEnabled(true);
                        } else if (sku.equals(SKU_2)) {
                            buyButton1.setText(price);
                            buyButton1.setEnabled(true);
                        } else if (sku.equals(SKU_3)) {
                            buyButton2.setText(price);
                            buyButton2.setEnabled(true);
                        } else if (sku.equals(SKU_4)) {
                            buyButton3.setText(price);
                            buyButton3.setEnabled(true);
                        }
                    }
                }
            }
        } catch (RemoteException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_BASE:
                if (resultCode == RESULT_OK) {
                    buyButton.setEnabled(false);
                }
                break;
            case REQUEST_STANDARD:
                if (resultCode == RESULT_OK) {
                    buyButton1.setEnabled(false);
                }
                break;
            case REQUEST_PRO:
                if (resultCode == RESULT_OK) {
                    buyButton2.setEnabled(false);
                }
                break;
            case REQUEST_TOP:
                if (resultCode == RESULT_OK) {
                    buyButton3.setEnabled(false);
                }
                break;
        }
    }

    public class RandomString {

        /*
         * static { for (int idx = 0; idx < 10; ++idx) symbols[idx] = (char)
         * ('0' + idx); for (int idx = 10; idx < 36; ++idx) symbols[idx] =
         * (char) ('a' + idx - 10); }
         */

        private final Random random = new Random();

        private final char[] buf;

        public RandomString(int length) {
            if (length < 1)
                throw new IllegalArgumentException("length < 1: " + length);
            buf = new char[length];
        }

        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }

    }

    public final class SessionIdentifierGenerator {

        private SecureRandom random = new SecureRandom();

        public String nextSessionId() {
            return new BigInteger(130, random).toString(32);
        }

    }
}

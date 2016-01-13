package com.backdoor.moove;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.iab.IabHelper;
import com.backdoor.moove.core.iab.IabResult;
import com.backdoor.moove.core.iab.Inventory;
import com.backdoor.moove.core.iab.Purchase;
import com.backdoor.moove.core.iab.SkuDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DonateActivity extends AppCompatActivity implements IabHelper.QueryInventoryFinishedListener {

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

    private IabHelper mHelper;

    private Button buyButton, buyButton1, buyButton2, buyButton3;

    private String mPayload;

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
        buyButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(SKU_2, REQUEST_STANDARD);
            }
        });
        buyButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(SKU_3, REQUEST_PRO);
            }
        });
        buyButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(SKU_4, REQUEST_TOP);
            }
        });

        RandomString randomString = new RandomString(36);
        mPayload = randomString.nextString();

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjyOOepL/ahDs5UJd2h6t9QedIM6wVJ6N4FiV65az9W07976iU4/JTOfsKg2Eps4vTqnK/WnvJeQRHFLtaOKz1xAueddFwmVZYJaz2Y0vDvS6ivjC+8CUuAskSysNeFCW8HoBssJMii45Oq45FLHSgqZ9ITh1CC1yMh/ESPsH8/uc0jIjQvX18bbAhorFzAbEemy+nQVf69Edz2uKkw7R0F+eVCvNbxQzy/DlVVb4Jicy5nqLhfn7nsAndu7eTVWTUSFwBjdnr1ezOiONO8yUi+Nzg2mLfS3v6GOxfoV6AKcsrzb+ELBoqnZjLmLZy3MO8nOQ5a2xPJtSOzuEBg4J2QIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        //mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    return;
                }

                if (mHelper == null) return;

                List<String> list = new ArrayList<>();
                list.add(SKU_1);
                list.add(SKU_2);
                list.add(SKU_3);
                list.add(SKU_4);
                mHelper.queryInventoryAsync(true, list, DonateActivity.this);
                setWaitScreen(true);
            }
        });
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                setWaitScreen(false);
                return;
            }

            switch (purchase.getSku()) {
                case SKU_1:
                    buyButton.setEnabled(false);
                    setWaitScreen(false);
                    break;
                case SKU_2:
                    buyButton1.setEnabled(false);
                    setWaitScreen(false);
                    break;
                case SKU_3:
                    buyButton2.setEnabled(false);
                    setWaitScreen(false);
                    break;
                case SKU_4:
                    buyButton3.setEnabled(false);
                    setWaitScreen(false);
                    break;
            }
        }
    };

    private boolean verifyDeveloperPayload(Purchase purchase) {
        String payload = purchase.getDeveloperPayload();
        return payload.matches(mPayload);
    }

    private void buyItem(String sku, int requestCode) {
        mHelper.launchPurchaseFlow(this, sku, requestCode,
                mPurchaseFinishedListener, mPayload);
    }

    void setWaitScreen(boolean set) {
        findViewById(R.id.waitProgress).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(Constants.LOG_TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
        if (result.isFailure()) {
            return;
        }

        if (inv != null) {
            SkuDetails details = inv.getSkuDetails(SKU_1);
            if (details != null) {
                buyButton.setEnabled(true);
                buyButton.setText(details.getPrice());
                Purchase purchase = inv.getPurchase(SKU_1);
                if (purchase != null) buyButton.setEnabled(false);
            }

            details = inv.getSkuDetails(SKU_2);
            if (details != null) {
                buyButton1.setEnabled(true);
                buyButton1.setText(details.getPrice());
                Purchase purchase = inv.getPurchase(SKU_2);
                if (purchase != null) buyButton1.setEnabled(false);
            }

            details = inv.getSkuDetails(SKU_3);
            if (details != null) {
                buyButton2.setEnabled(true);
                buyButton2.setText(details.getPrice());
                Purchase purchase = inv.getPurchase(SKU_3);
                if (purchase != null) buyButton2.setEnabled(false);
            }

            details = inv.getSkuDetails(SKU_4);
            if (details != null) {
                buyButton3.setEnabled(true);
                buyButton3.setText(details.getPrice());
                Purchase purchase = inv.getPurchase(SKU_4);
                if (purchase != null) buyButton3.setEnabled(false);
            }
        }
        setWaitScreen(false);
    }

    public class RandomString {
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
}

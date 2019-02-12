package com.backdoor.moove

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button

import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.iab.IabHelper
import com.backdoor.moove.core.iab.IabResult
import com.backdoor.moove.core.iab.Inventory
import com.backdoor.moove.core.iab.Purchase
import com.backdoor.moove.core.iab.SkuDetails

import java.util.ArrayList
import java.util.Random

class DonateActivity : AppCompatActivity(), IabHelper.QueryInventoryFinishedListener {

    private var mHelper: IabHelper? = null
    private var buyButton: Button? = null
    private var buyButton1: Button? = null
    private var buyButton2: Button? = null
    private var buyButton3: Button? = null
    private var mPayload: String? = null

    // Callback for when a purchase is finished
    internal var mPurchaseFinishedListener: IabHelper.OnIabPurchaseFinishedListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
        // if we were disposed of in the meantime, quit.
        if (mHelper == null) return@OnIabPurchaseFinishedListener

        if (result.isFailure) {
            setWaitScreen(false)
            return@OnIabPurchaseFinishedListener
        }
        if (!verifyDeveloperPayload(purchase!!)) {
            setWaitScreen(false)
            return@OnIabPurchaseFinishedListener
        }

        when (purchase.sku) {
            SKU_1 -> {
                buyButton!!.isEnabled = false
                setWaitScreen(false)
            }
            SKU_2 -> {
                buyButton1!!.isEnabled = false
                setWaitScreen(false)
            }
            SKU_3 -> {
                buyButton2!!.isEnabled = false
                setWaitScreen(false)
            }
            SKU_4 -> {
                buyButton3!!.isEnabled = false
                setWaitScreen(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        buyButton = findViewById(R.id.buyButton)
        buyButton1 = findViewById(R.id.buyButton1)
        buyButton2 = findViewById(R.id.buyButton2)
        buyButton3 = findViewById(R.id.buyButton3)

        buyButton!!.isEnabled = false
        buyButton2!!.isEnabled = false
        buyButton1!!.isEnabled = false
        buyButton3!!.isEnabled = false

        buyButton!!.setOnClickListener { v -> buyItem(SKU_1, REQUEST_BASE) }
        buyButton1!!.setOnClickListener { v -> buyItem(SKU_2, REQUEST_STANDARD) }
        buyButton2!!.setOnClickListener { v -> buyItem(SKU_3, REQUEST_PRO) }
        buyButton3!!.setOnClickListener { v -> buyItem(SKU_4, REQUEST_TOP) }

        val randomString = RandomString(36)
        mPayload = randomString.nextString()

        val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjyOOepL/ahDs5UJd2h6t9QedIM6wVJ6N4FiV65az9W07976iU4/JTOfsKg2Eps4vTqnK/WnvJeQRHFLtaOKz1xAueddFwmVZYJaz2Y0vDvS6ivjC+8CUuAskSysNeFCW8HoBssJMii45Oq45FLHSgqZ9ITh1CC1yMh/ESPsH8/uc0jIjQvX18bbAhorFzAbEemy+nQVf69Edz2uKkw7R0F+eVCvNbxQzy/DlVVb4Jicy5nqLhfn7nsAndu7eTVWTUSFwBjdnr1ezOiONO8yUi+Nzg2mLfS3v6GOxfoV6AKcsrzb+ELBoqnZjLmLZy3MO8nOQ5a2xPJtSOzuEBg4J2QIDAQAB"

        mHelper = IabHelper(this, base64EncodedPublicKey)

        //mHelper.enableDebugLogging(true);
        mHelper!!.startSetup { result ->
            if (!result.isSuccess) {
                return@mHelper.startSetup
            }

            if (mHelper == null) return@mHelper.startSetup

            val list = ArrayList<String>()
            list.add(SKU_1)
            list.add(SKU_2)
            list.add(SKU_3)
            list.add(SKU_4)
            mHelper!!.queryInventoryAsync(true, list, this@DonateActivity)
            setWaitScreen(true)
        }
    }

    private fun verifyDeveloperPayload(purchase: Purchase): Boolean {
        val payload = purchase.developerPayload
        return payload.matches(mPayload.toRegex())
    }

    private fun buyItem(sku: String, requestCode: Int) {
        mHelper!!.launchPurchaseFlow(this, sku, requestCode,
                mPurchaseFinishedListener, mPayload)
    }

    internal fun setWaitScreen(set: Boolean) {
        findViewById<View>(R.id.waitProgress).visibility = if (set) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mHelper != null) {
            mHelper!!.dispose()
            mHelper = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (mHelper == null) return

        if (!mHelper!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        } else {
            Log.d(Constants.LOG_TAG, "onActivityResult handled by IABUtil.")
        }
    }

    override fun onQueryInventoryFinished(result: IabResult, inv: Inventory?) {
        if (result.isFailure) {
            return
        }

        if (inv != null) {
            var details = inv.getSkuDetails(SKU_1)
            if (details != null) {
                buyButton!!.isEnabled = true
                buyButton!!.text = details.price
                val purchase = inv.getPurchase(SKU_1)
                if (purchase != null) buyButton!!.isEnabled = false
            }

            details = inv.getSkuDetails(SKU_2)
            if (details != null) {
                buyButton1!!.isEnabled = true
                buyButton1!!.text = details.price
                val purchase = inv.getPurchase(SKU_2)
                if (purchase != null) buyButton1!!.isEnabled = false
            }

            details = inv.getSkuDetails(SKU_3)
            if (details != null) {
                buyButton2!!.isEnabled = true
                buyButton2!!.text = details.price
                val purchase = inv.getPurchase(SKU_3)
                if (purchase != null) buyButton2!!.isEnabled = false
            }

            details = inv.getSkuDetails(SKU_4)
            if (details != null) {
                buyButton3!!.isEnabled = true
                buyButton3!!.text = details.price
                val purchase = inv.getPurchase(SKU_4)
                if (purchase != null) buyButton3!!.isEnabled = false
            }
        }
        setWaitScreen(false)
    }

    inner class RandomString internal constructor(length: Int) {
        private val random = Random()

        private val buf: CharArray

        init {
            if (length < 1)
                throw IllegalArgumentException("length < 1: $length")
            buf = CharArray(length)
        }

        internal fun nextString(): String {
            for (idx in buf.indices)
                buf[idx] = symbols[random.nextInt(symbols.size)]
            return String(buf)
        }

    }

    companion object {

        private val REQUEST_BASE = 1005
        private val REQUEST_STANDARD = 1006
        private val REQUEST_PRO = 1007
        private val REQUEST_TOP = 1008

        private val SKU_1 = "donate01"
        private val SKU_2 = "donate02"
        private val SKU_3 = "donate03"
        private val SKU_4 = "donate04"

        private val symbols = CharArray(36)

        init {
            for (idx in 0..9)
                symbols[idx] = ('0'.toInt() + idx).toChar()
            for (idx in 10..35)
                symbols[idx] = ('a'.toInt() + idx - 10).toChar()
        }
    }
}

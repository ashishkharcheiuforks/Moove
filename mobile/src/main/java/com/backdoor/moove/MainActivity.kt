package com.backdoor.moove

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.transition.Slide
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout

import com.backdoor.moove.core.adapters.RemindersRecyclerAdapter
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.consts.QuickReturnViewType
import com.backdoor.moove.core.data.ReminderDataProvider
import com.backdoor.moove.core.data.ReminderModel
import com.backdoor.moove.core.dialogs.ChangeDialog
import com.backdoor.moove.core.dialogs.RateDialog
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.core.helper.Reminder
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.interfaces.ActionCallbacks
import com.backdoor.moove.core.interfaces.RecyclerListener
import com.backdoor.moove.core.utils.QuickReturnUtils
import com.backdoor.moove.utils.SuperUtil
import com.backdoor.moove.core.views.ReturnScrollListener

import java.util.ArrayList

class MainActivity : AppCompatActivity(), RecyclerListener, ActionCallbacks {

    private var currentList: RecyclerView? = null
    private var emptyItem: LinearLayout? = null
    private var arrayList: ArrayList<ReminderModel>? = null

    private var fab: FloatingActionButton? = null
    private var scrollListener: ReturnScrollListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Module.isLollipop) {
            val slideTransition = Slide()
            slideTransition.slideEdge = Gravity.START
            slideTransition.duration = resources.getInteger(R.integer.anim_duration_long).toLong()
            window.reenterTransition = slideTransition
            window.exitTransition = slideTransition
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        emptyItem = findViewById(R.id.emptyItem)
        emptyItem!!.visibility = View.VISIBLE

        val emptyImage = findViewById<ImageView>(R.id.emptyImage)
        emptyImage.setImageResource(R.drawable.ic_alarm_off_48px_white)

        currentList = findViewById(R.id.currentList)
        val mLayoutManager = LinearLayoutManager(this)
        currentList!!.layoutManager = mLayoutManager

        fab = findViewById(R.id.fab)
        fab!!.setOnClickListener { view ->
            if (Permissions.checkPermission(this@MainActivity, Permissions.WRITE_EXTERNAL)) {
                val intent = Intent(this@MainActivity, ReminderManagerActivity::class.java)
                startActivity(intent)
            } else {
                Permissions.requestPermission(this@MainActivity, 1116,
                        Permissions.WRITE_EXTERNAL)
            }
        }
    }

    /**
     * Load data to recycler view.
     */
    fun loaderAdapter() {
        arrayList = ReminderDataProvider.load(this)
        reloadView()
        val adapter = RemindersRecyclerAdapter(this, arrayList)
        adapter.eventListener = this
        currentList!!.setHasFixedSize(true)
        currentList!!.itemAnimator = DefaultItemAnimator()
        currentList!!.adapter = adapter

        if (scrollListener != null) {
            currentList!!.removeOnScrollListener(scrollListener!!)
        }
        scrollListener = ReturnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(fab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build()

        if (Module.isLollipop) {
            currentList!!.addOnScrollListener(scrollListener!!)
        } else {
            currentList!!.setOnScrollListener(scrollListener)
        }
    }

    /**
     * Hide/show recycler view depends on data.
     */
    private fun reloadView() {
        val size = arrayList!!.size
        if (size > 0) {
            currentList!!.visibility = View.VISIBLE
            emptyItem!!.visibility = View.GONE
        } else {
            currentList!!.visibility = View.GONE
            emptyItem!!.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            R.id.action_places -> {
                startActivity(Intent(this@MainActivity, PlacesListActivity::class.java))
                return true
            }
            R.id.action_directions -> {
                startActivity(Intent(this@MainActivity, LocationsMapActivity::class.java))
                return true
            }
            R.id.action_more -> {
                SuperUtil.showMore(this@MainActivity)
                return true
            }
            R.id.action_donate -> {
                startActivity(Intent(this@MainActivity, DonateActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loaderAdapter()

        showRate()
        isChangesShown()
    }

    private fun showChanges() {
        startActivity(Intent(this, ChangeDialog::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun isChangesShown() {
        var pInfo: PackageInfo? = null
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        var version: String? = null
        if (pInfo != null) {
            version = pInfo.versionName
        }
        val prefs = SharedPrefs.getInstance(this)
        if (prefs != null) {
            val ranBefore = prefs.loadVersionBoolean(version)
            if (!ranBefore) {
                prefs.saveVersionBoolean(version)
                showChanges()
            }
        }
    }

    private fun showRate() {
        val prefs = SharedPrefs.getInstance(this) ?: return
        if (prefs.isString(Prefs.RATE_SHOW)) {
            if (!prefs.loadBoolean(Prefs.RATE_SHOW)) {
                val counts = prefs.loadInt(Prefs.APP_RUNS_COUNT)
                if (counts < 10) {
                    prefs.saveInt(Prefs.APP_RUNS_COUNT, counts + 1)
                } else {
                    prefs.saveInt(Prefs.APP_RUNS_COUNT, 0)
                    startActivity(Intent(this, RateDialog::class.java))
                }
            }
        } else {
            prefs.saveBoolean(Prefs.RATE_SHOW, false)
            prefs.saveInt(Prefs.APP_RUNS_COUNT, 0)
        }
    }

    override fun onItemSwitched(position: Int, view: View) {
        Reminder.toggle(arrayList!![position].id, this, this)
        loaderAdapter()
    }

    override fun onItemClicked(position: Int, view: View) {
        Reminder.edit(arrayList!![position].id, this@MainActivity)
    }

    override fun onItemLongClicked(position: Int, view: View) {
        val items = arrayOf<CharSequence>(getString(R.string.edit), getString(R.string.delete))
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setItems(items) { dialog, item ->
            dialog.dismiss()
            val item1 = arrayList!![position]
            when (item) {
                0 -> Reminder.edit(item1.id, this@MainActivity)
                1 -> {
                    Reminder.delete(item1.id, this@MainActivity)
                    showSnackbar(R.string.deleted)
                    loaderAdapter()
                }
            }
        }
        val alert = builder.create()
        alert.show()
    }

    override fun showSnackbar(message: Int) {
        Snackbar.make(fab!!, message, Snackbar.LENGTH_LONG)
                .show()
    }

    override fun showSnackbar(message: Int, actionTitle: Int, listener: View.OnClickListener) {
        Snackbar.make(fab!!, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show()
    }
}

package com.backdoor.moove

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.backdoor.moove.modern_ui.places.list.PlacesAdapter
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.QuickReturnViewType
import com.backdoor.moove.core.data.PlaceDataProvider
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.core.interfaces.SimpleListener
import com.backdoor.moove.core.utils.LocationUtil
import com.backdoor.moove.core.utils.QuickReturnUtils
import com.backdoor.moove.core.views.ReturnScrollListener

class PlacesListActivity : AppCompatActivity(), SimpleListener {

    private var listView: RecyclerView? = null
    private var emptyItem: LinearLayout? = null
    private var mFab: FloatingActionButton? = null

    private var provider: PlaceDataProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@PlacesListActivity)
        setTheme(cs.style)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cs.colorPrimaryDark()
        }
        setContentView(R.layout.places_activity_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = getString(R.string.places)

        emptyItem = findViewById(R.id.emptyItem)
        emptyItem!!.visibility = View.VISIBLE

        val emptyText = findViewById<TextView>(R.id.emptyText)
        emptyText.text = getString(R.string.no_places)

        val emptyImage = findViewById<ImageView>(R.id.emptyImage)
        emptyImage.setImageResource(R.drawable.place_white)

        listView = findViewById(R.id.currentList)

        mFab = findViewById(R.id.fab)
        mFab!!.setOnClickListener { v ->
            if (LocationUtil.playServicesFullCheck(this@PlacesListActivity)) {
                if (Permissions.checkPermission(this@PlacesListActivity, Permissions.ACCESS_COARSE_LOCATION)) {
                    startActivity(Intent(this@PlacesListActivity, NewPlaceActivity::class.java))
                } else {
                    Permissions.requestPermission(this@PlacesListActivity, 101, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    private fun loadPlaces() {
        provider = PlaceDataProvider(this, true)
        reloadView()
        val adapter = PlacesAdapter(this, provider, false)
        adapter.eventListener = this
        listView!!.layoutManager = LinearLayoutManager(this)
        listView!!.adapter = adapter
        listView!!.itemAnimator = DefaultItemAnimator()
        val scrollListener = ReturnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build()

        if (Module.isLollipop) {
            listView!!.addOnScrollListener(scrollListener)
        } else {
            listView!!.setOnScrollListener(scrollListener)
        }
    }

    private fun reloadView() {
        if (provider == null) return
        val size = provider!!.count
        if (size > 0) {
            listView!!.visibility = View.VISIBLE
            emptyItem!!.visibility = View.GONE
        } else {
            listView!!.visibility = View.GONE
            emptyItem!!.visibility = View.VISIBLE
        }
    }

    private fun deletePlace(position: Int) {
        if (provider == null) return
        val id = provider!!.getItem(position)!!.id
        if (id != 0L) {
            val db = DataBase(this)
            db.open()
            db.deletePlace(id)
            db.close()
            Snackbar.make(mFab!!, R.string.deleted, Snackbar.LENGTH_LONG)
                    .show()
            loadPlaces()
        }
    }

    private fun editPlace(position: Int) {
        if (provider == null) return
        startActivity(Intent(this, NewPlaceActivity::class.java)
                .putExtra(Constants.ITEM_ID_INTENT, provider!!.getItem(position)!!.id))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlaces()
    }

    override fun onItemClicked(position: Int, view: View) {
        editPlace(position)
    }

    override fun onItemLongClicked(position: Int, view: View) {
        val items = arrayOf<CharSequence>(getString(R.string.edit), getString(R.string.delete))
        val builder = AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            dialog.dismiss()
            if (item == 0) {
                editPlace(position)
            }
            if (item == 1) {
                deletePlace(position)
            }
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            101 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this@PlacesListActivity, NewPlaceActivity::class.java))
            } else {
                Permissions.showInfo(this@PlacesListActivity, Permissions.READ_CALENDAR)
            }
        }
    }
}

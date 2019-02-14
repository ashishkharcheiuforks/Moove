package com.backdoor.moove

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast

import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.fragments.MapFragment
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.interfaces.MapListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class NewPlaceActivity : AppCompatActivity(), MapListener {

    private var placeName: EditText? = null
    private var fragment: MapFragment? = null

    private var place: LatLng? = null
    private var placeTitle: String? = null
    private var id: Long = 0
    private var mItem: Item? = null
    private val mMapCallback = MapFragment.MapCallback {
        if (mItem != null) {
            fragment!!.addMarker(mItem!!.pos, mItem!!.title, true, true, mItem!!.radius)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@NewPlaceActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cs.colorPrimaryDark()
        }
        setContentView(R.layout.new_place_activity_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0)
        placeName = findViewById(R.id.placeName)

        val prefs = SharedPrefs.getInstance(this)

        fragment = MapFragment.newInstance(false, false, false, false,
                prefs?.loadInt(Prefs.MARKER_STYLE) ?: GoogleMap.MAP_TYPE_NORMAL)
        fragment!!.setListener(this)
        fragment!!.setMapReadyCallback(mMapCallback)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment!!)
                .addToBackStack(null)
                .commit()
    }

    private fun addPlace() {
        if (place != null) {
            var task: String? = placeName!!.text.toString().trim { it <= ' ' }
            if (task!!.matches("".toRegex())) {
                task = placeTitle
            }
            if (task == null || task.matches("".toRegex())) {
                placeName!!.error = getString(R.string.empty_field)
                return
            }
            val latitude = place!!.latitude
            val longitude = place!!.longitude

            val db = DataBase(this@NewPlaceActivity)
            db.open()
            if (id != 0L) {
                db.updatePlace(id, task, latitude, longitude)
            } else {
                db.insertPlace(task, latitude, longitude)
            }
            db.close()
            finish()
        } else {
            Toast.makeText(this@NewPlaceActivity, getString(R.string.no_place_selected), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_add -> {
                addPlace()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.save_menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        loadPlace()
    }

    private fun loadPlace() {
        if (id != 0L) {
            val prefs = SharedPrefs.getInstance(this)
            val radius = prefs?.loadInt(Prefs.LOCATION_RADIUS) ?: 25
            val db = DataBase(this@NewPlaceActivity)
            db.open()
            val c = db.getPlace(id)
            if (c != null && c.moveToFirst()) {
                val text = c.getString(c.getColumnIndex(DataBase.NAME))
                val latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE))
                mItem = Item(text, LatLng(latitude, longitude), radius)
                placeName!!.setText(text)
            }
            c?.close()
            db.close()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun placeChanged(place: LatLng) {
        this.place = place
    }

    override fun onZoomClick(isFull: Boolean) {

    }

    override fun placeName(name: String) {
        this.placeTitle = name
    }

    override fun onBackClick() {

    }

    private inner class Item internal constructor(private val title: String, private val pos: LatLng, private val radius: Int)
}

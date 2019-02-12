package com.backdoor.moove

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View

import com.backdoor.moove.core.adapters.PlaceRecyclerAdapter
import com.backdoor.moove.core.data.MarkerModel
import com.backdoor.moove.core.data.PlaceDataProvider
import com.backdoor.moove.core.fragments.MapFragment
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.core.helper.Reminder
import com.backdoor.moove.core.interfaces.SimpleListener

class LocationsMapActivity : AppCompatActivity(), SimpleListener {

    private var provider: PlaceDataProvider? = null
    private var fragment: MapFragment? = null
    private val mMapCallback = MapFragment.MapCallback { this.showMarkers() }

    private fun showMarkers() {
        for (markerModel in provider!!.data!!) {
            fragment!!.addMarker(markerModel.position, markerModel.title, false, markerModel.icon, false, markerModel.radius)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@LocationsMapActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cs.colorPrimaryDark()
        }
        setContentView(R.layout.activity_locations_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setTitle(R.string.directions)

        fragment = MapFragment.newInstance(false, true, false, false, false, false)
        fragment!!.setAdapter(loadPlaces())
        fragment!!.setMapReadyCallback(mMapCallback)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment!!)
                .addToBackStack(null)
                .commit()
    }

    private fun loadPlaces(): PlaceRecyclerAdapter {
        provider = PlaceDataProvider(this, false)
        val adapter = PlaceRecyclerAdapter(this, provider, true)
        adapter.eventListener = this
        return adapter
    }

    private fun editPlace(position: Int) {
        Reminder.edit(provider!!.getItem(position)!!.id, this@LocationsMapActivity)
    }

    private fun moveToPlace(position: Int) {
        fragment!!.moveCamera(provider!!.getItem(position)!!.position)
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

    override fun onBackPressed() {
        if (!fragment!!.onBackPressed()) {
            return
        }
        finish()
    }

    override fun onItemClicked(position: Int, view: View) {
        moveToPlace(position)
    }

    override fun onItemLongClicked(position: Int, view: View) {
        val items = arrayOf<CharSequence>(getString(R.string.edit))
        val builder = AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            dialog.dismiss()
            if (item == 0) {
                editPlace(position)
            }
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            101 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this@LocationsMapActivity, NewPlaceActivity::class.java))
            } else {
                Permissions.showInfo(this@LocationsMapActivity, Permissions.READ_CALENDAR)
            }
        }
    }
}

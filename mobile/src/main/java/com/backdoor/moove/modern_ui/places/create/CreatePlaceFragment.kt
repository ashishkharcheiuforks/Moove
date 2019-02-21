package com.backdoor.moove.modern_ui.places.create

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.R
import com.backdoor.moove.data.Place
import com.backdoor.moove.databinding.CreatePlaceFragmentBinding
import com.backdoor.moove.modern_ui.map.MapFragment
import com.backdoor.moove.utils.MapCallback
import com.backdoor.moove.utils.MapListener
import com.backdoor.moove.utils.Prefs
import com.google.android.gms.maps.model.LatLng
import org.koin.android.ext.android.inject

class CreatePlaceFragment : Fragment(), MapListener, MapCallback {

    val prefs: Prefs by inject()

    private lateinit var viewModel: CreatePlaceViewModel
    private lateinit var binding: CreatePlaceFragmentBinding

    private var mMap: MapFragment? = null
    private var mPlace: Place? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = CreatePlaceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActionBar()

        mMap = MapFragment.newInstance(isTouch = true, isPlaces = true, isSearch = true, isStyles = true, isBack = false)
        mMap?.setListener(this)
        mMap?.setCallback(this)

        childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mMap!!)
                .commit()
    }

    private fun initActionBar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CreatePlaceViewModel::class.java)
    }

    private fun showPlace(place: Place?) {
        this.mPlace = place
        place?.let {
            binding.toolbar.title = getString(R.string.edit)
            if (!viewModel.isPlaceEdited) {
                binding.placeName.setText(place.name)
                viewModel.place = place
                viewModel.isPlaceEdited = true
                showPlaceOnMap()
            }
        }
    }

    private fun addPlace() {
        if (viewModel.place.hasLatLng()) {
            var name: String = binding.placeName.text.toString().trim()
            if (name == "") {
                name = viewModel.place.name
            }
            if (name == "") {
                binding.placeLayout.error = getString(R.string.empty_field)
                binding.placeLayout.isErrorEnabled = true
                return
            }
            val latitude = viewModel.place.latitude
            val longitude = viewModel.place.longitude
            val marker = mMap?.markerStyle ?: prefs.markerStyle
            val item = (mPlace ?: Place()).apply {
                this.name = name
                this.latitude = latitude
                this.longitude = longitude
                this.markerColor = marker
            }
            viewModel.savePlace(item)
        } else {
            Toast.makeText(context, getString(R.string.no_place_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteItem() {
        mPlace?.let { viewModel.deletePlace(it) }
    }

    private fun showPlaceOnMap() {
        val map = mMap ?: return
        if (viewModel.place.hasLatLng()) {
            map.setStyle(viewModel.place.markerColor)
            map.addMarker(viewModel.place.latLng(), viewModel.place.name, true, true, -1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_place, menu)
        if (mPlace != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                addPlace()
                true
            }
            MENU_ITEM_DELETE -> {
                deleteItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady() {
        if (viewModel.isPlaceEdited) {
            showPlaceOnMap()
        }
    }

    override fun placeChanged(place: LatLng, address: String) {
        viewModel.place.apply {
            this.latitude = place.latitude
            this.longitude = place.longitude
            this.name = address
        }
        if (binding.placeName.text.toString().trim() == "") {
            binding.placeName.setText(address)
        }
    }

    override fun onBackClick() {
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
    }
}

package com.backdoor.moove.modern_ui.events_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.databinding.EventsMapFragmentBinding
import com.backdoor.moove.modern_ui.events_map.list.LocationsRecyclerAdapter
import com.backdoor.moove.modern_ui.map.MapFragment
import com.backdoor.moove.utils.ActionsListener
import com.backdoor.moove.utils.ListActions
import com.backdoor.moove.utils.MapCallback
import com.backdoor.moove.utils.MapListener
import com.google.android.gms.maps.model.LatLng

class EventsMapFragment : Fragment(), MapListener, MapCallback {

    private val viewModel: EventsMapViewModel by lazy {
        ViewModelProviders.of(this).get(EventsMapViewModel::class.java)
    }
    private lateinit var binding: EventsMapFragmentBinding
    private val adapter = LocationsRecyclerAdapter()
    private var mMap: MapFragment? = null
    private var isDataShowed: Boolean = false

    private val mBackHandler: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            mMap?.onBackPressed() == false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = EventsMapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(mBackHandler)
        initList()

        mMap = MapFragment.newInstance(isTouch = false, isPlaces = false, isSearch = false, isStyles = false, isBack = true)
        mMap?.setListener(this)
        mMap?.setCallback(this)

        childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mMap!!)
                .commit()
    }

    private fun initList() {
        adapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                if (t != null) {
                    when (actions) {
                        ListActions.OPEN -> showEvent(t)
                        else -> {}
                    }
                }
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = adapter
    }

    private fun showEvent(reminder: Reminder) {
        if (reminder.hasPlace()) {
            mMap?.moveCamera(reminder.latLng(), 0, 0, 0, 0)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.reminders.observe(this, Observer {
            if (it != null) {
                showReminders(it)
            }
        })
    }

    private fun showReminders(list: List<Reminder>) {
        adapter.data = list.toMutableList()
        if (list.isEmpty()) {
            binding.emptyItem.visibility = View.VISIBLE
        } else {
            binding.emptyItem.visibility = View.GONE
        }

        val map = mMap
        if (isDataShowed || map == null) {
            return
        }
        var mapReady = false
        for (reminder in list) {
            if (reminder.hasPlace()) {
                mapReady = map.addMarker(reminder.latLng(), reminder.summary, false, reminder.markerColor, false, reminder.radius)
                if (!mapReady) {
                    break
                }
            }
        }
        isDataShowed = mapReady
    }

    override fun onMapReady() {
        val data = viewModel.reminders.value
        if (data != null) showReminders(data)
    }

    override fun placeChanged(place: LatLng, address: String) {

    }

    override fun onBackClick() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBackHandler.isEnabled = false
    }
}

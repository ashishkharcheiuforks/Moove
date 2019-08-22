package com.backdoor.moove.modern_ui.places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.data.Place
import com.backdoor.moove.databinding.PlacesFragmentBinding
import com.backdoor.moove.modern_ui.places.list.PlacesAdapter
import com.backdoor.moove.utils.ActionsListener
import com.backdoor.moove.utils.Dialogues
import com.backdoor.moove.utils.ListActions

class PlacesFragment : Fragment() {

    private lateinit var binding: PlacesFragmentBinding
    private val viewModel: PlacesViewModel by lazy {
        ViewModelProviders.of(this).get(PlacesViewModel::class.java)
    }
    private val adapter = PlacesAdapter(true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = PlacesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener {
            findNavController().navigate(PlacesFragmentDirections.actionPlacesFragmentToCreatePlaceFragment(""))
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        initList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }

    private fun initList() {
        adapter.actionsListener = object : ActionsListener<Place> {
            override fun onAction(view: View, position: Int, t: Place?, actions: ListActions) {
                if (t != null) {
                    when (actions) {
                        ListActions.OPEN -> {
                            findNavController().navigate(PlacesFragmentDirections.actionPlacesFragmentToCreatePlaceFragment(t.uuId))
                        }
                        ListActions.MORE -> showMore(view, t)
                        else -> {}
                    }
                }
            }
        }
        binding.placesList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.placesList.adapter = adapter
        refreshEmpty(0)
    }

    private fun showMore(view: View, t: Place) {
        Dialogues.showPopup(view, { i ->
            when (i) {
                0 -> {
                    findNavController().navigate(PlacesFragmentDirections.actionPlacesFragmentToCreatePlaceFragment(t.uuId))
                }
                1 -> {
                    viewModel.deletePlace(t)
                }
            }
        }, getString(R.string.edit), getString(R.string.delete))
    }

    private fun initViewModel() {
        viewModel.places.observe(this, Observer {
            if (it != null) {
                adapter.setPlaces(it)
                refreshEmpty(it.size)
            }
        })
    }

    private fun refreshEmpty(count: Int) {
        if (count > 0) {
            binding.emptyView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.VISIBLE
        }
    }
}

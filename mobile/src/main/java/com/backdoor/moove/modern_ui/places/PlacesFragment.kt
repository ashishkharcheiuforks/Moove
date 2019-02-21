package com.backdoor.moove.modern_ui.places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.data.Place
import com.backdoor.moove.databinding.PlacesFragmentBinding
import com.backdoor.moove.modern_ui.places.list.PlacesAdapter
import com.backdoor.moove.utils.ActionsListener
import com.backdoor.moove.utils.ListActions

class PlacesFragment : Fragment() {

    private lateinit var binding: PlacesFragmentBinding
    private lateinit var viewModel: PlacesViewModel
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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        initList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }

    private fun initList() {
        adapter.actionsListener = object : ActionsListener<Place> {
            override fun onAction(view: View, position: Int, t: Place?, actions: ListActions) {

            }
        }
        binding.placesList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.placesList.adapter = adapter
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)
    }
}

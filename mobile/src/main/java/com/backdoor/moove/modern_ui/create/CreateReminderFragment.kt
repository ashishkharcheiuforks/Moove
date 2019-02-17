package com.backdoor.moove.modern_ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.backdoor.moove.databinding.CreateReminderFragmentBinding

class CreateReminderFragment : Fragment() {

    private lateinit var binding: CreateReminderFragmentBinding
    private lateinit var viewModel: CreateReminderViewModel
    private var mId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mId = CreateReminderFragmentArgs.fromBundle(it).argId
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = CreateReminderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, CreateReminderViewModel.Factory(mId)).get(CreateReminderViewModel::class.java)
    }
}

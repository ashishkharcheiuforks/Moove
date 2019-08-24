package com.backdoor.moove.modern_ui.donate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.backdoor.moove.databinding.DonateFragmentBinding

class DonateFragment : Fragment() {

    private lateinit var binding: DonateFragmentBinding
    private val viewModel: DonateViewModel by lazy {
        ViewModelProviders.of(this).get(DonateViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DonateFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buyButton.setOnClickListener { buyClick(DonateViewModel.SKU_1) }
        binding.buyButton1.setOnClickListener { buyClick(DonateViewModel.SKU_2) }
        binding.buyButton2.setOnClickListener { buyClick(DonateViewModel.SKU_3) }
        binding.buyButton3.setOnClickListener { buyClick(DonateViewModel.SKU_4) }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun buyClick(sku: String) {
        viewModel.buy(activity!!, sku)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.isLoading.observe(this, Observer {
            if (it != null) {
                showLoading(it)
            }
        })
        viewModel.skuDetails.observe(this, Observer {
            if (it != null) {
                updateDetails(it)
            }
        })
        viewModel.purchases.observe(this, Observer {
            if (it != null) {
                updateButtons(it)
            }
        })
        lifecycle.addObserver(viewModel)
    }

    private fun updateButtons(list: List<Purchase>) {
        for (purchase in list) {
            when (purchase.sku) {
                DonateViewModel.SKU_1 -> {
                    binding.buyButton.isEnabled = false
                }
                DonateViewModel.SKU_2 -> {
                    binding.buyButton1.isEnabled = false
                }
                DonateViewModel.SKU_3 -> {
                    binding.buyButton2.isEnabled = false
                }
                DonateViewModel.SKU_4 -> {
                    binding.buyButton3.isEnabled = false
                }
            }
        }
    }

    private fun updateDetails(list: List<SkuDetails>) {
        for (skuDetails in list) {
            when (skuDetails.sku) {
                DonateViewModel.SKU_1 -> {
                    binding.buyButton.text = skuDetails.price
                }
                DonateViewModel.SKU_2 -> {
                    binding.buyButton1.text = skuDetails.price
                }
                DonateViewModel.SKU_3 -> {
                    binding.buyButton2.text = skuDetails.price
                }
                DonateViewModel.SKU_4 -> {
                    binding.buyButton3.text = skuDetails.price
                }
            }
        }
    }

    private fun showLoading(b: Boolean) {
        binding.waitProgress.visibility = if (b) View.VISIBLE else View.GONE
        binding.buyButton.isEnabled = !b
        binding.buyButton1.isEnabled = !b
        binding.buyButton2.isEnabled = !b
        binding.buyButton3.isEnabled = !b
        viewModel.purchases.value?.let {
            updateButtons(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(viewModel)
    }
}

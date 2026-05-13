package com.mtoanng.datastream.ui.pillars

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.mtoanng.datastream.R
import com.mtoanng.datastream.databinding.FragmentPillarsBinding
import com.mtoanng.datastream.ui.common.BaseFragment
import com.mtoanng.datastream.util.snack

class PillarsFragment : BaseFragment() {

    private var _binding: FragmentPillarsBinding? = null
    private val binding get() = _binding!!

    /**
     * Single shared VM scoped to *this* fragment so the 4 child Pillar*Fragment
     * instances (created by the FragmentStateAdapter) can all observe the same data.
     */
    private val viewModel: PillarsViewModel by viewModels { viewModelFactory }

    private val titles by lazy {
        listOf(
            getString(R.string.pillar_1_short),
            getString(R.string.pillar_2_short),
            getString(R.string.pillar_3_short),
            getString(R.string.pillar_4_short),
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPillarsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = PillarsPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        arguments?.getInt("pillarTabIndex", 0)?.let { binding.viewPager.setCurrentItem(it, false) }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) binding.root.snack(msg, isError = true)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.progress.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.refreshAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

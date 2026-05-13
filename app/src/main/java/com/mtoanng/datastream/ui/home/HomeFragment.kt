package com.mtoanng.datastream.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mtoanng.datastream.R
import com.mtoanng.datastream.databinding.FragmentHomeBinding
import com.mtoanng.datastream.ui.common.BaseFragment
import com.mtoanng.datastream.util.EnergySecurityHelper
import com.mtoanng.datastream.util.Formatters
import com.mtoanng.datastream.util.snack

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        // Pillar mini-cards: tap navigates to PillarsFragment with selected tab.
        binding.cardPillar1.setOnClickListener { goToPillar(0) }
        binding.cardPillar2.setOnClickListener { goToPillar(1) }
        binding.cardPillar3.setOnClickListener { goToPillar(2) }
        binding.cardPillar4.setOnClickListener { goToPillar(3) }

        viewModel.score.observe(viewLifecycleOwner) { score ->
            score ?: return@observe
            binding.scoreGauge.setScore(score.overallScore, score.status)
            binding.tvOverallStatus.text = getString(EnergySecurityHelper.statusStringRes(score.status))
            binding.tvOverallStatus.setTextColor(EnergySecurityHelper.statusColor(requireContext(), score.status))
            binding.tvUpdated.text = getString(R.string.label_updated_at, Formatters.isoToLocal(score.computedAt))

            binding.tvP1Score.text = Formatters.score(score.pillar1Score)
            binding.tvP2Score.text = Formatters.score(score.pillar2Score)
            binding.tvP3Score.text = Formatters.score(score.pillar3Score)
            binding.tvP4Score.text = Formatters.score(score.pillar4Score)

            binding.badgeP1.setStatus(getString(EnergySecurityHelper.statusStringRes(EnergySecurityHelper.statusFromScore(score.pillar1Score))), EnergySecurityHelper.statusFromScore(score.pillar1Score))
            binding.badgeP2.setStatus(getString(EnergySecurityHelper.statusStringRes(EnergySecurityHelper.statusFromScore(score.pillar2Score))), EnergySecurityHelper.statusFromScore(score.pillar2Score))
            binding.badgeP3.setStatus(getString(EnergySecurityHelper.statusStringRes(EnergySecurityHelper.statusFromScore(score.pillar3Score))), EnergySecurityHelper.statusFromScore(score.pillar3Score))
            binding.badgeP4.setStatus(getString(EnergySecurityHelper.statusStringRes(EnergySecurityHelper.statusFromScore(score.pillar4Score))), EnergySecurityHelper.statusFromScore(score.pillar4Score))
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) binding.root.snack(msg, isError = true)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopAutoRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToPillar(index: Int) {
        val args = Bundle().apply { putInt("pillarTabIndex", index) }
        findNavController().navigate(R.id.pillarsFragment, args)
    }
}

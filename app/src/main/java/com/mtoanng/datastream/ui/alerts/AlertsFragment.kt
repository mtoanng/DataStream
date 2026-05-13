package com.mtoanng.datastream.ui.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.mtoanng.datastream.R
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.databinding.BottomSheetAlertBinding
import com.mtoanng.datastream.databinding.FragmentAlertsBinding
import com.mtoanng.datastream.ui.common.BaseFragment
import com.mtoanng.datastream.util.EnergySecurityHelper
import com.mtoanng.datastream.util.Formatters
import com.mtoanng.datastream.util.snack

class AlertsFragment : BaseFragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlertsViewModel by viewModels { viewModelFactory }
    private val adapter = AlertsAdapter { showDetails(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipAll
            val severity = when (checkedId) {
                R.id.chipCritical -> "CRITICAL"
                R.id.chipWarning  -> "WARNING"
                R.id.chipInfo     -> "INFO"
                else              -> "ALL"
            }
            viewModel.setFilter(severity)
        }

        viewModel.items.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) binding.root.snack(it, isError = true)
        }

        viewModel.refresh()
    }

    private fun showDetails(alert: AlertDto) {
        val bs = BottomSheetDialog(requireContext())
        val b = BottomSheetAlertBinding.inflate(layoutInflater)
        val ctx = requireContext()
        b.tvTitle.text = alert.ruleName ?: alert.metricType
        b.tvSeverity.text = alert.severity
        b.tvSeverity.setTextColor(EnergySecurityHelper.severityColor(ctx, alert.severity))
        b.tvMessage.text = alert.message ?: "--"
        b.tvMetric.text = alert.metricType
        b.tvOperator.text = alert.operator ?: "--"
        b.tvThreshold.text = Formatters.number(alert.threshold)
        b.tvTriggered.text = Formatters.number(alert.triggeredPrice)
        b.tvRegion.text = alert.region ?: "--"
        b.tvFuel.text = alert.fuelType ?: "--"
        b.tvLocation.text = alert.location ?: "--"
        b.tvEventTime.text = Formatters.isoToLocal(alert.eventTimestamp)
        b.tvAlertTime.text = Formatters.isoToLocal(alert.alertTimestamp)
        b.tvAge.text = Formatters.ago(alert.ageSeconds)
        bs.setContentView(b.root)
        bs.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.mtoanng.datastream.ui.pillars

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.mtoanng.datastream.R
import com.mtoanng.datastream.databinding.FragmentPillar2Binding
import com.mtoanng.datastream.ui.common.BaseFragment
import com.mtoanng.datastream.util.EnergySecurityHelper
import com.mtoanng.datastream.util.Formatters

class Pillar2Fragment : BaseFragment() {

    private var _binding: FragmentPillar2Binding? = null
    private val binding get() = _binding!!

    private val parentVm: PillarsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory },
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPillar2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentVm.pillar2.observe(viewLifecycleOwner) { rows ->
            val first = rows?.firstOrNull() ?: return@observe
            binding.tvScore.text = Formatters.score(first.pillar2Score)
            binding.tvScore.setTextColor(EnergySecurityHelper.statusColor(requireContext(), first.status))
            binding.badge.setStatus(getString(EnergySecurityHelper.statusStringRes(first.status)), first.status)
            binding.tvFuel.text = first.fuelType
            binding.tvSigma.text = Formatters.score(first.sigma30d)
            binding.tvGap.text = Formatters.percentRaw(first.priceGapPct)
            binding.tvBeta.text = Formatters.score(first.betaCrude)
            binding.tvAffordability.text = Formatters.score(first.affordabilityIdx)
            binding.tvUpdated.text = getString(R.string.label_updated_at, Formatters.isoToLocal(first.computedAt))

            val capped = rows.take(6)
            val entries = capped.mapIndexed { idx, dto -> BarEntry(idx.toFloat(), dto.pillar2Score.toFloat()) }
            val labels = capped.map { it.fuelType }
            val set = BarDataSet(entries, getString(R.string.pillar_2_short)).apply {
                setColors(intArrayOf(R.color.pillar_2), requireContext())
                valueTextSize = 9f
            }
            binding.chart.data = BarData(set)
            binding.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.chart.xAxis.granularity = 1f
            binding.chart.xAxis.labelRotationAngle = -30f
            binding.chart.axisRight.isEnabled = false
            binding.chart.description.isEnabled = false
            binding.chart.invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

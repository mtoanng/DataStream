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
import com.mtoanng.datastream.databinding.FragmentPillar4Binding
import com.mtoanng.datastream.ui.common.BaseFragment
import com.mtoanng.datastream.util.EnergySecurityHelper
import com.mtoanng.datastream.util.Formatters

class Pillar4Fragment : BaseFragment() {

    private var _binding: FragmentPillar4Binding? = null
    private val binding get() = _binding!!

    private val parentVm: PillarsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory },
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPillar4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentVm.pillar4.observe(viewLifecycleOwner) { rows ->
            val first = rows?.firstOrNull() ?: return@observe
            binding.tvScore.text = Formatters.score(first.pillar4Score)
            binding.tvScore.setTextColor(EnergySecurityHelper.statusColor(requireContext(), first.status))
            binding.badge.setStatus(getString(EnergySecurityHelper.statusStringRes(first.status)), first.status)
            binding.tvRegion.text = first.regionCode
            binding.tvRenewable.text = Formatters.percentRaw(first.renewablePct)
            binding.tvCo2.text = getString(R.string.unit_kg_per_mwh, Formatters.number(first.co2Intensity))
            binding.tvCurtail.text = Formatters.percentRaw(first.curtailmentRate)
            binding.tvNetzero.text = Formatters.percentRaw(first.netzeroProgress)
            binding.tvUpdated.text = getString(R.string.label_updated_at, Formatters.isoToLocal(first.computedAt))

            val capped = rows.take(6)
            val entries = capped.mapIndexed { idx, dto -> BarEntry(idx.toFloat(), dto.pillar4Score.toFloat()) }
            val labels = capped.map { it.regionCode.removePrefix("VN_") }
            val set = BarDataSet(entries, getString(R.string.pillar_4_short)).apply {
                setColors(intArrayOf(R.color.pillar_4), requireContext())
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

package com.mtoanng.datastream.ui.alerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mtoanng.datastream.R
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.databinding.ItemAlertBinding
import com.mtoanng.datastream.util.EnergySecurityHelper
import com.mtoanng.datastream.util.Formatters
import com.mtoanng.datastream.util.dpToPx

class AlertsAdapter(private val onClick: (AlertDto) -> Unit) :
    ListAdapter<AlertDto, AlertsAdapter.VH>(DIFF) {

    inner class VH(val b: ItemAlertBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val alert = getItem(position)
        val b = holder.b
        val ctx = b.root.context

        val color = EnergySecurityHelper.severityColor(ctx, alert.severity)
        b.severityStripe.setBackgroundColor(color)
        b.tvSeverity.text = alert.severity
        b.tvSeverity.setTextColor(color)
        b.tvTitle.text = alert.ruleName ?: alert.metricType
        b.tvMessage.text = alert.message ?: ""
        b.tvLocation.text = listOfNotNull(alert.region, alert.location, alert.fuelType).joinToString(" • ").ifEmpty { "—" }
        val triggered = Formatters.number(alert.triggeredPrice)
        val threshold = Formatters.number(alert.threshold)
        b.tvValue.text = ctx.getString(R.string.alert_value_template, triggered, alert.operator ?: "?", threshold)
        b.tvAge.text = Formatters.ago(alert.ageSeconds)

        b.root.setPadding(12.dpToPx())
        b.root.setOnClickListener { onClick(alert) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AlertDto>() {
            override fun areItemsTheSame(oldItem: AlertDto, newItem: AlertDto) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: AlertDto, newItem: AlertDto) = oldItem == newItem
        }
    }
}

package com.mtoanng.datastream.ui.recommendations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mtoanng.datastream.R
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.databinding.ItemRecommendationBinding
import com.mtoanng.datastream.util.EnergySecurityHelper
import com.mtoanng.datastream.util.Formatters

class RecommendationsAdapter(private val onAck: (RecommendationDto) -> Unit) :
    ListAdapter<RecommendationDto, RecommendationsAdapter.VH>(DIFF) {

    inner class VH(val b: ItemRecommendationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.b
        val ctx = b.root.context

        b.tvTitle.text = item.title
        b.tvMessage.text = item.message ?: "--"
        b.tvPillar.text = ctx.getString(R.string.label_pillar_n, item.pillar)
        b.tvPillar.setBackgroundColor(EnergySecurityHelper.pillarColor(ctx, item.pillar))
        b.tvAge.text = Formatters.ago(item.ageSeconds)
        b.tvAction.text = item.actionType
        b.badgeSeverity.setStatus(item.severity, item.severity)
        b.btnAck.setOnClickListener { onAck(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecommendationDto>() {
            override fun areItemsTheSame(oldItem: RecommendationDto, newItem: RecommendationDto) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: RecommendationDto, newItem: RecommendationDto) = oldItem == newItem
        }
    }
}

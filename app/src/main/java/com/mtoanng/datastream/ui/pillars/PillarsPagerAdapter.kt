package com.mtoanng.datastream.ui.pillars

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PillarsPagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> Pillar1Fragment()
        1 -> Pillar2Fragment()
        2 -> Pillar3Fragment()
        3 -> Pillar4Fragment()
        else -> error("Unknown position: $position")
    }
}

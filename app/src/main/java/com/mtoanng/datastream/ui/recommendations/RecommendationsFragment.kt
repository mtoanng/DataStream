package com.mtoanng.datastream.ui.recommendations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.mtoanng.datastream.R
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.databinding.FragmentRecommendationsBinding
import com.mtoanng.datastream.ui.common.BaseFragment
import com.mtoanng.datastream.util.snack

class RecommendationsFragment : BaseFragment() {

    private var _binding: FragmentRecommendationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecommendationsViewModel by viewModels { viewModelFactory }
    private val adapter = RecommendationsAdapter { showAckDialog(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecommendationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val pillar = when (checkedIds.firstOrNull()) {
                R.id.chipP1 -> 1
                R.id.chipP2 -> 2
                R.id.chipP3 -> 3
                R.id.chipP4 -> 4
                else -> 0
            }
            viewModel.setFilter(pillar)
        }

        viewModel.items.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) binding.root.snack(it, isError = true)
        }
        viewModel.ackResult.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) binding.root.snack(it)
        }

        viewModel.refresh()
    }

    private fun showAckDialog(rec: RecommendationDto) {
        val input = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.dialog_ack_note_hint)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_ack_title, rec.id))
            .setMessage(rec.title)
            .setView(input)
            .setPositiveButton(R.string.action_acknowledge) { _, _ ->
                val note = input.text?.toString()?.takeIf { it.isNotBlank() }
                viewModel.acknowledge(rec.id, note)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.mtoanng.datastream.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mtoanng.datastream.DataStreamApp
import com.mtoanng.datastream.databinding.FragmentChatBinding
import com.mtoanng.datastream.ui.alerts.AlertsViewModel
import com.mtoanng.datastream.ui.common.ViewModelFactory
import com.mtoanng.datastream.ui.home.HomeViewModel
import com.mtoanng.datastream.ui.recommendations.RecommendationsViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val factory by lazy { ViewModelFactory(requireActivity().application as DataStreamApp) }

    private val viewModel: ChatViewModel by viewModels { factory }

    // activityViewModels để tái dùng data đã load, không gọi API lại
    private val homeViewModel: HomeViewModel by activityViewModels { factory }
    private val alertsViewModel: AlertsViewModel by activityViewModels { factory }
    private val recommendationsViewModel: RecommendationsViewModel by activityViewModels { factory }

    private lateinit var adapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupInput()
        observeMessages()
        feedContextToChat()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        binding.rvMessages.apply {
            this.adapter = this@ChatFragment.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        }
    }

    private fun setupInput() {
        binding.etMessage.doAfterTextChanged { text ->
            binding.btnSend.isEnabled = !text.isNullOrBlank()
        }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
        }
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.btnClear.setOnClickListener { viewModel.clearHistory() }
    }

    private fun observeMessages() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }
        viewModel.isTyping.observe(viewLifecycleOwner) { isTyping ->
            binding.typingIndicator.isVisible = isTyping
            binding.btnSend.isEnabled = !isTyping && binding.etMessage.text?.isNotBlank() == true
        }
    }

    private fun feedContextToChat() {
        homeViewModel.score.observe(viewLifecycleOwner) { score ->
            viewModel.updateContext(securityScore = score)
        }
        alertsViewModel.items.observe(viewLifecycleOwner) { alerts ->
            viewModel.updateContext(alerts = alerts)
        }
        recommendationsViewModel.items.observe(viewLifecycleOwner) { recs ->
            viewModel.updateContext(recommendations = recs)
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text?.toString()?.trim() ?: return
        if (text.isBlank()) return
        binding.etMessage.setText("")
        viewModel.sendMessage(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.mtoanng.datastream.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.mtoanng.datastream.BuildConfig
import com.mtoanng.datastream.R
import com.mtoanng.datastream.databinding.FragmentSettingsBinding
import com.mtoanng.datastream.ui.common.BaseFragment
import com.mtoanng.datastream.ui.login.LoginActivity
import com.mtoanng.datastream.util.snack

class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etBaseUrl.setText(viewModel.currentBaseUrl())
        binding.btnSaveServer.setOnClickListener {
            val url = binding.etBaseUrl.text?.toString().orEmpty()
            viewModel.saveBaseUrl(url)
            binding.etBaseUrl.setText(viewModel.currentBaseUrl())
            binding.root.snack(getString(R.string.toast_server_saved, viewModel.currentBaseUrl()))
        }
        binding.btnTestConnection.setOnClickListener { viewModel.testConnection() }
        binding.btnLogout.setOnClickListener {
            // Logout Google trước để lần sau phải chọn lại tài khoản
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(requireContext(), gso).signOut().addOnCompleteListener {
                viewModel.logout()
                startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            }
        }
        binding.tvAppVersion.text = getString(R.string.label_app_version, BuildConfig.VERSION_NAME)
        binding.tvBackendVersion.text = getString(R.string.label_backend_version, "v1.0.0")
        binding.tvGithubLink.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mtoanng/DataStream")))
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUsername.text = user.username
                binding.tvFullName.text = user.fullName ?: "--"
                binding.tvEmail.text = user.email ?: "--"
                binding.tvRole.text = user.role
            }
        }
        viewModel.testResult.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) binding.root.snack(it)
        }

        viewModel.refreshUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

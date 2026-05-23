package com.app.shoppy

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.app.shoppy.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.app.shoppy.utils.UIUtils
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.app.shoppy.ui.viewmodel.AuthViewModel
import com.app.shoppy.ui.viewmodel.AuthState
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                Glide.with(this).load(it).placeholder(UIUtils.getShimmerDrawable()).into(binding.imgAvatar)
                requireActivity().getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
                    .edit().putString("user_avatar", it.toString()).apply()
                    
                val email = requireActivity().getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE).getString("user_email", "")
                if (!email.isNullOrEmpty()) {
                    val updatedUser = com.app.shoppy.data.remote.model.UserDto(0, "", email, it.toString(), false, 0)
                    authViewModel.updateProfile(email, updatedUser)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateProfileUI()
        observeViewModel()

        binding.imgAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSignIn.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnEditProfile.setOnClickListener {
            val sharedPrefs = requireActivity().getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
            val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)

            val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfName)
            val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfEmail)
            val etPhone = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfPhone)
            val etCountry = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfCountry)
            val etCity = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfCity)
            val etDistrict = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfDistrict)
            val etStreet = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfStreet)
            val btnSave = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveProfile)

            // Pre-fill
            etName.setText(sharedPrefs.getString("user_name", ""))
            etEmail.setText(sharedPrefs.getString("user_email", ""))
            etPhone.setText(sharedPrefs.getString("user_phone", ""))
            etCountry.setText(sharedPrefs.getString("user_country", ""))
            etCity.setText(sharedPrefs.getString("user_city", ""))
            etDistrict.setText(sharedPrefs.getString("user_district", ""))
            etStreet.setText(sharedPrefs.getString("user_street", ""))

            btnSave.setOnClickListener {
                val newName = etName.text.toString()
                val newEmail = etEmail.text.toString()

                sharedPrefs.edit().apply {
                    putString("user_name", newName)
                    putString("user_email", newEmail)
                    putString("user_phone", etPhone.text.toString())
                    putString("user_country", etCountry.text.toString())
                    putString("user_city", etCity.text.toString())
                    putString("user_district", etDistrict.text.toString())
                    putString("user_street", etStreet.text.toString())
                    apply()
                }

                binding.tvUserName.text = newName.ifBlank { "Guest User" }
                binding.tvUserEmail.text = newEmail.ifBlank { "guest@stylenest.co.ke" }
                
                val email = sharedPrefs.getString("user_email", "")
                if (!email.isNullOrEmpty()) {
                    val updatedUser = com.app.shoppy.data.remote.model.UserDto(0, newName, email, null, false, 0)
                    authViewModel.updateProfile(email, updatedUser)
                }
                
                bottomSheetDialog.dismiss()
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }

            bottomSheetDialog.setContentView(dialogView)
            bottomSheetDialog.show()
        }

        // Setup initial switch state based on current night mode
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val isSystemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = currentNightMode == AppCompatDelegate.MODE_NIGHT_YES ||
            (currentNightMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED && isSystemDark)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.btnOrderHistory.setOnClickListener {
            val sharedPrefs = requireActivity().getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
            if (sharedPrefs.getBoolean("is_logged_in", false)) {
                val intent = Intent(requireContext(), OrderHistoryActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Please sign in to view order history", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }

        binding.btnHelpCenter.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Help Center...", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            val sharedPrefs = requireActivity().getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()

            // Also sign out from Google Client
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso)
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
                updateProfileUI()
            }
        }

        binding.switch2FA.setOnCheckedChangeListener { _, isChecked ->
            val sharedPrefs = requireActivity().getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
            val email = sharedPrefs.getString("user_email", "")
            if (!email.isNullOrEmpty()) {
                sharedPrefs.edit().putBoolean("two_factor_enabled", isChecked).apply()
                val updatedUser = com.app.shoppy.data.remote.model.UserDto(0, "", email, null, isChecked, 0)
                authViewModel.updateProfile(email, updatedUser)
                Toast.makeText(requireContext(), if (isChecked) "2FA Enabled" else "2FA Disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateProfileUI()
    }

    private fun updateProfileUI() {
        if (_binding == null) return
        val sharedPrefs = requireActivity().getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)

        val savedName = sharedPrefs.getString("user_name", "") ?: ""
        val savedEmail = sharedPrefs.getString("user_email", "") ?: ""
        binding.tvUserName.text = savedName.ifBlank { "Guest User" }
        binding.tvUserEmail.text = savedEmail.ifBlank { "guest@stylenest.co.ke" }

        val savedAvatar = sharedPrefs.getString("user_avatar", "")
        if (!savedAvatar.isNullOrBlank()) {
            Glide.with(this)
                .load(savedAvatar)
                .placeholder(UIUtils.getShimmerDrawable())
                .into(binding.imgAvatar)
        } else {
            binding.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        if (isLoggedIn) {
            binding.layoutProfileDetails.visibility = View.VISIBLE
            binding.btnSignIn.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE

            // Fetch profile for latest loyalty points
            if (savedEmail.isNotEmpty()) {
                authViewModel.fetchUserProfile(savedEmail)
            }

            val biometricManager = androidx.biometric.BiometricManager.from(requireContext())
            if (biometricManager.canAuthenticate(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                binding.layout2fa.visibility = View.VISIBLE

                // Read current state from shared prefs
                if (savedEmail.isNotEmpty()) {
                    binding.switch2FA.isChecked = sharedPrefs.getBoolean("two_factor_enabled", false)
                }
            } else {
                binding.layout2fa.visibility = View.GONE
            }
        } else {
            binding.layoutProfileDetails.visibility = View.GONE
            binding.btnSignIn.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
            binding.layout2fa.visibility = View.GONE
            binding.cardLoyaltyPoints.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                if (state is AuthState.Success) {
                    binding.cardLoyaltyPoints.visibility = View.VISIBLE
                    binding.tvLoyaltyPoints.text = "${state.user.loyaltyPoints} Points"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

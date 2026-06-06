package com.app.stylenest

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.app.stylenest.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.app.stylenest.utils.UIUtils
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.app.stylenest.data.DatabaseHelper
import androidx.biometric.BiometricManager

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                Glide.with(this).load(it).placeholder(UIUtils.getShimmerDrawable()).into(binding.imgAvatar)
                requireActivity().getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
                    .edit().putString("user_avatar", it.toString()).apply()
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

        binding.imgAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        
        binding.btnSignIn.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnEditProfile.setOnClickListener {
            val sharedPrefs = requireActivity().getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
            val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
            
            val etName = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfName)
            val etEmail = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfEmail)
            val etPhone = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfPhone)
            val etCountry = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfCountry)
            val etCity = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfCity)
            val etDistrict = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfDistrict)
            val etStreet = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etProfStreet)
            val btnSave = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveProfile)
            
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
                
                bottomSheetDialog.dismiss()
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            
            bottomSheetDialog.setContentView(view)
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
            val sharedPrefs = requireActivity().getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
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
            val sharedPrefs = requireActivity().getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
            // Capture the email BEFORE clearing prefs, so we can scope the DB cleanup
            val userEmail = sharedPrefs.getString("user_email", "") ?: ""

            // Clear the user's cart & wishlist from DB (guest rows are left untouched)
            if (userEmail.isNotBlank()) {
                val repository = com.app.stylenest.data.StyleNestRepository(requireContext())
                repository.clearUserData(userEmail)
            }

            sharedPrefs.edit().clear().apply()
            
            // Also sign out from Google Client
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso)
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
                updateProfileUI()
                (activity as? MainActivity)?.updateBadges()
            }
        }

        binding.switch2FA.setOnCheckedChangeListener { _, isChecked ->
            val sharedPrefs = requireActivity().getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
            val email = sharedPrefs.getString("user_email", "")
            if (!email.isNullOrEmpty()) {
                val db = DatabaseHelper(requireContext()).writableDatabase
                db.execSQL("UPDATE users SET two_factor_enabled=? WHERE email=?", arrayOf(if (isChecked) 1 else 0, email))
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
        val sharedPrefs = requireActivity().getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
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
            
            val biometricManager = BiometricManager.from(requireContext())
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
                binding.layout2fa.visibility = View.VISIBLE
                
                // Read current state from DB
                if (savedEmail.isNotEmpty()) {
                    val db = DatabaseHelper(requireContext()).readableDatabase
                    val cursor = db.rawQuery("SELECT two_factor_enabled FROM users WHERE email=?", arrayOf(savedEmail))
                    if (cursor.moveToFirst()) {
                        binding.switch2FA.isChecked = cursor.getInt(0) == 1
                    }
                    cursor.close()
                }
            } else {
                binding.layout2fa.visibility = View.GONE
            }

        } else {
            binding.layoutProfileDetails.visibility = View.GONE
            binding.btnSignIn.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
            binding.layout2fa.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

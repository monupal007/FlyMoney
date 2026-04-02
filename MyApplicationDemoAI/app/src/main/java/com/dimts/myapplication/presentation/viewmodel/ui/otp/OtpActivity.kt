package com.dimts.myapplication.presentation.viewmodel.ui.otp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dimts.myapplication.R
import com.dimts.myapplication.core.utils.Resource
import com.dimts.myapplication.databinding.ActivityOtpBinding
import com.dimts.myapplication.presentation.viewmodel.OtpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private val viewModel: OtpViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null
    private var mobileNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get mobile number from intent
        mobileNumber = intent.getStringExtra(EXTRA_MOBILE_NUMBER) ?: ""

        setupUI()
        setupObservers()
        setupClickListeners()
        setupOtpInputListeners()
    }

    private fun setupUI() {
        // Setup status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_blue)

        // Display mobile number
        binding.tvMobileNumber.text = if (mobileNumber.isNotEmpty()) {
            "+91 $mobileNumber"
        } else {
            "Mobile Number"
        }

        // Start countdown timer for resend OTP
        startResendTimer()
    }

    private fun setupObservers() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                // Update OTP fields if they don't match current state
                updateOtpFields(uiState.otp)

                // Handle OTP error
                if (uiState.otpError != null) {
                    binding.tvError.text = uiState.otpError
                    binding.tvError.visibility = View.VISIBLE
                } else {
                    binding.tvError.visibility = View.GONE
                }
            }
        }

        // Observe OTP verification state
        lifecycleScope.launch {
            viewModel.otpVerificationState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnVerify.isEnabled = false
                        binding.tvError.visibility = View.GONE
                    }

                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnVerify.isEnabled = true

                        val response = resource.data
                        if (response?.success == true) {
                            Toast.makeText(
                                this@OtpActivity,
                                getString(R.string.otp_verified),
                                Toast.LENGTH_LONG
                            ).show()

                            // Navigate to main activity or next screen
                            navigateToMain()
                        } else {
                            binding.tvError.text = response?.message ?: getString(R.string.otp_verification_failed)
                            binding.tvError.visibility = View.VISIBLE
                        }
                        viewModel.clearOtpVerificationState()
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnVerify.isEnabled = true
                        binding.tvError.text = resource.message ?: getString(R.string.otp_verification_failed)
                        binding.tvError.visibility = View.VISIBLE
                        viewModel.clearOtpVerificationState()
                    }

                    null -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnVerify.isEnabled = true
                    }
                }
            }
        }

        // Observe resend OTP state
        lifecycleScope.launch {
            viewModel.resendOtpState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvResendOtp.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResendOtp.isEnabled = true

                        val response = resource.data
                        if (response?.success == true) {
                            Toast.makeText(
                                this@OtpActivity,
                                getString(R.string.otp_sent),
                                Toast.LENGTH_LONG
                            ).show()

                            // Clear OTP fields and restart timer
                            clearOtpFields()
                            startResendTimer()
                        } else {
                            Toast.makeText(
                                this@OtpActivity,
                                response?.message ?: getString(R.string.otp_resend_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        viewModel.clearResendOtpState()
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResendOtp.isEnabled = true
                        Toast.makeText(
                            this@OtpActivity,
                            resource.message ?: getString(R.string.otp_resend_failed),
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.clearResendOtpState()
                    }

                    null -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResendOtp.isEnabled = true
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnVerify.setOnClickListener {
            val otp = getOtpFromFields()
            if (otp.length == 6) {
                viewModel.verifyOtp(mobileNumber, otp)
            } else {
                binding.tvError.text = getString(R.string.otp_required)
                binding.tvError.visibility = View.VISIBLE
            }
        }

        binding.tvResendOtp.setOnClickListener {
            if (mobileNumber.isNotEmpty()) {
                viewModel.resendOtp(mobileNumber)
            }
        }
    }

    private fun setupOtpInputListeners() {
        val otpFields = arrayOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to next field
                        if (index < otpFields.size - 1) {
                            otpFields[index + 1].requestFocus()
                        }
                    } else if (s?.length == 0 && before == 1) {
                        // Move to previous field on backspace
                        if (index > 0) {
                            otpFields[index - 1].requestFocus()
                        }
                    }

                    // Update ViewModel
                    val currentOtp = getOtpFromFields()
                    viewModel.updateOtp(currentOtp)
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Handle backspace key
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    editText.text?.isEmpty() == true) {
                    if (index > 0) {
                        otpFields[index - 1].requestFocus()
                        otpFields[index - 1].text?.clear()
                    }
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    private fun getOtpFromFields(): String {
        return "${binding.etOtp1.text}${binding.etOtp2.text}${binding.etOtp3.text}${binding.etOtp4.text}${binding.etOtp5.text}${binding.etOtp6.text}"
    }

    private fun updateOtpFields(otp: String) {
        val currentOtp = getOtpFromFields()
        if (currentOtp != otp && otp.length <= 6) {
            binding.etOtp1.setText(if (otp.length > 0) otp[0].toString() else "")
            binding.etOtp2.setText(if (otp.length > 1) otp[1].toString() else "")
            binding.etOtp3.setText(if (otp.length > 2) otp[2].toString() else "")
            binding.etOtp4.setText(if (otp.length > 3) otp[3].toString() else "")
            binding.etOtp5.setText(if (otp.length > 4) otp[4].toString() else "")
            binding.etOtp6.setText(if (otp.length > 5) otp[5].toString() else "")
        }
    }

    private fun clearOtpFields() {
        binding.etOtp1.text?.clear()
        binding.etOtp2.text?.clear()
        binding.etOtp3.text?.clear()
        binding.etOtp4.text?.clear()
        binding.etOtp5.text?.clear()
        binding.etOtp6.text?.clear()
        binding.etOtp1.requestFocus()
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()

        binding.tvResendOtp.visibility = View.GONE
        binding.tvTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = "(${seconds}s)"
            }

            override fun onFinish() {
                binding.tvTimer.visibility = View.GONE
                binding.tvResendOtp.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun navigateToMain() {
        // Navigate to MainActivity or the next screen in your app
        val intent = Intent(this, com.dimts.myapplication.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    companion object {
        const val EXTRA_MOBILE_NUMBER = "extra_mobile_number"

        fun createIntent(context: android.content.Context, mobileNumber: String): Intent {
            return Intent(context, OtpActivity::class.java).apply {
                putExtra(EXTRA_MOBILE_NUMBER, mobileNumber)
            }
        }
    }
}
package com.inacap.smartparkingiot.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.inacap.smartparkingiot.data.security.AuthManager
import com.inacap.smartparkingiot.databinding.ActivityLoginBinding
import com.inacap.smartparkingiot.ui.dashboard.DashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text?.toString().orEmpty()
            val pass = binding.etPassword.text?.toString().orEmpty()

            if (user.isBlank() || pass.isBlank()) {
                showError("Ingresa usuario y contraseña")
                return@setOnClickListener
            }

            if (authManager.authenticate(user, pass)) {
                binding.tvError.visibility = View.GONE
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                showError("Credenciales inválidas")
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}
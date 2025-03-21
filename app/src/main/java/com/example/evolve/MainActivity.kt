package com.example.evolve

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Check login state before setting the UI
        val preferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean("isLoggedIn", true)

        if (!isLoggedIn) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return  // ✅ Stops further execution
        }

        // ✅ Now set the UI after login check
        setContentView(R.layout.temp)
        auth = FirebaseAuth.getInstance()

        // ✅ Make sure the ID exists in your XML
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Temp)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Logout functionality
        val logoutTextView = findViewById<TextView>(R.id.textView3)
        logoutTextView.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            preferences.edit().putBoolean("isLoggedIn", false).apply()

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}

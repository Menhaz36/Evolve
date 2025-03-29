package com.example.evolve

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var stepCountTextView: TextView
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var auth: FirebaseAuth
    private val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 1001 // Unique request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Ensure correct content view setup
        setContentView(R.layout.temp)

        stepCountTextView = findViewById(R.id.stepCountTextView)

        // ✅ Check login state before setting the UI
        val preferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean("isLoggedIn", true)

        if (!isLoggedIn) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return  // ✅ Stops further execution
        }

        auth = FirebaseAuth.getInstance()

        // ✅ Apply system insets for UI layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Temp)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Setup Google Fit
        setupGoogleFit()

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

    private fun setupGoogleFit() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.fitness.Fitness.SCOPE_ACTIVITY_READ)
            .build()

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (googleSignInAccount == null) {
            val signInClient = GoogleSignIn.getClient(this, gso)
            startActivityForResult(signInClient.signInIntent, 1002)
        } else {
            handler.post(runnable) // Start fetching steps every 3 seconds
        }
    }

    // ✅ Runnable for continuous updates
    private val runnable = object : Runnable {
        override fun run() {
            fetchStepsFromGoogleFit() // Fetch real-time steps
            handler.postDelayed(this, 3000) // Re-run every 3 seconds
        }
    }

    private fun fetchStepsFromGoogleFit() {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (googleSignInAccount != null) {
            val endTime = Calendar.getInstance().timeInMillis
            val startTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            Fitness.getHistoryClient(this, googleSignInAccount)
                .readData(readRequest)
                .addOnSuccessListener { response ->
                    val totalSteps = response.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)
                        .dataPoints
                        .sumOf { it.getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt() }

                    runOnUiThread {
                        stepCountTextView.text = "Steps: $totalSteps"
                    }
                }
                .addOnFailureListener {
                    runOnUiThread {
                        stepCountTextView.text = "Failed to fetch steps"
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Stop updates when activity is destroyed
    }
}

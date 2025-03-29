package com.example.evolve

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA
import com.google.android.gms.fitness.data.Field
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 1001 // Unique request code
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

        //googlefit initialisation
        setupGoogleFit()

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

    private fun setupGoogleFit() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            // Ask for permission only if not already granted
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSION_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            // Permissions already granted, start reading data
            readStepsData()
        }
    }

    private fun readStepsData() {
        val account = GoogleSignIn.getLastSignedInAccount(this) ?: return

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val totalSteps = dataSet.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                Log.d("GoogleFitIntegration", "Total steps today: $totalSteps")
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitIntegration", "Error reading steps: ${e.message}")
            }
    }

}

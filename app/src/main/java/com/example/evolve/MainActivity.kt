package com.example.evolve

import android.content.Intent
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 1003

    private fun requestActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        } else{
            Log.d("GoogleFit", "✅ ACTIVITY_RECOGNITION permission already granted")
        }
    }

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

        requestActivityRecognitionPermission()  // ✅ Ask permission before Google Fit

        // ✅ Setup Google Fit
        setupGoogleFit()

        /////////////////////////////yaha
        val editTextGoals = findViewById<EditText>(R.id.textView_goals)
        val linearLayoutGoals = findViewById<LinearLayout>(R.id.linearLayoutGoals)

// When user taps the layout, focus on EditText
        linearLayoutGoals.setOnClickListener {
            editTextGoals.isFocusableInTouchMode = true
            editTextGoals.requestFocus()
        }

// Save goal when focus is lost
        editTextGoals.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveGoal(editTextGoals.text.toString())
            }
        }

// Handle Enter key press
        editTextGoals.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                saveGoal(editTextGoals.text.toString())
                editTextGoals.clearFocus() // Remove focus
                hideKeyboard(editTextGoals) // Hide keyboard
                true
            } else {
                false
            }
        }

        ///////////////////////////

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
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
            .build()

        val googleSignInAccount = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSION_REQUEST_CODE,
                googleSignInAccount,
                fitnessOptions
            )
        } else {
            Log.d("GoogleFit", "Google Fit permissions already granted ✅")
            fetchStepsFromGoogleFit()
            handler.post(runnable) // Start step tracking
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_FIT_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d("GoogleFit", "✅ Google Fit permissions granted")
                fetchStepsFromGoogleFit() // Now safe to fetch steps
            } else {
                Log.e("GoogleFit", "❌ Google Fit permissions denied!")
                stepCountTextView.text = "Google Fit access denied. Please grant permissions."
            }
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
            val endTime = System.currentTimeMillis()
            val startTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)  // Ensure no leftover seconds
                set(Calendar.MILLISECOND, 0)  // Reset milliseconds for accuracy
            }.timeInMillis

            Log.d("GoogleFit", "Start Time: $startTime, End Time: $endTime")

            val readRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            Fitness.getHistoryClient(this, googleSignInAccount)
                .readData(readRequest)
                .addOnSuccessListener { response ->
                    var totalSteps = 0
                    for (bucket in response.buckets) {
                        for (dataSet in bucket.dataSets) {
                            for (dataPoint in dataSet.dataPoints) {
                                for (field in dataPoint.dataType.fields) {
                                    totalSteps += dataPoint.getValue(field).asInt()
                                }
                            }
                        }
                    }

                    Log.d("GoogleFit", "✅ Steps fetched successfully: $totalSteps")
                    runOnUiThread {
                        stepCountTextView.text = "Steps: $totalSteps"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleFit", "Failed to fetch steps", e)
                    runOnUiThread {
                        stepCountTextView.text = "Failed to fetch steps: ${e.localizedMessage}"
                    }
                }
        } else {
            Log.e("GoogleFit", "GoogleSignInAccount is null")
        }
    }



    ////////////yaHA
    // Function to save the goal
    fun saveGoal(goal: String) {
        if (goal.isNotEmpty()) {
            Toast.makeText(this, "Goal Updated: $goal", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to hide the keyboard
    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Stop updates when activity is destroyed
    }
}

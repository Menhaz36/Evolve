package com.example.evolve

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class temp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StepsTrackerScreen()
        }
    }
}

@Composable
fun StepsTrackerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A22))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Streak Header
        Card(
            shape = RoundedCornerShape(400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.padding(top = 30.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2A2A))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "100", color = Color(0xFFF03D1F), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Streak", color = Color(0xFFF03D1F), fontSize = 12.sp)
                Text(text = "Aag laga diya bhai ne", color = Color.White, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Streak Icon
        Image(
            painter = painterResource(id = R.drawable.streak_icon),
            contentDescription = "Streak Icon",
            modifier = Modifier.size(141.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Background Circle
        Image(
            painter = painterResource(id = R.drawable.ellipse),
            contentDescription = "Background Circle",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth().height(624.dp)
        )

        Text(
            text = "10000 steps",
            color = Color.Black,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 204.dp)
        )

        // Cat Image
        Image(
            painter = painterResource(id = R.drawable.anda_wali_bilii),
            contentDescription = "Cat Image",
            modifier = Modifier.size(300.dp, 500.dp)
        )

        // Scrollable Bottom Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "GOALS", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Activity", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Logout", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewStepsTrackerScreen() {
    StepsTrackerScreen()
}
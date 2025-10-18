package com.example.campusrunner.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController? = null) {
    Scaffold(
        topBar = {
            Text(
                text = "👤 我的",
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Text("这里将展示用户资料、接单记录等信息")
        }
    }
}
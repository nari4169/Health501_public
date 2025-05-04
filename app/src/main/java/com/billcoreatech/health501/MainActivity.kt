package com.billcoreatech.health501

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billcoreatech.health501.presentaion.HealthConnectApp
import com.billcoreatech.health501.ui.theme.Health501Theme
import com.billcoreatech.health501.viewmodels.HealthConnectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: HealthConnectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Health501Theme {
                Surface ( modifier = Modifier.padding(20.dp)) {
                    HealthConnectApp()
                }
            }
        }
    }
}

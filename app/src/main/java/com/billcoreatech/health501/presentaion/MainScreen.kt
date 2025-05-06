package com.billcoreatech.health501.presentaion

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.billcoreatech.health501.component.BucketRow
import com.billcoreatech.health501.viewmodels.HealthConnectViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun MainScreen(
    viewModel: HealthConnectViewModel = hiltViewModel()
) {
    val _stepCount = viewModel.stepsTotal.collectAsState()
    val stepCount = _stepCount.value
    val _bucketDataList = viewModel.bucketDataList.collectAsState()
    val bucketDataList = _bucketDataList.value
    var startTime = viewModel.startTime.collectAsState().value
    var endTime = viewModel.endTime.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("만보계 앱") },
                navigationIcon = {
                    IconButton(onClick = {
                        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
                            .truncatedTo(ChronoUnit.HOURS)
                            .plusHours(1)
                        val startTime = endTime.minusWeeks(1)
                        viewModel.readAggregateData(startTime, endTime)
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "메뉴")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5E82FC),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn ( modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    StepIndicator(steps = stepCount.toInt(), startTime, endTime)
                }
            }
            itemsIndexed (bucketDataList) { index, item ->
                BucketRow(item)
            }
        }
    }
}


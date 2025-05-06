package com.billcoreatech.health501.presentaion

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.billcoreatech.health501.R
import com.billcoreatech.health501.component.BucketRow
import com.billcoreatech.health501.data.HealthConnectManager
import com.billcoreatech.health501.helper.PermissionHelper
import com.billcoreatech.health501.ui.theme.Health501Theme
import com.billcoreatech.health501.viewmodels.HealthConnectViewModel
import com.billcoreatech.health501.viewmodels.SeriesRecordsType
import com.example.healthconnectsample.data.dateTimeWithOffsetOrDefault
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

const val TAG = "Health Connect sample"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HealthConnectApp(
    permissionHelper : PermissionHelper,
    viewModel : HealthConnectViewModel = hiltViewModel(),
) {
    val healthConnectManager = viewModel.healthConnectManager
    val permissions = viewModel.permissions
    val backgroundReadAvailable by viewModel.backgroundReadAvailable.collectAsState()
    val backgroundReadGranted by viewModel.backgroundReadGranted.collectAsState()
    val permissionsGranted by viewModel.permissionsGranted
    val onPermissionsResult = {
        viewModel.initialLoad()
        viewModel.subscribeData()
    }
    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            onPermissionsResult()
        }

    // Step Count 정보를 수집 하기 위해서 추가
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.hasPermission.value = isGranted
        viewModel.subscribeData()
    }
    viewModel.hasPermission.value = permissionHelper.hasPermission(viewModel.permissionStep)

    Health501Theme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            if (!permissionsGranted || !viewModel.hasPermission.value) {
                HealthConnectScreen(
                    healthConnectManager = healthConnectManager,
                    modifier = Modifier.padding(innerPadding),
                    permissionsGranted,
                    viewModel.hasPermission.value,
                    onResumeAvailabilityCheck = {
                        healthConnectManager.checkAvailability()
                    },
                    onPermissionLaunch = {
                        Log.e("", "permissionsLauncher.launch(permissions)")
                        permissionsLauncher.launch(permissions)
                    },
                    onPermissionLaunch2 = {
                        Log.e("", "launcher.launch(viewModel.permissionStep)")
                        launcher.launch(viewModel.permissionStep)
                    }
                )
            } else {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun HealthSessionListScreen(viewModel: HealthConnectViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val _sessionList = viewModel.sessionsList.collectAsState()
    val sessionList = _sessionList.value
    val _sessionMetrics = viewModel.sessionMetrics.collectAsState()
    val sessionMetrics = _sessionMetrics.value
    val _records = viewModel.recordList.collectAsState()
    val records = _records.value
    val _stepsCount = viewModel.stepsTotal.collectAsState()
    val stepsCount = _stepsCount.value
    val sType = SeriesRecordsType.valueOf(viewModel.seriesRecordsTypeString.value)
    val _bucketDataList = viewModel.bucketDataList.collectAsState()
    val bucketDataList = _bucketDataList.value

    LazyColumn (
        modifier = Modifier.fillMaxSize()
    ){
        if (sessionList.isEmpty()) {
            item {
                TextButton( onClick = {
                    viewModel.insertExerciseSession()
                }) {
                    Text(
                        text = "No sessions available, add one ???",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        itemsIndexed (sessionList){
                index, session ->
            Log.e(TAG, "session: $session")
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Session $index: ${session.startTime.toLocalDateTime()}",
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = "${session.endTime.toLocalDateTime()}",
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = "${session.sourceAppInfo?.packageName}",
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = "${session.id}",
                    modifier = Modifier.padding(8.dp)
                )
                TextButton(onClick = {
                    viewModel.readAssociatedSessionData(session.id)
                }) {
                    Text(
                        text = "${session.title}",
                        modifier = Modifier.padding(8.dp)
                    )
                }
                IconButton(onClick = {
                    if (session.sourceAppInfo.toString().contains(context.packageName)) {
                        viewModel.deleteExerciseSession(session.id)
                    } else {
                        Log.e(TAG, "Cannot delete session from another app")
                        MaterialDialog(context).show {
                            icon(R.drawable.ic_health_connect_logo)
                            title(text = "Cannot delete session from another app")
                            message(text = "You cannot delete a session that was created by another app.")
                            positiveButton(text = "OK") {
                                dismiss()
                            }
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Session"
                    )
                }
            }
        }
        item {
            Text(
                text = "uid: ${sessionMetrics.uid}",
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            TextButton( onClick = {
                viewModel.seriesRecordsTypeString.value = "STEPS"
                viewModel._recordList.value = listOf()
                viewModel.readRecordList(sessionMetrics.uid, viewModel.seriesRecordsTypeString.value)
                viewModel.startStepTracking()

                val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
                    .truncatedTo(ChronoUnit.HOURS)
                    .plusHours(1)
                val startTime = endTime.minusWeeks(1)
                viewModel.readAggregateData(startTime, endTime)

            }) {
                Text(
                    text = "totalSteps: ${sessionMetrics.totalSteps}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        item {
            Text(
                text = "steps: $stepsCount",
                modifier = Modifier.padding(8.dp)
            )
        }
        itemsIndexed(bucketDataList) {
                index, bucketData ->
            Log.e(TAG, "bucketData: $bucketData")
            BucketRow(
                bucketData = bucketData,
            )
        }
        item {
            Text(
                text = "totalDistance: ${sessionMetrics.totalDistance}",
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            Text(
                text = "maxHeartRate: ${sessionMetrics.maxHeartRate}",
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            Text(
                text = "minHeartRate: ${sessionMetrics.minHeartRate}",
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            Text(
                text = "avgHeartRate: ${sessionMetrics.avgHeartRate}",
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            Text(
                text = "totalActiveTime: ${sessionMetrics.totalActiveTime}",
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            Text(
                text = "totalEnergyBurned: ${sessionMetrics.totalEnergyBurned}",
                modifier = Modifier.padding(8.dp)
            )
        }
        when (sType) {
            SeriesRecordsType.STEPS -> {
                for (record in records.map { it as StepsRecord }) {
                    renderData(record, record.startTime, record.endTime,
                        "Count: " + record.count
                    )
                }
            }

            SeriesRecordsType.DISTANCE -> {
                for (record in records.map { it as DistanceRecord }) {
                    renderData(record, record.startTime, record.endTime,
                        "Count: " + record.distance
                    )
                }
            }

            SeriesRecordsType.CALORIES -> {
                for (record in records.map { it as TotalCaloriesBurnedRecord }) {
                    renderData(record, record.startTime, record.endTime,
                        "Energy: " + record.energy
                    )
                }
            }

            SeriesRecordsType.HEARTRATE -> {
                for (record in records.map { it as HeartRateRecord }) {
                    renderData(record, record.startTime, record.endTime,
                        "Heartbeat Samples: " +
                                record.samples.map { it.beatsPerMinute }
                                    .joinToString(", ")

                    )
                }
            }
        }
    }
}

fun LazyListScope.renderData(
    record: Record,
    startTime: Instant,
    endTime: Instant,
    data: String
) {
    item {
        androidx.compose.material.Text(
            text = record.metadata.id,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.primary
        )
        androidx.compose.material.Text(
            text = formatDisplayTimeStartEnd(startTime, null, endTime, null),
            style = MaterialTheme.typography.body2
        )
        androidx.compose.material.Text(
            text = data,
            style = MaterialTheme.typography.body2
        )
    }
}

fun formatDisplayTimeStartEnd(
    startTime: Instant,
    startZoneOffset: ZoneOffset?,
    endTime: Instant,
    endZoneOffset: ZoneOffset?
): String {
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    val start = timeFormatter.format(dateTimeWithOffsetOrDefault(startTime, startZoneOffset))
    val end = timeFormatter.format(dateTimeWithOffsetOrDefault(endTime, endZoneOffset))
    return "$start - $end"
}

@Composable
fun HealthConnectScreen(
    healthConnectManager: HealthConnectManager, modifier: Modifier, permissionsGranted: Boolean, permissionHelper: Boolean,
    onResumeAvailabilityCheck: () -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onPermissionLaunch: () -> Unit = {},
    onPermissionLaunch2: () -> Unit = {},
) {
    val availability by healthConnectManager.availability

    val currentOnAvailabilityCheck by rememberUpdatedState(onResumeAvailabilityCheck)
    Log.e("TAG", "HealthConnectScreen permissionsGranted: $permissionsGranted")
    if (!permissionsGranted) {
        onPermissionLaunch()
    }
    if (!permissionHelper) {
        onPermissionLaunch2()
    }

// 시작 화면이 다시 시작될 때마다 Health Connect가 설치되었는지 다시 확인하는 리스너를 추가합니다.
// 이렇게 하면 사용자가 Play 스토어로 리디렉션되고
// 온보딩 흐름을 따른 경우, 앱이 다시 시작될 때 사용자에게 Health Connect를 설치하라는 메시지를 표시하는 대신
// 앱이 Health Connect를 사용할 수 있음을 인식하고
// 적절한 환영 메시지를 표시합니다.

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentOnAvailabilityCheck()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = {
            onPermissionLaunch()
        }) {
            Image(
                modifier = Modifier.fillMaxWidth(0.5f),
                painter = painterResource(id = R.drawable.ic_health_connect_logo),
                contentDescription = stringResource(id = R.string.health_connect_logo)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(id = R.string.welcome_message),
            color = MaterialTheme.colors.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        when (availability) {
            SDK_AVAILABLE -> InstalledMessage()
            SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> NotInstalledMessage()
            SDK_UNAVAILABLE -> NotSupportedMessage()
        }
    }
}

@Composable
fun NotSupportedMessage() {
    Text(
        text = stringResource(id = R.string.not_supported_message),
        color = MaterialTheme.colors.onBackground
    )
}

@Composable
fun NotInstalledMessage() {
    Text(
        text = stringResource(id = R.string.not_installed_message),
        color = MaterialTheme.colors.onBackground
    )
}

@Composable
fun InstalledMessage() {
    Text(
        text = stringResource(id = R.string.installed_message),
        color = MaterialTheme.colors.onBackground
    )
}
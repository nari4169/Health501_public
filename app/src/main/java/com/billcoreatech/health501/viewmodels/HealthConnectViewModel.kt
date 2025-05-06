package com.billcoreatech.health501.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.os.RemoteException
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.permission.HealthPermission.Companion.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcoreatech.health501.data.BucketData
import com.billcoreatech.health501.data.DataPointData
import com.billcoreatech.health501.data.DataSetData
import com.billcoreatech.health501.data.ExerciseSession
import com.billcoreatech.health501.data.ExerciseSessionData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.billcoreatech.health501.data.HealthConnectManager
import com.billcoreatech.health501.helper.PermissionHelper
import com.example.healthconnectsample.data.dateTimeWithOffsetOrDefault
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.data.LocalDataType.TYPE_STEP_COUNT_DELTA
import com.google.android.gms.fitness.data.LocalField
import com.google.android.gms.fitness.request.LocalDataReadRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach
import kotlin.random.Random
import kotlin.reflect.KClass

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    val healthConnectManager: HealthConnectManager
) : ViewModel() {

    var TAG = ""
    // ViewModel 로직 작성
    private val healthConnectCompatibleApps = healthConnectManager.healthConnectCompatibleApps
    val localRecordingClient = healthConnectManager.localRecordingClient

    @SuppressLint("StaticFieldLeak")

    val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getWritePermission(SpeedRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
    )

    val permissionStep = Manifest.permission.ACTIVITY_RECOGNITION
    var hasPermission = mutableStateOf(false)
        private set

    var uid = mutableStateOf("")
        private set

    var recordTypeString = mutableStateOf("EXERCISE_SESSION")
    var seriesRecordsTypeString = mutableStateOf("STEPS")

    val recordType: KClass<out Record> = RecordType.stringToClass(recordTypeString.value)
    val seriesRecordsType: KClass<out Record> = SeriesRecordsType.stringToClass(seriesRecordsTypeString.value)

    var permissionsGranted = mutableStateOf(false)
        private set

    val _backgroundReadAvailable = MutableStateFlow(false)
    val backgroundReadAvailable: StateFlow<Boolean> = _backgroundReadAvailable

    val _backgroundReadGranted = MutableStateFlow(false)
    val backgroundReadGranted: StateFlow<Boolean> = _backgroundReadGranted

    val _stepsTotal = MutableStateFlow(0L)
    val stepsTotal: StateFlow<Long> = _stepsTotal
    val _startTime = MutableStateFlow(0L)
    val startTime: StateFlow<Long> = _startTime
    val _endTime = MutableStateFlow(0L)
    val endTime: StateFlow<Long> = _endTime

    val _sessionsList: MutableStateFlow<List<ExerciseSession>> = MutableStateFlow(listOf())
    val sessionsList: StateFlow<List<ExerciseSession>> = _sessionsList
    val _sessionMetrics: MutableStateFlow<ExerciseSessionData> = MutableStateFlow(ExerciseSessionData(uid.value))
    val sessionMetrics: StateFlow<ExerciseSessionData> = _sessionMetrics
    val _recordList = MutableStateFlow<List<Record>>(listOf())
    val recordList: StateFlow<List<Record>> = _recordList

    val _bucketDataList : MutableStateFlow<List<BucketData>> = MutableStateFlow(listOf())
    val bucketDataList: StateFlow<List<BucketData>> = _bucketDataList

    var uiState: UiState by mutableStateOf(UiState.Uninitialized)
        private set

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    fun initialLoad() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                try {
                    readExerciseSessions()
                } catch (e: Exception) {
                    Log.e("", "Error reading exercise sessions: ${e.message}")
                }
            }
        }
    }

    fun startStepTracking() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                try {
                    _stepsTotal.value = healthConnectManager.readSteps()
                    Log.e("", "Steps: ${_stepsTotal.value}")
                } catch (e: Exception) {
                    Log.e("", "Error starting step tracking: ${e.message}")
                }
            }
        }
    }

    fun readAssociatedSessionData(uid : String) {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                try {
                    _sessionMetrics.value = healthConnectManager.readAssociatedSessionData(uid)
                } catch (e: Exception) {
                    Log.e("", "Error reading session data: ${e.message}")
                }
            }
        }
    }

    fun readRecordList(uid: String, srtString: String) {
        seriesRecordsTypeString.value = srtString
        viewModelScope.launch {
            tryWithPermissionsCheck {
                _recordList.value = listOf()
                try {
                    _recordList.value = healthConnectManager.fetchSeriesRecordsFromUid(
                        recordType,
                        uid,
                        seriesRecordsType
                    )
                } catch (e: Exception) {
                    Log.e("", "Error reading record list: ${e.message}")
                }
            }
        }
    }

    fun insertExerciseSession() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                val latestStartOfSession = ZonedDateTime.now().minusMinutes(1)
                val offset = Random.nextDouble()

                // Generate random start time between the start of the day and (now - 30mins).
                val startOfSession = startOfDay.plusSeconds(
                    (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
                )
                val endOfSession = startOfSession.plusDays(1)

                healthConnectManager.writeExerciseSession(startOfSession, endOfSession)
                readExerciseSessions()
            }
        }
    }

    fun deleteExerciseSession(uid: String) {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                Log.e("", "--------------------------\nExercise session deleted: $uid $sessionsList.size")
                healthConnectManager.deleteExerciseSession(uid)
                Log.e("", "--------------------------\nExercise session deleted: $uid $sessionsList.size")
                try {
                    readExerciseSessions()
                } catch (e: Exception) {
                    Log.e("", "Error deleting exercise session: ${e.message}")
                }
            }
        }
    }

    private suspend fun readExerciseSessions() {
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()
        Log.e("","appKeys=${healthConnectCompatibleApps.keys}")
        _sessionsList.value = healthConnectManager
            .readExerciseSessions(startOfDay.toInstant(), now)
            .filter { record ->
                record.metadata.dataOrigin.packageName in healthConnectCompatibleApps.keys
            }
            .map { record ->
                val packageName = record.metadata.dataOrigin.packageName
                ExerciseSession(
                    startTime = dateTimeWithOffsetOrDefault(record.startTime, record.startZoneOffset),
                    endTime = dateTimeWithOffsetOrDefault(record.startTime, record.startZoneOffset),
                    id = record.metadata.id,
                    sourceAppInfo = healthConnectCompatibleApps[packageName],
                    title = record.title
                )
            }
    }

    /**
     * Subscribes to the [LocalDataType.TYPE_STEP_COUNT_DELTA].
     */
    @SuppressLint("MissingPermission")
    fun subscribeData(){
        if (!hasPermission.value) {
            Log.e(TAG, "Permission ACTIVITY_RECOGNITION is not granted!")
            return
        }

        localRecordingClient
            .subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.e(TAG, "Successfully subscribed!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "There was a problem of subscribing.", e)
            }

    }

    /**
     * Reads raw data of [LocalDataType.TYPE_STEP_COUNT_DELTA] between the given start and end time.
     */
    fun readRawData(
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ){
        val readRequest = LocalDataReadRequest.Builder()
            .read(TYPE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(
                startTime.toEpochSecond(),
                endTime.toEpochSecond(),
                TimeUnit.SECONDS
            )
            .build()

        _stepsTotal.value = 0L
        localRecordingClient.readData(readRequest)
            .addOnSuccessListener { response ->
                _bucketDataList.value = listOf()
                _bucketDataList.value = response.buckets.mapIndexed({ bucketIndex, bucket ->
                    BucketData(
                        index = bucketIndex,
                        startTime = bucket.getStartTime(TimeUnit.SECONDS),
                        endTime = bucket.getEndTime(TimeUnit.SECONDS),
                        dataSetDataList = bucket.dataSets.mapIndexed({ dataSetIndex, dataSet ->
                            DataSetData(
                                index = dataSetIndex,
                                dataPointDataList = dataSet.dataPoints.mapIndexed({ dataPointIndex, dataPoint ->
                                    _startTime.value = dataPoint.getStartTime(TimeUnit.SECONDS)
                                    _endTime.value = dataPoint.getEndTime(TimeUnit.SECONDS)
                                    _stepsTotal.value += dataPoint.getValue(LocalField.FIELD_STEPS)
                                        .asInt()
                                    DataPointData(
                                        index = dataPointIndex,
                                        startTime = dataPoint.getStartTime(TimeUnit.SECONDS),
                                        endTime = dataPoint.getEndTime(TimeUnit.SECONDS),
                                        fieldName = LocalField.FIELD_STEPS.name,
                                        fieldValue = dataPoint.getValue(LocalField.FIELD_STEPS)
                                            .asInt()
                                    )
                                })
                            )
                        })
                    )
                })
                Log.e(TAG, "Successfully read raw data")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error reading data between $startTime and $endTime", e)
            }
    }

    /**
     * Reads aggregate data of [LocalDataType.TYPE_STEP_COUNT_DELTA] between the given start and end time.
     */
    fun readAggregateData(
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ){
        val readRequest = LocalDataReadRequest.Builder()
            .aggregate(TYPE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(
                startTime.toEpochSecond(),
                endTime.toEpochSecond(),
                TimeUnit.SECONDS
            )
            .build()

        localRecordingClient.readData(readRequest)
            .addOnSuccessListener { response ->
                _bucketDataList.value = listOf()
                _bucketDataList.value = response.buckets.mapIndexed({ bucketIndex, bucket ->
                    BucketData(
                        index = bucketIndex,
                        startTime = bucket.getStartTime(TimeUnit.SECONDS),
                        endTime = bucket.getEndTime(TimeUnit.SECONDS),
                        dataSetDataList = bucket.dataSets.mapIndexed({ dataSetIndex, dataSet ->
                            DataSetData(
                                index = dataSetIndex,
                                dataPointDataList = dataSet.dataPoints.mapIndexed({ dataPointIndex, dataPoint ->
                                    DataPointData(
                                        index = dataPointIndex,
                                        startTime = dataPoint.getStartTime(TimeUnit.SECONDS),
                                        endTime = dataPoint.getEndTime(TimeUnit.SECONDS),
                                        fieldName = LocalField.FIELD_STEPS.name,
                                        fieldValue = dataPoint.getValue(LocalField.FIELD_STEPS)
                                            .asInt()
                                    )
                                })
                            )
                        })
                    )
                })

                Log.i(TAG, "Successfully read aggregate data")
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error reading data between $startTime and $endTime", e)
            }
    }

    /**
     * Provides permission check and error handling for Health Connect suspend function calls.
     *
     * Permissions are checked prior to execution of [block], and if all permissions aren't granted
     * the [block] won't be executed, and [permissionsGranted] will be set to false, which will
     * result in the UI showing the permissions button.
     *
     * Where an error is caught, of the type Health Connect is known to throw, [uiState] is set to
     * [UiState.Error], which results in the snackbar being used to show the error message.
     *
     * Health Connect 일시 중단 함수 호출에 대한 권한 확인 및 오류 처리를 제공합니다.
     *
     * [block] 실행 전에 권한이 확인되며, 모든 권한이 부여되지 않으면
     * [block]이 실행되지 않고 [permissionsGranted]가 false로 설정되어
     * UI에 권한 버튼이 표시됩니다.
     *
     * Health Connect에서 발생하는 것으로 알려진 유형의 오류가 발견되면 [uiState]가
     * [UiState.Error]로 설정되어 스낵바를 통해 오류 메시지가 표시됩니다.
     */
    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
        _backgroundReadAvailable.value = healthConnectManager.isFeatureAvailable(
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
        )
        _backgroundReadGranted.value = healthConnectManager.hasAllPermissions(
            setOf(PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND)
        )
        uiState = try {
            if (permissionsGranted.value) {
                block()
            }
            UiState.Done
        } catch (remoteException: RemoteException) {
            UiState.Error(remoteException)
        } catch (securityException: SecurityException) {
            UiState.Error(securityException)
        } catch (ioException: IOException) {
            UiState.Error(ioException)
        } catch (illegalStateException: IllegalStateException) {
            UiState.Error(illegalStateException)
        }
    }

    sealed class UiState {
        object Uninitialized : UiState()
        object Done : UiState()

        // A random UUID is used in each Error object to allow errors to be uniquely identified,
        // and recomposition won't result in multiple snackbars.
        data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
    }
}

enum class RecordType(val clazz: KClass<out Record>) {
    EXERCISE_SESSION(ExerciseSessionRecord::class),
    SLEEP_SESSION(SleepSessionRecord::class);

    companion object {
        fun stringToClass(recordTypeString: String) = RecordType.valueOf(recordTypeString).clazz
    }
}

enum class SeriesRecordsType(val clazz: KClass<out Record>) {
    STEPS(StepsRecord::class),
    DISTANCE(DistanceRecord::class),
    CALORIES(TotalCaloriesBurnedRecord::class),
    HEARTRATE(HeartRateRecord::class);

    companion object {
        fun stringToClass(seriesRecordsTypeString: String) = SeriesRecordsType.valueOf(seriesRecordsTypeString).clazz
    }
}
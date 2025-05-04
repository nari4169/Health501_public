/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.healthconnectsample.data

import android.annotation.SuppressLint
import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.random.Random

/**
 * Creates a [ZonedDateTime] either using the offset stored in Health Connect, or falling back on
 * the zone offset for the device, where Health Connect contains no zone offset data. This fallback
 * may be correct in a number of circumstances, but may also not apply in others, so is used here
 * just as an example.
 * * Health Connect에 저장된 오프셋을 사용하거나, Health Connect에 존 오프셋 데이터가 없는 경우 기기의 존 오프셋을 대체하여 [ZonedDateTime]을 생성합니다.
 * * 이 대체 방식은
 * * 여러 상황에서는 정확할 수 있지만, 다른 상황에서는 적용되지 않을 수도 있으므로, 여기서는
 * * 예시로만 사용합니다.
 */
fun dateTimeWithOffsetOrDefault(time: Instant, offset: ZoneOffset?): ZonedDateTime =
    if (offset != null) {
        ZonedDateTime.ofInstant(time, offset)
    } else {
        ZonedDateTime.ofInstant(time, ZoneId.systemDefault())
    }

@SuppressLint("DefaultLocale")
fun Duration.formatTime() = String.format(
    "%02d:%02d:%02d",
    this.toHours() % 24,
    this.toMinutes() % 60,
    this.seconds % 60
)

@SuppressLint("DefaultLocale")
fun Duration.formatHoursMinutes() = String.format(
    "%01dh%02dm",
    this.toHours() % 24,
    this.toMinutes() % 60
)

/**
 * Generates a random sleep stage for the purpose of populating data. Excludes UNKNOWN sleep stage.
 */
fun randomSleepStage() = listOf(
    SleepSessionRecord.STAGE_TYPE_AWAKE,
    SleepSessionRecord.STAGE_TYPE_DEEP,
    SleepSessionRecord.STAGE_TYPE_LIGHT,
    SleepSessionRecord.STAGE_TYPE_OUT_OF_BED,
    SleepSessionRecord.STAGE_TYPE_REM,
    SleepSessionRecord.STAGE_TYPE_SLEEPING
).let { stages ->
    stages[Random.nextInt(stages.size)]
}

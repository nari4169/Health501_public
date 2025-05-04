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
package com.billcoreatech.health501.data

import androidx.health.connect.client.units.Mass
import java.time.ZonedDateTime

/**
 * Represents a weight record and associated data.
 * 무게 기록과 관련 데이터를 나타냅니다.
 */
data class WeightData(
    val weight: Mass,
    val id: String,
    val time: ZonedDateTime,
    val sourceAppInfo: HealthConnectAppInfo?
)
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

import android.graphics.drawable.Drawable

/**
 * Information about an app that can be used for displaying attribution.
 * 속성을 표시하는 데 사용할 수 있는 앱에 대한 정보입니다.
 */
data class HealthConnectAppInfo(
    val packageName: String,
    val appLabel: String,
    val icon: Drawable?
)
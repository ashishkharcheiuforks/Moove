package com.backdoor.moove.utils

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object TimeCount {

    const val SECOND: Long = 1000
    const val MINUTE: Long = 60 * SECOND
    const val HOUR: Long = MINUTE * 60
    private const val HALF_DAY: Long = HOUR * 12
    const val DAY: Long = HALF_DAY * 2

    fun isCurrent(eventTime: String?): Boolean {
        return TimeUtils.getDateTimeFromGmt(eventTime) > System.currentTimeMillis()
    }

    fun isCurrent(millis: Long): Boolean {
        return millis > System.currentTimeMillis()
    }
}

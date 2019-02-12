package com.backdoor.moove.core.data

import com.google.android.gms.maps.model.LatLng

/**
 * Copyright 2015 Nazar Suhovich
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
class MarkerModel {
    var title: String? = null
        private set
    val position: LatLng
    var icon: Int = 0
        private set
    val radius: Int
    var id: Long = 0
        private set

    constructor(title: String, id: Long) {
        this.title = title
        this.id = id
    }

    constructor(title: String, id: Long, icon: Int) {
        this.title = title
        this.id = id
        this.icon = icon
    }

    constructor(title: String, position: LatLng, icon: Int, id: Long, radius: Int) {
        this.position = position
        this.title = title
        this.icon = icon
        this.id = id
        this.radius = radius
    }
}

package com.backdoor.moove;

import android.support.multidex.MultiDexApplication;

import com.backdoor.moove.core.helper.Notifier;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Moove extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Notifier.createChannels(this);
        Fabric.with(this, new Crashlytics(), new Answers());
    }
}

package com.backdoor.moove.core.helper;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.backdoor.moove.core.widgets.LeftDistanceWidget;
import com.backdoor.moove.core.widgets.SimpleWidget;

/**
 * Copyright 2015 Nazar Suhovich
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
public class Widget {

    public static void updateWidgets(Context context) {
        Intent intent = new Intent(context, LeftDistanceWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new
                ComponentName(context, LeftDistanceWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);

        intent = new Intent(context, SimpleWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int idx[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new
                ComponentName(context, SimpleWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idx);
        context.sendBroadcast(intent);
    }
}

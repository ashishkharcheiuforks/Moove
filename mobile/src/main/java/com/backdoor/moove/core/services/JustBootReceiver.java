package com.backdoor.moove.core.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.backdoor.moove.core.async.DisableAsync;

public class JustBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, TaskButlerService.class));

        new DisableAsync(context).execute();
    }
}

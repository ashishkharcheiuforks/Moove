package com.backdoor.moove.core.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;

public class ReminderUtils {

    public ReminderUtils() {
    }

    /**
     * Generate human readable string for reminder type.
     *
     * @param context application context.
     * @param type    reminder type.
     * @return reminder type.
     */
    @NonNull
    public static String getTypeString(Context context, @NonNull String type) {
        String res;
        if (type.matches(Constants.TYPE_LOCATION_CALL) ||
                type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
            String init = context.getString(R.string.make_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.contains(Constants.TYPE_MESSAGE)) {
            String init = context.getString(R.string.send_message);
            res = init + " (" + getType(context, type) + ")";
        } else {
            String init = context.getString(R.string.reminder);
            res = init + " (" + getType(context, type) + ")";
        }
        return res;
    }

    /**
     * Get human readable string for reminder type.
     *
     * @param context application context.
     * @param type    reminder type.
     * @return reminder type.
     */
    @NonNull
    public static String getType(Context context, @NonNull String type) {
        String res;
        if (type.startsWith(Constants.TYPE_LOCATION)) {
            res = context.getString(R.string.location);
        } else {
            res = context.getString(R.string.place_out);
        }
        return res;
    }
}

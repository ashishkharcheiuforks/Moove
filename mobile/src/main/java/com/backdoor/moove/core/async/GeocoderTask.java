package com.backdoor.moove.core.async;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Task that helps find place by name.
 */
public class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

    private static final String TAG = "GeocoderTask";

    private GeocoderListener mListener;
    private Geocoder geocoder;

    public GeocoderTask(Context mContext, GeocoderListener mListener) {
        this.mListener = mListener;
        geocoder = new Geocoder(mContext);
    }

    @Override
    protected List<Address> doInBackground(String... locationName) {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(locationName[0], 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) {
        if (addresses == null || addresses.size() == 0) {
            Log.d(TAG, "No Location found");
        } else {
            if (mListener != null) {
                mListener.onAddressReceived(addresses);
            }
        }
    }

    /**
     * Listener for found places list.
     */
    public interface GeocoderListener {
        void onAddressReceived(List<Address> addresses);
    }
}

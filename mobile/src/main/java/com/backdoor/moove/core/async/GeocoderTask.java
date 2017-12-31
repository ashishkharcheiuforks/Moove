package com.backdoor.moove.core.async;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.backdoor.moove.core.consts.Constants;

import java.io.IOException;
import java.util.List;

/**
 * Task that helps find place by name.
 */
public class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

    private Context mContext;
    private GeocoderListener mListener;

    public GeocoderTask(Context mContext, GeocoderListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @Override
    protected List<Address> doInBackground(String... locationName) {
        // Creating an instance of Geocoder class
        Geocoder geocoder = new Geocoder(mContext);
        List<Address> addresses = null;

        try {
            // Getting a maximum of 3 Address that matches the input text
            addresses = geocoder.getFromLocationName(locationName[0], 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) {
        if (addresses == null || addresses.size() == 0) {
            Log.d(Constants.LOG_TAG, "No Location found");
        } else {
            if (mListener != null && mContext != null) {
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

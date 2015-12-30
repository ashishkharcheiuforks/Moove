package com.backdoor.moove.core.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.backdoor.moove.ContactsList;
import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Messages;
import com.backdoor.moove.core.helper.SharedPrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
public class SuperUtil {

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Concatenate many string to single.
     * @param strings string to concatenate.
     * @return concatenated string
     */
    public static String appendString(String... strings){
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings){
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

    /**
     * Load list of contacts and show chooser activity.
     * @param activity context activity.
     * @param requestCode result request code.
     */
    public static void selectContact(final Activity activity, final int requestCode){
        class Async extends AsyncTask<Void, Void, Void>{

            private ProgressDialog pd;
            private ArrayList<String> contacts;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = ProgressDialog.show(activity, null, activity.getString(R.string.loading_contacts), true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Cursor cursor = activity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                contacts = new ArrayList<>();
                contacts.clear();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            hasPhone = "true";
                        } else {
                            hasPhone = "false";
                        }
                        if (name != null) {
                            if (Boolean.parseBoolean(hasPhone)) {
                                contacts.add(name);
                            }
                        }
                    }
                    cursor.close();
                }
                try {
                    Collections.sort(contacts, new Comparator<String>() {
                        @Override
                        public int compare(String e1, String e2) {
                            return e1.compareToIgnoreCase(e2);
                        }
                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                Intent i = new Intent(activity, ContactsList.class);
                i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts);
                activity.startActivityForResult(i, requestCode);
            }
        }

        new Async().execute();
    }

    public static void showMore(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://search?q=pub:Nazar Suhovich"));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.could_not_launch_market, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Start voice listener for recognition.
     * @param activity activity.
     * @param requestCode result request code.
     */
    public static void startVoiceRecognitionActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something));
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e){
            Messages.toast(activity, activity.getString(R.string.no_recognizer_found));
        }
    }
}

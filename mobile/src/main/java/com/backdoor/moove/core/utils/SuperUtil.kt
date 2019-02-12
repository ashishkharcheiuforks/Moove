package com.backdoor.moove.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import android.widget.Toast

import com.backdoor.moove.ContactsListActivity
import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.helper.Messages

import java.util.ArrayList
import java.util.Collections

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
object SuperUtil {

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Load list of contacts and show chooser activity.
     *
     * @param activity    context activity.
     * @param requestCode result request code.
     */
    fun selectContact(activity: Activity, requestCode: Int) {
        class Async : AsyncTask<Void, Void, Void>() {

            private var pd: ProgressDialog? = null
            private var contacts: ArrayList<String>? = null

            override fun onPreExecute() {
                super.onPreExecute()
                pd = ProgressDialog.show(activity, null, activity.getString(R.string.loading_contacts), true)
            }

            override fun doInBackground(vararg params: Void): Void? {
                val cursor = activity.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
                contacts = ArrayList()
                contacts!!.clear()
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        var hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        if (hasPhone.equals("1", ignoreCase = true)) {
                            hasPhone = "true"
                        } else {
                            hasPhone = "false"
                        }
                        if (name != null) {
                            if (java.lang.Boolean.parseBoolean(hasPhone)) {
                                contacts!!.add(name)
                            }
                        }
                    }
                    cursor.close()
                }
                try {
                    Collections.sort(contacts) { obj, str -> obj.compareTo(str, ignoreCase = true) }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(aVoid: Void) {
                super.onPostExecute(aVoid)
                if (pd != null && pd!!.isShowing) {
                    pd!!.dismiss()
                }
                val i = Intent(activity, ContactsListActivity::class.java)
                i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts)
                activity.startActivityForResult(i, requestCode)
            }
        }

        Async().execute()
    }

    fun showMore(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://search?q=pub:Nazar Suhovich")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.could_not_launch_market, Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Start voice listener for recognition.
     *
     * @param activity    activity.
     * @param requestCode result request code.
     */
    fun startVoiceRecognitionActivity(activity: Activity, requestCode: Int) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something))
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Messages.toast(activity, activity.getString(R.string.no_recognizer_found))
        }

    }
}

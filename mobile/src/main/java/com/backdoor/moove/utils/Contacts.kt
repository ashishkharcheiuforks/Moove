package com.backdoor.moove.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object Contacts {

    fun getPhoto(contactId: Long): Uri? {
        if (contactId == 0L) {
            return null
        }
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
    }

    fun getContactIDFromNumber(contactNumber: String?, context: Context): Int {
        if (contactNumber == null) return 0
        var phoneContactID = 0
        try {
            val contact = Uri.encode(contactNumber)
            val cursor = context.contentResolver
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID), null, null, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    phoneContactID = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                }
                cursor.close()
            }
        } catch (iae: IllegalArgumentException) {
            return 0
        }

        return phoneContactID
    }

    fun getContactNameFromNumber(contactNumber: String?, context: Context): String? {
        if (contactNumber == null) return null
        var phoneContactID: String? = null
        try {
            val contact = Uri.encode(contactNumber)
            val cursor = context.contentResolver
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID), null, null, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    phoneContactID = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                }
                cursor.close()
            }
        } catch (iae: IllegalArgumentException) {
            return phoneContactID
        }

        return phoneContactID
    }
}

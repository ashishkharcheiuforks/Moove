package com.backdoor.moove.utils

import android.content.ContentUris
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract

import java.io.IOException

/**
 * Helper class for accessing to contacts.
 */
object Contacts {

    /**
     * Get photo of contact.
     *
     * @param context   application context.
     * @param contactId contact identifier.
     * @return Photo in bitmap format
     */
    fun getPhoto(context: Context, contactId: Long): Bitmap? {
        var bmp: Bitmap? = null
        if (contactId != 0L) {
            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
            try {
                val fd = context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
                if (fd != null) bmp = BitmapFactory.decodeStream(fd.createInputStream())
            } catch (e: IOException) {
                return null
            }

        }
        return bmp
    }

    /**
     * Get contact identifier by contact name.
     *
     * @param contactNumber contact name.
     * @param context       application context.
     * @return Contact identifier
     */
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

    /**
     * Get contact name by contact number.
     *
     * @param contactNumber contact number.
     * @param context       application context.
     * @return Contact name
     */
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

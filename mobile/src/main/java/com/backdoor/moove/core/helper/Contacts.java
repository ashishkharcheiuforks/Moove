package com.backdoor.moove.core.helper;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * Helper class for accessing to contacts.
 */
public class Contacts {

    private Contacts() {
    }

    /**
     * Get photo of contact.
     *
     * @param context   application context.
     * @param contactId contact identifier.
     * @return Photo in bitmap format
     */
    @Nullable
    public static Bitmap getPhoto(@NonNull Context context, long contactId) {
        Bitmap bmp = null;
        if (contactId != 0) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            try {
                AssetFileDescriptor fd =
                        context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
                if (fd != null) bmp = BitmapFactory.decodeStream(fd.createInputStream());
            } catch (IOException e) {
                return null;
            }
        }
        return bmp;
    }

    /**
     * Get contact identifier by contact name.
     *
     * @param contactNumber contact name.
     * @param context       application context.
     * @return Contact identifier
     */
    public static int getContactIDFromNumber(@Nullable String contactNumber, @NonNull Context context) {
        if (contactNumber == null) return 0;
        int phoneContactID = 0;
        try {
            String contact = Uri.encode(contactNumber);
            Cursor cursor = context.getContentResolver()
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                            null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    phoneContactID = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                }
                cursor.close();
            }
        } catch (IllegalArgumentException iae) {
            return 0;
        }
        return phoneContactID;
    }

    /**
     * Get contact name by contact number.
     *
     * @param contactNumber contact number.
     * @param context       application context.
     * @return Contact name
     */
    @Nullable
    public static String getContactNameFromNumber(@Nullable String contactNumber, @NonNull Context context) {
        if (contactNumber == null) return null;
        String phoneContactID = null;
        try {
            String contact = Uri.encode(contactNumber);
            Cursor cursor = context.getContentResolver()
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                                    ContactsContract.PhoneLookup._ID},
                            null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    phoneContactID = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
                cursor.close();
            }
        } catch (IllegalArgumentException iae) {
            return phoneContactID;
        }
        return phoneContactID;
    }
}

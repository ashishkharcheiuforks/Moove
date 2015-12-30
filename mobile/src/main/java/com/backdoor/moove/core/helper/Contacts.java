package com.backdoor.moove.core.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.IOException;

/**
 * Helper class for accessing to contacts.
 */
public class Contacts {

    private Context mContext;

    public Contacts(Context context){
        this.mContext = context;
    }

    /**
     * Get photo of contact.
     * @param context application context.
     * @param contactId contact identifier.
     * @return Photo in bitmap format
     */
    public static Bitmap getPhoto(Context context, long contactId) {
        Bitmap bmp = null;
        if (contactId != 0) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            try {
                AssetFileDescriptor fd =
                        context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
                bmp = BitmapFactory.decodeStream(fd.createInputStream());
            } catch (IOException e) {
                return null;
            }
        }
        return bmp;
    }

    /**
     * Get contact identifier by contact name.
     * @param contactNumber contact name.
     * @param context application context.
     * @return Contact identifier
     */
    public static int getContactIDFromNumber(String contactNumber,Context context) {
        int phoneContactID = 0;
        try {
            String contact = Uri.encode(contactNumber);
            Cursor contactLookupCursor = context.getContentResolver()
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                            null, null, null);
            while(contactLookupCursor.moveToNext()){
                phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            contactLookupCursor.close();
        } catch (IllegalArgumentException iae) {
            return 0;
        }
        return phoneContactID;
    }

    /**
     * Get contact name by contact number.
     * @param contactNumber contact number.
     * @param context application context.
     * @return Contact name
     */
    public static String getContactNameFromNumber(String contactNumber, Context context) {
        String phoneContactID = null;
        if (contactNumber != null) {
            try {
            String contact = Uri.encode(contactNumber);
            Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact), new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            while (contactLookupCursor.moveToNext()) {
                phoneContactID = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            contactLookupCursor.close();
            } catch (IllegalArgumentException iae) {
                return phoneContactID;
            }
        }
        return phoneContactID;
    }

    /**
     * Get contact group identifier for contact.
     * @param contactId contact identifier.
     * @param context application context.
     * @return Group identifier
     */
    public static long getGroupIdFor(Long contactId, Context context){
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String where = String.format(
                "%s = ? AND %s = ?",
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID);

        String[] whereParams = new String[] {
                ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                Long.toString(contactId),
        };

        String[] selectColumns = new String[]{
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
        };


        Cursor groupIdCursor = context.getContentResolver().query(
                uri,
                selectColumns,
                where,
                whereParams,
                null);
        try{
            if (groupIdCursor.moveToFirst()) {
                return groupIdCursor.getLong(0);
            }
            return Long.MIN_VALUE; // Has no group ...
        } finally{
            groupIdCursor.close();
        }
    }

    /**
     * Get title for contact group.
     * @param context application context.
     * @param groupId group identifier.
     * @return
     */
    public static String getGroupTitle(Context context, long groupId){
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String where = String.format("%s = ?", ContactsContract.Groups._ID);
        String[] whereParams = new String[]{Long.toString(groupId)};
        String[] selectColumns = {ContactsContract.Groups.TITLE};
        Cursor c = context.getContentResolver().query(
                uri,
                selectColumns,
                where,
                whereParams,
                null);

        try{
            if (c.moveToFirst()){
                return c.getString(0);
            }
            return null;
        }finally{
            c.close();
        }
    }

    /**
     * Get contact group identifier by contact number.
     * @param context application context.
     * @param contactNumber contact number.
     * @return
     */
    public static String getContactGroupIdFromNumber(Context context, String contactNumber) {
        String phoneContactID = null;
        if (contactNumber != null) {
            try {
                Cursor cursor = context.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI, new String[] {ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP},
                        ContactsContract.CommonDataKinds.Phone.NUMBER + "='" + contactNumber + "'", null, null);
                while (cursor.moveToNext()) {
                    phoneContactID = cursor.getString(cursor
                            .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP));
                }
                cursor.close();
            } catch (IllegalArgumentException iae) {
                return phoneContactID;
            }
        }
        return phoneContactID;
    }

    /**
     * Get e=mail for contact.
     * @param id contact identifier.
     * @return
     */
    public String getMail(int id){
        String mail = null;
        if (id != 0) {
            ContentResolver cr = mContext.getContentResolver();
            Cursor emailCur = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{String.valueOf(id)}, null);
            while (emailCur.moveToNext()) {
                mail = emailCur.getString(
                        emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            emailCur.close();
        }
        return mail;
    }

    /**
     * Get contact number bu contact name.
     * @param name contact name.
     * @param context application context.
     * @return
     */
    public static String getNumber(String name, Context context) {
        String number="";
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like '%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c != null && c.moveToFirst()) {
            number = c.getString(0);
            c.close();
        }
        if (number == null){
            number = "noNumber";
        }
        return number;
    }
}
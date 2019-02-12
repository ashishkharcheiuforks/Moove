package com.backdoor.moove;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.helper.Coloring;

import java.util.ArrayList;

public class ContactsListActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private String name = "";

    private EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(new Coloring(this).colorPrimaryDark());
        }
        setContentView(R.layout.contact_picker_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        toolbar.setTitle(R.string.contacts);

        Intent intent = getIntent();
        final ArrayList<String> contacts = intent.getStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY);

        searchField = findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ContactsListActivity.this.adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ListView contactsList = findViewById(R.id.contactsList);
        adapter = new ArrayAdapter<>(ContactsListActivity.this, android.R.layout.simple_list_item_1, contacts);
        contactsList.setAdapter(adapter);

        contactsList.setOnItemClickListener((parent, view, position, id) -> {
            if (position != -1) {
                name = (String) parent.getItemAtPosition(position);
                Cursor c = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                        new String[]{name}, null);

                int phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

                if (c.getCount() > 1) { // contact has multiple phone numbers
                    final CharSequence[] numbers = new CharSequence[c.getCount()];
                    int i = 0;
                    if (c.moveToFirst()) {
                        while (!c.isAfterLast()) { // for each phone number, add it to the numbers array
                            String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                    getResources(), c.getInt(phoneType), ""); // insert a type string in front of the number
                            String number = type + ": " + c.getString(phoneIdx);
                            numbers[i++] = number;
                            c.moveToNext();
                        }
                        // build and show a simple dialog that allows the user to select a number
                        AlertDialog.Builder builder = new AlertDialog.Builder(ContactsListActivity.this);
                        builder.setTitle(getString(R.string.phone_number));
                        builder.setItems(numbers, (dialog, item) -> {
                            String number = (String) numbers[item];
                            int index = number.indexOf(":");
                            number = number.substring(index + 2);
                            Intent intent1 = new Intent();
                            intent1.putExtra(Constants.SELECTED_CONTACT_NUMBER, number);
                            intent1.putExtra(Constants.SELECTED_CONTACT_NAME, name);
                            setResult(RESULT_OK, intent1);
                            finish();
                        });
                        AlertDialog alert = builder.create();
                        alert.setOwnerActivity(ContactsListActivity.this);
                        alert.show();

                    } else Log.w(Constants.LOG_TAG, "No results");
                } else if (c.getCount() == 1) {
                    if (c.moveToFirst()) {
                        String number = c.getString(phoneIdx);
                        Intent intent1 = new Intent();
                        intent1.putExtra(Constants.SELECTED_CONTACT_NUMBER, number);
                        intent1.putExtra(Constants.SELECTED_CONTACT_NAME, name);
                        setResult(RESULT_OK, intent1);
                        finish();
                    }
                } else if (c.getCount() == 0) {
                    Intent intent1 = new Intent();
                    intent1.putExtra(Constants.SELECTED_CONTACT_NAME, name);
                    setResult(RESULT_OK, intent1);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
        super.onPause();
    }
}

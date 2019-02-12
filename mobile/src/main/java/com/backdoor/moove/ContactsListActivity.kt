package com.backdoor.moove

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView

import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.helper.Coloring

import java.util.ArrayList

class ContactsListActivity : AppCompatActivity() {

    private var adapter: ArrayAdapter<String>? = null
    private var name = ""

    private var searchField: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Coloring(this).colorPrimaryDark()
        }
        setContentView(R.layout.contact_picker_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        }
        toolbar.setTitle(R.string.contacts)

        val intent = intent
        val contacts = intent.getStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY)

        searchField = findViewById(R.id.searchField)
        searchField!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                this@ContactsListActivity.adapter!!.filter.filter(s)
                adapter!!.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        val contactsList = findViewById<ListView>(R.id.contactsList)
        adapter = ArrayAdapter(this@ContactsListActivity, android.R.layout.simple_list_item_1, contacts)
        contactsList.adapter = adapter

        contactsList.setOnItemClickListener { parent, view, position, id ->
            if (position != -1) {
                name = parent.getItemAtPosition(position) as String
                val c = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                        arrayOf(name), null)

                val phoneIdx = c!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

                if (c.count > 1) { // contact has multiple phone numbers
                    val numbers = arrayOfNulls<CharSequence>(c.count)
                    var i = 0
                    if (c.moveToFirst()) {
                        while (!c.isAfterLast) { // for each phone number, add it to the numbers array
                            val type = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                    resources, c.getInt(phoneType), "") as String // insert a type string in front of the number
                            val number = type + ": " + c.getString(phoneIdx)
                            numbers[i++] = number
                            c.moveToNext()
                        }
                        // build and show a simple dialog that allows the user to select a number
                        val builder = AlertDialog.Builder(this@ContactsListActivity)
                        builder.setTitle(getString(R.string.phone_number))
                        builder.setItems(numbers) { dialog, item ->
                            var number = numbers[item] as String
                            val index = number.indexOf(":")
                            number = number.substring(index + 2)
                            val intent1 = Intent()
                            intent1.putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
                            intent1.putExtra(Constants.SELECTED_CONTACT_NAME, name)
                            setResult(Activity.RESULT_OK, intent1)
                            finish()
                        }
                        val alert = builder.create()
                        alert.ownerActivity = this@ContactsListActivity
                        alert.show()

                    } else
                        Log.w(Constants.LOG_TAG, "No results")
                } else if (c.count == 1) {
                    if (c.moveToFirst()) {
                        val number = c.getString(phoneIdx)
                        val intent1 = Intent()
                        intent1.putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
                        intent1.putExtra(Constants.SELECTED_CONTACT_NAME, name)
                        setResult(Activity.RESULT_OK, intent1)
                        finish()
                    }
                } else if (c.count == 0) {
                    val intent1 = Intent()
                    intent1.putExtra(Constants.SELECTED_CONTACT_NAME, name)
                    setResult(Activity.RESULT_OK, intent1)
                    finish()
                }
            }
        }
    }

    override fun onPause() {
        val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(searchField!!.windowToken, 0)
        super.onPause()
    }
}

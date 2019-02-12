package com.backdoor.moove

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem

import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.fragments.GeneralSettingsFragment
import com.backdoor.moove.core.fragments.LocationSettingsFragment
import com.backdoor.moove.core.fragments.NotificationSettingsFragment
import com.backdoor.moove.core.fragments.OtherSettingsFragment
import com.backdoor.moove.core.fragments.SettingsFragment
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.Dialogues
import com.backdoor.moove.core.helper.SharedPrefs

import java.io.File

/**
 * Custom setting activity.
 */
class SettingsActivity : AppCompatActivity(), SettingsFragment.OnHeadlineSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cSetter = Coloring(this@SettingsActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cSetter.colorPrimaryDark()
        }
        setContentView(R.layout.category_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setTitle(R.string.settings)
        }

        if (findViewById<View>(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return
            }
            val firstFragment = SettingsFragment()
            firstFragment.arguments = intent.extras
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, firstFragment).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Attach settings fragment.
     *
     * @param position list position.
     */
    override fun onArticleSelected(position: Int) {
        if (position == 0) {
            attachFragment(GeneralSettingsFragment())
        } else if (position == 1) {
            attachFragment(NotificationSettingsFragment())
        } else if (position == 2) {
            attachFragment(LocationSettingsFragment())
        } else if (position == 3) {
            attachFragment(OtherSettingsFragment())
        }
    }

    private fun attachFragment(fragment: Fragment) {
        val args = Bundle()
        fragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            103 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Dialogues.melodyType(this@SettingsActivity, Prefs.CUSTOM_SOUND, 201)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            201 -> if (resultCode == Activity.RESULT_OK) {
                val prefs = SharedPrefs.getInstance(this)
                if (prefs != null) {
                    prefs.saveBoolean(Prefs.CUSTOM_SOUND, true)
                    val pathStr = data!!.getStringExtra(Constants.FILE_PICKED)
                    if (pathStr != null) {
                        val fileC = File(pathStr)
                        if (fileC.exists()) {
                            prefs.savePrefs(Prefs.CUSTOM_SOUND_FILE, fileC.toString())
                        }
                    }
                }
            }
            Constants.ACTION_REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data!!.data
                val prefs = SharedPrefs.getInstance(this)
                if (prefs != null && selectedImage != null) {
                    prefs.savePrefs(Prefs.REMINDER_IMAGE, selectedImage.toString())
                }
            }
        }
    }
}
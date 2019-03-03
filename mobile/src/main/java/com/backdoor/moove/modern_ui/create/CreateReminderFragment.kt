package com.backdoor.moove.modern_ui.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.R
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.databinding.CreateReminderFragmentBinding
import com.backdoor.moove.modern_ui.map.MapFragment
import com.backdoor.moove.utils.*
import com.backdoor.moove.utils.contacts.SelectContactActivity
import com.backdoor.moove.utils.file_explorer.FileExplorerActivity
import com.backdoor.moove.views.ActionView
import com.backdoor.moove.views.DateTimeView
import com.google.android.gms.common.util.DeviceProperties.isTablet
import com.google.android.gms.maps.model.LatLng
import org.koin.android.ext.android.inject
import timber.log.Timber

class CreateReminderFragment : Fragment(), MapCallback {

    private val prefs: Prefs by inject()

    private lateinit var binding: CreateReminderFragmentBinding
    private lateinit var viewModel: CreateReminderViewModel
    private var mId = ""
    private var lastPos: LatLng? = null

    private var mMap: MapFragment? = null
    private val mListener = object : MapListener {
        override fun placeChanged(place: LatLng, address: String) {
            lastPos = place
        }

        override fun onBackClick() {
            if (binding.mapContainer.isVisible()) {
                ViewUtils.fadeOutAnimation(binding.mapContainer)
                ViewUtils.fadeInAnimation(binding.scrollView)
            }
        }
    }
    private val mBackHandler: OnBackPressedCallback = OnBackPressedCallback {
        mMap?.onBackPressed() == false
    }
    private val mReminderObserver: Observer<in Reminder> = Observer {
        if (it != null && !viewModel.isReminderEdited) {
            viewModel.reminder = it
            viewModel.original = it.copy()
            viewModel.isReminderEdited = true
            viewModel.isPaused = true
            viewModel.pauseReminder(it)
            binding.toolbar.menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
            binding.toolbar.title = getString(R.string.edit)
            editReminder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mId = CreateReminderFragmentArgs.fromBundle(it).argId
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = CreateReminderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, CreateReminderViewModel.Factory(mId)).get(CreateReminderViewModel::class.java)
        viewModel.loadedReminder.observe(this, mReminderObserver)

        editReminder()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActionBar()
        activity?.addOnBackPressedCallback(mBackHandler)

        binding.taskSummary.filters = arrayOf(InputFilter.LengthFilter(150))
        binding.taskSummary.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.reminder.summary = s?.toString()?.trim() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.dateView.onDateChangeListener = object : DateTimeView.OnDateChangeListener {
            override fun onChanged(mills: Long) {
                viewModel.reminder.delayTime = TimeUtils.getGmtFromDateTime(mills)
            }
        }

        binding.actionView.visibility = View.VISIBLE
        binding.actionView.setActivity(activity!!)
        binding.actionView.setContactClickListener(View.OnClickListener { selectContact() })
        binding.actionView.setListener(object : ActionView.OnActionListener {
            override fun onStateChanged(hasAction: Boolean, type: Int, phone: String) {
                if (hasAction) {
                    viewModel.reminder.phoneNumber = phone
                    viewModel.isMessage = type == ActionView.TYPE_MESSAGE
                }
            }
        })

        binding.loudnessView.onLevelUpdateListener = { viewModel.reminder.volume = it }
        binding.melodyView.onFileUpdateListener = { viewModel.reminder.melody = it }
        binding.melodyView.onFileSelectListener = { selectMelody() }
        binding.ledView.onLedChangeListener = { viewModel.reminder.ledColor = it }

        mMap = MapFragment.newInstance(isRadius = true)
        mMap?.setListener(mListener)
        mMap?.setCallback(this)

        childFragmentManager.beginTransaction()
                .replace(R.id.mapFrame, mMap!!)
                .commit()

        binding.mapContainer.visibility = View.GONE
        binding.mapButton.visibility = View.VISIBLE
        binding.searchBlock.visibility = View.VISIBLE

        binding.delayLayout.visibility = View.GONE
        binding.attackDelay.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isDelayAdded = isChecked
            if (isChecked) {
                binding.delayLayout.visibility = View.VISIBLE
            } else {
                binding.delayLayout.visibility = View.GONE
            }
        }

        binding.leaveCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isLeave = isChecked
        }

        binding.clearButton.setOnClickListener { binding.addressField.setText("") }
        binding.mapButton.setOnClickListener { toggleMap() }
        binding.addressField.setOnItemClickListener { _, _, position, _ ->
            val sel = binding.addressField.getAddress(position) ?: return@setOnItemClickListener
            val lat = sel.latitude
            val lon = sel.longitude
            val pos = LatLng(lat, lon)
            var title: String? = binding.taskSummary.text.toString().trim()
            if (title != null && title.matches("".toRegex())) title = pos.toString()
            mMap?.addMarker(pos, title, true, animate = true)
        }
    }

    private fun initActionBar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.inflateMenu(R.menu.save_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_add -> saveReminder()
                MENU_ITEM_DELETE -> deleteReminder()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun deleteReminder() {
        viewModel.loadedReminder.removeObserver(mReminderObserver)
        viewModel.isSaving = true
        viewModel.deleteReminder(viewModel.reminder)
        findNavController().popBackStack()
    }

    private fun saveReminder() {
        val reminder = prepare() ?: return
        Timber.d("saveReminder: $reminder")
        viewModel.loadedReminder.removeObserver(mReminderObserver)
        viewModel.isSaving = true
        viewModel.saveAndStart(reminder, prefs.autoPlace)
        findNavController().popBackStack()
    }

    private fun showPlaceOnMap() {
        val reminder = viewModel.reminder
        val text = reminder.summary
        if (reminder.hasPlace()) {
            val latitude = reminder.latitude
            val longitude = reminder.longitude
            mMap?.let {
                mMap?.markerRadius = reminder.radius
                lastPos = LatLng(latitude, longitude)
                mMap?.addMarker(lastPos, text, true, animate = true)
                toggleMap()
            }
        }
    }

    private fun prepare(): Reminder? {
        if (!Permissions.ensureForeground(activity!!, 112)) {
            return null
        }
        if (!SuperUtil.checkLocationEnable(context!!)) {
            Toast.makeText(context, getString(R.string.gps_is_not_enabled), Toast.LENGTH_SHORT).show()
            return null
        }
        val reminder = viewModel.reminder
        val map = mMap ?: return null
        var type = if (binding.enterCheck.isChecked) ReminderUtils.TYPE_LOCATION else ReminderUtils.TYPE_LOCATION_OUT
        val pos = lastPos
        if (pos == null) {
            Toast.makeText(context, getString(R.string.no_place_selected), Toast.LENGTH_SHORT).show()
            return null
        }
        val summary = binding.taskSummary.text.toString().trim()
        if (TextUtils.isEmpty(summary)) {
            binding.taskLayout.error = getString(R.string.empty_field)
            binding.taskLayout.isErrorEnabled = true
            map.invokeBack()
            return null
        }
        var number = ""
        if (binding.actionView.hasAction()) {
            number = binding.actionView.number
            if (TextUtils.isEmpty(number)) {
                Toast.makeText(context, getString(R.string.empty_field), Toast.LENGTH_SHORT).show()
                return null
            }
            type = if (binding.actionView.type == ActionView.TYPE_CALL) {
                if (binding.enterCheck.isChecked) ReminderUtils.TYPE_LOCATION_CALL else ReminderUtils.TYPE_LOCATION_OUT_CALL
            } else {
                if (binding.enterCheck.isChecked) ReminderUtils.TYPE_LOCATION_MESSAGE else ReminderUtils.TYPE_LOCATION_OUT_MESSAGE
            }
        }
        reminder.summary = summary
        reminder.phoneNumber = number
        reminder.latitude = pos.latitude
        reminder.longitude = pos.longitude
        reminder.radius = map.markerRadius
        reminder.markerColor = map.markerStyle
        reminder.type = type
        reminder.isActive = true
        reminder.isRemoved = false
        reminder.hasDelay = binding.attackDelay.isChecked
        if (binding.attackDelay.isChecked) {
            val startTime = binding.dateView.dateTime
            reminder.delayTime = TimeUtils.getGmtFromDateTime(startTime)
            Timber.d("EVENT_TIME %s", TimeUtils.getFullDateTime(startTime, true))
        } else {
            reminder.delayTime = ""
        }
        return reminder
    }

    private fun toggleMap() {
        if (binding.mapContainer.isVisible()) {
            ViewUtils.fadeOutAnimation(binding.mapContainer)
            ViewUtils.fadeInAnimation(binding.scrollView)
        } else {
            ViewUtils.fadeOutAnimation(binding.scrollView)
            ViewUtils.fadeInAnimation(binding.mapContainer)
        }
    }

    private fun editReminder() {
        val reminder = viewModel.reminder
        Timber.d("editReminder: %s", reminder)
        if (reminder.delayTime != "") {
            binding.dateView.setDateTime(reminder.delayTime)
        }
        binding.attackDelay.isChecked = viewModel.isDelayAdded
        if (viewModel.isLeave && reminder.type.startsWith(ReminderUtils.TYPE_LOCATION_OUT)) {
            binding.leaveCheck.isChecked = true
        } else {
            binding.enterCheck.isChecked = true
        }
        binding.dateView.setDateTime(reminder.delayTime)
        val phone = reminder.phoneNumber
        if (phone != "") {
            binding.actionView.setAction(true)
            if (reminder.type.contains(ReminderUtils.TYPE_CALL)) {
                binding.actionView.type = ActionView.TYPE_CALL
            } else if (reminder.type.contains(ReminderUtils.TYPE_MESSAGE)) {
                binding.actionView.type = ActionView.TYPE_MESSAGE
            }
            Timber.d("editReminder: $phone")
            binding.actionView.number = phone
        }
        binding.loudnessView.setVolume(reminder.volume)
        binding.melodyView.file = reminder.melody
        binding.ledView.led = reminder.ledColor
        binding.taskSummary.setText(reminder.summary)

        showPlaceOnMap()
    }

    override fun onMapReady() {
        showPlaceOnMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.removeOnBackPressedCallback(mBackHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewUtils.hideKeyboard(activity)
        if (viewModel.isReminderEdited && viewModel.isPaused && !viewModel.isSaving) {
            viewModel.original?.let { viewModel.resumeReminder(it) }
        }
    }

    private fun selectMelody() {
        if (Permissions.ensurePermissions(activity!!,330, Permissions.READ_EXTERNAL)) {
            startActivityForResult(Intent(activity!!, FileExplorerActivity::class.java),
                    SELECT_MELODY)
        }
    }

    private fun selectContact() {
        if (Permissions.ensurePermissions(activity!!, CONTACTS, Permissions.READ_CONTACTS)) {
            startActivityForResult(Intent(activity!!, SelectContactActivity::class.java),
                    SELECT_CONTACT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_MELODY && resultCode == Activity.RESULT_OK) {
            val melodyPath = data?.getStringExtra(Module.FILE_PICKED) ?: ""
            binding.melodyView.file = melodyPath
        } else if (requestCode == SELECT_CONTACT && resultCode == Activity.RESULT_OK) {
            val phoneNumber = data?.getStringExtra(Module.CONTACT_SELECTED) ?: ""
            binding.actionView.number = phoneNumber
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        binding.actionView.onRequestPermissionsResult(requestCode, grantResults)
        when (requestCode) {
            330 -> if (Permissions.isAllGranted(grantResults)) {
               selectMelody()
            }
            CONTACTS -> if (Permissions.isAllGranted(grantResults)) {
                selectContact()
            }
        }
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
        private const val SELECT_MELODY = 1425
        private const val SELECT_CONTACT = 1426
        private const val CONTACTS = 112
    }
}

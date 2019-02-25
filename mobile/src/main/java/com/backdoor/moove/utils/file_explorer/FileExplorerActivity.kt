package com.backdoor.moove.utils.file_explorer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.backdoor.moove.R
import com.backdoor.moove.databinding.ActivityFileExplorerBinding
import com.backdoor.moove.utils.*
import com.backdoor.moove.utils.filter.SearchModifier
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

/**
 * Copyright 2016 Nazar Suhovich
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
class FileExplorerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileExplorerBinding
    private lateinit var viewModel: SelectFileViewModel
    private var mFileName: String = ""
    private var mFilePath: String = ""

    val prefs: Prefs by inject()

    private val mAdapter: FileRecyclerAdapter = FileRecyclerAdapter()
    private val searchModifier = object : SearchModifier<FileItem>(null, {
        mAdapter.submitList(it)
        showList(it.size)
    }) {
        override fun filter(v: FileItem): Boolean {
            return searchValue.isEmpty() || v.fileName.toLowerCase().contains(searchValue.toLowerCase())
        }
    }
    private var mSound: Sound? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_explorer)
        viewModel = ViewModelProviders.of(this).get(SelectFileViewModel::class.java)
        viewModel.files.observe(this, androidx.lifecycle.Observer { files ->
            files?.let {
                searchModifier.original = it
            }
        })
        viewModel.isLoading.observe(this, androidx.lifecycle.Observer { isLoading ->
            isLoading?.let {
                if (it) binding.loaderView.visibility = View.VISIBLE
                else binding.loaderView.visibility = View.GONE
            }
        })

        mSound = Sound(this, prefs)

        viewModel.filType = intent.getStringExtra(FILE_TYPE) ?: ""
        if (viewModel.filType == "") viewModel.filType = TYPE_MUSIC

        initRecyclerView()
        initPlayer()
        initSearch()
        initButtons()
    }

    override fun onStart() {
        super.onStart()
        if (Permissions.ensurePermissions(this, SD_CARD, Permissions.READ_EXTERNAL)) {
            viewModel.loadFileList()
        }
    }

    private fun selectFile(position: Int) {
        val item = mAdapter.getFileItem(position)
        Timber.d("selectFile: $item")
        mFileName = item.fileName
        mFilePath = item.filePath
        val sel = File(item.filePath)
        if (sel.isDirectory) {
            viewModel.str.add(mFileName)
            binding.searchField.setText("")
            viewModel.loadFileList(sel, false)
        } else if (item.isUp) {
            moveUp()
        } else {
            if (viewModel.filType.matches("any".toRegex())) {
                sendFile()
            } else {
                if (isMelody(mFileName)) {
                    play()
                } else {
                    Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun moveUp() {
        val s = viewModel.str.removeAt(viewModel.str.size - 1)
        viewModel.moveUp(s, viewModel.str.isEmpty())
    }

    private fun sendFile() {
        val intent = Intent()
        intent.putExtra(Module.FILE_PICKED, mFilePath)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun initPlayer() {
        binding.playerLayout.visibility = View.GONE
    }

    private fun initRecyclerView() {
        mAdapter.clickListener = object : ActionsListener<FileItem> {
            override fun onAction(view: View, position: Int, t: FileItem?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> selectFile(position)
                    else -> {
                    }
                }
            }
        }
        if (ViewUtils.isHorizontal(this)) {
            binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.isNestedScrollingEnabled = false
        ViewUtils.listenScrollableView(binding.scroller) {
            binding.toolbarView.isSelected = it > 0
        }
    }

    private fun initSearch() {
        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchModifier.setSearchValue(s.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun initButtons() {
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.saveButton.setOnClickListener { saveChoice() }
        binding.pauseButton.setOnClickListener { pause() }
        binding.stopButton.setOnClickListener { stop() }
        binding.playButton.setOnClickListener { play() }
    }

    private fun showList(count: Int) {
        if (count == 0) {
            Toast.makeText(this, getString(R.string.no_files), Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.smoothScrollToPosition(0)
        refreshView(count)
    }

    private fun play() {
        if (mSound?.isPlaying == false) {
            if (binding.playerLayout.visibility == View.GONE) {
                binding.playerLayout.visibility = View.VISIBLE
            }
            if (mSound?.isPaused == true && mSound?.isSameFile(mFilePath) == true) {
                mSound?.resume()
            } else {
                mSound?.play(mFilePath)
                binding.currentMelody.text = mFileName
            }
        } else {
            if (mSound?.isSameFile(mFilePath) == true) {
                return
            }
            mSound?.play(mFilePath)
            binding.currentMelody.text = mFileName
        }
    }

    private fun pause() {
        if (mSound?.isPlaying == true) {
            mSound?.pause()
        }
    }

    private fun stop() {
        if (mSound?.isPlaying == true) {
            mSound?.stop(true)
        }
        binding.playerLayout.visibility = View.GONE
    }

    private fun isMelody(file: String?): Boolean {
        return file != null && (file.endsWith(".mp3") || file.endsWith(".ogg")
                || file.endsWith(".m4a") || file.endsWith(".flac"))
    }

    private fun isImage(file: String?): Boolean {
        return file != null && (file.endsWith(".jpg") || file.endsWith(".jpeg")
                || file.endsWith(".png") || file.endsWith(".tiff"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                exit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if ((!viewModel.isRoot)) {
            moveUp()
        } else {
            exit()
        }
    }

    private fun exit() {
        if (isMelody(mFileName)) {
            stop()
        }
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun saveChoice() {
        if (isMelody(mFileName)) {
            stop()
            sendFile()
        } else {
            Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshView(count: Int) {
        if (count > 0) {
            binding.emptyItem.visibility = View.GONE
            binding.scroller.visibility = View.VISIBLE
        } else {
            binding.scroller.visibility = View.GONE
            binding.emptyItem.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewUtils.hideKeyboard(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SD_CARD -> if (Permissions.isAllGranted(grantResults)) {
                viewModel.loadFileList()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    companion object {
        private const val SD_CARD = 444
        const val TYPE_MUSIC = "music"
        const val FILE_TYPE = "arg_file_type"
    }
}

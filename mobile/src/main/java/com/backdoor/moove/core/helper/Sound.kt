package com.backdoor.moove.core.helper

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri

import java.io.File
import java.io.IOException

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
class Sound(private val mContext: Context) {
    private var mMediaPlayer: MediaPlayer? = null
    /**
     * Check if media player is paused.
     *
     * @return boolean
     */
    var isPaused: Boolean = false
        private set
    private var lastFile: String? = null

    /**
     * Check if media player is playing.
     *
     * @return boolean
     */
    val isPlaying: Boolean
        get() = mMediaPlayer != null && mMediaPlayer!!.isPlaying

    /**
     * Stop playing melody.
     */
    fun stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            isPaused = false
        }
    }

    /**
     * Pause playing melody.
     */
    fun pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.pause()
            isPaused = true
        }
    }

    /**
     * Resume playing melody.
     */
    fun resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.start()
            isPaused = false
        }
    }

    /**
     * Check if media player already play this file.
     *
     * @return boolean
     */
    fun isSameFile(path: String): Boolean {
        return lastFile != null && path.equals(lastFile!!, ignoreCase = true)
    }

    /**
     * Play melody file.
     *
     * @param path path to file.
     */
    fun play(path: String) {
        lastFile = path
        val file = File(path)
        val soundUri = Uri.fromFile(file)
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
        }
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(mContext, soundUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer!!.isLooping = false
        mMediaPlayer!!.setOnPreparedListener(OnPreparedListener { it.start() })
        try {
            mMediaPlayer!!.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    /**
     * Play melody for reminder.
     *
     * @param path    Uri path for melody file.
     * @param looping flag for media player looping.
     */
    fun playAlarm(path: Uri, looping: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
        }
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(mContext, path)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer!!.isLooping = looping
        mMediaPlayer!!.setOnPreparedListener(OnPreparedListener { it.start() })
        try {
            mMediaPlayer!!.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    /**
     * Play built-in reminder melody from assets.
     *
     * @param afd     file descriptor for built-in melody.
     * @param looping flag for media player looping.
     */
    fun playAlarm(afd: AssetFileDescriptor, looping: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
        }
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer!!.isLooping = looping
        mMediaPlayer!!.setOnPreparedListener(OnPreparedListener { it.start() })
        try {
            mMediaPlayer!!.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }
}

package com.backdoor.moove

import androidx.multidex.MultiDexApplication
import com.backdoor.moove.utils.EventJobService
import com.backdoor.moove.utils.Notifier
import com.backdoor.moove.utils.utilModule
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import timber.log.Timber

class Moove : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        val logger = object : Logger(level = Level.DEBUG) {
            override fun log(level: Level, msg: MESSAGE) {
            }
        }
        startKoin{
            logger(logger)
            androidContext(this@Moove)
            modules(listOf(utilModule()))
        }
        Notifier.createChannels(this)
        Fabric.with(this, Crashlytics())
        JobManager.create(this).addJobCreator { EventJobService() }
    }
}

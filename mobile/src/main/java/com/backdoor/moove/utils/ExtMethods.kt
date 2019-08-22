package com.backdoor.moove.utils

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import com.backdoor.moove.data.RoomDb
import kotlinx.coroutines.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File
import java.io.InputStream
import java.util.*

fun <ViewT : View> View.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnSynchronized {
        findViewById<ViewT>(idRes)
    }
}

fun <ViewT : View> Activity.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnSynchronized {
        findViewById<ViewT>(idRes)
    }
}

fun <T> lazyUnSynchronized(initializer: () -> T): Lazy<T> =
        lazy(LazyThreadSafetyMode.NONE, initializer)

suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T)
        : T = withContext(Dispatchers.Main, block)

fun launchDefault(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit)
        : Job = GlobalScope.launch(Dispatchers.Default, start, block)

fun launchIo(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit)
        : Job = GlobalScope.launch(Dispatchers.IO, start, block)

fun EditText.onChanged(function: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            function.invoke(s.toString().trim())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
        }
    })
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isNotVisible(): Boolean = visibility == View.INVISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
}

fun Calendar.sameDayAs(calendar: Calendar): Boolean {
    val d = this.get(Calendar.DAY_OF_MONTH)
    val m = this.get(Calendar.MONTH)
    val y = this.get(Calendar.YEAR)
    val d1 = calendar.get(Calendar.DAY_OF_MONTH)
    val m1 = calendar.get(Calendar.MONTH)
    val y1 = calendar.get(Calendar.YEAR)
    return d == d1 && m == m1 && y == y1
}

fun Date.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

fun utilModule() = module {
    single { RoomDb.getInMemoryDatabase(androidContext()) }
    single { Prefs(androidContext()) }
    single { SoundStackHolder(androidContext(), get()) }
    single { Coloring(androidContext()) }
    single { LocationEvent(androidContext()) }
    single { Dialogues(get()) }
    single { Language(get()) }
}
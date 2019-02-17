package com.backdoor.moove.views.binding

import android.view.View
import androidx.annotation.IdRes
import com.backdoor.moove.utils.lazyUnSynchronized

open class Binding(val view: View) {

    fun <ViewT : View> bindView(@IdRes idRes: Int): Lazy<ViewT> {
        return lazyUnSynchronized {
            view.findViewById<ViewT>(idRes)
        }
    }
}
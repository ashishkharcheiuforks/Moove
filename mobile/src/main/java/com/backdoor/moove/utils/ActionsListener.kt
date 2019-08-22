package com.backdoor.moove.utils

import android.view.View

interface ActionsListener<T> {
    fun onAction(view: View, position: Int, t: T?, actions: ListActions)
}

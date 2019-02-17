package com.backdoor.moove.modern_ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateReminderViewModel(val uuId: String) : ViewModel() {


    class Factory(val id: String) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CreateReminderViewModel(id) as T
        }
    }
}

package com.example.bluetoothtestapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
  val isEnabled = MutableLiveData<Boolean>().apply {
    value = false
  }

  val count = MutableLiveData<Int>().apply {
    value = 0
  }

  fun updateCount(value: Int) {
    count.value = value
  }

  fun increaseCount() {
    count.value?.let {
      count.value = it.plus(1)
    }
  }

  fun updateStateBluetooth(_isEnabled: Boolean) {
    isEnabled.value = _isEnabled
  }
}
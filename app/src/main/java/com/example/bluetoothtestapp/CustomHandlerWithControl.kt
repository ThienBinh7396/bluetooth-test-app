package com.example.bluetoothtestapp

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.*

class CustomHandlerWithControl(looper: Looper): Handler(looper) {
  private var statckMessage = Stack<Message>()
  private var isPaused = true

  @Synchronized
  fun start(){
    isPaused = false
  }

  @Synchronized
  fun pause() {
    isPaused = true
  }

  @Synchronized
  fun resume() {
    isPaused = false
    while (!statckMessage.empty()) {
      sendMessageAtFrontOfQueue(statckMessage.pop())
    }
  }

  override fun dispatchMessage(msg: Message) {
    if (isPaused) {
      statckMessage.push(Message.obtain(msg))
      return
    } else {
      super.dispatchMessage(msg)
    }
  }

}
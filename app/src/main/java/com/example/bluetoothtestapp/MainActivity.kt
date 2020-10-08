package com.example.bluetoothtestapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bluetoothtestapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

  val handler = CustomHandlerWithControl(Looper.getMainLooper())

  var runnable: Runnable? = null

  var devices = mutableListOf<BluetoothDevice>()
    set(value) {
      field = value

      Log.d("Binh", "Devices: ${devices.size}")
    }


  private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

  private val mainActivityViewModel = MainActivityViewModel()

  private val REQUEST_ENABLE_BT = 101

  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(_context: Context?, _intent: Intent?) {
      _context?.apply {
        if (_intent == null) return@apply

        when (_intent.action) {
          BluetoothDevice.ACTION_FOUND -> {
            val device = _intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

            device?.let {
              Log.d(
                "Binh",
                "Device: ${it.name} ${it.address} ${it.bondState} ${BluetoothDevice.BOND_BONDED}"
              )
              devices.add(it)
            }
          }

          BluetoothAdapter.ACTION_STATE_CHANGED -> {
            val state = _intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

            Log.d("Binh", "State: $state")

            when (state) {
              BluetoothAdapter.STATE_OFF -> mainActivityViewModel.updateStateBluetooth(false)
              BluetoothAdapter.STATE_TURNING_OFF -> mainActivityViewModel.updateStateBluetooth(false)
              BluetoothAdapter.STATE_ON -> mainActivityViewModel.updateStateBluetooth(true)
              BluetoothAdapter.STATE_TURNING_ON -> mainActivityViewModel.updateStateBluetooth(true)
            }
          }
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (runnable == null) {
      runnable = Runnable {
        mainActivityViewModel.increaseCount()

        Log.d("Binh", "Counting: ${mainActivityViewModel.count.value}")

        runnable?.let {
          handler.postDelayed(it, 1000)
        }
      }
    }

    DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
      mMainActivityViewModel = mainActivityViewModel

      lifecycleOwner = this@MainActivity

      btnStartCount.setOnClickListener {
        Log.d("Binh", "Start count $runnable")

        runnable?.let {
          handler.postDelayed(it, 1000)
          handler.start()
        }
      }

      btnClearCount.setOnClickListener {
        Log.d("Binh", "Clear count $runnable")

        runnable?.let {
          handler.removeCallbacks(it)
        }
        mainActivityViewModel.updateCount(0)
      }

      btnToggleState.setOnClickListener {
        bluetoothAdapter ?: return@setOnClickListener

        if (bluetoothAdapter.isEnabled) {
          bluetoothAdapter.disable()
        } else {
          bluetoothAdapter.enable()
        }
//        val intent =
//          Intent(if (bluetoothAdapter.isEnabled) BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE else BluetoothAdapter.ACTION_REQUEST_ENABLE)
//
//        startActivityForResult(intent, REQUEST_ENABLE_BT)

      }

      btnScanDevice.setOnClickListener {
        if (bluetoothAdapter == null) {
          Snackbar.make(container, "Device doesn't support Bluetooth!", Snackbar.LENGTH_SHORT)
            .show()
        }

        if (!bluetoothAdapter.isEnabled) {
          Snackbar.make(container, "Turn on Bluetooth to start scan device!", Snackbar.LENGTH_SHORT)
            .show()
        }

        if (bluetoothAdapter.isDiscovering) {
          bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery()
      }

      lifecycleOwner = this@MainActivity
    }
  }

  override fun onPause() {
    super.onPause()

    Log.d("Binh", "On pause")

    handler.pause()
  }

  override fun onResume() {
    super.onResume()

    Log.d("Binh", "On resume")

    handler.resume()
  }

  override fun onStart() {
    super.onStart()

    if (bluetoothAdapter == null) {
      Log.d("Binh", "Device doesn't support Bluetooth.")
    } else {
      Log.d("Binh", "Device support Bluetooth.")
    }

    mainActivityViewModel.updateStateBluetooth(bluetoothAdapter?.isEnabled ?: false)

    val filter = IntentFilter()

    filter.addAction(BluetoothDevice.ACTION_FOUND)
    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)

    registerReceiver(receiver, filter)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    when (requestCode) {
      REQUEST_ENABLE_BT -> {
        if (resultCode == 1 && bluetoothAdapter?.isEnabled == true) {
          bluetoothAdapter.disable()
        }
      }
    }

    Log.d("Binh", "Request: $requestCode $resultCode")

  }

  override fun onDestroy() {
    super.onDestroy()

    unregisterReceiver(receiver)
  }
}
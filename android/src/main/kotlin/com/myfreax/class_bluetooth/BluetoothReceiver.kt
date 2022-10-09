package com.myfreax.class_bluetooth

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.KITKAT)
class BluetoothReceiver(private val listener: Listener) :
  BroadcastReceiver() {
  companion object {
    private const val TAG = "BluetoothReceiver"
  }

  override fun onReceive(context: Context?, intent: Intent) {
    when (intent.action) {
      BluetoothAdapter.ACTION_STATE_CHANGED -> {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
        Log.d(TAG, "STATE: $state")
        listener.bluetoothStateChanged(state)
      }
      BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
        listener.scanStarted()
      }
      BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
        listener.scanFinished()
      }
      BluetoothDevice.ACTION_FOUND -> {
        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        if (device != null) {
          Log.d(ClassBluetoothPlugin.TAG, device.name ?: device.address)
          listener.scanning(device)
        }
      }
      BluetoothDevice.ACTION_PAIRING_REQUEST -> {
        listener.paringRequest()
      }
      BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
        val state  =intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0)
        Log.i(TAG, "BOND_STATE: $state")
        listener.pairFinished(state)
      }
      BluetoothDevice.ACTION_ACL_CONNECTED -> {
        ///val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        Log.d(TAG,"ACL CONNECTED");
        //listener.deviceConnected(device)
      }
      BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
        //val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        Log.d(TAG,"ACL DISCONNECTED");
        //listener.deviceDisconnected(device)
      }
      BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> Log.i(
        TAG,
        "CONN_STATE: " + intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0)
      )
      BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> Log.i(
        TAG,
        "CONN_STATE: " + intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0)
      )
      BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> Log.i(
        TAG,
        "CONN_STATE: " + intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, 0)
      )
    }
  }

  fun createInterFilter(): IntentFilter {
    val filter = IntentFilter()
    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    filter.addAction(BluetoothDevice.ACTION_FOUND)
    filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
    filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
    filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
    filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
    return filter
  }

  interface Listener {
    fun bluetoothStateChanged(state: Int)
    fun scanStarted()
    fun scanFinished()
    fun scanning(device: BluetoothDevice)
    fun paringRequest()
    fun pairFinished(state: Int)
  }
}
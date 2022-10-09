package com.myfreax.class_bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class BluetoothClient(private val bluetoothAdapter: BluetoothAdapter) :
  CoroutineScope {
  companion object {
    private const val MESSAGE = 0
    private const val FILE = 1
    const val TAG = "BluetoothClient"
  }

  var onMessage: (message: String) -> Unit = {}
  var onDisConnected: (device: BluetoothDevice) -> Unit = {}
  var onConnected: (device: BluetoothDevice) -> Unit = {}
  var onConnectError: (device: BluetoothDevice, errorCode: Int, errorMessage: String) -> Unit =
    { _: BluetoothDevice, _: Int, _: String -> }
  private var outputStream: DataOutputStream? = null
  private var inputStream: DataInputStream? = null
  private var bluetoothSocket: BluetoothSocket? = null
  private var bluetoothDevice:BluetoothDevice? = null
  private var job: Job = Job()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  fun close() {
    Log.d(TAG, "cancel")
    bluetoothSocket?.close()
  }

  fun sendFile(data: ByteArray, filename: String) {
    outputStream?.writeInt(FILE)
    outputStream?.writeUTF(filename)
    outputStream?.writeLong(data.size.toLong())
    outputStream?.write(data, 0, data.size)
    outputStream?.flush()
  }

  fun sendMessage(message: String) {
    outputStream?.writeInt(MESSAGE)
    outputStream?.writeUTF(message)
    outputStream?.flush()
  }


  fun connect(device: BluetoothDevice) = launch {
    bluetoothAdapter.cancelDiscovery()
    try {
      if (device.address != bluetoothDevice?.address){
        bluetoothDevice = device
        bluetoothSocket =
          device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
      }
      if (bluetoothSocket?.isConnected == false) {
        bluetoothSocket?.connect()
      }else{
        Log.d(TAG, "${device.address} BluetoothDevice connected")
        onConnected(device)
        return@launch
      }
      outputStream = DataOutputStream(bluetoothSocket?.outputStream)
      inputStream = DataInputStream(bluetoothSocket?.inputStream)
      onConnected(device)
      Log.d(TAG, "${device.address} BluetoothDevice connected")
      while (true) {
        when (inputStream!!.readInt()) {
          MESSAGE -> {
            val message: String = inputStream!!.readUTF()
            Log.d(TAG, message)
            onMessage(message)
          }
        }
      }
    } catch (e: IOException) {
      Log.d(TAG, e.message.toString())
      outputStream?.close()
      inputStream?.close()
      if (e.message?.startsWith("bt socket closed") == true) {
        bluetoothDevice = null
        onDisConnected(device)
      } else {
        onConnectError(device, e.hashCode(), e.message ?: "")
      }

    }
  }
}
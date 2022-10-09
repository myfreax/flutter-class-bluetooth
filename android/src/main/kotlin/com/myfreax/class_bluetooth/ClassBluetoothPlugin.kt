package com.myfreax.class_bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

data class BluetoothDeviceInformation(val address: String, val name: String?)

/** ClassBluetoothPlugin */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class ClassBluetoothPlugin : FlutterPlugin, MethodCallHandler, BluetoothReceiver.Listener {

  companion object {
    const val TAG = "ClassBluetoothPlugin"
  }

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel: MethodChannel
  private lateinit var appContext: Context
  private lateinit var pluginEventChannel: EventChannel
  private var pluginEventSink: EventChannel.EventSink? = null
  private val bluetoothClient by lazy {
    BluetoothClient(bluetoothAdapter)
  }
  private val bluetoothReceiver by lazy {
    BluetoothReceiver(this)
  }
  private val devices by lazy {
    mutableMapOf<String, BluetoothDevice>()
  }
  private val json by lazy {
    Gson()
  }
  private val bluetoothAdapter by lazy {
    val bluetoothManager: BluetoothManager =
      appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    bluetoothManager.adapter
  }

  private val uiThreadHandler by lazy {
    Handler(Looper.getMainLooper())
  }


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "class_bluetooth")
    channel.setMethodCallHandler(this)
    appContext = flutterPluginBinding.applicationContext
    appContext.registerReceiver(bluetoothReceiver, bluetoothReceiver.createInterFilter())

    pluginEventChannel =
      EventChannel(flutterPluginBinding.binaryMessenger, "BluetoothPluginEventChannel")
    pluginEventChannel.setStreamHandler(object : EventChannel.StreamHandler {
      override fun onCancel(arguments: Any?) {
        pluginEventSink = null
      }

      override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        pluginEventSink = events
      }
    })

    bluetoothClient.onMessage = {
      sendMessage("message", it)
    }

    bluetoothClient.onConnected = {
      deviceConnected(it)
    }

    bluetoothClient.onDisConnected = {
      deviceDisconnected(it)
    }

    bluetoothClient.onConnectError = { bluetoothDevice: BluetoothDevice, i: Int, s: String ->
      deviceConnectError(bluetoothDevice, i, s)
    }

  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    val args = call.arguments as HashMap<*, *>
    when (call.method) {
      "status" -> result.success(bluetoothAdapter.isEnabled)
      "disable" -> disable(result)
      "enable" -> enable(result)
      "sendMessage" -> sendMessage(args["message"] as String)
      "sendFile" -> sendFile(args["data"] as ByteArray, args["fileName"] as String)
      "startScan" -> startScan(result)
      "connect" -> connect(args["address"] as String, result)
      "cancelScan" -> cancelScan(result)
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    appContext.unregisterReceiver(bluetoothReceiver)
    channel.setMethodCallHandler(null)
  }


  private fun enable(result: Result) {
    val isEnabled = bluetoothAdapter.isEnabled
    if (!isEnabled) {
      bluetoothAdapter.enable()
      result.success(true)
    }
  }

  private fun disable(result: Result) {
    val isEnabled = bluetoothAdapter.isEnabled
    if (isEnabled) {
      bluetoothAdapter.disable()
      result.success(true)
    }
  }

  private fun sendMessage(eventName: String, message: String) {
    val hash = hashMapOf<String, Any>()
    hash["message"] = message
    success(eventName, hash)
  }

  private fun sendMessage(message: String) {
    bluetoothClient.sendMessage(message)
  }

  private fun sendFile(data: ByteArray, filename: String) {
    bluetoothClient.sendFile(data, filename)
  }

  private fun success(
    eventName: String,
    data: HashMap<String, Any>,
  ) {
    data["eventName"] = eventName
    if (pluginEventSink == null){
      Log.d(TAG,"pluginEventSink Is Null")
    }
    if (pluginEventChannel == null){
      Log.d(TAG,"pluginEventChannel Is Null")
    }
    uiThreadHandler.post {
      pluginEventSink?.success(json.toJson(data))
    }
  }

  private fun startScan(result: Result) {
    bluetoothAdapter.startDiscovery()
    result.success(true)
  }

  private fun cancelScan(result: Result) {
    bluetoothAdapter.cancelDiscovery()
    result.success(true)
  }

  private fun connect(address: String, result: Result) {
    val device = devices[address]
    return if (devices.isNotEmpty() && device != null) {
      bluetoothClient.connect(device)
      val deviceInformation = BluetoothDeviceInformation(device.address, device.name)
      result.success(json.toJson(deviceInformation))
    } else {
      result.success(null)
    }
  }

  override fun bluetoothStateChanged(state: Int) {
    val hash = hashMapOf<String, Any>()
    hash["state"] = state
    success("bluetoothStateChanged", hash)
  }

  override fun scanStarted() {
    success("scanStarted", hashMapOf())
  }

  override fun scanFinished() {
    val hash = hashMapOf<String, Any>()
    hash["bluetoothDevices"] = devices
    success("scanFinished", hash)
  }

  override fun scanning(device: BluetoothDevice) {
    devices[device.address] = device
    val hash = hashMapOf<String, Any>()
    hash["bluetoothDevice"] =
      BluetoothDeviceInformation(device.address, device.name)
    success("scanning", hash)
  }

  override fun paringRequest() {
    success("paringRequest", hashMapOf())
  }

  override fun pairFinished(state: Int) {
    val hash = hashMapOf<String, Any>()
    hash["state"] = state
    success("pairFinished", hash)
  }

  private fun deviceConnected(device: BluetoothDevice) {
    val hash = hashMapOf<String, Any>()
    hash["bluetoothDevice"] =
      BluetoothDeviceInformation(device.address, device.name)
    success("deviceConnected", hash)
  }

  private fun deviceDisconnected(device: BluetoothDevice) {
    val hash = hashMapOf<String, Any>()
    hash["bluetoothDevice"] =
      BluetoothDeviceInformation(device.address, device.name)
    success("deviceDisconnected", hash)
  }

  private fun deviceConnectError(device: BluetoothDevice, errorCode: Int, errorMessage: String) {
    val hash = hashMapOf<String, Any>()
    hash["code"] = errorCode
    hash["message"] = errorMessage
    hash["bluetoothDevice"] =
      BluetoothDeviceInformation(device.address, device.name)
    success("onConnectError", hash)
  }
}

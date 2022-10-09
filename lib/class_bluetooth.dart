import 'dart:async';
import 'dart:convert';
import 'dart:ffi';
import 'package:class_bluetooth/bluetooth_device.dart';
import 'package:flutter/services.dart';

class ClassBluetooth {
  ClassBluetooth._internal_();

  static Future<bool> get status async {
    bool isnEable = await await _channel.invokeMethod("status", {});
    return isnEable;
  }

  static Function() onScanStarted = (() => Void);
  static Function() onScanFinished = (() => Void);
  static Function(int state) onStateChanged = ((state) => Void);
  static Function(String message) onMessage = ((message) => Void);
  static Function(int code, String message) onConnectError =
      ((code, message) => Void);
  static Function() onDisconnected = (() => Void);
  static Function() onConnected = (() => Void);
  static Function(int state) onPairFinished = ((int state) => Void);
  static Function() onParingRequest = (() => Void);
  static Function(BluetoothDevice bluetoothDevice) onScanning =
      ((BluetoothDevice bluetoothDevice) => Void);
  static const MethodChannel _channel = MethodChannel('class_bluetooth');
  static const EventChannel _pluginEventChannel =
      EventChannel('BluetoothPluginEventChannel');

  factory ClassBluetooth() {
    _pluginEventChannel.receiveBroadcastStream().map((event) {
      Map<String, dynamic> map = jsonDecode(event);
      return map;
    }).listen((event) {
      String eventName = event["eventName"];
      switch (eventName) {
        case "onConnectError":
          onConnectError(event["code"], event["message"]);
          break;
        case "message":
          onMessage(event["message"]);
          break;
        case "deviceDisconnected":
          onDisconnected();
          break;
        case "deviceConnected":
          onConnected();
          break;
        case "pairFinished":
          onPairFinished(event["state"]);
          break;
        case "paringRequest":
          onParingRequest();
          break;
        case "scanFinished":
          onScanFinished();
          break;
        case "scanStarted":
          onScanStarted();
          break;
        case "scanning":
          event["bluetoothDevice"]["channel"] = _channel;
          BluetoothDevice device =
              BluetoothDevice.fromJson(event["bluetoothDevice"]);
          onScanning(device);
          break;
        case "bluetoothStateChanged":
          onStateChanged(event["state"]);
          break;
        default:
          throw Exception('Unkown Event Name $eventName');
      }
    });
    return ClassBluetooth._internal_();
  }

  Future<bool> disable() async {
    return await _channel.invokeMethod("disable", {});
  }

  Future<bool> enable() async {
    return await _channel.invokeMethod("enable", {});
  }

  Future startScan() async {
    await _channel.invokeMethod('startScan', {});
  }

  Future cancelScan() async {
    await _channel.invokeMethod('cancelScan', {});
  }
}

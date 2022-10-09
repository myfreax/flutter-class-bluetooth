import 'dart:typed_data';

import 'package:flutter/services.dart';

class BluetoothDevice {
  final String _address;
  final String _name;
  String get name => _name;
  String get address => _address;
  final MethodChannel _channel;
  BluetoothDevice(this._address, this._name, this._channel);

  BluetoothDevice.fromJson(Map<String, dynamic> map)
      : _name = map["name"] ?? "",
        _channel = map["channel"],
        _address = map["address"];

  connect() async {
    _channel.invokeMethod("connect", {"address": address});
  }

  Future<void> sendFile(String fileName, Uint8List data) async {
    _channel.invokeMethod("sendFile", {"fileName": fileName, "data": data});
  }

  Future<void> sendMessage(String message) async {
    _channel.invokeMethod("sendMessage", {"message": message});
  }
}

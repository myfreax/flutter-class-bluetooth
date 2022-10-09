import 'dart:convert';
import 'dart:typed_data';

import 'package:class_bluetooth/bluetooth_device.dart';
import 'package:class_bluetooth/class_bluetooth.dart';
import 'package:flutter/material.dart';

class Bluetooth extends StatefulWidget {
  final BluetoothDevice _bluetoothDevice;
  const Bluetooth(this._bluetoothDevice, {Key? key}) : super(key: key);

  @override
  // ignore: no_logic_in_create_state
  State<Bluetooth> createState() => _Bluetooth(_bluetoothDevice);
}

class _Bluetooth extends State<Bluetooth> {
  final BluetoothDevice _bluetoothDevice;
  List<String> messages = [];

  _Bluetooth(this._bluetoothDevice);

  @override
  void initState() {
    super.initState();
    ClassBluetooth.onMessage = (message) {
      messages.add(message);
      setState(() {});
    };
    ClassBluetooth.onConnected = () => print("onConnected");
    ClassBluetooth.onDisconnected = () => print("onDisconnected");
    ClassBluetooth.onParingRequest = () => print("onParingRequest");
    ClassBluetooth.onPairFinished = (state) => print(state);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Device"),
        ),
        body: Container(
            margin: const EdgeInsets.all(20),
            child: Stack(
              children: [
                Align(
                  child: ListView.builder(
                      itemCount: messages.length,
                      itemBuilder: ((context, index) {
                        String message = messages[index];
                        return ListTile(
                          title: Text(message),
                        );
                      })),
                ),
                Align(
                    alignment: Alignment.bottomLeft,
                    child: TextButton(
                      child: const Text("send file"),
                      onPressed: () {
                        List<int> binaryMessage = utf8.encode('hello world');
                        Uint8List data = Uint8List.fromList(binaryMessage);
                        _bluetoothDevice.sendFile("init.py", data);
                      },
                    )),
                Align(
                    alignment: Alignment.bottomRight,
                    child: TextButton(
                      child: const Text("#import os"),
                      onPressed: () {
                        _bluetoothDevice
                            .sendMessage("import os\r\nprint(1)\r\n");
                      },
                    ))
              ],
            )));
  }
}

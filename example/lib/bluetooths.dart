import 'package:class_bluetooth/bluetooth_device.dart';
import 'package:class_bluetooth/class_bluetooth.dart';
import 'package:class_bluetooth_example/bluetooth.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

class Bluetooths extends StatefulWidget {
  const Bluetooths({Key? key}) : super(key: key);

  @override
  State<Bluetooths> createState() => _Bluetooths();
}

class _Bluetooths extends State<Bluetooths> {
  final _currentPageNotifier = ValueNotifier<int>(0);
  List<BluetoothDevice> devices = [];
  ClassBluetooth classBluetooth = ClassBluetooth();
  BluetoothDevice? bluetoothDevice;
  bool isEnabled = false;

  @override
  void initState() {
    super.initState();
    ClassBluetooth.status.then((value) {
      isEnabled = value;
      setState(() {});
    });
    //ClassBluetooth.onMessage = (message) => print(message);
    //ClassBluetooth.onConnectError = (code, message) => print("$code $message");

    ClassBluetooth.onStateChanged = (state) => print("onStateChanged: $state");
    ClassBluetooth.onScanFinished = () => print("onScanFinished");
    ClassBluetooth.onScanStarted = () => print("onScanStarted");
    ClassBluetooth.onScanning = (bluetoothDevice) {
      devices.add(bluetoothDevice);
      _currentPageNotifier.value = devices.length;
      setState(() {});
    };
  }

  Future<bool> requestLocationPermission() async {
    PermissionStatus status = await Permission.location.status;
    if (status.isDenied || status.isRestricted) {
      if (await Permission.location.request().isGranted) {
        return true;
      } else {
        throw Exception("Permission Denied");
      }
    } else {
      return true;
    }
  }

  Widget buildItem(BluetoothDevice device) {
    return GestureDetector(
      child: Container(
        margin: const EdgeInsets.all(20),
        height: 50,
        color: Colors.white,
        child: Center(
            child: Text(device.name.isEmpty ? device.address : device.name)),
      ),
      onTap: () async {
        bluetoothDevice = await device.connect();
        bluetoothDevice = device;
        Navigator.pop(context, false);
      },
    );
  }

  Future showModal(BuildContext context, List<BluetoothDevice> devices) {
    return showDialog(
        context: context,
        builder: (BuildContext context) {
          return StatefulBuilder(
            builder: ((context, setState) {
              _currentPageNotifier.addListener(() {
                setState(() {});
              });
              return SizedBox(
                height: 500,
                width: 50,
                child: ListView(
                  padding: const EdgeInsets.all(80),
                  children: devices.map((e) => buildItem(e)).toList(),
                ),
              );
            }),
          );
        });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          actions: [
            IconButton(
                onPressed: () async {
                  if (isEnabled) {
                    await classBluetooth.disable();
                    isEnabled = false;
                    setState(() {});
                  } else {
                    await classBluetooth.enable();
                    isEnabled = true;
                    setState(() {});
                  }
                },
                icon: Icon(
                  Icons.bluetooth,
                  color: isEnabled ? Colors.white : Colors.grey,
                )),
            IconButton(
                onPressed: () {
                  if (bluetoothDevice != null) {
                    bluetoothDevice?.sendMessage("hello world");
                  }
                },
                icon: const Icon(Icons.send))
          ],
          title: const Text("Devices"),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            await requestLocationPermission();
            await classBluetooth.cancelScan();
            setState(() {
              devices = [];
            });
            await classBluetooth.startScan();
            //await showModal(context, devices);
          },
          child: const Icon(Icons.search),
        ),
        body: ListView.builder(
            itemCount: devices.length,
            itemBuilder: (BuildContext context, int index) {
              BluetoothDevice device = devices[index];
              return GestureDetector(
                onTap: (() async {
                  await device.connect();
                  Navigator.push(context, MaterialPageRoute(builder: (context) {
                    return Bluetooth(device);
                  }));
                }),
                child: ListTile(
                  title: Text(device.name.isEmpty
                      ? device.address
                      : "${device.name}===${device.address}"),
                ),
              );
            }));
  }
}
